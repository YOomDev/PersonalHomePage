package com.ypeoomes.DTO;

import com.ypeoomes.Entities.CalendarVisibility;
import com.ypeoomes.Entities.ScheduleType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringScheduleDto {
    private Long id;

    private String name;

    private ScheduleType type;

    private CalendarVisibility visibility;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer cycleLength;

    private List<ScheduleShiftDto> shifts;
}
