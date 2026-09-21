package com.ust.lms.serviceImplTest;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.EmployeeNotFoundException;
import com.ust.lms.common.exception.InsufficientLeaveBalanceException;
import com.ust.lms.common.exception.InvalidLeaveDatesException;
import com.ust.lms.common.exception.InvalidLeaveStatusTransitionException;
import com.ust.lms.common.exception.LeaveBalanceNotFoundException;
import com.ust.lms.common.exception.LeaveExceedsMaximumException;
import com.ust.lms.common.exception.LeaveRequestNotFoundException;
import com.ust.lms.common.exception.LeaveTypeNotFoundException;
import com.ust.lms.common.exception.ManagerNotFoundException;
import com.ust.lms.common.exception.OverlappingLeaveException;
import com.ust.lms.common.exception.PastDateLeaveException;
import com.ust.lms.common.exception.UnauthorizedLeaveActionException;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.leaverequest.impl.LeaveRequestServiceImpl;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LeaveRequestServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private LeaveRequestServiceImpl leaveRequestService;

    private User employeeUser;
    private User managerUser;
    private Employee employee;
    private LeaveType leaveType;
    private EmployeeLeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {

        employeeUser = new User();
        employeeUser.setId(1L);
        employeeUser.setName("Employee");
        employeeUser.setEmail("employee@example.com");
        employeeUser.setRole(Role.EMPLOYEE);

        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setName("Manager");
        managerUser.setEmail("manager@example.com");
        managerUser.setRole(Role.MANAGER);

        employee = new Employee();
        employee.setId(10L);
        employee.setUser(employeeUser);
        employee.setJoiningDate(LocalDate.of(2026, 1, 1));
        employee.setLeaveBalance(10);

        leaveType = new LeaveType();
        leaveType.setId(20L);
        leaveType.setName("Earned Leave");
        leaveType.setMaxDays(8);

        leaveBalance = new EmployeeLeaveBalance();
        leaveBalance.setId(30L);
        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveType(leaveType);
        leaveBalance.setBalance(8);
        leaveBalance.setLeaveYearStart(employee.getJoiningDate());
        leaveBalance.setLeaveYearEnd(employee.getJoiningDate().plusYears(1).minusDays(1));
    }

    /**
     * Tests successful leave application.
     */
    @Test
    void apply_success() {

        LeaveRequestRequestDto dto = createRequestDto();

        LeaveRequest saved = createLeaveRequest();

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.findOverlapping(
                10L, dto.getStartDate(), dto.getEndDate()))
                .thenReturn(List.of());
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenReturn(saved);

        LeaveRequestResponseDto result =
                leaveRequestService.apply(dto);

        assertEquals(10L, result.getEmployeeId());
        assertEquals(20L, result.getLeaveTypeId());
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    /**
     * Tests that applying leave for a nonexistent employee throws an exception.
     */
    @Test
    void apply_employeeNotFound_throwsEmployeeNotFound() {

        LeaveRequestRequestDto dto = createRequestDto();

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that applying leave for a nonexistent leave type throws an exception.
     */
    @Test
    void apply_leaveTypeNotFound_throwsLeaveTypeNotFound() {

        LeaveRequestRequestDto dto = createRequestDto();

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveTypeNotFoundException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that a past leave date is rejected.
     */
    @Test
    void apply_pastDate_throwsPastDateLeave() {

        LeaveRequestRequestDto dto = createRequestDto();
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now());

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                PastDateLeaveException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that an invalid date range is rejected.
     */
    @Test
    void apply_invalidDates_throwsInvalidLeaveDates() {

        LeaveRequestRequestDto dto = createRequestDto();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(2));

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));

        assertThrows(
                InvalidLeaveDatesException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that a missing leave balance throws an exception.
     */
    @Test
    void apply_leaveBalanceNotFound_throwsLeaveBalanceNotFound() {

        LeaveRequestRequestDto dto = createRequestDto();

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveBalanceNotFoundException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that insufficient leave balance is rejected.
     */
    @Test
    void apply_insufficientBalance_throwsInsufficientLeaveBalance() {

        LeaveRequestRequestDto dto = createRequestDto();
        dto.setEndDate(dto.getStartDate().plusDays(8));

        leaveBalance.setBalance(2);

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));

        assertThrows(
                InsufficientLeaveBalanceException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that requesting more than the leave type maximum is rejected.
     */
    @Test
    void apply_exceedsMaximum_throwsLeaveExceedsMaximum() {

        LeaveRequestRequestDto dto = createRequestDto();
        dto.setEndDate(dto.getStartDate().plusDays(8));

        leaveBalance.setBalance(20);
        leaveType.setMaxDays(5);

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));

        assertThrows(
                LeaveExceedsMaximumException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests that overlapping leave requests are rejected.
     */
    @Test
    void apply_overlappingLeave_throwsOverlappingLeave() {

        LeaveRequestRequestDto dto = createRequestDto();

        when(employeeRepository.findById(10L))
                .thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.findOverlapping(
                10L, dto.getStartDate(), dto.getEndDate()))
                .thenReturn(List.of(createLeaveRequest()));

        assertThrows(
                OverlappingLeaveException.class,
                () -> leaveRequestService.apply(dto)
        );
    }

    /**
     * Tests successful retrieval of a leave request by ID.
     */
    @Test
    void getById_success() {

        LeaveRequest leaveRequest = createLeaveRequest();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));

        LeaveRequestResponseDto result =
                leaveRequestService.getById(1L);

        assertEquals(10L, result.getEmployeeId());
        assertEquals(20L, result.getLeaveTypeId());
    }

    /**
     * Tests that retrieving a nonexistent leave request throws an exception.
     */
    @Test
    void getById_notFound_throwsLeaveRequestNotFound() {

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveRequestNotFoundException.class,
                () -> leaveRequestService.getById(1L)
        );
    }

    /**
     * Tests successful retrieval of all leave requests.
     */
    @Test
    void getAll_success() {

        Pageable pageable = PageRequest.of(0, 10);

        LeaveRequest leaveRequest = createLeaveRequest();

        Page<LeaveRequest> page =
                new PageImpl<>(List.of(leaveRequest), pageable, 1);

        when(leaveRequestRepository.findByDeletedFalse(pageable))
                .thenReturn(page);

        PageResponseDto<LeaveRequestResponseDto> result =
                leaveRequestService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    /**
     * Tests successful retrieval of leave requests by employee.
     */
    @Test
    void getByEmployeeId_success() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaveRequest> page =
                new PageImpl<>(
                        List.of(createLeaveRequest()),
                        pageable,
                        1
                );

        when(leaveRequestRepository
                .findByEmployeeIdAndDeletedFalse(10L, pageable))
                .thenReturn(page);

        PageResponseDto<LeaveRequestResponseDto> result =
                leaveRequestService.getByEmployeeId(10L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * Tests successful retrieval of leave requests by status.
     */
    @Test
    void getByStatus_success() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaveRequest> page =
                new PageImpl<>(
                        List.of(createLeaveRequest()),
                        pageable,
                        1
                );

        when(leaveRequestRepository
                .findByLeaveStatusAndDeletedFalse(
                        LeaveStatus.PENDING, pageable))
                .thenReturn(page);

        PageResponseDto<LeaveRequestResponseDto> result =
                leaveRequestService.getByStatus(
                        LeaveStatus.PENDING, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * Tests successful retrieval of leave requests by department.
     */
    @Test
    void getByDepartmentId_success() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<LeaveRequest> page =
                new PageImpl<>(
                        List.of(createLeaveRequest()),
                        pageable,
                        1
                );

        when(leaveRequestRepository
                .findByEmployeeDepartmentIdAndDeletedFalse(
                        5L, pageable))
                .thenReturn(page);

        PageResponseDto<LeaveRequestResponseDto> result =
                leaveRequestService.getByDepartmentId(5L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * Tests successful cancellation of a pending leave request.
     */
    @Test
    void cancel_pending_success() {

        LeaveRequest leaveRequest = createLeaveRequest();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenReturn(leaveRequest);

        leaveRequestService.cancel(1L);

        assertTrue(leaveRequest.isDeleted());
        verify(leaveRequestRepository).save(leaveRequest);
    }

    /**
     * Tests successful approval of a pending leave request.
     */
    @Test
    void approve_success() {

        LeaveRequest leaveRequest = createLeaveRequest();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(managerUser));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponseDto result =
                leaveRequestService.approve(1L, 2L);

        assertEquals(LeaveStatus.APPROVED, leaveRequest.getLeaveStatus());
        assertEquals(managerUser, leaveRequest.getApprovedBy());
        assertEquals(6, leaveBalance.getBalance());
        assertEquals(8, employee.getLeaveBalance());
        assertEquals(10L, result.getEmployeeId());
    }

    /**
     * Tests that approval by a regular employee is rejected.
     */
    @Test
    void approve_employee_throwsUnauthorizedLeaveAction() {

        LeaveRequest leaveRequest = createLeaveRequest();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(employeeUser));

        assertThrows(
                UnauthorizedLeaveActionException.class,
                () -> leaveRequestService.approve(1L, 1L)
        );
    }

    /**
     * Tests that a nonexistent approver throws an exception.
     */
    @Test
    void approve_managerNotFound_throwsManagerNotFound() {

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(createLeaveRequest()));
        when(userRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                ManagerNotFoundException.class,
                () -> leaveRequestService.approve(1L, 2L)
        );
    }

    /**
     * Tests that approving an already processed request is rejected.
     */
    @Test
    void approve_nonPending_throwsInvalidLeaveStatusTransition() {

        LeaveRequest leaveRequest = createLeaveRequest();
        leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(managerUser));

        assertThrows(
                InvalidLeaveStatusTransitionException.class,
                () -> leaveRequestService.approve(1L, 2L)
        );
    }

    /**
     * Tests successful rejection of a pending leave request.
     */
    @Test
    void reject_success() {

        LeaveRequest leaveRequest = createLeaveRequest();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(managerUser));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponseDto result =
                leaveRequestService.reject(1L, 2L);

        assertEquals(LeaveStatus.REJECTED, leaveRequest.getLeaveStatus());
        assertEquals(managerUser, leaveRequest.getApprovedBy());
        assertEquals(10, employee.getLeaveBalance());
        assertEquals(10L, result.getEmployeeId());
    }

    /**
     * Tests successful update of a pending leave request.
     */
    @Test
    void update_success() {

        LeaveRequest leaveRequest = createLeaveRequest();
        LeaveRequestRequestDto dto = createRequestDto();

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));
        when(leaveTypeRepository.findById(20L))
                .thenReturn(Optional.of(leaveType));
        when(employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        10L, 20L, employee.getJoiningDate()))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.findOverlappingExcludingId(
                10L, 1L, dto.getStartDate(), dto.getEndDate()))
                .thenReturn(List.of());
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponseDto result =
                leaveRequestService.update(1L, dto);

        assertEquals(dto.getStartDate(), leaveRequest.getStartDate());
        assertEquals(dto.getEndDate(), leaveRequest.getEndDate());
        assertEquals(10L, result.getEmployeeId());
    }

    /**
     * Tests that a non-pending leave request cannot be updated.
     */
    @Test
    void update_nonPending_throwsInvalidLeaveStatusTransition() {

        LeaveRequest leaveRequest = createLeaveRequest();
        leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(1L))
                .thenReturn(Optional.of(leaveRequest));

        assertThrows(
                InvalidLeaveStatusTransitionException.class,
                () -> leaveRequestService.update(
                        1L, createRequestDto())
        );
    }

    /**
     * Creates a valid leave request DTO for testing.
     *
     * @return leave request DTO
     */
    private LeaveRequestRequestDto createRequestDto() {

        LeaveRequestRequestDto dto = new LeaveRequestRequestDto();

        dto.setEmployeeId(10L);
        dto.setLeaveTypeId(20L);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(6));
        dto.setReason("Personal leave");

        return dto;
    }

    /**
     * Creates a leave request entity for testing.
     *
     * @return leave request entity
     */
    private LeaveRequest createLeaveRequest() {

        LeaveRequest leaveRequest = new LeaveRequest();

        leaveRequest.setId(1L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(5));
        leaveRequest.setEndDate(LocalDate.now().plusDays(6));
        leaveRequest.setReason("Personal leave");
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
        leaveRequest.setDeleted(false);

        return leaveRequest;
    }
}