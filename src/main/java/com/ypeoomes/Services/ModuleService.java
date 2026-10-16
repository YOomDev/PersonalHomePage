package com.ypeoomes.Services;

import com.ypeoomes.DTO.ModuleDto;
import com.ypeoomes.Entities.User;
import com.ypeoomes.Entities.UserModule;
import com.ypeoomes.Repositories.UserModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ModuleService {
    private final UserModuleRepository moduleRepository;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    @Value("${ollama.timeout:30000}")
    private int ollamaTimeout;

    @Transactional(readOnly = true)
    public List<UserModule> getUserModules(User user) {
        return moduleRepository.findByUserAndEnabledTrueOrderByDisplayOrderAsc(user);
    }

    @Transactional
    public List<UserModule> createDefaultModules(User user) {
        var weatherModule = UserModule.builder()
                .user(user)
                .type("WEATHER")
                .config(Map.of("location", "Amsterdam", "units", "metric"))
                .enabled(true)
                .displayOrder(0)
                .build();

        return moduleRepository.saveAll(List.of(weatherModule));
    }

    @Transactional
    public UserModule createModule(User user, ModuleDto dto) {
        Map<String, Object> config = dto.getConfig();
        
        if ("OLLAMA".equalsIgnoreCase(dto.getType())) {
            config = new java.util.HashMap<>(config != null ? config : Map.of());
            config.putIfAbsent("baseUrl", ollamaBaseUrl);
            config.putIfAbsent("model", ollamaModel);
            config.putIfAbsent("timeout", ollamaTimeout);
            config.putIfAbsent("systemPrompt", "You are a helpful assistant.");
            config.putIfAbsent("temperature", 0.7);
            config.putIfAbsent("maxTokens", 2000);
        }

        UserModule module = UserModule.builder()
                .user(user)
                .type(dto.getType())
                .config(config)
                .enabled(dto.isEnabled())
                .displayOrder(dto.getDisplayOrder())
                .build();

        return moduleRepository.save(module);
    }

    @Transactional
    public UserModule updateModule(User user, Long id, ModuleDto dto) {
        UserModule module = moduleRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));

        Map<String, Object> config = dto.getConfig();
        
        if ("OLLAMA".equalsIgnoreCase(dto.getType())) {
            config = new java.util.HashMap<>(config != null ? config : Map.of());
            config.putIfAbsent("baseUrl", ollamaBaseUrl);
            config.putIfAbsent("model", ollamaModel);
            config.putIfAbsent("timeout", ollamaTimeout);
            config.putIfAbsent("systemPrompt", "You are a helpful assistant.");
            config.putIfAbsent("temperature", 0.7);
            config.putIfAbsent("maxTokens", 2000);
        }

        module.setType(dto.getType());
        module.setConfig(config);
        module.setEnabled(dto.isEnabled());
        module.setDisplayOrder(dto.getDisplayOrder());

        return moduleRepository.save(module);
    }

    @Transactional
    public void deleteModule(User user, Long id) {
        UserModule module = moduleRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));

        moduleRepository.delete(module);
    }
}