package com.ust.lms.serviceImplTest;

import com.ust.lms.common.PageResponseDto;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmployeeServiceImpl}.
 *
 * <p>Verifies employee service business logic using mocked repository
 * dependencies.</p>
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private User user;
    private Department department;
    private Employee employee;
    private LeaveType sickLeave;
    private LeaveType earnedLeave;

    /**
     * Sets up common test data before each test.
     */
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");

        department = new Department();
        department.setId(10L);
        department.setName("Engineering");

        employee = new Employee();
        employee.setId(100L);
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(LocalDate.now());
        employee.setLeaveBalance(20);
        employee.setDeleted(false);

        sickLeave = new LeaveType();
        sickLeave.setId(1L);
        sickLeave.setName("Sick Leave");
        sickLeave.setMaxDays(6);

        earnedLeave = new LeaveType();
        earnedLeave.setId(2L);
        earnedLeave.setName("Earned Leave");
        earnedLeave.setMaxDays(8);
    }

    /**
     * Verifies that an employee is created successfully with
     * leave balances initialized for all active leave types.
     */
    @Test
    void create_success() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(1L);
        dto.setDepartmentId(10L);
        dto.setJoiningDate(LocalDate.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(leaveTypeRepository.findByDeletedFalse())
                .thenReturn(List.of(sickLeave, earnedLeave));
        when(employeeLeaveBalanceRepository.save(any(EmployeeLeaveBalance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDto result = employeeService.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Engineering", result.getDepartmentName());

        verify(employeeRepository, times(2)).save(any(Employee.class));
        verify(employeeLeaveBalanceRepository, times(2))
                .save(any(EmployeeLeaveBalance.class));

        assertEquals(14, employee.getLeaveBalance());
    }

    /**
     * Verifies that UserNotFoundException is thrown when the
     * specified user does not exist.
     */
    @Test
    void create_userNotFound_throwsUserNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(99L);
        dto.setDepartmentId(10L);
        dto.setJoiningDate(LocalDate.now());

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> employeeService.create(dto)
        );

        verify(departmentRepository, never()).findById(anyLong());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that DepartmentNotFoundException is thrown when the
     * specified department does not exist.
     */
    @Test
    void create_departmentNotFound_throwsDepartmentNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setUserId(1L);
        dto.setDepartmentId(99L);
        dto.setJoiningDate(LocalDate.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> employeeService.create(dto)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that an employee is retrieved successfully by ID.
     */
    @Test
    void getById_success() {
        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        EmployeeResponseDto result = employeeService.getById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Engineering", result.getDepartmentName());
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when the
     * employee does not exist.
     */
    @Test
    void getById_notFound_throwsEmployeeNotFound() {
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getById(99L)
        );
    }

    /**
     * Verifies that a soft-deleted employee cannot be retrieved.
     */
    @Test
    void getById_softDeletedRecord_throwsEmployeeNotFound() {
        employee.setDeleted(true);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getById(100L)
        );
    }

    /**
     * Verifies that all active employees are retrieved using pagination.
     */
    @Test
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(
                List.of(employee),
                pageable,
                1
        );

        when(employeeRepository.findByDeletedFalse(pageable))
                .thenReturn(page);

        PageResponseDto<EmployeeResponseDto> result =
                employeeService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(
                "John Doe",
                result.getContent().get(0).getUserName()
        );

        verify(employeeRepository).findByDeletedFalse(pageable);
    }

    /**
     * Verifies that active employees belonging to a department
     * are retrieved successfully.
     */
    @Test
    void getByDepartmentId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(
                List.of(employee),
                pageable,
                1
        );

        when(employeeRepository.findByDepartmentIdAndDeletedFalse(
                10L,
                pageable
        )).thenReturn(page);

        PageResponseDto<EmployeeResponseDto> result =
                employeeService.getByDepartmentId(10L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(
                "Engineering",
                result.getContent().get(0).getDepartmentName()
        );

        verify(employeeRepository)
                .findByDepartmentIdAndDeletedFalse(10L, pageable);
    }

    /**
     * Verifies that the employee ID is retrieved using the
     * associated user ID.
     */
    @Test
    void getIdByUserId_success() {
        when(employeeRepository.findByUserId(1L))
                .thenReturn(Optional.of(employee));

        Long employeeId = employeeService.getIdByUserId(1L);

        assertEquals(100L, employeeId);

        verify(employeeRepository).findByUserId(1L);
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when
     * no employee exists for the specified user.
     */
    @Test
    void getIdByUserId_notFound_throwsEmployeeNotFound() {
        when(employeeRepository.findByUserId(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getIdByUserId(99L)
        );
    }

    /**
     * Verifies that all active leave balances are retrieved successfully
     * along with their corresponding leave type details.
     */
    @Test
    void getLeaveBalances_success() {
        EmployeeLeaveBalance sickBalance = new EmployeeLeaveBalance();
        sickBalance.setId(201L);
        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);
        sickBalance.setBalance(6);
        sickBalance.setLeaveYearStart(employee.getJoiningDate());
        sickBalance.setLeaveYearEnd(
                employee.getJoiningDate().plusYears(1).minusDays(1)
        );
        sickBalance.setDeleted(false);

        EmployeeLeaveBalance earnedBalance = new EmployeeLeaveBalance();
        earnedBalance.setId(202L);
        earnedBalance.setEmployee(employee);
        earnedBalance.setLeaveType(earnedLeave);
        earnedBalance.setBalance(8);
        earnedBalance.setLeaveYearStart(employee.getJoiningDate());
        earnedBalance.setLeaveYearEnd(
                employee.getJoiningDate().plusYears(1).minusDays(1)
        );
        earnedBalance.setDeleted(false);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(100L))
                .thenReturn(List.of(sickBalance, earnedBalance));

        List<EmployeeLeaveBalanceResponseDto> result =
                employeeService.getLeaveBalances(100L);

        assertNotNull(result);
        assertEquals(2, result.size());

        EmployeeLeaveBalanceResponseDto sickResult =
                result.stream()
                        .filter(dto -> dto.getLeaveTypeId().equals(1L))
                        .findFirst()
                        .orElseThrow();

        assertEquals(201L, sickResult.getId());
        assertEquals(100L, sickResult.getEmployeeId());
        assertEquals(1L, sickResult.getLeaveTypeId());
        assertEquals("Sick Leave", sickResult.getLeaveTypeName());
        assertEquals(6, sickResult.getBalance());
        assertEquals(
                employee.getJoiningDate(),
                sickResult.getLeaveYearStart()
        );
        assertEquals(
                employee.getJoiningDate().plusYears(1).minusDays(1),
                sickResult.getLeaveYearEnd()
        );

        EmployeeLeaveBalanceResponseDto earnedResult =
                result.stream()
                        .filter(dto -> dto.getLeaveTypeId().equals(2L))
                        .findFirst()
                        .orElseThrow();

        assertEquals(202L, earnedResult.getId());
        assertEquals(100L, earnedResult.getEmployeeId());
        assertEquals(2L, earnedResult.getLeaveTypeId());
        assertEquals("Earned Leave", earnedResult.getLeaveTypeName());
        assertEquals(8, earnedResult.getBalance());

        verify(employeeRepository).findById(100L);
        verify(employeeLeaveBalanceRepository)
                .findByEmployeeIdAndDeletedFalse(100L);
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when attempting
     * to retrieve leave balances for a non-existent employee.
     */
    @Test
    void getLeaveBalances_employeeNotFound_throwsEmployeeNotFound() {
        when(employeeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getLeaveBalances(999L)
        );

        verify(employeeLeaveBalanceRepository, never())
                .findByEmployeeIdAndDeletedFalse(anyLong());
    }

    /**
     * Verifies that a soft-deleted employee cannot have their
     * leave balances retrieved.
     */
    @Test
    void getLeaveBalances_softDeletedRecord_throwsEmployeeNotFound() {
        employee.setDeleted(true);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.getLeaveBalances(100L)
        );

        verify(employeeLeaveBalanceRepository, never())
                .findByEmployeeIdAndDeletedFalse(anyLong());
    }

    /**
     * Verifies that an employee's department is updated successfully.
     */
    @Test
    void update_success() {
        Department newDepartment = new Department();
        newDepartment.setId(20L);
        newDepartment.setName("HR");

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(20L);
        dto.setJoiningDate(LocalDate.of(2026, 9, 15));

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));
        when(departmentRepository.findById(20L))
                .thenReturn(Optional.of(newDepartment));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDto result =
                employeeService.update(100L, dto);

        assertNotNull(result);
        assertEquals("HR", result.getDepartmentName());
        assertEquals(
                LocalDate.of(2026, 9, 15),
                employee.getJoiningDate()
        );

        verify(employeeRepository).save(employee);
    }

    /**
     * Verifies that an employee's joining date can be updated
     * without changing the department.
     */
    @Test
    void update_joiningDateOnly_success() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setJoiningDate(LocalDate.of(2026, 10, 1));

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponseDto result =
                employeeService.update(100L, dto);

        assertNotNull(result);
        assertEquals(
                LocalDate.of(2026, 10, 1),
                employee.getJoiningDate()
        );
        assertEquals("Engineering", employee.getDepartment().getName());

        verify(departmentRepository, never()).findById(anyLong());
        verify(employeeRepository).save(employee);
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when
     * attempting to update a non-existent employee.
     */
    @Test
    void update_employeeNotFound_throwsEmployeeNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(20L);

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.update(99L, dto)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that a soft-deleted employee cannot be updated.
     */
    @Test
    void update_softDeletedRecord_throwsEmployeeNotFound() {
        employee.setDeleted(true);

        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(20L);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.update(100L, dto)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that DepartmentNotFoundException is thrown when
     * updating an employee with a non-existent department.
     */
    @Test
    void update_departmentNotFound_throwsDepartmentNotFound() {
        EmployeeRequestDto dto = new EmployeeRequestDto();
        dto.setDepartmentId(99L);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));
        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> employeeService.update(100L, dto)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that an employee is soft deleted successfully.
     */
    @Test
    void delete_success() {
        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        employeeService.delete(100L);

        assertTrue(employee.isDeleted());

        verify(employeeRepository, times(1)).save(employee);
    }

    /**
     * Verifies that EmployeeNotFoundException is thrown when
     * attempting to delete a non-existent employee.
     */
    @Test
    void delete_notFound_throwsEmployeeNotFound() {
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.delete(99L)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    /**
     * Verifies that a soft-deleted employee cannot be deleted again.
     */
    @Test
    void delete_softDeletedRecord_throwsEmployeeNotFound() {
        employee.setDeleted(true);

        when(employeeRepository.findById(100L))
                .thenReturn(Optional.of(employee));

        assertThrows(
                EmployeeNotFoundException.class,
                () -> employeeService.delete(100L)
        );

        verify(employeeRepository, never()).save(any(Employee.class));
    }
}