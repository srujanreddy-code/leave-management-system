package com.ust.lms.leaverequest.impl;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.Role;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ForbiddenException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import com.ust.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LeaveRequestServiceIntegrationTest {

    @Autowired
    private com.ust.lms.leaverequest.LeaveRequestService leaveRequestService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @BeforeEach
    void setUp() {
        leaveRequestRepository.deleteAll();
        employeeRepository.deleteAll();
        userRepository.deleteAll();
        leaveTypeRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    @Test
    void apply_shouldCreateLeaveRequest() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "Family trip"
        );

        LeaveRequestResponseDto response = leaveRequestService.apply(dto);

        assertNotNull(response);
        assertEquals(employee.getId(), response.getEmployeeId());
        assertEquals("John", response.getEmployeeName());
        assertEquals(leaveType.getId(), response.getLeaveTypeId());
        assertEquals("Annual Leave", response.getLeaveTypeName());
        assertEquals(LeaveStatus.PENDING, response.getLeaveStatus());
        assertEquals("Family trip", response.getReason());
    }

    @Test
    void apply_shouldThrowWhenEmployeeNotFound() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                999L,
                leaveType.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Test"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void apply_shouldThrowWhenLeaveTypeNotFound() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                999L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Test"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void apply_shouldThrowForPastDate() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                "Test"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void apply_shouldThrowWhenStartDateAfterEndDate() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(2),
                "Test"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void apply_shouldThrowWhenInsufficientLeaveBalance() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 2);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                "Test"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void apply_shouldThrowWhenOverlappingLeaveExists() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(4),
                LeaveStatus.PENDING
        );

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(5),
                "Overlapping"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.apply(dto));
    }

    @Test
    void update_shouldUpdatePendingLeaveRequest() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);

        LeaveType oldType = createLeaveType("Annual Leave", 20);
        LeaveType newType = createLeaveType("Sick Leave", 10);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                oldType,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(3),
                LeaveStatus.PENDING
        );

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                newType.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                "Updated reason"
        );

        LeaveRequestResponseDto response =
                leaveRequestService.update(leaveRequest.getId(), dto);

        assertEquals(newType.getId(), response.getLeaveTypeId());
        assertEquals("Sick Leave", response.getLeaveTypeName());
        assertEquals(LocalDate.now().plusDays(5), response.getStartDate());
        assertEquals(LocalDate.now().plusDays(7), response.getEndDate());
        assertEquals("Updated reason", response.getReason());
        assertEquals(LeaveStatus.PENDING, response.getLeaveStatus());
    }

    @Test
    void update_shouldThrowWhenRequestNotFound() {
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequestRequestDto dto = createRequest(
                1L,
                leaveType.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                "Test"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.update(999L, dto));
    }

    @Test
    void update_shouldThrowWhenNotPending() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.APPROVED
        );

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                "Test"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.update(leaveRequest.getId(), dto));
    }

    @Test
    void update_shouldThrowForPastDate() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(3),
                LeaveStatus.PENDING
        );

        LeaveRequestRequestDto dto = createRequest(
                employee.getId(),
                leaveType.getId(),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                "Past"
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.update(leaveRequest.getId(), dto));
    }

    @Test
    void cancel_shouldSoftDeleteLeaveRequest() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        leaveRequestService.cancel(leaveRequest.getId());

        LeaveRequest saved =
                leaveRequestRepository.findById(leaveRequest.getId()).orElseThrow();

        assertTrue(saved.isDeleted());
    }

    @Test
    void cancel_shouldRestoreBalanceForApprovedLeave() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                LeaveStatus.APPROVED
        );

        employee.setLeaveBalance(17);
        employeeRepository.save(employee);

        leaveRequestService.cancel(leaveRequest.getId());

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId()).orElseThrow();

        assertEquals(20, updatedEmployee.getLeaveBalance());

        LeaveRequest saved =
                leaveRequestRepository.findById(leaveRequest.getId()).orElseThrow();

        assertTrue(saved.isDeleted());
    }

    @Test
    void cancel_shouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.cancel(999L));
    }

    @Test
    void approve_shouldApproveLeaveAndReduceBalance() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User manager =
                createUser("Manager", "manager@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                LeaveStatus.PENDING
        );

        LeaveRequestResponseDto response =
                leaveRequestService.approve(leaveRequest.getId(), manager.getId());

        assertEquals(LeaveStatus.APPROVED, response.getLeaveStatus());

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId()).orElseThrow();

        assertEquals(17, updatedEmployee.getLeaveBalance());

        LeaveRequest saved =
                leaveRequestRepository.findById(leaveRequest.getId()).orElseThrow();

        assertEquals(LeaveStatus.APPROVED, saved.getLeaveStatus());
        assertEquals(manager.getId(), saved.getApprovedBy().getId());
    }

    @Test
    void approve_shouldAllowAdmin() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User admin =
                createUser("Admin", "admin@test.com", Role.ADMIN);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        LeaveRequestResponseDto response =
                leaveRequestService.approve(leaveRequest.getId(), admin.getId());

        assertEquals(LeaveStatus.APPROVED, response.getLeaveStatus());

        LeaveRequest saved =
                leaveRequestRepository.findById(leaveRequest.getId()).orElseThrow();

        assertEquals(admin.getId(), saved.getApprovedBy().getId());
    }

    @Test
    void approve_shouldThrowWhenApproverIsEmployee() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User anotherEmployee =
                createUser("Mike", "mike@test.com", Role.EMPLOYEE);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        assertThrows(ForbiddenException.class,
                () -> leaveRequestService.approve(
                        leaveRequest.getId(),
                        anotherEmployee.getId()
                ));
    }

    @Test
    void approve_shouldThrowWhenEmployeeApprovesOwnRequest() {
        User employeeUser =
                createUser("John", "john@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        assertThrows(ForbiddenException.class,
                () -> leaveRequestService.approve(
                        leaveRequest.getId(),
                        employeeUser.getId()
                ));
    }

    @Test
    void approve_shouldThrowWhenAlreadyApproved() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User manager =
                createUser("Manager", "manager@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.APPROVED
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.approve(
                        leaveRequest.getId(),
                        manager.getId()
                ));
    }

    @Test
    void reject_shouldRejectLeaveRequest() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User manager =
                createUser("Manager", "manager@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        LeaveRequestResponseDto response =
                leaveRequestService.reject(leaveRequest.getId(), manager.getId());

        assertEquals(LeaveStatus.REJECTED, response.getLeaveStatus());

        LeaveRequest saved =
                leaveRequestRepository.findById(leaveRequest.getId()).orElseThrow();

        assertEquals(LeaveStatus.REJECTED, saved.getLeaveStatus());
        assertEquals(manager.getId(), saved.getApprovedBy().getId());
    }

    @Test
    void reject_shouldThrowWhenApproverIsEmployee() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User anotherEmployee =
                createUser("Mike", "mike@test.com", Role.EMPLOYEE);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        assertThrows(ForbiddenException.class,
                () -> leaveRequestService.reject(
                        leaveRequest.getId(),
                        anotherEmployee.getId()
                ));
    }

    @Test
    void reject_shouldThrowWhenEmployeeRejectsOwnRequest() {
        User employeeUser =
                createUser("John", "john@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        assertThrows(ForbiddenException.class,
                () -> leaveRequestService.reject(
                        leaveRequest.getId(),
                        employeeUser.getId()
                ));
    }

    @Test
    void reject_shouldThrowWhenAlreadyRejected() {
        User employeeUser =
                createUser("John", "john@test.com", Role.EMPLOYEE);

        User manager =
                createUser("Manager", "manager@test.com", Role.MANAGER);

        Department department = createDepartment("IT");
        Employee employee = createEmployee(employeeUser, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.REJECTED
        );

        assertThrows(BadRequestException.class,
                () -> leaveRequestService.reject(
                        leaveRequest.getId(),
                        manager.getId()
                ));
    }

    @Test
    void getById_shouldReturnLeaveRequest() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        LeaveRequestResponseDto response =
                leaveRequestService.getById(leaveRequest.getId());

        assertEquals(leaveRequest.getId(), response.getId());
        assertEquals(employee.getId(), response.getEmployeeId());
        assertEquals(leaveType.getId(), response.getLeaveTypeId());
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.getById(999L));
    }

    @Test
    void getById_shouldThrowWhenDeleted() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        LeaveRequest leaveRequest = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        leaveRequest.setDeleted(true);
        leaveRequestRepository.save(leaveRequest);

        assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.getById(leaveRequest.getId()));
    }

    @Test
    void getAll_shouldExcludeDeletedRequests() {
        User user = createUser("John", "john@test.com", Role.EMPLOYEE);
        Department department = createDepartment("IT");
        Employee employee = createEmployee(user, department, 20);
        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        LeaveRequest deleted = createLeaveRequest(
                employee,
                leaveType,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                LeaveStatus.PENDING
        );

        deleted.setDeleted(true);
        leaveRequestRepository.save(deleted);

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveRequestResponseDto> response =
                leaveRequestService.getAll(pageable);

        assertEquals(1, response.getContent().size());
    }

    @Test
    void getByEmployeeId_shouldReturnEmployeeRequests() {
        User user1 = createUser("John", "john@test.com", Role.EMPLOYEE);
        User user2 = createUser("Mike", "mike@test.com", Role.EMPLOYEE);

        Department department = createDepartment("IT");
        Employee employee1 = createEmployee(user1, department, 20);
        Employee employee2 = createEmployee(user2, department, 20);

        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        createLeaveRequest(
                employee1,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        createLeaveRequest(
                employee2,
                leaveType,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                LeaveStatus.PENDING
        );

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveRequestResponseDto> response =
                leaveRequestService.getByEmployeeId(employee1.getId(), pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(employee1.getId(),
                response.getContent().get(0).getEmployeeId());
    }

    @Test
    void getByStatus_shouldReturnMatchingRequests() {
        User user1 = createUser("John", "john@test.com", Role.EMPLOYEE);
        User user2 = createUser("Mike", "mike@test.com", Role.EMPLOYEE);

        Department department = createDepartment("IT");
        Employee employee1 = createEmployee(user1, department, 20);
        Employee employee2 = createEmployee(user2, department, 20);

        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        createLeaveRequest(
                employee1,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        createLeaveRequest(
                employee2,
                leaveType,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                LeaveStatus.APPROVED
        );

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveRequestResponseDto> response =
                leaveRequestService.getByStatus(
                        LeaveStatus.PENDING,
                        pageable
                );

        assertEquals(1, response.getContent().size());
        assertEquals(
                LeaveStatus.PENDING,
                response.getContent().get(0).getLeaveStatus()
        );
    }

    @Test
    void getByDepartmentId_shouldReturnDepartmentRequests() {
        User user1 = createUser("John", "john@test.com", Role.EMPLOYEE);
        User user2 = createUser("Mike", "mike@test.com", Role.EMPLOYEE);

        Department department1 = createDepartment("IT");
        Department department2 = createDepartment("HR");

        Employee employee1 = createEmployee(user1, department1, 20);
        Employee employee2 = createEmployee(user2, department2, 20);

        LeaveType leaveType = createLeaveType("Annual Leave", 20);

        createLeaveRequest(
                employee1,
                leaveType,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                LeaveStatus.PENDING
        );

        createLeaveRequest(
                employee2,
                leaveType,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(6),
                LeaveStatus.PENDING
        );

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveRequestResponseDto> response =
                leaveRequestService.getByDepartmentId(
                        department1.getId(),
                        pageable
                );

        assertEquals(1, response.getContent().size());
        assertEquals(
                employee1.getId(),
                response.getContent().get(0).getEmployeeId()
        );
    }

    private User createUser(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setDeleted(false);
        user.setStatus(true);
        return userRepository.save(user);
    }

    private Department createDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription("Test department");
        department.setDeleted(false);
        department.setStatus(true);
        return departmentRepository.save(department);
    }

    private LeaveType createLeaveType(String name, int maxDays) {
        LeaveType leaveType = new LeaveType();
        leaveType.setName(name);
        leaveType.setMaxDays(maxDays);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);
        return leaveTypeRepository.save(leaveType);
    }

    private Employee createEmployee(
            User user,
            Department department,
            int leaveBalance
    ) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(LocalDate.now().minusMonths(6));
        employee.setLeaveBalance(leaveBalance);
        employee.setDeleted(false);
        employee.setStatus(true);
        return employeeRepository.save(employee);
    }

    private LeaveRequest createLeaveRequest(
            Employee employee,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            LeaveStatus status
    ) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setReason("Test leave");
        leaveRequest.setLeaveStatus(status);
        leaveRequest.setDeleted(false);
        leaveRequest.setStatus(true);
        return leaveRequestRepository.save(leaveRequest);
    }

    private LeaveRequestRequestDto createRequest(
            Long employeeId,
            Long leaveTypeId,
            LocalDate startDate,
            LocalDate endDate,
            String reason
    ) {
        LeaveRequestRequestDto dto = new LeaveRequestRequestDto();
        dto.setEmployeeId(employeeId);
        dto.setLeaveTypeId(leaveTypeId);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setReason(reason);
        return dto;
    }
}