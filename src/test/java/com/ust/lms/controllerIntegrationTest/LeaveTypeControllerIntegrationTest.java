package com.ust.lms.controllerIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.Role;
import com.ust.lms.controller.LeaveTypeController;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
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

import java.time.LocalDate;
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
 * Integration tests for {@link LeaveTypeController}.
 *
 * <p>Verifies leave type REST endpoints using the actual Spring application
 * context, MockMvc, JWT authentication, security configuration, services,
 * repositories, and test database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveTypeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;

    private User adminUser;
    private User managerUser;
    private User employeeUser;

    private Department department;
    private Employee employee;

    /**
     * Sets up MockMvc and creates users and an employee for security
     * and leave balance testing.
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
                "admin-leave-type-" + System.nanoTime()
                        + "@example.com"
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
                "manager-leave-type-" + System.nanoTime()
                        + "@example.com"
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
                "employee-leave-type-" + System.nanoTime()
                        + "@example.com"
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
                "Engineering-" + System.nanoTime()
        );
        department.setDescription(
                "Engineering Department"
        );
        department.setDeleted(false);
        department.setStatus(true);

        department = departmentRepository.save(department);

        // =========================
        // EMPLOYEE
        // =========================

        employee = new Employee();
        employee.setUser(employeeUser);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.now().minusMonths(2)
        );
        employee.setLeaveBalance(0);
        employee.setDeleted(false);
        employee.setStatus(true);

        employee = employeeRepository.save(employee);
    }

    /**
     * Generates a real JWT using the application's JwtUtil.
     *
     * @param user authenticated user
     * @return generated JWT
     */
    private String generateToken(User user) {

        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Creates a leave type directly in the database.
     *
     * @param name leave type name
     * @param maxDays maximum allowed leave days
     * @return persisted leave type
     */
    private LeaveType createLeaveType(
            String name,
            int maxDays
    ) {

        LeaveType leaveType =
                new LeaveType();

        leaveType.setName(name);
        leaveType.setMaxDays(maxDays);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);

        return leaveTypeRepository.save(
                leaveType
        );
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Verifies that an administrator can create a leave type.
     */
    @Test
    @DisplayName("POST /api/leave-types - Admin creates leave type successfully")
    void create_admin_success() throws Exception {

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName(
                "Sick Leave-" + System.nanoTime()
        );
        requestDto.setMaxDays(6);

        mockMvc.perform(
                        post("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
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
                        jsonPath("$.maxDays")
                                .value(6)
                );
    }

    /**
     * Verifies that a manager is forbidden from creating a leave type.
     */
    @Test
    @DisplayName("POST /api/leave-types - Manager receives 403")
    void create_manager_forbidden() throws Exception {

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName(
                "Manager Leave-" + System.nanoTime()
        );
        requestDto.setMaxDays(5);

        mockMvc.perform(
                        post("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(managerUser)
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
     * Verifies that an employee is forbidden from creating a leave type.
     */
    @Test
    @DisplayName("POST /api/leave-types - Employee receives 403")
    void create_employee_forbidden() throws Exception {

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName(
                "Employee Leave-" + System.nanoTime()
        );
        requestDto.setMaxDays(5);

        mockMvc.perform(
                        post("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(employeeUser)
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
     * Verifies that invalid create data is rejected by validation.
     */
    @Test
    @DisplayName("POST /api/leave-types - Invalid request returns 400")
    void create_invalidRequest_badRequest() throws Exception {

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName("");
        requestDto.setMaxDays(null);

        mockMvc.perform(
                        post("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
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
     * Verifies that an administrator can retrieve all leave types.
     */
    @Test
    @DisplayName("GET /api/leave-types - Admin gets all leave types")
    void getAll_admin_success() throws Exception {

        createLeaveType(
                "Sick Leave-" + System.nanoTime(),
                6
        );

        mockMvc.perform(
                        get("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }

    /**
     * Verifies that a manager can retrieve all leave types.
     */
    @Test
    @DisplayName("GET /api/leave-types - Manager gets all leave types")
    void getAll_manager_success() throws Exception {

        createLeaveType(
                "Manager Test Leave-" + System.nanoTime(),
                5
        );

        mockMvc.perform(
                        get("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }

    /**
     * Verifies that an employee can retrieve leave types because the
     * endpoint does not have a role restriction.
     */
    @Test
    @DisplayName("GET /api/leave-types - Employee gets leave types")
    void getAll_employee_success() throws Exception {

        createLeaveType(
                "Employee Test Leave-" + System.nanoTime(),
                5
        );

        mockMvc.perform(
                        get("/api/leave-types")
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content").isArray()
                );
    }

    /**
     * Verifies pagination parameters on the get-all endpoint.
     */
    @Test
    @DisplayName("GET /api/leave-types - Pagination works")
    void getAll_pagination_success() throws Exception {

        createLeaveType(
                "Leave One-" + System.nanoTime(),
                5
        );

        createLeaveType(
                "Leave Two-" + System.nanoTime(),
                7
        );

        mockMvc.perform(
                        get("/api/leave-types")
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
                        jsonPath("$.content")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalPages")
                                .value(2)
                );
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    /**
     * Verifies that a leave type can be retrieved by ID.
     */
    @Test
    @DisplayName("GET /api/leave-types/{id} - Get leave type successfully")
    void getById_success() throws Exception {

        LeaveType leaveType =
                createLeaveType(
                        "Sick Leave-" + System.nanoTime(),
                        6
                );

        mockMvc.perform(
                        get(
                                "/api/leave-types/{id}",
                                leaveType.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveType.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(leaveType.getName())
                )
                .andExpect(
                        jsonPath("$.maxDays")
                                .value(6)
                );
    }

    /**
     * Verifies that requesting a non-existent leave type returns 404.
     */
    @Test
    @DisplayName("GET /api/leave-types/{id} - Non-existent ID returns 404")
    void getById_notFound() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/leave-types/{id}",
                                999999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    /**
     * Verifies that an administrator can update a leave type.
     */
    @Test
    @DisplayName("PUT /api/leave-types/{id} - Admin updates successfully")
    void update_admin_success() throws Exception {

        LeaveType leaveType =
                createLeaveType(
                        "Old Leave-" + System.nanoTime(),
                        5
                );

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName(
                "Updated Leave-" + System.nanoTime()
        );
        requestDto.setMaxDays(12);

        mockMvc.perform(
                        put(
                                "/api/leave-types/{id}",
                                leaveType.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
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
                                .value(leaveType.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value(requestDto.getName())
                )
                .andExpect(
                        jsonPath("$.maxDays")
                                .value(12)
                );

        LeaveType updated =
                leaveTypeRepository
                        .findById(leaveType.getId())
                        .orElseThrow();

        assertEquals(
                requestDto.getName(),
                updated.getName()
        );

        assertEquals(
                12,
                updated.getMaxDays()
        );
    }

    /**
     * Verifies that a manager cannot update a leave type.
     */
    @Test
    @DisplayName("PUT /api/leave-types/{id} - Manager receives 403")
    void update_manager_forbidden() throws Exception {

        LeaveType leaveType =
                createLeaveType(
                        "Manager Update Leave-" + System.nanoTime(),
                        5
                );

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName("Updated Leave");
        requestDto.setMaxDays(10);

        mockMvc.perform(
                        put(
                                "/api/leave-types/{id}",
                                leaveType.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(managerUser)
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
     * Verifies that updating a non-existent leave type returns 404.
     */
    @Test
    @DisplayName("PUT /api/leave-types/{id} - Non-existent ID returns 404")
    void update_notFound() throws Exception {

        LeaveTypeRequestDto requestDto =
                new LeaveTypeRequestDto();

        requestDto.setName("Updated Leave");
        requestDto.setMaxDays(10);

        mockMvc.perform(
                        put(
                                "/api/leave-types/{id}",
                                999999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
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
     * Verifies that an administrator can soft delete a leave type.
     */
    @Test
    @DisplayName("DELETE /api/leave-types/{id} - Admin soft deletes successfully")
    void delete_admin_success() throws Exception {

        LeaveType leaveType =
                createLeaveType(
                        "Delete Leave-" + System.nanoTime(),
                        5
                );

        mockMvc.perform(
                        delete(
                                "/api/leave-types/{id}",
                                leaveType.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNoContent());

        Optional<LeaveType> deleted =
                leaveTypeRepository.findById(
                        leaveType.getId()
                );

        assertTrue(deleted.isPresent());
        assertTrue(deleted.get().isDeleted());
        assertTrue(!deleted.get().isStatus());
    }

    /**
     * Verifies that a manager cannot delete a leave type.
     */
    @Test
    @DisplayName("DELETE /api/leave-types/{id} - Manager receives 403")
    void delete_manager_forbidden() throws Exception {

        LeaveType leaveType =
                createLeaveType(
                        "Manager Delete Leave-" + System.nanoTime(),
                        5
                );

        mockMvc.perform(
                        delete(
                                "/api/leave-types/{id}",
                                leaveType.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that deleting a non-existent leave type returns 404.
     */
    @Test
    @DisplayName("DELETE /api/leave-types/{id} - Non-existent ID returns 404")
    void delete_notFound() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/leave-types/{id}",
                                999999L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer "
                                                + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNotFound());
    }
}