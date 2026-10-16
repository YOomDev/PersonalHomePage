package com.ypeoomes.Controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ypeoomes.DTO.AuthRequest;
import com.ypeoomes.DTO.AuthResponse;
import com.ypeoomes.DTO.LoginRequest;
import com.ypeoomes.DTO.UserPreferencesRequest;
import com.ypeoomes.DTO.UserPreferencesResponse;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/auth", "/api/auth"})
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaDefaultModel;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> me(@AuthenticationPrincipal User user) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .theme(user.getTheme())
                .build();
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserPreferencesResponse.builder()
                .theme(user.getTheme())
                .build());
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(@AuthenticationPrincipal User user, @Valid @RequestBody UserPreferencesRequest request) {
        user.setTheme(request.getTheme());
        return ResponseEntity.ok(UserPreferencesResponse.builder()
                .theme(user.getTheme())
                .build());
    }

    @GetMapping("/ollama/models")
    public ResponseEntity<Map<String, Object>> getOllamaModels() {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(ollamaBaseUrl + "/api/tags"))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode models = root.path("models");
                
                List<String> modelNames = new java.util.ArrayList<>();
                if (models.isArray()) {
                    for (JsonNode model : models) {
                        String name = model.path("name").asText();
                        if (!name.isEmpty()) {
                            modelNames.add(name);
                        }
                    }
                }
                
                if (modelNames.isEmpty()) {
                    modelNames.add(ollamaDefaultModel);
                }
                
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("models", modelNames);
                result.put("defaultModel", ollamaDefaultModel);
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            // Fall back to default model
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("models", List.of(ollamaDefaultModel));
        result.put("defaultModel", ollamaDefaultModel);
        return ResponseEntity.ok(result);
    }
}