package com.ypeoomes.Controllers;

import com.ypeoomes.DTO.OllamaModelRequest;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.OllamaAdminService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Admin-only proxy for Ollama model management (list/pull/delete/load/unload).
 */
@RestController
@RequestMapping("/api/admin/ollama")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OllamaAdminController {
    private final OllamaAdminService adminService;

    @GetMapping("/models")
    public ResponseEntity<Object> listModels(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminService.listModels());
    }

    @GetMapping("/running")
    public ResponseEntity<Object> runningModels(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(adminService.runningModels());
    }

    @PostMapping("/show")
    public ResponseEntity<Object> showModel(@AuthenticationPrincipal User user, @Valid @RequestBody OllamaModelRequest request) {
        return ResponseEntity.ok(adminService.showModel(request.getModel()));
    }

    /** Relays the pull progress stream (NDJSON) to the client as it downloads. */
    @PostMapping(value = "/pull", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public void pull(@AuthenticationPrincipal User user, @Valid @RequestBody OllamaModelRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_NDJSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = response.getWriter();
        adminService.pull(request.getModel(), line -> {
            writer.write(line);
            writer.write('\n');
            writer.flush();
        });
        writer.flush();
    }

    @PostMapping("/preload")
    public ResponseEntity<Object> preload(@AuthenticationPrincipal User user, @Valid @RequestBody OllamaModelRequest request) {
        return ResponseEntity.ok(adminService.preloadModel(request.getModel(), request.getKeepAlive()));
    }

    @PostMapping("/unload")
    public ResponseEntity<Object> unload(@AuthenticationPrincipal User user, @Valid @RequestBody OllamaModelRequest request) {
        return ResponseEntity.ok(adminService.unloadModel(request.getModel()));
    }

    /** DELETE /api/delete needs a JSON body, so model goes in the body instead of the path. */
    @DeleteMapping("/models")
    public ResponseEntity<Object> deleteModel(@AuthenticationPrincipal User user, @Valid @RequestBody OllamaModelRequest request) {
        return ResponseEntity.ok(adminService.deleteModel(request.getModel()));
    }
}
