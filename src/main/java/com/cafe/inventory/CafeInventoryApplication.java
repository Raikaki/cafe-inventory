package com.cafe.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CafeInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeInventoryApplication.class, args);
    }
}
