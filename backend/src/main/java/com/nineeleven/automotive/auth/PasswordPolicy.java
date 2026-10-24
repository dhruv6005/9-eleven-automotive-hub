package com.nineeleven.automotive.auth;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {
    public boolean isValid(String value) {
        return value != null && value.length() >= 12 && value.length() <= 72
                && value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72
                && value.matches(".*[a-z].*") && value.matches(".*[A-Z].*")
                && value.matches(".*\\d.*") && value.matches(".*[^A-Za-z0-9].*");
    }
    public void requireValid(String value) {
        if (!isValid(value)) throw new IllegalArgumentException(
                "Password must be 12–72 characters and include lowercase, uppercase, a number, and a symbol.");
    }
}
