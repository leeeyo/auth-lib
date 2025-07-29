package com.digi.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

@Configuration
public class LdapConfig {

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:389");
        contextSource.setBase("dc=maxcrc,dc=com");
        contextSource.setUserDn("cn=Manager,dc=maxcrc,dc=com");
        contextSource.setPassword("secret");
        return contextSource;
    }

    @Bean
    public BindAuthenticator ldapAuthenticator(LdapContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        authenticator.setUserDnPatterns(new String[] { "uid={0},ou=users" });
        return authenticator;
    }

    @Bean
    public LdapAuthenticationProvider ldapAuthenticationProvider(LdapAuthenticator ldapAuthenticator) {
        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(ldapAuthenticator);
        ldapAuthenticationProvider.setUserDetailsContextMapper(new LdapUserDetailsMapper());
        return ldapAuthenticationProvider;
    }

    @Bean
    public LdapShaPasswordEncoder passwordEncoder() {
        return new LdapShaPasswordEncoder();
    }
}
