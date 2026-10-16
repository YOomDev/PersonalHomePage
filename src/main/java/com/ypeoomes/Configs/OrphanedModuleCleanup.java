package com.ypeoomes.Configs;

import com.ypeoomes.Repositories.UserModuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OrphanedModuleCleanup {

    private final UserModuleRepository moduleRepository;

    // Run daily at 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanedModules() {
        int deleted = moduleRepository.deleteOrphanedModules();
        if (deleted > 0) {
            log.info("Cleaned up {} orphaned user modules", deleted);
        }
    }
}