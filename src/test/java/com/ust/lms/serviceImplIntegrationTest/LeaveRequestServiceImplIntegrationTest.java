package com.ust.lms.serviceImplIntegrationTest;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.EmployeeNotFoundException;
import com.ust.lms.common.exception.InvalidLeaveStatusTransitionException;
import com.ust.lms.common.exception.LeaveRequestNotFoundException;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.leaverequest.impl.LeaveRequestServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for LeaveRequestServiceImpl.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveRequestServiceImplIntegrationTest {

    @Autowired
    private LeaveRequestServiceImpl leaveRequestService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User employeeUser;
    private User managerUser;
    private Employee employee;
    private LeaveType leaveType;
    private EmployeeLeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {

        employeeUser = new User();
        employeeUser.setName("Employee");
        employeeUser.setEmail("employee" + System.nanoTime() + "@example.com");
        employeeUser.setPassword("password");
        employeeUser.setRole(Role.EMPLOYEE);
        employeeUser.setDeleted(false);
        employeeUser.setStatus(true);
        employeeUser = userRepository.save(employeeUser);

        managerUser = new User();
        managerUser.setName("Manager");
        managerUser.setEmail("manager" + System.nanoTime() + "@example.com");
        managerUser.setPassword("password");
        managerUser.setRole(Role.MANAGER);
        managerUser.setDeleted(false);
        managerUser.setStatus(true);
        managerUser = userRepository.save(managerUser);

        Department department = new Department();
        department.setName("Engineering" + System.nanoTime());
        department.setDescription("Engineering Department");
        department.setDeleted(false);
        department.setStatus(true);
        department = departmentRepository.save(department);

        employee = new Employee();
        employee.setUser(employeeUser);
        employee.setDepartment(department);
        employee.setJoiningDate(LocalDate.now());
        employee.setLeaveBalance(10);
        employee.setDeleted(false);
        employee.setStatus(true);
        employee = employeeRepository.save(employee);

        leaveType = new LeaveType();
        leaveType.setName("Earned Leave" + System.nanoTime());
        leaveType.setMaxDays(8);
        leaveType.setDeleted(false);
        leaveType.setStatus(true);
        leaveType = leaveTypeRepository.save(leaveType);

        leaveBalance = new EmployeeLeaveBalance();
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setBalance(8);
        leaveBalance.setLeaveYearStart(employee.getJoiningDate());
        leaveBalance.setLeaveYearEnd(
                employee.getJoiningDate().plusYears(1).minusDays(1));
        leaveBalance.setDeleted(false);
        leaveBalance.setStatus(true);
        leaveBalance = employeeLeaveBalanceRepository.save(leaveBalance);
    }

    /**
     * Tests successful creation of a leave request.
     */
    @Test
    void apply_success() {

        LeaveRequestRequestDto dto = createRequestDto();

        LeaveRequestResponseDto response =
                leaveRequestService.apply(dto);

        assertEquals(employee.getId(), response.getEmployeeId());
        assertEquals(leaveType.getId(), response.getLeaveTypeId());
        assertEquals(
                LeaveStatus.PENDING,
                leaveRequestRepository.findById(response.getId())
                        .orElseThrow()
                        .getLeaveStatus()
        );
    }

    /**
     * Tests that applying leave for a nonexistent employee fails.
     */
    @Test
    void apply_employeeNotFound_throwsEmployeeNotFound() {

        LeaveRequestRequestDto dto = createRequestDto();
        dto.setEmployeeId(999999L);

        assertThrows(
                EmployeeNotFoundException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests successful retrieval of a leave request by ID.
     */
    @Test
    void getById_success() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        LeaveRequestResponseDto result =
                leaveRequestService.getById(created.getId());

        assertEquals(created.getId(), result.getId());
        assertEquals(employee.getId(), result.getEmployeeId());
    }

    /**
     * Tests that retrieving a nonexistent leave request fails.
     */
    @Test
    void getById_notFound_throwsLeaveRequestNotFound() {

        assertThrows(
                LeaveRequestNotFoundException.class,
                () -> leaveRequestService.getById(999999L)
        );
    }

    /**
     * Tests successful retrieval of all leave requests.
     */
    @Test
    void getAll_success() {

        leaveRequestService.apply(createRequestDto());

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<LeaveRequestResponseDto> result =
                leaveRequestService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * Tests successful cancellation of a pending leave request.
     */
    @Test
    void cancel_success() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        leaveRequestService.cancel(created.getId());

        LeaveRequest cancelled =
                leaveRequestRepository.findById(created.getId())
                        .orElseThrow();

        assertEquals(true, cancelled.isDeleted());
    }

    /**
     * Tests successful update of a pending leave request.
     */
    @Test
    void update_success() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        LeaveRequestRequestDto updateDto = createRequestDto();
        updateDto.setStartDate(LocalDate.now().plusDays(10));
        updateDto.setEndDate(LocalDate.now().plusDays(11));
        updateDto.setReason("Updated reason");

        LeaveRequestResponseDto result =
                leaveRequestService.update(created.getId(), updateDto);

        assertEquals(updateDto.getStartDate(), result.getStartDate());
        assertEquals(updateDto.getEndDate(), result.getEndDate());
        assertEquals("Updated reason", result.getReason());
    }

    /**
     * Tests successful approval of a leave request.
     */
    @Test
    void approve_success() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        LeaveRequestResponseDto result =
                leaveRequestService.approve(
                        created.getId(),
                        managerUser.getId()
                );

        assertEquals(LeaveStatus.APPROVED, result.getLeaveStatus());

        EmployeeLeaveBalance updatedBalance =
                employeeLeaveBalanceRepository
                        .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                                employee.getId(),
                                leaveType.getId(),
                                employee.getJoiningDate())
                        .orElseThrow();

        assertEquals(6, updatedBalance.getBalance());
    }

    /**
     * Tests successful rejection of a leave request.
     */
    @Test
    void reject_success() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        LeaveRequestResponseDto result =
                leaveRequestService.reject(
                        created.getId(),
                        managerUser.getId()
                );

        assertEquals(LeaveStatus.REJECTED, result.getLeaveStatus());
    }

    /**
     * Tests that an approved leave request cannot be updated.
     */
    @Test
    void update_approvedLeave_throwsInvalidLeaveStatusTransition() {

        LeaveRequestResponseDto created =
                leaveRequestService.apply(createRequestDto());

        leaveRequestService.approve(
                created.getId(),
                managerUser.getId()
        );

        LeaveRequestRequestDto updateDto = createRequestDto();

        assertThrows(
                InvalidLeaveStatusTransitionException.class,
                () -> leaveRequestService.update(
                        created.getId(), updateDto)
        );
    }

    /**
     * Creates a valid leave request DTO.
     *
     * @return leave request DTO
     */
    private LeaveRequestRequestDto createRequestDto() {

        LeaveRequestRequestDto dto = new LeaveRequestRequestDto();

        dto.setEmployeeId(employee.getId());
        dto.setLeaveTypeId(leaveType.getId());
        dto.setStartDate(LocalDate.now().plusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(3));
        dto.setReason("Personal leave");

        return dto;
    }
}