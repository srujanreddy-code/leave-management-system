package com.ust.lms.employee;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponseDto create(EmployeeRequestDto dto);
    EmployeeResponseDto getById(Long id);
    PageResponseDto<EmployeeResponseDto> getAll(Pageable pageable);
    PageResponseDto<EmployeeResponseDto> getByDepartmentId(Long departmentId, Pageable pageable);
    Long getIdByUserId(Long userId);
    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);
    void delete(Long id);
}