package com.ust.lms.controllerIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.Role;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
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
 * Integration tests for the LeaveRequestController.
 *
 * <p>Tests the leave request controller through the complete Spring application
 * context, including security, JWT authentication, services, and repositories.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveRequestControllerIntegrationTest {

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
    private LeaveRequestRepository leaveRequestRepository;

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
    private User otherEmployeeUser;

    private Department department;

    private Employee employee;
    private Employee otherEmployee;

    private LeaveType leaveType;

    private LeaveRequest leaveRequest;

    /**
     * Sets up the Spring Security MockMvc environment and creates
     * the users, employees, leave type, balance, and leave request
     * required for the integration tests.
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
        // OTHER EMPLOYEE USER
        // =========================

        otherEmployeeUser = new User();
        otherEmployeeUser.setName("Other Employee");
        otherEmployeeUser.setEmail(
                "otheremployee" + System.nanoTime() + "@example.com"
        );
        otherEmployeeUser.setPassword(
                passwordEncoder.encode("password")
        );
        otherEmployeeUser.setRole(Role.EMPLOYEE);
        otherEmployeeUser.setDeleted(false);
        otherEmployeeUser.setStatus(true);

        otherEmployeeUser = userRepository.save(otherEmployeeUser);

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

        // =========================
        // EMPLOYEE
        // =========================

        employee = new Employee();
        employee.setUser(employeeUser);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.now().minusMonths(2)
        );
        employee.setLeaveBalance(10);
        employee.setDeleted(false);
        employee.setStatus(true);

        employee = employeeRepository.save(employee);

        // =========================
        // OTHER EMPLOYEE
        // =========================

        otherEmployee = new Employee();
        otherEmployee.setUser(otherEmployeeUser);
        otherEmployee.setDepartment(department);
        otherEmployee.setJoiningDate(
                LocalDate.now().minusMonths(2)
        );
        otherEmployee.setLeaveBalance(10);
        otherEmployee.setDeleted(false);
        otherEmployee.setStatus(true);

        otherEmployee = employeeRepository.save(otherEmployee);

        // =========================
        // LEAVE TYPE
        // =========================

        leaveType = new LeaveType();
        leaveType.setName(
                "Earned Leave" + System.nanoTime()
        );
        leaveType.setMaxDays(10);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);

        leaveType = leaveTypeRepository.save(leaveType);

        // =========================
        // EMPLOYEE LEAVE BALANCE
        // =========================

        EmployeeLeaveBalance employeeBalance =
                new EmployeeLeaveBalance();

        employeeBalance.setEmployee(employee);
        employeeBalance.setLeaveType(leaveType);
        employeeBalance.setBalance(10);
        employeeBalance.setLeaveYearStart(
                employee.getJoiningDate()
        );
        employeeBalance.setLeaveYearEnd(
                employee.getJoiningDate()
                        .plusYears(1)
                        .minusDays(1)
        );
        employeeBalance.setDeleted(false);
        employeeBalance.setStatus(true);

        employeeLeaveBalanceRepository.save(employeeBalance);

        // =========================
        // OTHER EMPLOYEE LEAVE BALANCE
        // =========================

        EmployeeLeaveBalance otherEmployeeBalance =
                new EmployeeLeaveBalance();

        otherEmployeeBalance.setEmployee(otherEmployee);
        otherEmployeeBalance.setLeaveType(leaveType);
        otherEmployeeBalance.setBalance(10);
        otherEmployeeBalance.setLeaveYearStart(
                otherEmployee.getJoiningDate()
        );
        otherEmployeeBalance.setLeaveYearEnd(
                otherEmployee.getJoiningDate()
                        .plusYears(1)
                        .minusDays(1)
        );
        otherEmployeeBalance.setDeleted(false);
        otherEmployeeBalance.setStatus(true);

        employeeLeaveBalanceRepository.save(otherEmployeeBalance);

        // =========================
        // LEAVE REQUEST
        // =========================

        leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(
                LocalDate.now().plusDays(5)
        );
        leaveRequest.setEndDate(
                LocalDate.now().plusDays(6)
        );
        leaveRequest.setReason("Personal leave");
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
        leaveRequest.setDeleted(false);
        leaveRequest.setStatus(true);

        leaveRequest = leaveRequestRepository.save(leaveRequest);
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

    // =========================================================
    // APPLY
    // =========================================================

    @Test
    @DisplayName("POST /api/leave-requests - Employee applies for leave successfully")
    void apply_employee_success() throws Exception {

        LeaveRequestRequestDto requestDto =
                new LeaveRequestRequestDto();

        requestDto.setEmployeeId(employee.getId());
        requestDto.setLeaveTypeId(leaveType.getId());
        requestDto.setStartDate(
                LocalDate.now().plusDays(10)
        );
        requestDto.setEndDate(
                LocalDate.now().plusDays(11)
        );
        requestDto.setReason("Family function");

        mockMvc.perform(
                        post("/api/leave-requests")
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(
                        jsonPath("$.employeeId")
                                .value(employee.getId())
                )
                .andExpect(
                        jsonPath("$.leaveTypeId")
                                .value(leaveType.getId())
                );
    }

    @Test
    @DisplayName("POST /api/leave-requests - Other employee receives 403")
    void apply_otherEmployee_forbidden() throws Exception {

        LeaveRequestRequestDto requestDto =
                new LeaveRequestRequestDto();

        requestDto.setEmployeeId(employee.getId());
        requestDto.setLeaveTypeId(leaveType.getId());
        requestDto.setStartDate(
                LocalDate.now().plusDays(10)
        );
        requestDto.setEndDate(
                LocalDate.now().plusDays(11)
        );
        requestDto.setReason("Personal leave");

        mockMvc.perform(
                        post("/api/leave-requests")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(otherEmployeeUser)
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

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    @DisplayName("GET /api/leave-requests - Admin gets all leave requests")
    void getAll_admin_success() throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/leave-requests - Manager gets all leave requests")
    void getAll_manager_success() throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/leave-requests - Employee gets only own leave requests")
    void getAll_employee_returnsOwnRequests() throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(leaveRequest.getId())
                );
    }

    @Test
    @DisplayName("GET /api/leave-requests - Admin filters by status")
    void getAll_admin_statusFilter_success()
            throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .param(
                                        "status",
                                        LeaveStatus.PENDING.name()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/leave-requests - Admin filters by employee")
    void getAll_admin_employeeFilter_success()
            throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .param(
                                        "employeeId",
                                        employee.getId().toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/leave-requests - Admin filters by department")
    void getAll_admin_departmentFilter_success()
            throws Exception {

        mockMvc.perform(
                        get("/api/leave-requests")
                                .param(
                                        "departmentId",
                                        department.getId().toString()
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    @DisplayName("GET /api/leave-requests/{id} - Owner gets own leave request")
    void getById_owner_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveRequest.getId())
                )
                .andExpect(
                        jsonPath("$.employeeId")
                                .value(employee.getId())
                );
    }

    @Test
    @DisplayName("GET /api/leave-requests/{id} - Manager gets leave request")
    void getById_manager_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveRequest.getId())
                );
    }

    @Test
    @DisplayName("GET /api/leave-requests/{id} - Other employee receives 403")
    void getById_otherEmployee_forbidden()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(otherEmployeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    @DisplayName("PUT /api/leave-requests/{id} - Owner updates leave request")
    void update_owner_success() throws Exception {

        LeaveRequestRequestDto requestDto =
                new LeaveRequestRequestDto();

        requestDto.setEmployeeId(employee.getId());
        requestDto.setLeaveTypeId(leaveType.getId());
        requestDto.setStartDate(
                LocalDate.now().plusDays(7)
        );
        requestDto.setEndDate(
                LocalDate.now().plusDays(8)
        );
        requestDto.setReason("Updated reason");

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
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
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveRequest.getId())
                );
    }

    @Test
    @DisplayName("PUT /api/leave-requests/{id} - Other employee receives 403")
    void update_otherEmployee_forbidden()
            throws Exception {

        LeaveRequestRequestDto requestDto =
                new LeaveRequestRequestDto();

        requestDto.setEmployeeId(employee.getId());
        requestDto.setLeaveTypeId(leaveType.getId());
        requestDto.setStartDate(
                LocalDate.now().plusDays(7)
        );
        requestDto.setEndDate(
                LocalDate.now().plusDays(8)
        );
        requestDto.setReason("Updated reason");

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(otherEmployeeUser)
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

    // =========================================================
    // CANCEL
    // =========================================================

    @Test
    @DisplayName("DELETE /api/leave-requests/{id} - Owner cancels leave request")
    void cancel_owner_success() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isNoContent());

        Optional<LeaveRequest> deletedLeaveRequest =
                leaveRequestRepository.findById(
                        leaveRequest.getId()
                );

        assertTrue(deletedLeaveRequest.isPresent());
        assertTrue(
                deletedLeaveRequest.get().isDeleted()
        );
    }

    @Test
    @DisplayName("DELETE /api/leave-requests/{id} - Other employee receives 403")
    void cancel_otherEmployee_forbidden()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/leave-requests/{id}",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(otherEmployeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // APPROVE
    // =========================================================

    @Test
    @DisplayName("PUT /api/leave-requests/{id}/approve - Manager approves leave request")
    void approve_manager_success() throws Exception {

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}/approve",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveRequest.getId())
                )
                .andExpect(
                        jsonPath("$.leaveStatus")
                                .value(LeaveStatus.APPROVED.name())
                );

        LeaveRequest approvedLeaveRequest =
                leaveRequestRepository
                        .findById(leaveRequest.getId())
                        .orElseThrow();

        assertEquals(
                LeaveStatus.APPROVED,
                approvedLeaveRequest.getLeaveStatus()
        );
    }

    @Test
    @DisplayName("PUT /api/leave-requests/{id}/approve - Employee receives 403")
    void approve_employee_forbidden()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}/approve",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // REJECT
    // =========================================================

    @Test
    @DisplayName("PUT /api/leave-requests/{id}/reject - Manager rejects leave request")
    void reject_manager_success() throws Exception {

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}/reject",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(leaveRequest.getId())
                )
                .andExpect(
                        jsonPath("$.leaveStatus")
                                .value(LeaveStatus.REJECTED.name())
                );

        LeaveRequest rejectedLeaveRequest =
                leaveRequestRepository
                        .findById(leaveRequest.getId())
                        .orElseThrow();

        assertEquals(
                LeaveStatus.REJECTED,
                rejectedLeaveRequest.getLeaveStatus()
        );
    }

    @Test
    @DisplayName("PUT /api/leave-requests/{id}/reject - Employee receives 403")
    void reject_employee_forbidden()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/leave-requests/{id}/reject",
                                leaveRequest.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }
}