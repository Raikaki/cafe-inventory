package com.cafe.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keeps attendance data to the current + previous month only.
 * Runs once on startup and daily at 00:30 (Asia/Ho_Chi_Minh).
 */
@Component
@RequiredArgsConstructor
public class AttendanceRetentionJob {

    private final AttendanceService attendanceService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        attendanceService.purgeOld();
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void daily() {
        attendanceService.purgeOld();
    }
}
