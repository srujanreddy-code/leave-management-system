package com.ust.lms.department;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for managing departments.
 */
public interface DepartmentService {

    /**
     * Creates a new department.
     *
     * @param dto department details
     * @return response containing the created department
     */
    DepartmentResponseDto create(DepartmentRequestDto dto);

    /**
     * Retrieves a department by its ID.
     *
     * @param id department ID
     * @return response containing the requested department
     */
    DepartmentResponseDto getById(Long id);

    /**
     * Retrieves all active departments using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing departments
     */
    PageResponseDto<DepartmentResponseDto> getAll(Pageable pageable);

    /**
     * Updates an existing department.
     *
     * @param id department ID
     * @param dto updated department details
     * @return response containing the updated department
     */
    DepartmentResponseDto update(Long id, DepartmentRequestDto dto);

    /**
     * Soft deletes a department by its ID.
     *
     * @param id department ID
     */
    void delete(Long id);
}