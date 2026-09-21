package com.ust.lms.serviceImplIntegrationTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.DuplicateLeaveTypeException;
import com.ust.lms.common.exception.LeaveTypeNotFoundException;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.leavetype.impl.LeaveTypeServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link LeaveTypeServiceImpl}.
 *
 * <p>Verifies leave type service operations using the actual Spring context,
 * repositories, and test database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveTypeServiceImplIntegrationTest {

    @Autowired
    private LeaveTypeServiceImpl leaveTypeService;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User user;
    private Department department;
    private Employee employee;

    /**
     * Sets up an active employee before each test.
     */
    @BeforeEach
    void setUp() {

        user = new User();
        user.setName("Test Employee");
        user.setEmail(
                "leave-type-test-" + System.nanoTime()
                        + "@example.com"
        );
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(false);
        user.setStatus(true);

        user = userRepository.save(user);

        department = new Department();
        department.setName(
                "Engineering-" + System.nanoTime()
        );
        department.setDescription("Engineering Department");
        department.setDeleted(false);
        department.setStatus(true);

        department = departmentRepository.save(department);

        employee = new Employee();
        employee.setUser(user);
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
     * Verifies that a leave type is created and a leave balance is
     * initialized for every active employee.
     */
    @Test
    void create_success() {

        LeaveTypeRequestDto dto =
                new LeaveTypeRequestDto();

        dto.setName(
                "Sick Leave-" + System.nanoTime()
        );
        dto.setMaxDays(6);

        LeaveTypeResponseDto result =
                leaveTypeService.create(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getMaxDays(), result.getMaxDays());

        LeaveType savedLeaveType =
                leaveTypeRepository
                        .findById(result.getId())
                        .orElseThrow();

        assertEquals(
                dto.getName(),
                savedLeaveType.getName()
        );

        List<EmployeeLeaveBalance> balances =
                employeeLeaveBalanceRepository
                        .findByEmployeeIdAndDeletedFalse(employee.getId());

        assertTrue(
                balances.stream()
                        .anyMatch(balance ->
                                balance.getLeaveType()
                                        .getId()
                                        .equals(savedLeaveType.getId())
                                        && balance.getBalance() == 6
                        )
        );

        Employee savedEmployee =
                employeeRepository
                        .findById(employee.getId())
                        .orElseThrow();

        assertEquals(
                6,
                savedEmployee.getLeaveBalance()
        );
    }

    /**
     * Verifies that duplicate leave type names are rejected.
     */
    @Test
    void create_duplicateName_throwsDuplicateLeaveType() {

        LeaveType existing = new LeaveType();
        existing.setName(
                "Casual Leave-" + System.nanoTime()
        );
        existing.setMaxDays(10);
        existing.setDeleted(false);
        existing.setStatus(true);

        existing = leaveTypeRepository.save(existing);

        LeaveTypeRequestDto dto =
                new LeaveTypeRequestDto();

        dto.setName(existing.getName());
        dto.setMaxDays(12);

        assertThrows(
                DuplicateLeaveTypeException.class,
                () -> leaveTypeService.create(dto)
        );
    }

    /**
     * Verifies that a leave type is retrieved successfully by ID.
     */
    @Test
    void getById_success() {

        LeaveType leaveType =
                createLeaveType(
                        "Sick Leave-" + System.nanoTime(),
                        6
                );

        LeaveTypeResponseDto result =
                leaveTypeService.getById(
                        leaveType.getId()
                );

        assertNotNull(result);
        assertEquals(
                leaveType.getId(),
                result.getId()
        );
        assertEquals(
                leaveType.getName(),
                result.getName()
        );
        assertEquals(
                leaveType.getMaxDays(),
                result.getMaxDays()
        );
    }

    /**
     * Verifies that requesting a non-existent leave type throws
     * LeaveTypeNotFoundException.
     */
    @Test
    void getById_notFound_throwsLeaveTypeNotFound() {

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.getById(999999L)
        );
    }

    /**
     * Verifies that a soft-deleted leave type cannot be retrieved.
     */
    @Test
    void getById_softDeleted_throwsLeaveTypeNotFound() {

        LeaveType leaveType =
                createLeaveType(
                        "Deleted Leave-" + System.nanoTime(),
                        5
                );

        leaveType.setDeleted(true);
        leaveTypeRepository.save(leaveType);

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.getById(
                        leaveType.getId()
                )
        );
    }

    /**
     * Verifies that active leave types are returned with pagination.
     */
    @Test
    void getAll_success() {

        LeaveType leaveType =
                createLeaveType(
                        "Annual Leave-" + System.nanoTime(),
                        12
                );

        Pageable pageable =
                PageRequest.of(0, 10);

        PageResponseDto<LeaveTypeResponseDto> result =
                leaveTypeService.getAll(pageable);

        assertNotNull(result);

        assertTrue(
                result.getTotalElements() >= 1
        );

        assertTrue(
                result.getContent()
                        .stream()
                        .anyMatch(dto ->
                                dto.getId()
                                        .equals(leaveType.getId())
                        )
        );
    }

    /**
     * Verifies that an existing leave type is updated successfully.
     */
    @Test
    void update_success() {

        LeaveType leaveType =
                createLeaveType(
                        "Old Leave-" + System.nanoTime(),
                        5
                );

        LeaveTypeRequestDto dto =
                new LeaveTypeRequestDto();

        dto.setName(
                "Updated Leave-" + System.nanoTime()
        );
        dto.setMaxDays(12);

        LeaveTypeResponseDto result =
                leaveTypeService.update(
                        leaveType.getId(),
                        dto
                );

        assertNotNull(result);

        assertEquals(
                dto.getName(),
                result.getName()
        );

        assertEquals(
                dto.getMaxDays(),
                result.getMaxDays()
        );

        LeaveType updated =
                leaveTypeRepository
                        .findById(leaveType.getId())
                        .orElseThrow();

        assertEquals(
                dto.getName(),
                updated.getName()
        );

        assertEquals(
                dto.getMaxDays(),
                updated.getMaxDays()
        );
    }

    /**
     * Verifies that updating a non-existent leave type throws
     * LeaveTypeNotFoundException.
     */
    @Test
    void update_notFound_throwsLeaveTypeNotFound() {

        LeaveTypeRequestDto dto =
                new LeaveTypeRequestDto();

        dto.setName("Updated Leave");
        dto.setMaxDays(10);

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.update(
                        999999L,
                        dto
                )
        );
    }

    /**
     * Verifies that duplicate names are rejected during update.
     */
    @Test
    void update_duplicateName_throwsDuplicateLeaveType() {

        LeaveType first =
                createLeaveType(
                        "First Leave-" + System.nanoTime(),
                        5
                );

        LeaveType second =
                createLeaveType(
                        "Second Leave-" + System.nanoTime(),
                        10
                );

        LeaveTypeRequestDto dto =
                new LeaveTypeRequestDto();

        dto.setName(first.getName());
        dto.setMaxDays(15);

        assertThrows(
                DuplicateLeaveTypeException.class,
                () -> leaveTypeService.update(
                        second.getId(),
                        dto
                )
        );
    }

    /**
     * Verifies that a leave type is soft deleted successfully.
     */
    @Test
    void delete_success() {

        LeaveType leaveType =
                createLeaveType(
                        "Delete Leave-" + System.nanoTime(),
                        5
                );

        leaveTypeService.delete(
                leaveType.getId()
        );

        LeaveType deleted =
                leaveTypeRepository
                        .findById(leaveType.getId())
                        .orElseThrow();

        assertTrue(deleted.isDeleted());
        assertTrue(!deleted.isStatus());
    }

    /**
     * Verifies that deleting a non-existent leave type throws
     * LeaveTypeNotFoundException.
     */
    @Test
    void delete_notFound_throwsLeaveTypeNotFound() {

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.delete(999999L)
        );
    }

    /**
     * Verifies that an already soft-deleted leave type cannot be deleted again.
     */
    @Test
    void delete_softDeleted_throwsLeaveTypeNotFound() {

        LeaveType leaveType =
                createLeaveType(
                        "Already Deleted-" + System.nanoTime(),
                        5
                );

        leaveType.setDeleted(true);
        leaveTypeRepository.save(leaveType);

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveTypeService.delete(
                        leaveType.getId()
                )
        );
    }

    /**
     * Creates and persists a leave type.
     *
     * @param name leave type name
     * @param maxDays maximum number of leave days
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
}
