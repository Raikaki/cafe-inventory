package com.cafe.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class CafeInventoryApplication {

    public static void main(String[] args) {
        // Run everything in Hanoi time (+7) regardless of the host/container timezone (Render = UTC).
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(CafeInventoryApplication.class, args);
    }

    @PostConstruct
    void ensureTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }
}
