package com.ust.lms.controller;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.PaginationConstants;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.leavetype.LeaveTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles REST API requests for leave type management.
 */
@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController extends BaseController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    /**
     * Creates a new leave type.
     *
     * @param dto leave type details
     * @return response containing the created leave type
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponseDto> create(@Valid @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.create(dto));
    }

    /**
     * Retrieves all active leave types with pagination and sorting.
     *
     * @param page          page number, starting from 1
     * @param limit         number of records per page
     * @param sortDirection sorting direction
     * @param sort          fields to sort by
     * @return paginated list of leave types
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<LeaveTypeResponseDto>> getAll(
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_LIMIT) int limit,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String... sort) {

        int zeroBasedPage = Math.max(page - 1, 0);
        Pageable pageable = getPageable(zeroBasedPage, limit, sortDirection, sort);
        return ResponseEntity.ok(leaveTypeService.getAll(pageable));
    }

    /**
     * Retrieves a leave type by its ID.
     *
     * @param id leave type ID
     * @return response containing the requested leave type
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.getById(id));
    }

    /**
     * Updates an existing leave type.
     *
     * @param id  leave type ID
     * @param dto updated leave type details
     * @return response containing the updated leave type
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponseDto> update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.ok(leaveTypeService.update(id, dto));
    }

    /**
     * Soft deletes a leave type by ID.
     *
     * @param id leave type ID
     * @return response with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}