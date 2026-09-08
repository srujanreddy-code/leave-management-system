package com.ust.lms.leaverequest.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ForbiddenException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.leaverequest.LeaveRequestService;
import com.ust.lms.model.Employee;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import com.ust.lms.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveRequestServiceImpl extends CommonService implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository,
                                   EmployeeRepository employeeRepository,
                                   LeaveTypeRepository leaveTypeRepository,
                                   UserRepository userRepository,
                                   ModelMapper modelMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto apply(LeaveRequestRequestDto dto) {

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply for leave in the past");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        long requestedDays = ChronoUnit.DAYS.between(
                dto.getStartDate(),
                dto.getEndDate()
        ) + 1;

        if (requestedDays > employee.getLeaveBalance()) {
            throw new BadRequestException("Insufficient leave balance");
        }

        if (requestedDays > leaveType.getMaxDays()) {
            throw new BadRequestException(
                    "Requested days exceed the maximum allowed for " + leaveType.getName()
            );
        }

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                employee.getId(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Overlapping leave request exists");
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

        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto update(Long id, LeaveRequestRequestDto dto) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be edited");
        }

        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot set leave to a past date");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        long requestedDays = ChronoUnit.DAYS.between(
                dto.getStartDate(),
                dto.getEndDate()
        ) + 1;

        if (requestedDays > leaveRequest.getEmployee().getLeaveBalance()) {
            throw new BadRequestException("Insufficient leave balance");
        }

        if (requestedDays > leaveType.getMaxDays()) {
            throw new BadRequestException(
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
            throw new BadRequestException("Overlapping leave request exists");
        }

        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setReason(dto.getReason());

        setAuditFields(leaveRequest, false);

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);

        return toResponseDto(updated);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (leaveRequest.getLeaveStatus() == LeaveStatus.APPROVED) {
            long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
            Employee employee = leaveRequest.getEmployee();
            employee.setLeaveBalance(employee.getLeaveBalance() + (int) days);
            employeeRepository.save(employee);
        }

        softDelete(leaveRequest);
        setAuditFields(leaveRequest, false);
        leaveRequestRepository.save(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto approve(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (approver.getRole() != Role.MANAGER && approver.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only a manager or admin can approve leave requests");
        }
        if (leaveRequest.getEmployee().getUser().getId().equals(approverId)) {
            throw new ForbiddenException("You cannot approve your own leave request");
        }
        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be approved");
        }

        long days = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        Employee employee = leaveRequest.getEmployee();
        employee.setLeaveBalance(employee.getLeaveBalance() - (int) days);
        employeeRepository.save(employee);

        leaveRequest.setLeaveStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        setAuditFields(leaveRequest, false);
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return toResponseDto(updated);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto reject(Long id, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        if (approver.getRole() != Role.MANAGER && approver.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only a manager or admin can reject leave requests");
        }
        if (leaveRequest.getEmployee().getUser().getId().equals(approverId)) {
            throw new ForbiddenException("You cannot reject your own leave request");
        }
        if (leaveRequest.getLeaveStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be rejected");
        }

        leaveRequest.setLeaveStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        setAuditFields(leaveRequest, false);
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        return toResponseDto(updated);
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto getById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .filter(lr -> !lr.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        return toResponseDto(leaveRequest);
    }

    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getAll(Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByDeletedFalse(pageable);
        return toPageResponseDto(result);
    }

    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByEmployeeId(Long employeeId, Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByEmployeeIdAndDeletedFalse(employeeId, pageable);
        return toPageResponseDto(result);
    }

    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByStatus(LeaveStatus status, Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByLeaveStatusAndDeletedFalse(status, pageable);
        return toPageResponseDto(result);
    }

    @Override
    @Transactional
    public PageResponseDto<LeaveRequestResponseDto> getByDepartmentId(Long departmentId, Pageable pageable) {
        Page<LeaveRequest> result = leaveRequestRepository.findByEmployeeDepartmentIdAndDeletedFalse(departmentId, pageable);
        return toPageResponseDto(result);
    }

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