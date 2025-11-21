package com.assignment.surest_member_management.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("Admin password hash: " + passwordEncoder.encode("admin123"));
        System.out.println("User password hash: " + passwordEncoder.encode("user123"));
    }
}
