package com.digi.auth;

import com.digi.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ActiveProfiles("test")
public class TestConnectionLdap {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Test
    public void testSimpleAuthentication2() {
        String userDn = "uid=aziz,ou=users";
        assertDoesNotThrow(() -> ldapTemplate.authenticate(userDn, "(objectclass=inetOrgPerson)", "123"));
    }
        @Autowired
        private LdapShaPasswordEncoder passwordEncoder;
        @Autowired
        UserService userService;

    @Test
    public void testLdapConnection() {
        assertDoesNotThrow(() -> {
            ldapTemplate.lookup("ou=users");
            System.out.println("LDAP connection successful");
        });
    }
    @Test
    public void testPasswordEncoding() {
        String rawPassword = "123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
    }


    @Test
    public void testSimpleAuthentication() {
        String username = "aziz";
        String rawPassword = "123";
        String userDnPattern = "uid=aziz,ou=users";
        String userDn = String.format(userDnPattern, username);

//        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertDoesNotThrow(() -> {
            boolean authenticated = ldapTemplate.authenticate(userDn, "(objectclass=inetOrgPerson)", rawPassword);
            assertTrue(authenticated, "Authentication failed");
        });
    }

}

