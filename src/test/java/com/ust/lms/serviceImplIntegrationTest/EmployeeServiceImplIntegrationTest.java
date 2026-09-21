package com.ust.lms.serviceImplIntegrationTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.DepartmentNotFoundException;
import com.ust.lms.common.exception.EmployeeNotFoundException;
import com.ust.lms.common.exception.UserNotFoundException;
import com.ust.lms.dto.EmployeeLeaveBalanceResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.impl.EmployeeServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link EmployeeServiceImpl}.
 *
 * <p>Verifies employee service operations using the actual Spring context,
 * repositories, and test database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeServiceImplIntegrationTest {

    @Autowired
    private EmployeeServiceImpl employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    private User user;
    private Department department;
    private LeaveType sickLeave;
    private LeaveType earnedLeave;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("john" + System.nanoTime() + "@example.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(false);
        user.setStatus(true);
        userRepository.save(user);

        department = new Department();
        department.setName("Engineering" + System.nanoTime());
        department.setDescription("Engineering Department");
        department.setDeleted(false);
        department.setStatus(true);
        departmentRepository.save(department);

        sickLeave = new LeaveType();
        sickLeave.setName("Sick Leave" + System.nanoTime());
        sickLeave.setMaxDays(6);
        sickLeave.setDeleted(false);
        sickLeave.setStatus(true);
        leaveTypeRepository.save(sickLeave);

        earnedLeave = new LeaveType();
        earnedLeave.setName("Earned Leave" + System.nanoTime());
        earnedLeave.setMaxDays(8);
        earnedLeave.setDeleted(false);
        earnedLeave.setStatus(true);
        leaveTypeRepository.save(earnedLeave);
    }

    /**
     * Verifies that an employee is created successfully and leave balances
     * are automatically created for all active leave types.
     */
    @Test
    void create_success() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(user.getId());
        dto.setDepartmentId(department.getId());
        dto.setJoiningDate(LocalDate.now());

        EmployeeResponseDto result = employeeService.create(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals(department.getId(), result.getDepartmentId());
        assertEquals(department.getName(), result.getDepartmentName());

        Employee savedEmployee =
                employeeRepository.findById(result.getId()).orElseThrow();

        assertEquals(14, savedEmployee.getLeaveBalance());

        List<EmployeeLeaveBalance> balances =
                employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(
                        savedEmployee.getId()
                );

        assertEquals(2, balances.size());

        assertTrue(
                balances.stream()
                        .anyMatch(balance ->
                                balance.getLeaveType().getId().equals(sickLeave.getId())
                                        && balance.getBalance() == 6)
        );

        assertTrue(
                balances.stream()
                        .anyMatch(balance ->
                                balance.getLeaveType().getId().equals(earnedLeave.getId())
                                        && balance.getBalance() == 8)
        );
    }

    /**
     * Verifies that UserNotFoundException is thrown when the specified
     * user does not exist.
     */
    @Test
    void create_userNotFound_throwsUserNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(999999L);
        dto.setDepartmentId(department.getId());
        dto.setJoiningDate(LocalDate.now());

        assertThrows(
                UserNotFoundException.class,
                () -> employeeService.create(dto)
        );
    }

    /**
     * Verifies that DepartmentNotFoundException is thrown when the specified
     * department does not exist.
     */
    @Test
    void create_departmentNotFound_throwsDepartmentNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(user.getId());
        dto.setDepartmentId(999999L);
        dto.setJoiningDate(LocalDate.now());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> employeeService.create(dto)
        );
    }

    /**
     * Verifies that an employee is retrieved successfully by ID.
     */
    @Test
    void getById_success() {
        Employee employee = createEmployee();

        EmployeeResponseDto result =
                employeeService.getById(employee.getId());

        assertNotNull(result);
        assertEquals(employee.getId(), result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals(department.getName(), result.getDepartmentName());
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when an employee
     * does not exist.
     */
    @Test
    void getById_notFound_throwsEmployeeNotFound() {
        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getById(999999L)
        );
    }

    /**
     * Verifies that a soft-deleted employee cannot be retrieved.
     */
    @Test
    void getById_softDeletedRecord_throwsEmployeeNotFound() {
        Employee employee = createEmployee();

        employee.setDeleted(true);
        employeeRepository.save(employee);

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getById(employee.getId())
        );
    }

    /**
     * Verifies that all active employees are retrieved using pagination.
     */
    @Test
    void getAll_success() {
        Employee employee = createEmployee();

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<EmployeeResponseDto> result =
                employeeService.getAll(pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);

        assertTrue(
                result.getContent().stream()
                        .anyMatch(dto -> dto.getId().equals(employee.getId()))
        );
    }

    /**
     * Verifies that active employees belonging to a department
     * are retrieved successfully.
     */
    @Test
    void getByDepartmentId_success() {
        Employee employee = createEmployee();

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<EmployeeResponseDto> result =
                employeeService.getByDepartmentId(
                        department.getId(),
                        pageable
                );

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 1);

        assertTrue(
                result.getContent().stream()
                        .anyMatch(dto -> dto.getId().equals(employee.getId()))
        );
    }

    /**
     * Verifies that the employee ID associated with a user ID
     * is retrieved successfully.
     */
    @Test
    void getIdByUserId_success() {
        Employee employee = createEmployee();

        Long employeeId =
                employeeService.getIdByUserId(user.getId());

        assertEquals(employee.getId(), employeeId);
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when no employee
     * exists for the specified user.
     */
    @Test
    void getIdByUserId_notFound_throwsEmployeeNotFound() {
        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getIdByUserId(999999L)
        );
    }

    /**
     * Verifies that all leave balances for an employee are retrieved
     * successfully along with their leave type details.
     */
    @Test
    void getLeaveBalances_success() {
        Employee employee = createEmployee();

        List<EmployeeLeaveBalanceResponseDto> result =
                employeeService.getLeaveBalances(employee.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        EmployeeLeaveBalanceResponseDto sickBalance =
                result.stream()
                        .filter(dto -> dto.getLeaveTypeId().equals(sickLeave.getId()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(employee.getId(), sickBalance.getEmployeeId());
        assertEquals(sickLeave.getId(), sickBalance.getLeaveTypeId());
        assertEquals(sickLeave.getName(), sickBalance.getLeaveTypeName());
        assertEquals(6, sickBalance.getBalance());
        assertEquals(employee.getJoiningDate(), sickBalance.getLeaveYearStart());
        assertEquals(
                employee.getJoiningDate().plusYears(1).minusDays(1),
                sickBalance.getLeaveYearEnd()
        );

        EmployeeLeaveBalanceResponseDto earnedBalance =
                result.stream()
                        .filter(dto -> dto.getLeaveTypeId().equals(earnedLeave.getId()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(employee.getId(), earnedBalance.getEmployeeId());
        assertEquals(earnedLeave.getId(), earnedBalance.getLeaveTypeId());
        assertEquals(earnedLeave.getName(), earnedBalance.getLeaveTypeName());
        assertEquals(8, earnedBalance.getBalance());
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when attempting
     * to retrieve leave balances for a non-existent employee.
     */
    @Test
    void getLeaveBalances_employeeNotFound_throwsEmployeeNotFound() {
        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getLeaveBalances(999999L)
        );
    }

    /**
     * Verifies that leave balances belonging to a soft-deleted employee
     * cannot be retrieved.
     */
    @Test
    void getLeaveBalances_softDeletedRecord_throwsEmployeeNotFound() {
        Employee employee = createEmployee();

        employee.setDeleted(true);
        employeeRepository.save(employee);

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getLeaveBalances(employee.getId())
        );
    }

    /**
     * Verifies that only active leave balance records are returned.
     */
    @Test
    void getLeaveBalances_ignoresSoftDeletedBalances() {
        Employee employee = createEmployee();

        EmployeeLeaveBalance deletedBalance =
                employeeLeaveBalanceRepository
                        .findByEmployeeIdAndDeletedFalse(employee.getId())
                        .stream()
                        .filter(balance ->
                                balance.getLeaveType().getId().equals(sickLeave.getId()))
                        .findFirst()
                        .orElseThrow();

        deletedBalance.setDeleted(true);
        employeeLeaveBalanceRepository.save(deletedBalance);

        List<EmployeeLeaveBalanceResponseDto> result =
                employeeService.getLeaveBalances(employee.getId());

        assertEquals(1, result.size());
        assertEquals(
                earnedLeave.getId(),
                result.get(0).getLeaveTypeId()
        );
    }

    /**
     * Verifies that an employee's department and joining date are
     * updated successfully.
     */
    @Test
    void update_success() {
        Employee employee = createEmployee();

        Department newDepartment = new Department();
        newDepartment.setName("Human Resources" + System.nanoTime());
        newDepartment.setDescription("HR Department");
        newDepartment.setDeleted(false);
        newDepartment.setStatus(true);
        departmentRepository.save(newDepartment);

        LocalDate newJoiningDate = LocalDate.of(2026, 10, 1);

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(newDepartment.getId());
        dto.setJoiningDate(newJoiningDate);

        EmployeeResponseDto result =
                employeeService.update(employee.getId(), dto);

        assertNotNull(result);
        assertEquals(
                newDepartment.getId(),
                result.getDepartmentId()
        );
        assertEquals(
                newDepartment.getName(),
                result.getDepartmentName()
        );

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId()).orElseThrow();

        assertEquals(
                newDepartment.getId(),
                updatedEmployee.getDepartment().getId()
        );
        assertEquals(
                newJoiningDate,
                updatedEmployee.getJoiningDate()
        );
    }

    /**
     * Verifies that only the joining date is updated when no department
     * is supplied.
     */
    @Test
    void update_joiningDateOnly_success() {
        Employee employee = createEmployee();

        LocalDate newJoiningDate = LocalDate.of(2026, 11, 1);

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setJoiningDate(newJoiningDate);

        EmployeeResponseDto result =
                employeeService.update(employee.getId(), dto);

        assertNotNull(result);
        assertEquals(
                newJoiningDate,
                result.getJoiningDate()
        );
        assertEquals(
                department.getName(),
                result.getDepartmentName()
        );
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when attempting
     * to update a non-existent employee.
     */
    @Test
    void update_employeeNotFound_throwsEmployeeNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setJoiningDate(LocalDate.of(2026, 10, 1));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.update(999999L, dto)
        );
    }

    /**
     * Verifies that a soft-deleted employee cannot be updated.
     */
    @Test
    void update_softDeletedRecord_throwsEmployeeNotFound() {
        Employee employee = createEmployee();

        employee.setDeleted(true);
        employeeRepository.save(employee);

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setJoiningDate(LocalDate.of(2026, 10, 1));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.update(employee.getId(), dto)
        );
    }

    /**
     * Verifies that DepartmentNotFoundException is thrown when updating
     * an employee with a non-existent department.
     */
    @Test
    void update_departmentNotFound_throwsDepartmentNotFound() {
        Employee employee = createEmployee();

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(999999L);

        assertThrows(
                DepartmentNotFoundException.class,
                () -> employeeService.update(employee.getId(), dto)
        );
    }

    /**
     * Verifies that an employee is soft deleted successfully.
     */
    @Test
    void delete_success() {
        Employee employee = createEmployee();

        employeeService.delete(employee.getId());

        Employee deletedEmployee =
                employeeRepository.findById(employee.getId()).orElseThrow();

        assertTrue(deletedEmployee.isDeleted());
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when attempting
     * to delete a non-existent employee.
     */
    @Test
    void delete_notFound_throwsEmployeeNotFound() {
        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.delete(999999L)
        );
    }

    /**
     * Verifies that a soft-deleted employee cannot be deleted again.
     */
    @Test
    void delete_softDeletedRecord_throwsEmployeeNotFound() {
        Employee employee = createEmployee();

        employee.setDeleted(true);
        employeeRepository.save(employee);

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.delete(employee.getId())
        );
    }

    /**
     * Creates and persists an employee using the service.
     *
     * @return persisted employee
     */
    private Employee createEmployee() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(user.getId());
        dto.setDepartmentId(department.getId());
        dto.setJoiningDate(LocalDate.now());

        EmployeeResponseDto response =
                employeeService.create(dto);

        return employeeRepository.findById(response.getId())
                .orElseThrow();
    }
}
