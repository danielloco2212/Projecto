package com.example.pagebuilder;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncodingTest {
    @Test
    void testPasswordEncoding() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "mySecretPassword123";
        
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertNotNull(encodedPassword);
        assertTrue(encodedPassword.length() > 0);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
        String secondEncoding = passwordEncoder.encode(rawPassword);
        assertNotEquals(encodedPassword, secondEncoding, "Los hashes deberían ser diferentes gracias al salt");
        assertTrue(passwordEncoder.matches(rawPassword, secondEncoding));
    }
}