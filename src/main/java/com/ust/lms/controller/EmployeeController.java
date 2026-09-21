package com.ust.lms.controller;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.PaginationConstants;
import com.ust.lms.dto.EmployeeLeaveBalanceResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.EmployeeService;
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

import java.util.List;

/**
 * Handles REST API requests for employee management.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController extends BaseController {

    private final EmployeeService employeeService;
    private final UserService userService;

    public EmployeeController(EmployeeService employeeService, UserService userService) {
        this.employeeService = employeeService;
        this.userService = userService;
    }

    /**
     * Creates a new employee.
     *
     * @param dto employee details
     * @return response containing the created employee
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> create(@Valid @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    /**
     * Retrieves employees with optional department filtering, pagination, and sorting.
     * Regular employees can only retrieve their own employee record.
     *
     * @param departmentId optional department ID used to filter employees
     * @param page         page number, starting from 1
     * @param limit        number of records per page
     * @param sortDirection sorting direction
     * @param sort         fields to sort by
     * @return paginated list of employees
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<EmployeeResponseDto>> getAll(
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
            EmployeeResponseDto own = employeeService.getById(ownEmployeeId);
            return ResponseEntity.ok(new PageResponseDto<>(List.of(own), 1, 1, 1, 1));
        }

        if (departmentId != null) {
            return ResponseEntity.ok(employeeService.getByDepartmentId(departmentId, pageable));
        }
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    /**
     * Retrieves an employee by ID if the authenticated user is the owner,
     * a manager, or an administrator.
     *
     * @param id employee ID
     * @return response containing the requested employee
     */
    @GetMapping("/{id}")
    @PreAuthorize("@employeeSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    /**
     * Updates an existing employee.
     *
     * @param id  employee ID
     * @param dto updated employee details
     * @return response containing the updated employee
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    /**
     * Soft deletes an employee by ID.
     *
     * @param id employee ID
     * @return response with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Retrieves all leave balances for the currently authenticated employee.
     *
     * @return list of the employee's leave balances along with their leave types
     */
    @GetMapping("/me/leave-balances")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<EmployeeLeaveBalanceResponseDto>> getMyLeaveBalances() {

        Long userId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
        Long employeeId = employeeService.getIdByUserId(userId);

        return ResponseEntity.ok(employeeService.getLeaveBalances(employeeId));
    }
}