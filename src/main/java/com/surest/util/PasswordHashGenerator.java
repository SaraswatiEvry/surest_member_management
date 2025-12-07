package com.surest.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        log.info("Admin password hash: {}", passwordEncoder.encode("admin123"));
        log.info("User password hash: {}", passwordEncoder.encode("user123"));
    }
}
