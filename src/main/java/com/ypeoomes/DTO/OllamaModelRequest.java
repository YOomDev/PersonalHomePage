package com.ypeoomes.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OllamaModelRequest {
    /** Model name in Ollama form, e.g. {@code llama3.2:8b} or {@code hf.co/user/repo:Q4_K_M}. */
    @NotBlank
    private String model;

    /** Optional keep_alive duration for preload, e.g. "2h" or "-1". */
    private String keepAlive;
}
