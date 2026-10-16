package com.ypeoomes.DTO;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleDto {
    private Long id;
    private String type;
    private Map<String, Object> config;
    private boolean enabled = true;
    private int displayOrder = 0;
}