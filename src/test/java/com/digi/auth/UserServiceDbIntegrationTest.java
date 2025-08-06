package com.digi.auth;

import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;
import com.digi.auth.repository.RoleRepository;
import com.digi.auth.repository.UserRepository;
import com.digi.auth.service.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Sql(scripts = "classpath:/sql/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@SpringBootTest
@ActiveProfiles("test-db")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceDbIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User createTestUser(String username, String email, String password, Role role) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .createTime(Instant.now())
                .status("offline")
                .role(role)
                .build();
    }

    private void ensureRolesExist() {
        if (!roleRepository.findByRoleName(RoleEnum.ADMIN).isPresent()) {
            roleRepository.save(Role.builder().roleName(RoleEnum.ADMIN).build());
        }
        if (!roleRepository.findByRoleName(RoleEnum.USER).isPresent()) {
            roleRepository.save(Role.builder().roleName(RoleEnum.USER).build());
        }
        if (!roleRepository.findByRoleName(RoleEnum.GUEST).isPresent()) {
            roleRepository.save(Role.builder().roleName(RoleEnum.GUEST).build());
        }
    }

    @Test
    void testCreateAndFetchUser() {
        ensureRolesExist();
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User user = createTestUser("testuser", "testuser@example.com", "password123", userRole);
        User saved = userService.saveUser(user);
        assertNotNull(saved.getId());
        List<User> users = userService.fetchAllUsers();
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("testuser")));
    }

    @Test
    void testUpdateUser() {
        ensureRolesExist();
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User user = createTestUser("toUpdate", "toUpdate@example.com", "password123", userRole);
        User saved = userService.saveUser(user);
        User update = User.builder().username("updatedName").build();
        User updated = userService.updateUser(update, saved.getId());
        assertEquals("updatedName", updated.getUsername());
    }

    @Test
    void testDeleteUser() {
        ensureRolesExist();
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User user = createTestUser("toDelete", "toDelete@example.com", "password123", userRole);
        User saved = userService.saveUser(user);
        userService.deleteUserById(saved.getId());
        assertFalse(userRepository.existsById(saved.getId()));
    }

    @Test
    void testFindUserByEmailAndPassword() {
        ensureRolesExist();
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User user = createTestUser("findme", "findme@example.com", "password123", userRole);
        userService.saveUser(user);
        User found = userService.findByEmailAndPassword("findme@example.com", "password123");
        assertEquals("findme@example.com", found.getEmail());
        assertTrue(passwordEncoder.matches("password123", found.getPassword()));
    }
}