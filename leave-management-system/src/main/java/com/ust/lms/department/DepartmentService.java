package com.ust.lms.department;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {
    DepartmentResponseDto create(DepartmentRequestDto dto);
    DepartmentResponseDto getById(Long id);
    PageResponseDto<DepartmentResponseDto> getAll(Pageable pageable);
    DepartmentResponseDto update(Long id, DepartmentRequestDto dto);
    void delete(Long id);
}