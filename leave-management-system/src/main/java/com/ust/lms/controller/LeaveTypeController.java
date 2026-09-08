package com.ust.lms.controller;

import com.ust.lms.common.PageResponseDto;
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

@RestController
@RequestMapping("/api/leave-types")
public class LeaveTypeController extends BaseController {

    private final LeaveTypeService leaveTypeService;

    public LeaveTypeController(LeaveTypeService leaveTypeService) {
        this.leaveTypeService = leaveTypeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponseDto> create(@Valid @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveTypeService.create(dto));
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<LeaveTypeResponseDto>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "id") String... sort) {

        int zeroBasedPage = Math.max(page - 1, 0);
        Pageable pageable = getPageable(zeroBasedPage, limit, sortDirection, sort);
        return ResponseEntity.ok(leaveTypeService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveTypeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeaveTypeResponseDto> update(@PathVariable Long id, @Valid @RequestBody LeaveTypeRequestDto dto) {
        return ResponseEntity.ok(leaveTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leaveTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}