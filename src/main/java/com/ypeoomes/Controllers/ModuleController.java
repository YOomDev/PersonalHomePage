package com.ypeoomes.Controllers;

import com.ypeoomes.DTO.ModuleDto;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Services.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;

    @GetMapping
    public List<ModuleDto> getModules(@AuthenticationPrincipal User user) {
        return moduleService.getUserModules(user).stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public ModuleDto createModule(@AuthenticationPrincipal User user, @RequestBody ModuleDto dto) {
        return toDto(moduleService.createModule(user, dto));
    }

    @PutMapping("/{id}")
    public ModuleDto updateModule(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody ModuleDto dto) {
        return toDto(moduleService.updateModule(user, id, dto));
    }

    @DeleteMapping("/{id}")
    public void deleteModule(@AuthenticationPrincipal User user, @PathVariable Long id) {
        moduleService.deleteModule(user, id);
    }

    private ModuleDto toDto(com.ypeoomes.Entities.UserModule module) {
        return ModuleDto.builder()
                .id(module.getId())
                .type(module.getType())
                .config(module.getConfig())
                .enabled(module.isEnabled())
                .displayOrder(module.getDisplayOrder())
                .build();
    }
}