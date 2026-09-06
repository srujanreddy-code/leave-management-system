package com.ust.lms.leavetype;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import org.springframework.data.domain.Pageable;

public interface LeaveTypeService {
    LeaveTypeResponseDto create(LeaveTypeRequestDto dto);
    LeaveTypeResponseDto getById(Long id);
    PageResponseDto<LeaveTypeResponseDto> getAll(Pageable pageable);
    LeaveTypeResponseDto update(Long id, LeaveTypeRequestDto dto);
    void delete(Long id);
}