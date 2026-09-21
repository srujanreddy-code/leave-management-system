package com.ust.lms.controllerIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.Role;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeControllerIntegrationTest {

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
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

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

    private LeaveType sickLeave;
    private LeaveType earnedLeave;

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

        // =========================
        // LEAVE TYPES
        // =========================

        sickLeave = new LeaveType();
        sickLeave.setName(
                "Sick Leave" + System.nanoTime()
        );
        sickLeave.setMaxDays(6);
        sickLeave.setDeleted(false);
        sickLeave.setStatus(true);

        sickLeave = leaveTypeRepository.save(sickLeave);

        earnedLeave = new LeaveType();
        earnedLeave.setName(
                "Earned Leave" + System.nanoTime()
        );
        earnedLeave.setMaxDays(8);
        earnedLeave.setDeleted(false);
        earnedLeave.setStatus(true);

        earnedLeave = leaveTypeRepository.save(earnedLeave);

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

        // =========================
        // EMPLOYEE LEAVE BALANCES
        // =========================

        EmployeeLeaveBalance sickBalance =
                new EmployeeLeaveBalance();

        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);
        sickBalance.setBalance(6);
        sickBalance.setLeaveYearStart(employee.getJoiningDate());
        sickBalance.setLeaveYearEnd(
                employee.getJoiningDate()
                        .plusYears(1)
                        .minusDays(1)
        );
        sickBalance.setDeleted(false);
        sickBalance.setStatus(true);

        employeeLeaveBalanceRepository.save(sickBalance);

        EmployeeLeaveBalance earnedBalance =
                new EmployeeLeaveBalance();

        earnedBalance.setEmployee(employee);
        earnedBalance.setLeaveType(earnedLeave);
        earnedBalance.setBalance(8);
        earnedBalance.setLeaveYearStart(employee.getJoiningDate());
        earnedBalance.setLeaveYearEnd(
                employee.getJoiningDate()
                        .plusYears(1)
                        .minusDays(1)
        );
        earnedBalance.setDeleted(false);
        earnedBalance.setStatus(true);

        employeeLeaveBalanceRepository.save(earnedBalance);
    }

    /**
     * Generates a real JWT using the application's JwtUtil.
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

    @Test
    @DisplayName("POST /api/employees - Admin creates employee successfully")
    void create_admin_success() throws Exception {

        User newUser = new User();
        newUser.setName("New Employee");
        newUser.setEmail(
                "newemployee" + System.nanoTime() + "@example.com"
        );
        newUser.setPassword(
                passwordEncoder.encode("password")
        );
        newUser.setRole(Role.EMPLOYEE);
        newUser.setDeleted(false);
        newUser.setStatus(true);

        newUser = userRepository.save(newUser);

        EmployeeRequestDto requestDto =
                new EmployeeRequestDto();

        requestDto.setUserId(newUser.getId());
        requestDto.setDepartmentId(department.getId());
        requestDto.setJoiningDate(LocalDate.now());

        mockMvc.perform(
                        post("/api/employees")
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
                        jsonPath("$.userId")
                                .value(newUser.getId())
                )
                .andExpect(
                        jsonPath("$.departmentId")
                                .value(department.getId())
                );
    }

    @Test
    @DisplayName("POST /api/employees - Employee receives 403")
    void create_employee_forbidden() throws Exception {

        EmployeeRequestDto requestDto =
                new EmployeeRequestDto();

        requestDto.setUserId(employeeUser.getId());
        requestDto.setDepartmentId(department.getId());
        requestDto.setJoiningDate(LocalDate.now());

        mockMvc.perform(
                        post("/api/employees")
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

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    @DisplayName("GET /api/employees - Admin gets all employees")
    void getAll_admin_success() throws Exception {

        mockMvc.perform(
                        get("/api/employees")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/employees - Manager gets all employees")
    void getAll_manager_success() throws Exception {

        mockMvc.perform(
                        get("/api/employees")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/employees - Employee gets only own employee")
    void getAll_employee_returnsOwnEmployee()
            throws Exception {

        mockMvc.perform(
                        get("/api/employees")
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
                                .value(employee.getId())
                );
    }

    @Test
    @DisplayName("GET /api/employees - Admin filters by department")
    void getAll_admin_departmentFilter_success()
            throws Exception {

        mockMvc.perform(
                        get("/api/employees")
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
    // GET MY LEAVE BALANCES
    // =========================================================

    @Test
    @DisplayName("GET /api/employees/me/leave-balances - Employee gets all own leave balances")
    void getMyLeaveBalances_employee_success()
            throws Exception {

        mockMvc.perform(
                        get("/api/employees/me/leave-balances")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$[0].employeeId")
                                .value(employee.getId())
                )
                .andExpect(
                        jsonPath("$[0].leaveTypeId")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[0].leaveTypeName")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[0].balance")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[1].employeeId")
                                .value(employee.getId())
                )
                .andExpect(
                        jsonPath("$[1].leaveTypeId")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[1].leaveTypeName")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[1].balance")
                                .exists()
                );
    }

    @Test
    @DisplayName("GET /api/employees/me/leave-balances - Manager receives 403")
    void getMyLeaveBalances_manager_forbidden()
            throws Exception {

        mockMvc.perform(
                        get("/api/employees/me/leave-balances")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/employees/me/leave-balances - Admin receives 403")
    void getMyLeaveBalances_admin_forbidden()
            throws Exception {

        mockMvc.perform(
                        get("/api/employees/me/leave-balances")
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    @DisplayName("GET /api/employees/{id} - Owner gets own employee")
    void getById_owner_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/employees/{id}",
                                employee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(employee.getId())
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(employeeUser.getId())
                );
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Admin gets employee")
    void getById_admin_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/employees/{id}",
                                employee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(employee.getId())
                );
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Manager gets employee")
    void getById_manager_success() throws Exception {

        mockMvc.perform(
                        get(
                                "/api/employees/{id}",
                                employee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(employee.getId())
                );
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Other employee gets 403")
    void getById_otherEmployee_forbidden()
            throws Exception {

        User otherUser = new User();
        otherUser.setName("Other Employee");
        otherUser.setEmail(
                "other" + System.nanoTime() + "@example.com"
        );
        otherUser.setPassword(
                passwordEncoder.encode("password")
        );
        otherUser.setRole(Role.EMPLOYEE);
        otherUser.setDeleted(false);
        otherUser.setStatus(true);

        otherUser = userRepository.save(otherUser);

        Employee otherEmployee = new Employee();
        otherEmployee.setUser(otherUser);
        otherEmployee.setDepartment(department);
        otherEmployee.setJoiningDate(LocalDate.now());
        otherEmployee.setLeaveBalance(0);
        otherEmployee.setDeleted(false);
        otherEmployee.setStatus(true);

        otherEmployee = employeeRepository.save(otherEmployee);

        mockMvc.perform(
                        get(
                                "/api/employees/{id}",
                                otherEmployee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(employeeUser)
                                )
                )
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    @DisplayName("PUT /api/employees/{id} - Admin updates employee")
    void update_admin_success() throws Exception {

        LocalDate newJoiningDate =
                LocalDate.of(2026, 10, 1);

        EmployeeRequestDto requestDto =
                new EmployeeRequestDto();

        requestDto.setUserId(employeeUser.getId());
        requestDto.setDepartmentId(department.getId());
        requestDto.setJoiningDate(newJoiningDate);

        mockMvc.perform(
                        put(
                                "/api/employees/{id}",
                                employee.getId()
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
                                .value(employee.getId())
                )
                .andExpect(
                        jsonPath("$.joiningDate")
                                .value(
                                        newJoiningDate.toString()
                                )
                );

        Optional<Employee> updatedEmployee =
                employeeRepository.findById(
                        employee.getId()
                );

        assertTrue(updatedEmployee.isPresent());

        assertEquals(
                newJoiningDate,
                updatedEmployee.get().getJoiningDate()
        );
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Manager receives 403")
    void update_manager_forbidden() throws Exception {

        EmployeeRequestDto requestDto =
                new EmployeeRequestDto();

        requestDto.setUserId(employeeUser.getId());
        requestDto.setDepartmentId(department.getId());
        requestDto.setJoiningDate(LocalDate.now());

        mockMvc.perform(
                        put(
                                "/api/employees/{id}",
                                employee.getId()
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

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    @DisplayName("DELETE /api/employees/{id} - Admin soft deletes employee")
    void delete_admin_success() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/employees/{id}",
                                employee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(adminUser)
                                )
                )
                .andExpect(status().isNoContent());

        Optional<Employee> deletedEmployee =
                employeeRepository.findById(
                        employee.getId()
                );

        assertTrue(deletedEmployee.isPresent());
        assertTrue(
                deletedEmployee.get().isDeleted()
        );
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Manager receives 403")
    void delete_manager_forbidden() throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/employees/{id}",
                                employee.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + generateToken(managerUser)
                                )
                )
                .andExpect(status().isForbidden());
    }
}