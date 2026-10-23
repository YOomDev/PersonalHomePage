package com.ypeoomes.DTO;

import com.ypeoomes.Entities.CalendarShareMode;
import com.ypeoomes.Entities.CalendarVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarShareRequest {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotNull
    private CalendarShareMode mode;

    @Builder.Default
    private Set<CalendarVisibility> allowedVisibilities = Set.of();
}
