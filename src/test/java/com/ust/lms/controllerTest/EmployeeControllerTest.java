package com.ust.lms.controllerTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.controller.EmployeeController;
import com.ust.lms.dto.EmployeeLeaveBalanceResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.EmployeeService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmployeeController.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private EmployeeController employeeController;

    /**
     * Tests that creating an employee returns HTTP 201 CREATED
     * and the created employee response.
     */
    @Test
    void create_success() {

        EmployeeRequestDto requestDto = new EmployeeRequestDto();

        EmployeeResponseDto responseDto = new EmployeeResponseDto();

        when(employeeService.create(requestDto)).thenReturn(responseDto);

        ResponseEntity<EmployeeResponseDto> response =
                employeeController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(employeeService).create(requestDto);
    }

    /**
     * Tests that a manager can retrieve all employees.
     */
    @Test
    void getAll_manager_success() {

        PageResponseDto<EmployeeResponseDto> pageResponse =
                new PageResponseDto<>(List.of(), 0, 0, 0, 0);

        when(employeeService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<EmployeeResponseDto>> response =
                    employeeController.getAll(
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(employeeService).getAll(any(Pageable.class));
        }
    }

    /**
     * Tests that an administrator can retrieve all employees
     * filtered by department.
     */
    @Test
    void getAll_admin_withDepartmentFilter_success() {

        PageResponseDto<EmployeeResponseDto> pageResponse =
                new PageResponseDto<>(List.of(), 0, 0, 0, 0);

        when(employeeService.getByDepartmentId(
                anyLong(),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(true);

            ResponseEntity<PageResponseDto<EmployeeResponseDto>> response =
                    employeeController.getAll(
                            2L,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(pageResponse, response.getBody());

            verify(employeeService).getByDepartmentId(
                    anyLong(),
                    any(Pageable.class)
            );
        }
    }

    /**
     * Tests that a regular employee receives only their own employee record.
     */
    @Test
    void getAll_employee_returnsOwnEmployee() {

        EmployeeResponseDto employeeResponse =
                new EmployeeResponseDto();

        when(userService.getIdByEmail("employee@example.com"))
                .thenReturn(10L);

        when(employeeService.getIdByUserId(10L))
                .thenReturn(20L);

        when(employeeService.getById(20L))
                .thenReturn(employeeResponse);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(() ->
                    SecurityUtils.hasAnyRole("MANAGER", "ADMIN")
            ).thenReturn(false);

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("employee@example.com");

            ResponseEntity<PageResponseDto<EmployeeResponseDto>> response =
                    employeeController.getAll(
                            null,
                            1,
                            10,
                            "desc",
                            "id"
                    );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(List.of(employeeResponse), response.getBody().getContent());

            verify(userService).getIdByEmail("employee@example.com");
            verify(employeeService).getIdByUserId(10L);
            verify(employeeService).getById(20L);
        }
    }

    /**
     * Tests that an employee can retrieve all of their leave balances.
     */
    @Test
    void getMyLeaveBalances_success() {

        EmployeeLeaveBalanceResponseDto sickLeave =
                new EmployeeLeaveBalanceResponseDto();

        sickLeave.setId(1L);
        sickLeave.setEmployeeId(20L);
        sickLeave.setLeaveTypeId(1L);
        sickLeave.setLeaveTypeName("Sick Leave");
        sickLeave.setBalance(6);

        EmployeeLeaveBalanceResponseDto earnedLeave =
                new EmployeeLeaveBalanceResponseDto();

        earnedLeave.setId(2L);
        earnedLeave.setEmployeeId(20L);
        earnedLeave.setLeaveTypeId(2L);
        earnedLeave.setLeaveTypeName("Earned Leave");
        earnedLeave.setBalance(8);

        List<EmployeeLeaveBalanceResponseDto> balances =
                List.of(sickLeave, earnedLeave);

        when(userService.getIdByEmail("employee@example.com"))
                .thenReturn(10L);

        when(employeeService.getIdByUserId(10L))
                .thenReturn(20L);

        when(employeeService.getLeaveBalances(20L))
                .thenReturn(balances);

        try (MockedStatic<SecurityUtils> securityUtils =
                     mockStatic(SecurityUtils.class)) {

            securityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn("employee@example.com");

            ResponseEntity<List<EmployeeLeaveBalanceResponseDto>> response =
                    employeeController.getMyLeaveBalances();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(balances, response.getBody());

            verify(userService).getIdByEmail("employee@example.com");
            verify(employeeService).getIdByUserId(10L);
            verify(employeeService).getLeaveBalances(20L);
        }
    }

    /**
     * Tests that retrieving an employee by ID returns HTTP 200 OK
     * and the requested employee.
     */
    @Test
    void getById_success() {

        EmployeeResponseDto responseDto =
                new EmployeeResponseDto();

        when(employeeService.getById(1L))
                .thenReturn(responseDto);

        ResponseEntity<EmployeeResponseDto> response =
                employeeController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(employeeService).getById(1L);
    }

    /**
     * Tests that updating an employee returns HTTP 200 OK
     * and the updated employee response.
     */
    @Test
    void update_success() {

        EmployeeRequestDto requestDto =
                new EmployeeRequestDto();

        EmployeeResponseDto responseDto =
                new EmployeeResponseDto();

        when(employeeService.update(1L, requestDto))
                .thenReturn(responseDto);

        ResponseEntity<EmployeeResponseDto> response =
                employeeController.update(1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());

        verify(employeeService).update(1L, requestDto);
    }

    /**
     * Tests that deleting an employee returns HTTP 204 NO CONTENT.
     */
    @Test
    void delete_success() {

        ResponseEntity<Void> response =
                employeeController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(null, response.getBody());

        verify(employeeService).delete(1L);
    }
}