package com.ypeoomes.DTO;

import com.ypeoomes.Entities.CalendarShareMode;
import com.ypeoomes.Entities.CalendarVisibility;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarShareDto {
    private String username;

    private CalendarShareMode mode;

    private Set<CalendarVisibility> allowedVisibilities;
}
