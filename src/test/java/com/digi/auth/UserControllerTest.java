package com.digi.auth;

import com.digi.auth.config.AuthConfig;
import com.digi.auth.model.Role;
import com.digi.auth.model.RoleEnum;
import com.digi.auth.model.User;
import com.digi.auth.repository.RoleRepository;
import com.digi.auth.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private UserRepository Repo;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthConfig authConfig;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();

    }
    @BeforeAll
    public void setUpBaseUrl() {
        baseUrl = baseUrl.concat(":" + port).concat("/api/v1/users");
    }

    @BeforeEach
    public void setUp() {

    }
    @AfterEach
    public void tearDown() {
        Repo.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @Sql(value = { "classpath:/sql/data.sql", "classpath:/sql/schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM users where username='username'", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testSaveUser() {
        String authType = authConfig.getType();

        if ("db".equals(authType)) {

            Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                    .orElseThrow(() -> new RuntimeException("role not found in database"));
            User user = User.builder()
                    .username("username")
                    .email("email@example.com")
                    .password("password")
                    .createTime(Instant.now())
                    .role(userRole)
                    .status("offline")
                    .build();

            ResponseEntity<User> response = restTemplate.postForEntity(baseUrl, user, User.class);

            assertNotNull(response, "Response should not be null");
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "Http status 200 Created");
            assertNotNull(response.getBody());
            assertEquals("username", response.getBody().getUsername());
            assertEquals("email@example.com", response.getBody().getEmail());
        }
    }


    @Test
    @Sql(value = { "classpath:/sql/data.sql", "classpath:/sql/schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testGetUsers() {
        String authType = authConfig.getType();

        if ("db".equals(authType)) {

            ResponseEntity<List<User>> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<User>>() {
                    }
            );

            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Status Code: " + response.getStatusCode());
            List<User> users = response.getBody();

            assertNotNull(users, "Users list != null");
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status : 201 OK");
            assertTrue(users.size() >= 4, "four users : True");
        }
    }



    @Test
    @Rollback
    @Sql(value = { "classpath:/sql/data.sql", "classpath:/sql/schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testUpdateUser() {
        Role userRole = roleRepository.findByRoleName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("role not found in database"));

        User existingUser = Repo.findByUsername("aziz0")
                .orElseThrow(() -> new RuntimeException("User not found in database"));

        Long localId = existingUser.getId();

        User user = User.builder()
                .username("aziz")
                .email("000@exemple.com")
                .password("123")
                .createTime(Instant.now())
                .role(userRole)
                .build();

        ResponseEntity<User> response = restTemplate.exchange(
                baseUrl + "/" + localId,
                HttpMethod.PUT,
                new HttpEntity<>(user),
                User.class
        );

        System.out.println("Updating user with ID: " + response.getBody().getId());

        // Fetch updated user from database
        User userFromDB = Repo.findById(localId)
                .orElseThrow(() -> new AssertionError("User not found"));

        assertAll(
                () -> assertNotNull(userFromDB),
                () -> assertEquals("aziz", userFromDB.getUsername(), "Username should be updated"),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be 200 OK")
        );
    }



    @Test
    @Sql(value = { "classpath:/sql/data.sql", "classpath:/sql/schema.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testDeleteUserById() {
        Role userRole = roleRepository.findByRoleName(RoleEnum.ADMIN)
                .orElseThrow(() -> new RuntimeException("Role not found in database"));

        User user = User.builder()
                .username("aziz15")
                .email("000000840@exemple.com")
                .password("123")
                .createTime(Instant.now())
                .role(userRole)
                .status("offline")
                .build();

        user = Repo.save(user);

        long localId = user.getId();
        int recordCount = Repo.findAll().size();
        System.out.println("Users Count: " + recordCount);
        System.out.println("user's id: " + user.getId());

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + localId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        System.out.println("Users Count after deletion: " + Repo.findAll().size());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "HTTP status : NO_CONTENT");
        assertEquals(recordCount - 1, Repo.findAll().size());
    }
}