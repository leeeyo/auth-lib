package com.digi.auth;

import com.digi.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-ldap")
class UserServiceLdapIntegrationTest {
    @Autowired
    private LdapTemplate ldapTemplate;
    @Autowired
    private UserService userService;

    @Test
    void testLdapConnection() {
        assertDoesNotThrow(() -> ldapTemplate.lookup("ou=users"));
    }

    @Test
    void testLdapAuthentication() {
        String userDn = "uid=aziz,ou=users";
        assertDoesNotThrow(() -> ldapTemplate.authenticate(userDn, "(objectclass=inetOrgPerson)", "123"));
    }

    @Test
    void testFindUserByEmailAndPassword() {
        String email = "aziz@example.com";
        String password = "123";
        assertDoesNotThrow(() -> {
            assertNotNull(userService.findByEmailAndPassword(email, password));
        });
    }
}