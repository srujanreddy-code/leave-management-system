package com.ust.lms.employee.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.EmployeeService;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeRepository;
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
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);

        return userRepository.save(user);
    }

    private Department createDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(name + " Department");

        return departmentRepository.save(department);
    }

    private EmployeeRequestDto createRequest(
            Long userId,
            Long departmentId,
            LocalDate joiningDate,
            Integer leaveBalance) {

        EmployeeRequestDto request = new EmployeeRequestDto();
        request.setUserId(userId);
        request.setDepartmentId(departmentId);
        request.setJoiningDate(joiningDate);
        request.setLeaveBalance(leaveBalance);

        return request;
    }

    @Test
    void createEmployee_shouldCreateSuccessfully() {
        User user = createUser(
                "John Doe",
                "john@example.com"
        );

        Department department =
                createDepartment("Engineering");

        EmployeeRequestDto request = createRequest(
                user.getId(),
                department.getId(),
                LocalDate.of(2026, 1, 10),
                20
        );

        EmployeeResponseDto response =
                employeeService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());

        assertEquals(user.getId(), response.getUserId());
        assertEquals("John Doe", response.getUserName());

        assertEquals(
                department.getId(),
                response.getDepartmentId()
        );
        assertEquals(
                "Engineering",
                response.getDepartmentName()
        );

        assertEquals(
                LocalDate.of(2026, 1, 10),
                response.getJoiningDate()
        );

        assertEquals(20, response.getLeaveBalance());

        assertEquals(1, employeeRepository.count());
    }

    @Test
    void createEmployee_shouldThrowExceptionWhenUserDoesNotExist() {
        Department department =
                createDepartment("Engineering");

        EmployeeRequestDto request = createRequest(
                999999L,
                department.getId(),
                LocalDate.of(2026, 1, 10),
                20
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.create(request)
        );

        assertEquals(0, employeeRepository.count());
    }

    @Test
    void createEmployee_shouldThrowExceptionWhenDepartmentDoesNotExist() {
        User user = createUser(
                "John Doe",
                "john@example.com"
        );

        EmployeeRequestDto request = createRequest(
                user.getId(),
                999999L,
                LocalDate.of(2026, 1, 10),
                20
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.create(request)
        );

        assertEquals(0, employeeRepository.count());
    }

    @Test
    void getById_shouldReturnEmployee() {
        User user = createUser(
                "John Doe",
                "john@example.com"
        );

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 10)
        );
        employee.setLeaveBalance(20);

        Employee saved = employeeRepository.save(employee);

        EmployeeResponseDto response =
                employeeService.getById(saved.getId());

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals(user.getId(), response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertEquals(
                department.getId(),
                response.getDepartmentId()
        );
        assertEquals(
                "Engineering",
                response.getDepartmentName()
        );
        assertEquals(
                LocalDate.of(2026, 1, 10),
                response.getJoiningDate()
        );
        assertEquals(20, response.getLeaveBalance());
    }

    @Test
    void getById_shouldThrowExceptionWhenEmployeeDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getById(999999L)
        );
    }

    @Test
    void getById_shouldThrowExceptionForDeletedEmployee() {
        User user = createUser(
                "Deleted Employee",
                "deleted@example.com"
        );

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 10)
        );
        employee.setLeaveBalance(20);
        employee.setDeleted(true);

        Employee saved = employeeRepository.save(employee);

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getById(saved.getId())
        );
    }

    @Test
    void getAll_shouldReturnOnlyNonDeletedEmployees() {
        User user1 =
                createUser("John", "john@example.com");

        User user2 =
                createUser("Jane", "jane@example.com");

        User deletedUser =
                createUser("Deleted", "deleted@example.com");

        Department department =
                createDepartment("Engineering");

        Employee employee1 = new Employee();
        employee1.setUser(user1);
        employee1.setDepartment(department);
        employee1.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee1.setLeaveBalance(20);

        Employee employee2 = new Employee();
        employee2.setUser(user2);
        employee2.setDepartment(department);
        employee2.setJoiningDate(
                LocalDate.of(2026, 2, 1)
        );
        employee2.setLeaveBalance(15);

        Employee deletedEmployee = new Employee();
        deletedEmployee.setUser(deletedUser);
        deletedEmployee.setDepartment(department);
        deletedEmployee.setJoiningDate(
                LocalDate.of(2026, 3, 1)
        );
        deletedEmployee.setLeaveBalance(10);
        deletedEmployee.setDeleted(true);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(deletedEmployee);

        Pageable pageable =
                PageRequest.of(0, 10);

        PageResponseDto<EmployeeResponseDto> response =
                employeeService.getAll(pageable);

        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(2, response.getContent().size());
    }

    @Test
    void getByDepartmentId_shouldReturnEmployeesFromDepartment() {
        User user1 =
                createUser("John", "john@example.com");

        User user2 =
                createUser("Jane", "jane@example.com");

        Department engineering =
                createDepartment("Engineering");

        Department hr =
                createDepartment("HR");

        Employee employee1 = new Employee();
        employee1.setUser(user1);
        employee1.setDepartment(engineering);
        employee1.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee1.setLeaveBalance(20);

        Employee employee2 = new Employee();
        employee2.setUser(user2);
        employee2.setDepartment(hr);
        employee2.setJoiningDate(
                LocalDate.of(2026, 2, 1)
        );
        employee2.setLeaveBalance(15);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        Pageable pageable =
                PageRequest.of(0, 10);

        PageResponseDto<EmployeeResponseDto> response =
                employeeService.getByDepartmentId(
                        engineering.getId(),
                        pageable
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                engineering.getId(),
                response.getContent()
                        .get(0)
                        .getDepartmentId()
        );

        assertEquals(
                "John",
                response.getContent()
                        .get(0)
                        .getUserName()
        );
    }

    @Test
    void getIdByUserId_shouldReturnEmployeeId() {
        User user =
                createUser("John", "john@example.com");

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);

        Employee saved =
                employeeRepository.save(employee);

        Long employeeId =
                employeeService.getIdByUserId(user.getId());

        assertEquals(saved.getId(), employeeId);
    }

    @Test
    void getIdByUserId_shouldThrowExceptionWhenEmployeeDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getIdByUserId(999999L)
        );
    }

    @Test
    void updateEmployee_shouldUpdateSuccessfully() {
        User user =
                createUser("John", "john@example.com");

        Department oldDepartment =
                createDepartment("Engineering");

        Department newDepartment =
                createDepartment("HR");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(oldDepartment);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);

        Employee saved =
                employeeRepository.save(employee);

        EmployeeRequestDto request =
                createRequest(
                        user.getId(),
                        newDepartment.getId(),
                        LocalDate.of(2026, 5, 1),
                        30
                );

        EmployeeResponseDto response =
                employeeService.update(
                        saved.getId(),
                        request
                );

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals(
                newDepartment.getId(),
                response.getDepartmentId()
        );
        assertEquals(
                "HR",
                response.getDepartmentName()
        );
        assertEquals(
                LocalDate.of(2026, 5, 1),
                response.getJoiningDate()
        );
        assertEquals(30, response.getLeaveBalance());

        Employee updated =
                employeeRepository
                        .findById(saved.getId())
                        .orElseThrow();

        assertEquals(
                newDepartment.getId(),
                updated.getDepartment().getId()
        );
        assertEquals(
                LocalDate.of(2026, 5, 1),
                updated.getJoiningDate()
        );
        assertEquals(
                30,
                updated.getLeaveBalance()
        );
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenEmployeeDoesNotExist() {
        User user =
                createUser("John", "john@example.com");

        Department department =
                createDepartment("Engineering");

        EmployeeRequestDto request =
                createRequest(
                        user.getId(),
                        department.getId(),
                        LocalDate.of(2026, 1, 1),
                        20
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.update(
                        999999L,
                        request
                )
        );
    }

    @Test
    void updateEmployee_shouldThrowExceptionWhenDepartmentDoesNotExist() {
        User user =
                createUser("John", "john@example.com");

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);

        Employee saved =
                employeeRepository.save(employee);

        EmployeeRequestDto request =
                createRequest(
                        user.getId(),
                        999999L,
                        LocalDate.of(2026, 5, 1),
                        30
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.update(
                        saved.getId(),
                        request
                )
        );
    }

    @Test
    void updateEmployee_shouldAllowPartialUpdate() {
        User user =
                createUser("John", "john@example.com");

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);

        Employee saved =
                employeeRepository.save(employee);

        EmployeeRequestDto request =
                new EmployeeRequestDto();

        request.setUserId(user.getId());
        request.setDepartmentId(null);
        request.setJoiningDate(null);
        request.setLeaveBalance(30);

        EmployeeResponseDto response =
                employeeService.update(
                        saved.getId(),
                        request
                );

        assertNotNull(response);
        assertEquals(30, response.getLeaveBalance());
        assertEquals(
                department.getId(),
                response.getDepartmentId()
        );
        assertEquals(
                LocalDate.of(2026, 1, 1),
                response.getJoiningDate()
        );
    }

    @Test
    void deleteEmployee_shouldSoftDeleteSuccessfully() {
        User user =
                createUser("John", "john@example.com");

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);

        Employee saved =
                employeeRepository.save(employee);

        employeeService.delete(saved.getId());

        Employee deleted =
                employeeRepository
                        .findById(saved.getId())
                        .orElseThrow();

        assertTrue(deleted.isDeleted());
    }

    @Test
    void deleteEmployee_shouldThrowExceptionWhenEmployeeDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.delete(999999L)
        );
    }

    @Test
    void deleteEmployee_shouldThrowExceptionWhenAlreadyDeleted() {
        User user =
                createUser(
                        "Deleted Employee",
                        "deleted@example.com"
                );

        Department department =
                createDepartment("Engineering");

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(
                LocalDate.of(2026, 1, 1)
        );
        employee.setLeaveBalance(20);
        employee.setDeleted(true);

        Employee saved =
                employeeRepository.save(employee);

        assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.delete(saved.getId())
        );
    }
}