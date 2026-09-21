package com.ust.lms.controllerIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.Role;
import com.ust.lms.controller.UserController;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link UserController}.
 *
 * <p>Verifies user controller endpoints using the real Spring context,
 * Spring Security, JWT authentication, repositories, and test database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User adminUser;
    private User managerUser;
    private User employeeUser;

    /**
     * Creates the MockMvc instance and test users before each test.
     */
    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // =========================
        // ADMIN USER
        // =========================

        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail(
                "admin" + System.nanoTime() + "@example.com"
        );
        adminUser.setPassword(
                passwordEncoder.encode("password")
        );
        adminUser.setRole(Role.ADMIN);
        adminUser.setDeleted(false);
        adminUser.setStatus(true);

        adminUser = userRepository.save(adminUser);

        // =========================
        // MANAGER USER
        // =========================

        managerUser = new User();
        managerUser.setName("Manager");
        managerUser.setEmail(
                "manager" + System.nanoTime() + "@example.com"
        );
        managerUser.setPassword(
                passwordEncoder.encode("password")
        );
        managerUser.setRole(Role.MANAGER);
        managerUser.setDeleted(false);
        managerUser.setStatus(true);

        managerUser = userRepository.save(managerUser);

        // =========================
        // EMPLOYEE USER
        // =========================

        employeeUser = new User();
        employeeUser.setName("Employee");
        employeeUser.setEmail(
                "employee" + System.nanoTime() + "@example.com"
        );
        employeeUser.setPassword(
                passwordEncoder.encode("password")
        );
        employeeUser.setRole(Role.EMPLOYEE);
        employeeUser.setDeleted(false);
        employeeUser.setStatus(true);

        employeeUser = userRepository.save(employeeUser);
    }

    /**
     * Generates a real JWT using the application's JwtUtil.
     *
     * @param user user for whom the token is generated
     * @return generated JWT token
     */
    private String generateToken(User user) {

        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Verifies that an administrator can retrieve all users.
     */
    @Test
    @DisplayName("GET /api/users - Admin gets all users")
    void getAll_admin_success() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentPage")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.pageSize")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }

    /**
     * Verifies that page=1 represents the first page and
     * limit=1 controls the page size.
     */
    @Test
    @DisplayName("GET /api/users - Pagination works")
    void getAll_pagination_success() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .param("page", "1")
                                .param("limit", "1")
                                .param("sortDirection", "asc")
                                .param("sort", "name")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentPage")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.pageSize")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content").isArray()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                );
    }

    /**
     * Verifies that a manager cannot access the administrator-only
     * user management endpoints.
     */
    @Test
    @DisplayName("GET /api/users - Manager receives 403")
    void getAll_manager_forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an employee cannot access the administrator-only
     * user management endpoints.
     */
    @Test
    @DisplayName("GET /api/users - Employee receives 403")
    void getAll_employee_forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an unauthenticated request is rejected.
     */
    @Test
    @DisplayName("GET /api/users - Unauthenticated request receives 403")
    void getAll_unauthenticated_forbidden() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an administrator can retrieve a user by ID.
     */
    @Test
    @DisplayName("GET /api/users/{id} - Admin gets user")
    void getById_admin_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(employeeUser.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Employee")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(employeeUser.getEmail())
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("EMPLOYEE")
                );
    }

    /**
     * Verifies that a non-administrator cannot retrieve a user.
     */
    @Test
    @DisplayName("GET /api/users/{id} - Employee receives 403")
    void getById_employee_forbidden() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/users/{id}",
                                adminUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an administrator can update a user and
     * the changes are persisted in the database.
     */
    @Test
    @DisplayName("PUT /api/users/{id} - Admin updates user")
    void update_admin_success() throws Exception {

        UserRequestDto requestDto =
                new UserRequestDto();

        requestDto.setName("Updated Employee");
        requestDto.setEmail(
                "updated" + System.nanoTime() + "@example.com"
        );
        requestDto.setPassword("newPassword");
        requestDto.setRole(Role.MANAGER);

        mockMvc.perform(
                        put(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(employeeUser.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Employee")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(requestDto.getEmail())
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("MANAGER")
                );

        Optional<User> updatedUser =
                userRepository.findById(
                        employeeUser.getId()
                );

        assertTrue(updatedUser.isPresent());

        assertEquals(
                "Updated Employee",
                updatedUser.get().getName()
        );

        assertEquals(
                requestDto.getEmail(),
                updatedUser.get().getEmail()
        );

        assertEquals(
                Role.MANAGER,
                updatedUser.get().getRole()
        );

        assertTrue(
                passwordEncoder.matches(
                        "newPassword",
                        updatedUser.get().getPassword()
                )
        );
    }

    /**
     * Verifies that a manager cannot update a user.
     */
    @Test
    @DisplayName("PUT /api/users/{id} - Manager receives 403")
    void update_manager_forbidden() throws Exception {

        UserRequestDto requestDto =
                new UserRequestDto();

        requestDto.setName("Updated");
        requestDto.setEmail(
                "updated" + System.nanoTime() + "@example.com"
        );
        requestDto.setPassword("password");
        requestDto.setRole(Role.EMPLOYEE);

        mockMvc.perform(
                        put(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an employee cannot update a user.
     */
    @Test
    @DisplayName("PUT /api/users/{id} - Employee receives 403")
    void update_employee_forbidden() throws Exception {

        UserRequestDto requestDto =
                new UserRequestDto();

        requestDto.setName("Updated");
        requestDto.setEmail(
                "updated" + System.nanoTime() + "@example.com"
        );
        requestDto.setPassword("password");
        requestDto.setRole(Role.EMPLOYEE);

        mockMvc.perform(
                        put(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an administrator can soft delete a user.
     */
    @Test
    @DisplayName("DELETE /api/users/{id} - Admin soft deletes user")
    void delete_admin_success() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNoContent());

        Optional<User> deletedUser =
                userRepository.findById(
                        employeeUser.getId()
                );

        assertTrue(deletedUser.isPresent());

        assertTrue(
                deletedUser.get().isDeleted()
        );

        assertEquals(
                false,
                deletedUser.get().isStatus()
        );
    }

    /**
     * Verifies that a manager cannot delete a user.
     */
    @Test
    @DisplayName("DELETE /api/users/{id} - Manager receives 403")
    void delete_manager_forbidden() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that an employee cannot delete a user.
     */
    @Test
    @DisplayName("DELETE /api/users/{id} - Employee receives 403")
    void delete_employee_forbidden() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that controller validation rejects an invalid user request.
     */
    @Test
    @DisplayName("PUT /api/users/{id} - Invalid request receives 400")
    void update_invalidRequest_badRequest() throws Exception {

        UserRequestDto requestDto =
                new UserRequestDto();

        requestDto.setName("");
        requestDto.setEmail("invalid-email");
        requestDto.setPassword("");
        requestDto.setRole(null);

        mockMvc.perform(
                        put(
                                "/api/users/{id}",
                                employeeUser.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }
}