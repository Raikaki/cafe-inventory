package com.cafe.inventory.config;

import com.cafe.inventory.entity.Role;
import com.cafe.inventory.entity.User;
import com.cafe.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the default ADMIN account on first startup so the password is hashed
 * with the application BCrypt encoder (cannot be reliably seeded from SQL).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;
    @Value("${app.admin.password}")
    private String adminPassword;
    @Value("${app.admin.full-name}")
    private String adminFullName;
    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists, skipping seed.", adminUsername);
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFullName(adminFullName);
        admin.setEmail(adminEmail);
        admin.setRole(Role.ADMIN);
        admin.setActiveFlag(true);
        admin.setCreatedBy("system");
        userRepository.save(admin);
        log.info("Created default ADMIN user '{}'.", adminUsername);
    }
}
