package com.ust.lms.controller;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.PaginationConstants;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.employee.EmployeeService;
import com.ust.lms.leaverequest.LeaveRequestService;
import com.ust.lms.security.SecurityUtils;
import com.ust.lms.user.UserService;
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
 * Handles REST API requests for leave request management.
 */
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController extends BaseController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;
    private final EmployeeService employeeService;

    public LeaveRequestController(LeaveRequestService leaveRequestService, UserService userService, EmployeeService employeeService) {
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
        this.employeeService = employeeService;
    }

    /**
     * Applies for leave on behalf of an employee.
     *
     * @param dto leave request details
     * @return response containing the created leave request
     */
    @PostMapping
    @PreAuthorize("@leaveRequestSecurity.canApplyForEmployee(#dto.employeeId, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> apply(@Valid @RequestBody LeaveRequestRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.apply(dto));
    }

    /**
     * Retrieves leave requests with optional filtering by status, employee,
     * or department, along with pagination and sorting.
     *
     * @param status        optional leave status filter
     * @param employeeId    optional employee ID filter
     * @param departmentId  optional department ID filter
     * @param page          page number, starting from 1
     * @param limit         number of records per page
     * @param sortDirection sorting direction
     * @param sort           fields to sort by
     * @return paginated list of leave requests
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> getAll(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_LIMIT) int limit,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String... sort) {

        int zeroBasedPage = Math.max(page - 1, 0);
        Pageable pageable = getPageable(zeroBasedPage, limit, sortDirection, sort);

        if (!SecurityUtils.hasAnyRole("MANAGER", "ADMIN")) {
            Long userId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
            Long ownEmployeeId = employeeService.getIdByUserId(userId);
            return ResponseEntity.ok(leaveRequestService.getByEmployeeId(ownEmployeeId, pageable));
        }

        if (status != null) {
            return ResponseEntity.ok(leaveRequestService.getByStatus(status, pageable));
        }
        if (employeeId != null) {
            return ResponseEntity.ok(leaveRequestService.getByEmployeeId(employeeId, pageable));
        }
        if (departmentId != null) {
            return ResponseEntity.ok(leaveRequestService.getByDepartmentId(departmentId, pageable));
        }
        return ResponseEntity.ok(leaveRequestService.getAll(pageable));
    }

    /**
     * Retrieves a leave request by ID if the authenticated user is the owner,
     * a manager, or an administrator.
     *
     * @param id leave request ID
     * @return response containing the requested leave request
     */
    @GetMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }

    /**
     * Updates an existing leave request.
     *
     * @param id  leave request ID
     * @param dto updated leave request details
     * @return response containing the updated leave request
     */
    @PutMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> update(@PathVariable Long id, @Valid @RequestBody LeaveRequestRequestDto dto) {
        return ResponseEntity.ok(leaveRequestService.update(id, dto));
    }

    /**
     * Cancels a leave request by performing a soft delete.
     *
     * @param id leave request ID
     * @return response with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        leaveRequestService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approves a pending leave request.
     *
     * @param id leave request ID
     * @return response containing the approved leave request
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> approve(@PathVariable Long id) {
        Long approverId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(leaveRequestService.approve(id, approverId));
    }

    /**
     * Rejects a pending leave request.
     *
     * @param id leave request ID
     * @return response containing the rejected leave request
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> reject(@PathVariable Long id) {
        Long approverId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(leaveRequestService.reject(id, approverId));
    }
}