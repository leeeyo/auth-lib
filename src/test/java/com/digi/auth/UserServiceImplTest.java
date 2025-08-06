package com.digi.auth;

import com.digi.auth.config.AuthConfig;
import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;
import com.digi.auth.repository.RoleRepository;
import com.digi.auth.repository.UserRepository;
import com.digi.auth.service.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Sql(statements = {"DELETE FROM users", "DELETE FROM roles"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@ActiveProfiles("test")
class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
    }
    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    private User createTestUser(String username, String email, String password) {
        return User.builder()
                .username(username)
                .email(email)
                .password(password)
                .createTime(Instant.now())
                .status("offline")
                .build();
    }

    @Test
    void testFetchAllUsers() {
        String authType = authConfig.getType();
        if ("db".equals(authType)) {
            List<User> firstCount = userService.fetchAllUsers();
            System.out.println("Fetched users at first: " + firstCount);
            //user 1
            Role userRole1 = roleRepository.findByRoleName(RoleEnum.USER)
                    .orElseThrow(() -> new RuntimeException("role not found in database"));
            User userToSave1 = createTestUser("TestUser1", "test1@example.com", "password123");
            userToSave1.setRole(userRole1);
            User savedUser1 = userRepository.save(userToSave1);
            //user 2
            Role userRole2 = roleRepository.findByRoleName(RoleEnum.USER)
                    .orElseThrow(() -> new RuntimeException("role not found in database"));
            User userToSave2 = createTestUser("TestUser2", "test2@example.com", "password123");
            userToSave2.setRole(userRole2);
            User savedUser = userRepository.save(userToSave2);

            List<User> fetchedUsers = userService.fetchAllUsers();
            System.out.println("Fetched users: " + fetchedUsers);

            assertEquals(firstCount.size() + 2, fetchedUsers.size());
            assertTrue(fetchedUsers.stream().anyMatch(user -> user.getUsername().equals("TestUser2")));
            assertTrue(fetchedUsers.stream().anyMatch(user -> user.getUsername().equals("TestUser1")));
        }
    }
    @Test
    void testFindUserEmailAndPassword() {
        String authType = authConfig.getType();

        if ("db".equals(authType)){

        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User userToSave = createTestUser("X", "test@example.com" , "password123");
        userToSave.setRole(userRole);
        userService.saveUser(userToSave);
        System.out.println("Attempting to find user with email and password...");
        Optional<User> userByEmailAndPasswordOpt =
                Optional.ofNullable(userService.findByEmailAndPassword("test@example.com", "password123"));
        System.out.println("Retrieved user: " + userByEmailAndPasswordOpt);

        assertTrue(userByEmailAndPasswordOpt.isPresent(), "User present");
        System.out.println("User present");

        assertEquals(1, userByEmailAndPasswordOpt.stream().count(), "User unique");
        System.out.println("User unique");

        User userByEmailAndPassword = userByEmailAndPasswordOpt.get();
        assertEquals("test@example.com", userByEmailAndPassword.getEmail());

        }else if ("ldap".equals(authType)) {
            String ldapEmail = "aziz@example.com";
            String ldapPassword = "123";

            Optional<User> userFromLdap = Optional.ofNullable(userService.findByEmailAndPassword(ldapEmail, ldapPassword));

            assertTrue(userFromLdap.isPresent(), "LDAP User should be found");
            User ldapUser = userFromLdap.get();
            assertEquals(ldapEmail, ldapUser.getEmail());
        }
        }


    @Test
    void testUpdateUser() {
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User userToSave = createTestUser("X", "test@example.com" , "password123");
        userToSave.setRole(userRole);
        User existingUser = userRepository.save(userToSave);

        User updatedData = User.builder().username("Aziz").build();

        User updatedUser = userService.updateUser(updatedData, existingUser.getId());

        assertNotNull(updatedUser);
        assertEquals("Aziz", updatedUser.getUsername());

        User persistedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals("Aziz", persistedUser.getUsername());
    }

    @Test
    void testDeleteUserById() {
        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("role not found in database"));
        User userToSave = createTestUser("X", "test@example.com" , "password123");
        userToSave.setRole(userRole);
        User existingUser = userRepository.save(userToSave);
        userService.deleteUserById(existingUser.getId());

        assertFalse(userRepository.existsById(existingUser.getId()));
    }
}
