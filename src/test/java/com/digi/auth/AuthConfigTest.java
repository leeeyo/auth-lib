package com.digi.auth;

import com.digi.auth.config.AuthConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class AuthConfigTest {

    @Autowired
    private AuthConfig authConfig;

    @Test
    void testAuthConfigBinding() {
        assertEquals("db", authConfig.getType());
    }
}