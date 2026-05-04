package com.example.fileserver.common.schedule;

import com.example.fileserver.modules.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecycleCleanTask {

    private final RecycleService recycleService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredRecycleRecords() {
        log.info("Starting expired recycle records cleanup task...");
        try {
            recycleService.cleanExpiredRecords();
            log.info("Expired recycle records cleanup task completed");
        } catch (Exception e) {
            log.error("Expired recycle records cleanup task failed", e);
        }
    }
}
