package com.ust.lms.leaverequest.impl;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.Role;
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
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import com.ust.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private LeaveRequestServiceImpl leaveRequestService;

    private Employee employee;
    private LeaveType leaveType;
    private User managerUser;
    private LeaveRequestRequestDto validDto;

    @BeforeEach
    void setUp() {
        leaveRequestService = new LeaveRequestServiceImpl(
                leaveRequestRepository, employeeRepository, leaveTypeRepository, userRepository, new ModelMapper());

        User employeeUser = new User();
        employeeUser.setId(10L);
        employeeUser.setName("Employee One");
        employeeUser.setRole(Role.EMPLOYEE);

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        employee = new Employee();
        employee.setId(1L);
        employee.setUser(employeeUser);
        employee.setDepartment(department);
        employee.setLeaveBalance(10);

        leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setName("Sick Leave");
        leaveType.setMaxDays(10);

        managerUser = new User();
        managerUser.setId(20L);
        managerUser.setName("Manager One");
        managerUser.setRole(Role.MANAGER);

        validDto = new LeaveRequestRequestDto();
        validDto.setEmployeeId(1L);
        validDto.setLeaveTypeId(1L);
        validDto.setStartDate(LocalDate.now().plusDays(5));
        validDto.setEndDate(LocalDate.now().plusDays(7));
        validDto.setReason("Personal work");
    }

    @Test
    void apply_withValidRequest_savesAsPending() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveRequestRepository.findOverlapping(anyLong(), any(), any())).thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponseDto result = leaveRequestService.apply(validDto);

        assertEquals(LeaveStatus.PENDING, result.getLeaveStatus());
        assertEquals("Employee One", result.getEmployeeName());
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    @Test
    void apply_withPastStartDate_throwsBadRequest() {
        validDto.setStartDate(LocalDate.now().minusDays(1));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        assertThrows(BadRequestException.class, () -> leaveRequestService.apply(validDto));
    }

    @Test
    void apply_withStartAfterEnd_throwsBadRequest() {
        validDto.setStartDate(LocalDate.now().plusDays(10));
        validDto.setEndDate(LocalDate.now().plusDays(5));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        assertThrows(BadRequestException.class, () -> leaveRequestService.apply(validDto));
    }

    @Test
    void apply_exceedingLeaveBalance_throwsBadRequest() {
        employee.setLeaveBalance(1);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));

        assertThrows(BadRequestException.class, () -> leaveRequestService.apply(validDto));
    }

    @Test
    void apply_withOverlappingRequest_throwsBadRequest() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        when(leaveRequestRepository.findOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of(mock(LeaveRequest.class)));

        assertThrows(BadRequestException.class, () -> leaveRequestService.apply(validDto));
    }

    @Test
    void apply_withUnknownEmployee_throwsResourceNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leaveRequestService.apply(validDto));
    }

    @Test
    void approve_byManager_deductsBalanceAndSetsApproved() {
        LeaveRequest pending = buildPendingLeaveRequest();
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(20L)).thenReturn(Optional.of(managerUser));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequestResponseDto result = leaveRequestService.approve(1L, 20L);

        assertEquals(LeaveStatus.APPROVED, result.getLeaveStatus());
        assertEquals(7, employee.getLeaveBalance());
        verify(employeeRepository).save(employee);
    }

    @Test
    void approve_byEmployee_throwsForbidden() {
        LeaveRequest pending = buildPendingLeaveRequest();
        User employeeRoleUser = new User();
        employeeRoleUser.setId(30L);
        employeeRoleUser.setRole(Role.EMPLOYEE);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(30L)).thenReturn(Optional.of(employeeRoleUser));

        assertThrows(ForbiddenException.class, () -> leaveRequestService.approve(1L, 30L));
    }

    @Test
    void approve_alreadyApproved_throwsBadRequest() {
        LeaveRequest approved = buildPendingLeaveRequest();
        approved.setLeaveStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(approved));
        when(userRepository.findById(20L)).thenReturn(Optional.of(managerUser));

        assertThrows(BadRequestException.class, () -> leaveRequestService.approve(1L, 20L));
    }

    @Test
    void cancel_approvedRequest_restoresBalance() {
        LeaveRequest approved = buildPendingLeaveRequest();
        approved.setLeaveStatus(LeaveStatus.APPROVED);
        employee.setLeaveBalance(7);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(approved));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.cancel(1L);

        assertEquals(10, employee.getLeaveBalance());
        assertEquals(true, approved.isDeleted());
        verify(employeeRepository).save(employee);
    }

    @Test
    void cancel_pendingRequest_doesNotChangeBalance() {
        LeaveRequest pending = buildPendingLeaveRequest();
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        leaveRequestService.cancel(1L);

        assertEquals(10, employee.getLeaveBalance());
    }

    private LeaveRequest buildPendingLeaveRequest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(LocalDate.now().plusDays(5));
        leaveRequest.setEndDate(LocalDate.now().plusDays(7));
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
        return leaveRequest;
    }

    @Test
    void approve_byRequestOwnerWhoIsAlsoManager_throwsForbidden() {
        User managerWhoOwnsRequest = new User();
        managerWhoOwnsRequest.setId(10L);
        managerWhoOwnsRequest.setRole(Role.MANAGER);
        employee.getUser().setRole(Role.MANAGER);
        employee.getUser().setId(10L);

        LeaveRequest pending = buildPendingLeaveRequest();
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee.getUser()));

        assertThrows(ForbiddenException.class, () -> leaveRequestService.approve(1L, 10L));
    }

    @Test
    void reject_byRequestOwnerWhoIsAlsoManager_throwsForbidden() {
        employee.getUser().setRole(Role.MANAGER);
        employee.getUser().setId(10L);

        LeaveRequest pending = buildPendingLeaveRequest();
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee.getUser()));

        assertThrows(ForbiddenException.class, () -> leaveRequestService.reject(1L, 10L));
    }
}