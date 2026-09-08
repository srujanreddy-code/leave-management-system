package com.ust.lms.controller;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
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

    @PostMapping
    @PreAuthorize("@leaveRequestSecurity.canApplyForEmployee(#dto.employeeId, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> apply(@Valid @RequestBody LeaveRequestRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.apply(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> getAll(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "id") String... sort) {

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

    @GetMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<LeaveRequestResponseDto> update(@PathVariable Long id, @Valid @RequestBody LeaveRequestRequestDto dto) {
        return ResponseEntity.ok(leaveRequestService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@leaveRequestSecurity.isOwnerOrManager(#id, authentication)")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        leaveRequestService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> approve(@PathVariable Long id) {
        Long approverId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(leaveRequestService.approve(id, approverId));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponseDto> reject(@PathVariable Long id) {
        Long approverId = userService.getIdByEmail(SecurityUtils.getCurrentUserEmail());
        return ResponseEntity.ok(leaveRequestService.reject(id, approverId));
    }
}