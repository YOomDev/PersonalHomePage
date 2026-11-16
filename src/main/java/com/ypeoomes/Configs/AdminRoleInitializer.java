package com.ypeoomes.Configs;

import com.ypeoomes.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grants ROLE_ADMIN to the account named in {@code app.admin.username}
 * (env: ADMIN_USERNAME) on startup, if it does not have it yet.
 * Empty/unset value disables this entirely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminRoleInitializer implements ApplicationRunner {
    private final UserRepository userRepository;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return;
        }
        userRepository.findByUsername(adminUsername).ifPresent(user -> {
            if (user.getRoles() == null || !user.getRoles().contains("ROLE_ADMIN")) {
                user.getRoles().add("ROLE_ADMIN");
                userRepository.save(user);
                log.info("Granted ROLE_ADMIN to '{}'", adminUsername);
            }
        });
    }
}
