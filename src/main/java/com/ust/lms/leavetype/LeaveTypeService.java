package com.ust.lms.leavetype;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for managing leave types.
 */
public interface LeaveTypeService {

    /**
     * Creates a new leave type.
     *
     * @param dto leave type details
     * @return response containing the created leave type
     */
    LeaveTypeResponseDto create(LeaveTypeRequestDto dto);

    /**
     * Retrieves a leave type by its ID.
     *
     * @param id leave type ID
     * @return response containing the requested leave type
     */
    LeaveTypeResponseDto getById(Long id);

    /**
     * Retrieves all active leave types using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing leave types
     */
    PageResponseDto<LeaveTypeResponseDto> getAll(Pageable pageable);

    /**
     * Updates an existing leave type.
     *
     * @param id leave type ID
     * @param dto updated leave type details
     * @return response containing the updated leave type
     */
    LeaveTypeResponseDto update(Long id, LeaveTypeRequestDto dto);

    /**
     * Soft deletes an existing leave type.
     *
     * @param id leave type ID
     */
    void delete(Long id);
}