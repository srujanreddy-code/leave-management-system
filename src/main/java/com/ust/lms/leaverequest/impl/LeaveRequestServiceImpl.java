package com.ust.lms.leaverequest.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.*;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.leaverequest.LeaveRequestService;
import com.ust.lms.model.Employee;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ust.lms.model.EmployeeLeaveBalance;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service implementation for managing leave requests, including applying,
 * updating, approving, rejecting, cancelling, and retrieving leave requests.
 */
@Service
@Slf4j
public class LeaveRequestServiceImpl extends CommonService implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository,
                                   LeaveTypeRepository leaveTypeRepository,
                                   UserRepository userRepository,
                                   ModelMapper modelMapper, EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.employeeLeaveBalanceRepository = employeeLeaveBalanceRepository;
    }

    /**
     * Creates a new leave request after validating dates, leave balance,
     * maximum allowed days, and overlapping requests.
     *
     * @param dto leave request details
     * @return response containing the created leave request
     * @throws EmployeeNotFoundException if the employee does not exist
     * @throws LeaveTypeNotFoundException if the leave type does not exist
     * @throws PastDateLeaveException if the leave starts in the past
     * @throws InvalidLeaveDatesException if the start date is after the end date
     * @throws LeaveBalanceNotFoundException if the leave balance does not exist
     * @throws InsufficientLeaveBalanceException if the employee has insufficient leave balance
     * @throws LeaveExceedsMaximumException if the requested days exceed the maximum allowed
     * @throws OverlappingLeaveException if another leave request overlaps the requested dates
     */
    @Override
    @Transactional
    public LeaveRequestResponseDto apply(LeaveRequestRequestDto dto) {

        log.info("Applying leave for employeeId: {}, leaveTypeId: {}",
                dto.getEmployeeId(), dto.getLeaveTypeId());

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new LeaveTypeNotFoundException("Leave type not found"));

        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new PastDateLeaveException("Cannot apply for leave in the past");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new InvalidLeaveDatesException("Start date cannot be after end date");
        }

        long requestedDays = ChronoUnit.DAYS.between(
                dto.getStartDate(),
                dto.getEndDate()
        ) + 1;

        EmployeeLeaveBalance leaveBalance = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        employee.getId(),
                        leaveType.getId(),
                        employee.getJoiningDate()
                )
                .orElseThrow(() -> new LeaveBalanceNotFoundException(
                        "Leave balance not found for this leave type"
                ));

        if (requestedDays > leaveBalance.getBalance()) {
            throw new InsufficientLeaveBalanceException(
                    "Insufficient " + leaveType.getName() + " balance"
            );
        }

        if (requestedDays > leaveType.getMaxDays()) {
            throw new LeaveExceedsMaximumException(
                    "Requested days exceed the maximum allowed for " + leaveType.getName()
            );
        }

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                employee.getId(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new OverlappingLeaveException("Overlapping leave request exists");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);

        setAuditFields(leaveRequest, true);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        log.info("Leave request created successfully with id: {}", saved.getId());

        return toResponseDto(saved);
    }

    /**
     * Updates an existing pending leave request after validating the new details.
     *
     * @param id leave request ID
     * @param dto updated leave request details
     * @return response containing the updated leave request
     * @throws LeaveRequestNotFoundException if the leave request does not exist
     * @throws InvalidLeaveStatusTransitionException if the leave request is not pending
     * @throws PastDateLeaveException if the leave starts in the past
     * @throws InvalidLeaveDatesException if the start date is after the end date
     * @throws LeaveTypeNotFoundException if the leave type does not exist
     * @throws LeaveBalanceNotFoundException if the leave balance does not exist
     * @throws InsufficientLeaveBalanceException if the employee has insufficient leave balance
     * @throws LeaveExceedsMaximumException if the requested days exceed the maximum allowed
     * @throws OverlappingLeaveException if another leave request overlaps the requested dates
     */
    @Override
    @Transactional
    public LeaveRequestResponseDto update(Long id, LeaveRequestRequestDto dto) {

        log.info("Updating leave request with id: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));

        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusTransitionException(
                    "Only pending leave requests can be edited"
            );
        }

        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new PastDateLeaveException("Cannot set leave to a past date");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new InvalidLeaveDatesException("Start date cannot be after end date");
        }

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new LeaveTypeNotFoundException("Leave type not found"));

        long requestedDays = ChronoUnit.DAYS.between(
                dto.getStartDate(),
                dto.getEndDate()
        ) + 1;

        Employee employee = leaveRequest.getEmployee();

        EmployeeLeaveBalance leaveBalance = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        employee.getId(),
                        leaveType.getId(),
                        employee.getJoiningDate()
                )
                .orElseThrow(() -> new LeaveBalanceNotFoundException(
                        "Leave balance not found for this leave type"
                ));

        if (requestedDays > leaveBalance.getBalance()) {
            throw new InsufficientLeaveBalanceException(
                    "Insufficient " + leaveType.getName() + " balance"
            );
        }

        if (requestedDays > leaveType.getMaxDays()) {
            throw new LeaveExceedsMaximumException(
                    "Requested days exceed the maximum allowed for " + leaveType.getName()
            );
        }

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingExcludingId(
                leaveRequest.getEmployee().getId(),
                id,
                dto.getStartDate(),
                dto.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new OverlappingLeaveException("Overlapping leave request exists");
        }

        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setReason(dto.getReason());

        setAuditFields(leaveRequest, false);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);

        log.info("Leave request updated successfully with id: {}", id);

        return toResponseDto(updated);
    }

    /**
     * Cancels an existing leave request using soft deletion.
     * Approved leave days are restored to the employee's leave balances.
     *
     * @param id leave request ID
     * @throws LeaveRequestNotFoundException if the leave request does not exist
     * @throws LeaveBalanceNotFoundException if the leave balance does not exist
     */
    @Override
    @Transactional
    public void cancel(Long id) {

        log.info("Cancelling leave request with id: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() ->new LeaveRequestNotFoundException("Leave request not found"));

        if (leaveRequest.getLeaveStatus() == LeaveStatus.APPROVED) {

            long days = ChronoUnit.DAYS.between(
                    leaveRequest.getStartDate(),
                    leaveRequest.getEndDate()
            ) + 1;

            Employee employee = leaveRequest.getEmployee();

            EmployeeLeaveBalance leaveBalance = employeeLeaveBalanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                            employee.getId(),
                            leaveRequest.getLeaveType().getId(),
                            employee.getJoiningDate()
                    )
                    .orElseThrow(() -> new LeaveBalanceNotFoundException(
                            "Leave balance not found for this leave type"
                    ));

            leaveBalance.setBalance(
                    leaveBalance.getBalance() + (int) days
            );

            employeeLeaveBalanceRepository.save(leaveBalance);

            employee.setLeaveBalance(
                    employee.getLeaveBalance() + (int) days
            );

            employeeRepository.save(employee);
        }

        softDelete(leaveRequest);
        setAuditFields(leaveRequest, false);
        leaveRequestRepository.save(leaveRequest);

        log.info("Leave request cancelled successfully with id: {}", id);
    }

    /**
     * Approves a pending leave request and deducts the requested days
     * from the employee's leave balances.
     *
     * @param id leave request ID
     * @param approverId ID of the user approving the request
     * @return response containing the approved leave request
     * @throws LeaveRequestNotFoundException if the leave request does not exist
     * @throws ManagerNotFoundException if the approver does not exist
     * @throws UnauthorizedLeaveActionException if the approver is not a manager or admin
     * @throws InvalidLeaveStatusTransitionException if the leave request is not pending
     * @throws LeaveBalanceNotFoundException if the leave balance does not exist
     * @throws InsufficientLeaveBalanceException if the employee has insufficient leave balance
     */
    @Override
    @Transactional
    public LeaveRequestResponseDto approve(Long id, Long approverId) {

        log.info("Approving leave request with id: {} by approverId: {}", id, approverId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ManagerNotFoundException("Approver not found"));

        if (approver.getRole() != Role.MANAGER && approver.getRole() != Role.ADMIN) {
            throw new UnauthorizedLeaveActionException(
                    "Only a manager or admin can approve leave requests"
            );
        }
        if (leaveRequest.getEmployee().getUser().getId().equals(approverId)) {
            throw new UnauthorizedLeaveActionException(
                    "You cannot approve your own leave request"
            );
        }
        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusTransitionException(
                    "Only pending leave requests can be approved"
            );
        }

        long days = ChronoUnit.DAYS.between(
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate()
        ) + 1;

        Employee employee = leaveRequest.getEmployee();

        EmployeeLeaveBalance leaveBalance = employeeLeaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
                        employee.getId(),
                        leaveRequest.getLeaveType().getId(),
                        employee.getJoiningDate()
                )
                .orElseThrow(() -> new LeaveBalanceNotFoundException(
                        "Leave balance not found for this leave type"
                ));

        if (leaveBalance.getBalance() < days) {
            throw new InsufficientLeaveBalanceException(
                    "Insufficient " + leaveRequest.getLeaveType().getName() + " balance"
            );
        }

        leaveBalance.setBalance(leaveBalance.getBalance() - (int) days);
        employeeLeaveBalanceRepository.save(leaveBalance);

        employee.setLeaveBalance(employee.getLeaveBalance() - (int) days);
        employeeRepository.save(employee);

        leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        setAuditFields(leaveRequest, false);
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);

        log.info("Leave request approved successfully with id: {}", id);

        return toResponseDto(updated);
    }

    /**
     * Rejects a pending leave request without deducting leave balance.
     *
     * @param id leave request ID
     * @param approverId ID of the user rejecting the request
     * @return response containing the rejected leave request
     * @throws LeaveRequestNotFoundException if the leave request does not exist
     * @throws ManagerNotFoundException if the approver does not exist
     * @throws UnauthorizedLeaveActionException if the approver is not a manager or admin
     * @throws InvalidLeaveStatusTransitionException if the leave request is not pending
     */
    @Override
    @Transactional
    public LeaveRequestResponseDto reject(Long id, Long approverId) {

        log.info("Rejecting leave request with id: {} by approverId: {}", id, approverId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ManagerNotFoundException("Approver not found"));

        if (approver.getRole() != Role.MANAGER && approver.getRole() != Role.ADMIN) {
            throw new UnauthorizedLeaveActionException(
                    "Only a manager or admin can reject leave requests"
            );
        }
        if (leaveRequest.getEmployee().getUser().getId().equals(approverId)) {
            throw new UnauthorizedLeaveActionException(
                    "You cannot reject your own leave request"
            );
        }
        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStatusTransitionException(
                    "Only pending leave requests can be rejected"
            );
        }

        leaveRequest.setLeaveStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        setAuditFields(leaveRequest, false);
        LeaveRequestResponseDto updated = toResponseDto(leaveRequestRepository.save(leaveRequest));

        log.info("Leave request rejected successfully with id: {}", id);

        return updated;
    }

    /**
     * Retrieves a leave request by its ID.
     *
     * @param id leave request ID
     * @return response containing the requested leave request
     * @throws LeaveRequestNotFoundException if the leave request does not exist or has been deleted
     */
    @Override
    @Transactional
    public LeaveRequestResponseDto getById(Long id) {

        log.info("Fetching leave request with id: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new LeaveRequestNotFoundException("Leave request not found"));
        return toResponseDto(leaveRequest);
    }

    /**
     * Retrieves all active leave requests using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing leave requests
     */
    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getAll(Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByDeletedFalse(pageable);
        return toPageResponseDto(result);
    }

    /**
     * Retrieves active leave requests for a specific employee using pagination.
     *
     * @param employeeId employee ID
     * @param pageable pagination and sorting information
     * @return paginated response containing the employee's leave requests
     */
    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByEmployeeId(Long employeeId, Pageable pageable) {

        log.info("Fetching leave requests for employeeId: {}", employeeId);

        Page<LeaveRequest> result = leaveRequestRepository.findByEmployeeIdAndDeletedFalse(employeeId, pageable);
        return toPageResponseDto(result);
    }

    /**
     * Retrieves active leave requests filtered by leave status.
     *
     * @param status leave status
     * @param pageable pagination and sorting information
     * @return paginated response containing matching leave requests
     */
    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByStatus(LeaveStatus status, Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByLeaveStatusAndDeletedFalse(status, pageable);
        return toPageResponseDto(result);
    }

    /**
     * Retrieves active leave requests for a specific department using pagination.
     *
     * @param departmentId department ID
     * @param pageable pagination and sorting information
     * @return paginated response containing leave requests in the department
     */
    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByDepartmentId(Long departmentId, Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByEmployeeDepartmentIdAndDeletedFalse(departmentId, pageable);
        return toPageResponseDto(result);
    }

    /**
     * Converts a leave request entity to its response DTO.
     *
     * @param leaveRequest leave request entity
     * @return mapped leave request response DTO
     */
    @Transactional
    private LeaveRequestResponseDto toResponseDto(LeaveRequest leaveRequest) {
        LeaveRequestResponseDto responseDto =
                modelMapper.map(leaveRequest, LeaveRequestResponseDto.class);

        responseDto.setEmployeeId(leaveRequest.getEmployee().getId());
        responseDto.setLeaveTypeId(leaveRequest.getLeaveType().getId());

        responseDto.setEmployeeName(
                leaveRequest.getEmployee().getUser().getName()
        );

        responseDto.setLeaveTypeName(
                leaveRequest.getLeaveType().getName()
        );

        if (leaveRequest.getApprovedBy() != null) {
            responseDto.setApprovedById(
                    leaveRequest.getApprovedBy().getId()
            );

            responseDto.setApprovedByName(
                    leaveRequest.getApprovedBy().getName()
            );
        }

        return responseDto;
    }

    /**
     * Converts a page of leave request entities to a paginated response DTO.
     *
     * @param pageResult page containing leave request entities
     * @return paginated leave request response DTO
     */
    @Transactional
    private PageResponseDto<LeaveRequestResponseDto> toPageResponseDto(Page<LeaveRequest> pageResult) {
        return new PageResponseDto<>(
                pageResult.getContent().stream().map(this::toResponseDto).toList(),
                pageResult.getNumber() + 1,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
}