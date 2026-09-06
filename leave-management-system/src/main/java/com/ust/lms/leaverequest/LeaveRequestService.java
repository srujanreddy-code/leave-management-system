package com.ust.lms.leaverequest;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import org.springframework.data.domain.Pageable;

public interface LeaveRequestService {
    LeaveRequestResponseDto apply(LeaveRequestRequestDto dto);
    LeaveRequestResponseDto getById(Long id);
    PageResponseDto<LeaveRequestResponseDto> getAll(Pageable pageable);
    PageResponseDto<LeaveRequestResponseDto> getByEmployeeId(Long employeeId, Pageable pageable);
    PageResponseDto<LeaveRequestResponseDto> getByStatus(LeaveStatus status, Pageable pageable);
    PageResponseDto<LeaveRequestResponseDto> getByDepartmentId(Long departmentId, Pageable pageable);
    LeaveRequestResponseDto update(Long id, LeaveRequestRequestDto dto);
    void cancel(Long id);
    LeaveRequestResponseDto approve(Long id, Long approverId);
    LeaveRequestResponseDto reject(Long id, Long approverId);
}