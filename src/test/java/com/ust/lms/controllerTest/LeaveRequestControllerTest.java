package com.ust.lms.controllerTest;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.controller.LeaveRequestController;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import com.ust.lms.employee.EmployeeService;
import com.ust.lms.leaverequest.LeaveRequestService;
import com.ust.lms.security.SecurityUtils;
import com.ust.lms.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LeaveRequestController.
 */
@ExtendWith(MockitoExtension.class)
class LeaveRequestControllerTest {

    @Mock
    private LeaveRequestService leaveRequestService;

    @Mock
    private UserService userService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private LeaveRequestController leaveRequestController;

    /**
     * Tests successful leave application.
     */
    @Test
    void apply_success() {

        LeaveRequestRequestDto requestDto = createRequestDto();

        LeaveRequestResponseDto responseDto =
                new LeaveRequestResponseDto();

        when(leaveRequestService.apply(requestDto))
                .thenReturn(responseDto);

        ResponseEntity<LeaveRequestResponseDto> response =
                leaveRequestController.apply(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(leaveRequestService).apply(requestDto);
    }

    /**
     * Tests retrieval of all leave requests by a manager or administrator.
     */
    @Test
    void getAll_admin_success() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(leaveRequestService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            null,
                            null,
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(leaveRequestService)
                    .getAll(any(Pageable.class));
        }
    }

    /**
     * Tests retrieval of leave requests filtered by status.
     */
    @Test
    void getAll_byStatus_success() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(leaveRequestService.getByStatus(
                eq(LeaveStatus.PENDING),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            LeaveStatus.PENDING,
                            null,
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(leaveRequestService)
                    .getByStatus(
                            eq(LeaveStatus.PENDING),
                            any(Pageable.class)
                    );
        }
    }

    /**
     * Tests retrieval of leave requests filtered by employee.
     */
    @Test
    void getAll_byEmployee_success() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(leaveRequestService.getByEmployeeId(
                eq(10L),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            null,
                            10L,
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(leaveRequestService)
                    .getByEmployeeId(
                            eq(10L),
                            any(Pageable.class)
                    );
        }
    }

    /**
     * Tests retrieval of leave requests filtered by department.
     */
    @Test
    void getAll_byDepartment_success() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(leaveRequestService.getByDepartmentId(
                eq(5L),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            null,
                            null,
                            5L,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(leaveRequestService)
                    .getByDepartmentId(
                            eq(5L),
                            any(Pageable.class)
                    );
        }
    }

    /**
     * Tests that a regular employee receives only their own leave requests.
     */
    @Test
    void getAll_employee_returnsOwnRequests() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(userService.getIdByEmail("employee@example.com"))
                .thenReturn(1L);

        when(employeeService.getIdByUserId(1L))
                .thenReturn(10L);

        when(leaveRequestService.getByEmployeeId(
                eq(10L),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(false);

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("employee@example.com");

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            null,
                            null,
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(userService)
                    .getIdByEmail("employee@example.com");

            verify(employeeService)
                    .getIdByUserId(1L);

            verify(leaveRequestService)
                    .getByEmployeeId(
                            eq(10L),
                            any(Pageable.class)
                    );
        }
    }

    /**
     * Tests that an employee cannot use the employeeId filter
     * to retrieve another employee's leave requests.
     */
    @Test
    void getAll_employee_ignoresEmployeeFilter() {

        PageResponseDto<LeaveRequestResponseDto> pageResponse =
                createPageResponse();

        when(userService.getIdByEmail("employee@example.com"))
                .thenReturn(1L);

        when(employeeService.getIdByUserId(1L))
                .thenReturn(10L);

        when(leaveRequestService.getByEmployeeId(
                eq(10L),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(false);

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("employee@example.com");

            ResponseEntity<PageResponseDto<LeaveRequestResponseDto>> response =
                    leaveRequestController.getAll(
                            null,
                            999L,
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(leaveRequestService)
                    .getByEmployeeId(
                            eq(10L),
                            any(Pageable.class)
                    );
        }
    }

    /**
     * Tests successful retrieval of a leave request by ID.
     */
    @Test
    void getById_success() {

        LeaveRequestResponseDto responseDto =
                new LeaveRequestResponseDto();

        when(leaveRequestService.getById(1L))
                .thenReturn(responseDto);

        ResponseEntity<LeaveRequestResponseDto> response =
                leaveRequestController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(leaveRequestService)
                .getById(1L);
    }

    /**
     * Tests successful update of a leave request.
     */
    @Test
    void update_success() {

        LeaveRequestRequestDto requestDto = createRequestDto();

        LeaveRequestResponseDto responseDto =
                new LeaveRequestResponseDto();

        when(leaveRequestService.update(1L, requestDto))
                .thenReturn(responseDto);

        ResponseEntity<LeaveRequestResponseDto> response =
                leaveRequestController.update(
                        1L,
                        requestDto
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(leaveRequestService)
                .update(1L, requestDto);
    }

    /**
     * Tests successful cancellation of a leave request.
     */
    @Test
    void cancel_success() {

        ResponseEntity<Void> response =
                leaveRequestController.cancel(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(leaveRequestService)
                .cancel(1L);
    }

    /**
     * Tests successful approval of a leave request.
     */
    @Test
    void approve_success() {

        LeaveRequestResponseDto responseDto =
                new LeaveRequestResponseDto();

        when(userService.getIdByEmail("manager@example.com"))
                .thenReturn(2L);

        when(leaveRequestService.approve(1L, 2L))
                .thenReturn(responseDto);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("manager@example.com");

            ResponseEntity<LeaveRequestResponseDto> response =
                    leaveRequestController.approve(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(responseDto, response.getBody());

            verify(userService)
                    .getIdByEmail("manager@example.com");

            verify(leaveRequestService)
                    .approve(1L, 2L);
        }
    }

    /**
     * Tests successful rejection of a leave request.
     */
    @Test
    void reject_success() {

        LeaveRequestResponseDto responseDto =
                new LeaveRequestResponseDto();

        when(userService.getIdByEmail("manager@example.com"))
                .thenReturn(2L);

        when(leaveRequestService.reject(1L, 2L))
                .thenReturn(responseDto);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("manager@example.com");

            ResponseEntity<LeaveRequestResponseDto> response =
                    leaveRequestController.reject(1L);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(responseDto, response.getBody());

            verify(userService)
                    .getIdByEmail("manager@example.com");

            verify(leaveRequestService)
                    .reject(1L, 2L);
        }
    }

    /**
     * Creates a sample page response.
     */
    private PageResponseDto<LeaveRequestResponseDto> createPageResponse() {

        return new PageResponseDto<>(
                List.of(),
                0,
                0,
                0,
                0
        );
    }

    /**
     * Creates a valid leave request DTO.
     *
     * @return leave request DTO
     */
    private LeaveRequestRequestDto createRequestDto() {

        LeaveRequestRequestDto dto =
                new LeaveRequestRequestDto();

        dto.setEmployeeId(10L);
        dto.setLeaveTypeId(20L);
        dto.setStartDate(
                LocalDate.now().plusDays(2)
        );
        dto.setEndDate(
                LocalDate.now().plusDays(3)
        );
        dto.setReason("Personal leave");

        return dto;
    }
}
