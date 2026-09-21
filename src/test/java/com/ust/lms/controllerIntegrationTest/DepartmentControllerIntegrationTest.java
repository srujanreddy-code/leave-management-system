package com.ust.lms.controllerIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.Role;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for DepartmentController.
 *
 * <p>This test class loads the complete Spring application context and
 * verifies department controller endpoints using MockMvc, Spring Security,
 * JWT authentication, and the test PostgreSQL database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DepartmentControllerIntegrationTest {

    /**
     * MockMvc used to perform HTTP requests against the application.
     */
    private MockMvc mockMvc;

    /**
     * Web application context used to build the MockMvc instance.
     */
    @Autowired
    private WebApplicationContext context;

    /**
     * Repository used to create and retrieve department test data.
     */
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Repository used to create test users for authentication.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Password encoder used to encode passwords for test users.
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * JWT utility used to generate real authentication tokens.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * ObjectMapper used to convert request DTOs into JSON.
     */
    private ObjectMapper objectMapper;

    /**
     * ADMIN user used for protected department operations.
     */
    private User adminUser;

    /**
     * MANAGER user used to verify authorization restrictions.
     */
    private User managerUser;

    /**
     * EMPLOYEE user used to verify authorization restrictions.
     */
    private User employeeUser;

    /**
     * Department used as the primary test data.
     */
    private Department department;

    /**
     * Initializes MockMvc, test users, and a test department before
     * each test method.
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

        // =========================
        // DEPARTMENT
        // =========================

        department = new Department();
        department.setName(
                "Engineering" + System.nanoTime()
        );
        department.setDescription(
                "Engineering Department"
        );
        department.setDeleted(false);
        department.setStatus(true);

        department = departmentRepository.save(department);
    }

    /**
     * Generates a real JWT token using the application's JwtUtil.
     *
     * @param user user for whom the token should be generated
     * @return generated JWT token
     */
    private String generateToken(User user) {
        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Tests that an ADMIN user can successfully create a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("POST /api/departments - Admin creates department successfully")
    void create_admin_success() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Finance" + System.nanoTime()
        );
        requestDto.setDescription(
                "Finance Department"
        );

        mockMvc.perform(
                        post("/api/departments")
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.name")
                                .value(requestDto.getName())
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Finance Department")
                );
    }

    /**
     * Tests that an EMPLOYEE user cannot create a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("POST /api/departments - Employee receives 403")
    void create_employee_forbidden() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Finance" + System.nanoTime()
        );
        requestDto.setDescription(
                "Finance Department"
        );

        mockMvc.perform(
                        post("/api/departments")
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
     * Tests that a MANAGER user cannot create a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("POST /api/departments - Manager receives 403")
    void create_manager_forbidden() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Finance" + System.nanoTime()
        );
        requestDto.setDescription(
                "Finance Department"
        );

        mockMvc.perform(
                        post("/api/departments")
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
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an unauthenticated user cannot create a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("POST /api/departments - Unauthenticated user receives 401")
    void create_unauthenticated_unauthorized() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Finance" + System.nanoTime()
        );
        requestDto.setDescription(
                "Finance Department"
        );

        mockMvc.perform(
                        post("/api/departments")
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
     * Tests validation failure when the department name is blank.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("POST /api/departments - Blank name returns 400")
    void create_blankName_badRequest() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName("");
        requestDto.setDescription(
                "Invalid Department"
        );

        mockMvc.perform(
                        post("/api/departments")
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

    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Tests that an ADMIN user can retrieve all departments.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments - Admin gets all departments")
    void getAll_admin_success() throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Tests that a MANAGER user can retrieve all departments.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments - Manager gets all departments")
    void getAll_manager_success() throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Tests that an EMPLOYEE user can retrieve all departments.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments - Employee gets all departments")
    void getAll_employee_success() throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Tests department pagination parameters.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments - Pagination works successfully")
    void getAll_pagination_success() throws Exception {

        mockMvc.perform(
                        get("/api/departments")
                                .param("page", "1")
                                .param("limit", "5")
                                .param("sortDirection", "asc")
                                .param("sort", "name")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    /**
     * Tests that an authenticated user can retrieve a department by ID.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments/{id} - Gets department successfully")
    void getById_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/departments/{id}",
                                department.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(department.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(department.getName())
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Engineering Department")
                );
    }

    /**
     * Tests that requesting a non-existing department returns 404.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments/{id} - Non-existing department returns 404")
    void getById_notFound() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/departments/{id}",
                                99999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Tests that an unauthenticated user cannot retrieve a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("GET /api/departments/{id} - Unauthenticated user receives 401")
    void getById_unauthenticated_unauthorized() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/departments/{id}",
                                department.getId()
                        )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Tests that an ADMIN user can successfully update a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("PUT /api/departments/{id} - Admin updates department successfully")
    void update_admin_success() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Updated Engineering"
        );
        requestDto.setDescription(
                "Updated Engineering Department"
        );

        mockMvc.perform(
                        put(
                                "/api/departments/{id}",
                                department.getId()
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
                                .value(department.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Engineering")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Updated Engineering Department")
                );

        Optional<Department> updatedDepartment =
                departmentRepository.findById(
                        department.getId()
                );

        assertTrue(updatedDepartment.isPresent());

        assertEquals(
                "Updated Engineering",
                updatedDepartment.get().getName()
        );

        assertEquals(
                "Updated Engineering Department",
                updatedDepartment.get().getDescription()
        );
    }

    /**
     * Tests that a MANAGER user cannot update a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("PUT /api/departments/{id} - Manager receives 403")
    void update_manager_forbidden() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Updated Engineering"
        );
        requestDto.setDescription(
                "Updated Engineering Department"
        );

        mockMvc.perform(
                        put(
                                "/api/departments/{id}",
                                department.getId()
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
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an EMPLOYEE user cannot update a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("PUT /api/departments/{id} - Employee receives 403")
    void update_employee_forbidden() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Updated Engineering"
        );
        requestDto.setDescription(
                "Updated Engineering Department"
        );

        mockMvc.perform(
                        put(
                                "/api/departments/{id}",
                                department.getId()
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
     * Tests that updating a non-existing department returns 404.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("PUT /api/departments/{id} - Non-existing department returns 404")
    void update_notFound() throws Exception {

        DepartmentRequestDto requestDto =
                new DepartmentRequestDto();

        requestDto.setName(
                "Updated Department"
        );
        requestDto.setDescription(
                "Updated Description"
        );

        mockMvc.perform(
                        put(
                                "/api/departments/{id}",
                                99999L
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
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // DELETE
    // =========================================================

    /**
     * Tests that an ADMIN user can successfully soft delete a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("DELETE /api/departments/{id} - Admin deletes department successfully")
    void delete_admin_success() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/departments/{id}",
                                department.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNoContent());

        Optional<Department> deletedDepartment =
                departmentRepository.findById(
                        department.getId()
                );

        assertTrue(deletedDepartment.isPresent());

        assertTrue(
                deletedDepartment.get().isDeleted()
        );
    }

    /**
     * Tests that a MANAGER user cannot delete a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("DELETE /api/departments/{id} - Manager receives 403")
    void delete_manager_forbidden() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/departments/{id}",
                                department.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an EMPLOYEE user cannot delete a department.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("DELETE /api/departments/{id} - Employee receives 403")
    void delete_employee_forbidden() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/departments/{id}",
                                department.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that deleting a non-existing department returns 404.
     *
     * @throws Exception if the HTTP request fails
     */
    @Test
    @DisplayName("DELETE /api/departments/{id} - Non-existing department returns 404")
    void delete_notFound() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/departments/{id}",
                                99999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNotFound());
    }
}
