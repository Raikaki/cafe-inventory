package com.cafe.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Keeps the Aiven free MySQL from powering off due to inactivity by running a
 * trivial query on a schedule. Aiven's free plan powers the database off when
 * idle (no queries); a periodic "SELECT 1" counts as activity and keeps it on.
 *
 * Note: this only runs while the app is alive — the Render self-ping
 * (KeepAliveService) keeps the app awake so this scheduled query keeps firing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DbKeepAliveService {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(initialDelay = 60_000, fixedRate = 600_000) // first after 1 min, then every 10 min
    public void ping() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("DB keep-alive query ok");
        } catch (Exception e) {
            log.warn("DB keep-alive query failed: {}", e.getMessage());
        }
    }
}
