package com.ypeoomes.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Proxies model-management calls to the Ollama server (admin only).
 * Endpoints used: /api/tags, /api/ps, /api/show, /api/delete, /api/generate (load/unload), /api/pull.
 * Returns plain Maps (not JsonNode) so the HTTP layer serializes them as raw JSON.
 */
@Service
public class OllamaAdminService {
    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaAdminService(@Value("${ollama.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** GET /api/tags — all locally installed models. */
    public Object listModels() {
        return requestJson("GET", "/api/tags", null);
    }

    /** GET /api/ps — models currently loaded into memory. */
    public Object runningModels() {
        return requestJson("GET", "/api/ps", null);
    }

    /** POST /api/show — details of one model. */
    public Object showModel(String model) {
        return requestJson("POST", "/api/show", body("model", model));
    }

    /** DELETE /api/delete — removes a model and its data. */
    public Object deleteModel(String model) {
        return requestJson("DELETE", "/api/delete", body("model", model));
    }

    /**
     * POST /api/generate with no prompt — loads the model into memory.
     * keepAlive may be a duration string ("5m", "1h"), a plain integer in seconds
     * (e.g. "-1" = keep until unloaded, sent as a JSON number since Ollama's duration
     * parser only accepts strings like "5m"), or null (server default, 5 minutes).
     */
    public Object preloadModel(String model, String keepAlive) {
        String body;
        if (keepAlive != null && !keepAlive.isBlank()) {
            String trimmed = keepAlive.trim();
            String keepJson = trimmed.matches("-?\\d+")
                    ? trimmed
                    : "\"" + escape(trimmed) + "\"";
            body = "{\"model\":\"" + escape(model) + "\",\"stream\":true,\"keep_alive\":" + keepJson + "}";
        } else {
            body = "{\"model\":\"" + escape(model) + "\",\"stream\":true}";
        }
        return streamToLast("/api/generate", body);
    }

    /** POST /api/generate with keep_alive 0 — unloads the model from memory. */
    public Object unloadModel(String model) {
        return streamToLast("/api/generate",
                "{\"model\":\"" + escape(model) + "\",\"stream\":true,\"keep_alive\":0}");
    }

    /**
     * POST /api/pull — relays Ollama's NDJSON progress stream line by line to the consumer.
     * Lines look like {"status":"pulling <digest>","digest":"...","total":n,"completed":n} and finish with {"status":"success"}.
     */
    public void pull(String model, java.util.function.Consumer<String> lineConsumer) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/pull"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30)) // until response headers; the body stream itself has no timeout
                    .POST(HttpRequest.BodyPublishers.ofString("{\"model\":\"" + escape(model) + "\"}"))
                    .build();

            HttpResponse<java.io.InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String err = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                lineConsumer.accept("{\"error\":\"Ollama returned HTTP " + response.statusCode() + ": "
                        + escape(truncate(err, 300)) + "\"}");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lineConsumer.accept(line);
                    }
                }
            }
        } catch (Exception e) {
            lineConsumer.accept("{\"error\":\"Pull failed: " + escape(String.valueOf(e.getMessage())) + "\"}");
        }
    }

    /** Streams an NDJSON response and returns the final (done) line's JSON, or an error map. */
    private Object streamToLast(String path, String jsonBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<java.io.InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String err = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                return Map.of("error", "Ollama returned HTTP " + response.statusCode() + ": " + truncate(err, 300));
            }

            Object last = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        last = parse(line);
                    }
                }
            }
            return last != null ? last : Map.of("status", "success");
        } catch (Exception e) {
            return Map.of("error", String.valueOf(e.getMessage()));
        }
    }

    private Object requestJson(String method, String path, String jsonBody) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30));
            if (jsonBody != null) {
                builder.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8));
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Map.of("error", "Ollama returned HTTP " + response.statusCode() + ": "
                        + truncate(response.body() == null ? "" : response.body(), 300));
            }
            return parse(response.body());
        } catch (Exception e) {
            return Map.of("error", String.valueOf(e.getMessage()));
        }
    }

    /** Parses raw JSON into a plain Map (plain Java types serialize correctly through the HTTP layer). */
    @SuppressWarnings("unchecked")
    private Object parse(String json) {
        if (json == null || json.isBlank()) {
            return Map.of("status", "success");
        }
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of("error", "Could not parse Ollama response: " + truncate(json, 200));
        }
    }

    private String body(String key, String value) {
        return "{\"" + key + "\":\"" + escape(value) + "\"}";
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private String truncate(String s, int max) {
        return s == null ? "" : (s.length() > max ? s.substring(0, max) : s);
    }
}
