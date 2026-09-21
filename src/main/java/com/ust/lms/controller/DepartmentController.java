package com.ust.lms.controller;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.PaginationConstants;
import com.ust.lms.department.DepartmentService;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
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
 * Handles REST API requests for department management.
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController extends BaseController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * Creates a new department.
     *
     * @param dto department details
     * @return response containing the created department
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDto> create(@Valid @RequestBody DepartmentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(dto));
    }

    /**
     * Retrieves all active departments with pagination and sorting.
     *
     * @param page          page number, starting from 1
     * @param limit         number of records per page
     * @param sortDirection sorting direction
     * @param sort          fields to sort by
     * @return paginated list of departments
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<DepartmentResponseDto>> getAll(
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_LIMIT) int limit,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String... sort){

        int zeroBasedPage = Math.max(page - 1, 0);
        Pageable pageable = getPageable(zeroBasedPage, limit, sortDirection, sort);
        return ResponseEntity.ok(departmentService.getAll(pageable));
    }

    /**
     * Retrieves a department by its ID.
     *
     * @param id department ID
     * @return response containing the requested department
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    /**
     * Updates an existing department.
     *
     * @param id  department ID
     * @param dto updated department details
     * @return response containing the updated department
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDto> update(@PathVariable Long id, @Valid @RequestBody DepartmentRequestDto dto) {
        return ResponseEntity.ok(departmentService.update(id, dto));
    }

    /**
     * Soft deletes a department by its ID.
     *
     * @param id department ID
     * @return response with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}