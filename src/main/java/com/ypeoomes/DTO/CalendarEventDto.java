package com.ypeoomes.DTO;

import com.ypeoomes.Entities.CalendarVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventDto {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 2000)
    private String description;

    @Size(max = 200)
    private String location;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private boolean allDay;

    private CalendarVisibility visibility;

    private String ownerUsername;

    private boolean masked;

    private String username;
}