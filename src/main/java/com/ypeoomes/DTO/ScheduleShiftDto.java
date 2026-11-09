package com.ypeoomes.DTO;

import lombok.*;

/**
 * Input uses {@code templateId}; output also carries the resolved template for convenience.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleShiftDto {
    private int dayPosition;
    private Long templateId;
    private ShiftTemplateDto template;
}
