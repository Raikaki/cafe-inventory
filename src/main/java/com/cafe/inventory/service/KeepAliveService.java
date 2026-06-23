package com.cafe.inventory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Keeps the free-tier instance awake. Render spins a free web service down after
 * ~15 minutes without inbound traffic; this self-pings the public URL every
 * 10 minutes so the idle timer never elapses. Active only when RENDER_EXTERNAL_URL
 * is present (i.e. on Render) — it is a no-op locally.
 */
@Slf4j
@Service
public class KeepAliveService {

    @Value("${RENDER_EXTERNAL_URL:}")
    private String externalUrl;

    private final RestClient restClient = RestClient.create();

    @Scheduled(initialDelay = 120_000, fixedRate = 600_000) // first after 2 min, then every 10 min
    public void keepAwake() {
        if (externalUrl == null || externalUrl.isBlank()) {
            return; // not running on Render
        }
        try {
            String url = externalUrl.replaceAll("/+$", "") + "/api/health";
            restClient.get().uri(url).retrieve().toBodilessEntity();
            log.debug("Keep-alive ping sent to {}", url);
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
