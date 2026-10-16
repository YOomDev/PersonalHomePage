package com.ypeoomes.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesRequest {
    @NotBlank
    @Pattern(regexp = "^(light|dark|auto)$", message = "Theme must be light, dark, or auto")
    private String theme;
}