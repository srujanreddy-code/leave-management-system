package com.ust.lms.controllerTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.controller.LeaveTypeController;
import com.ust.lms.dto.LeaveTypeRequestDto;
import com.ust.lms.dto.LeaveTypeResponseDto;
import com.ust.lms.leavetype.LeaveTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LeaveTypeController}.
 *
 * <p>Verifies controller responses and delegation to the leave type service
 * without starting the Spring application context.</p>
 */
@ExtendWith(MockitoExtension.class)
class LeaveTypeControllerTest {

    @Mock
    private LeaveTypeService leaveTypeService;

    @InjectMocks
    private LeaveTypeController leaveTypeController;

    private LeaveTypeRequestDto requestDto;
    private LeaveTypeResponseDto responseDto;

    /**
     * Sets up common test data.
     */
    @BeforeEach
    void setUp() {

        requestDto = new LeaveTypeRequestDto();
        requestDto.setName("Sick Leave");
        requestDto.setMaxDays(6);

        responseDto = new LeaveTypeResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Sick Leave");
        responseDto.setMaxDays(6);
    }

    /**
     * Verifies that creating a leave type returns HTTP 201 CREATED.
     */
    @Test
    void create_success() {

        when(leaveTypeService.create(requestDto))
                .thenReturn(responseDto);

        ResponseEntity<LeaveTypeResponseDto> response =
                leaveTypeController.create(requestDto);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertEquals(
                responseDto,
                response.getBody()
        );

        verify(leaveTypeService)
                .create(requestDto);
    }

    /**
     * Verifies that all leave types are returned successfully.
     */
    @Test
    void getAll_success() {

        PageResponseDto<LeaveTypeResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        1,
                        10,
                        1,
                        1
                );

        when(leaveTypeService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponseDto<LeaveTypeResponseDto>> response =
                leaveTypeController.getAll(
                        1,
                        10,
                        "desc",
                        "id"
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                pageResponse,
                response.getBody()
        );

        verify(leaveTypeService)
                .getAll(any(Pageable.class));
    }

    /**
     * Verifies that a leave type is retrieved successfully by ID.
     */
    @Test
    void getById_success() {

        when(leaveTypeService.getById(1L))
                .thenReturn(responseDto);

        ResponseEntity<LeaveTypeResponseDto> response =
                leaveTypeController.getById(1L);

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                responseDto,
                response.getBody()
        );

        verify(leaveTypeService)
                .getById(1L);
    }

    /**
     * Verifies that a leave type is updated successfully.
     */
    @Test
    void update_success() {

        when(leaveTypeService.update(1L, requestDto))
                .thenReturn(responseDto);

        ResponseEntity<LeaveTypeResponseDto> response =
                leaveTypeController.update(
                        1L,
                        requestDto
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                responseDto,
                response.getBody()
        );

        verify(leaveTypeService)
                .update(1L, requestDto);
    }

    /**
     * Verifies that a leave type is soft deleted successfully.
     */
    @Test
    void delete_success() {

        ResponseEntity<Void> response =
                leaveTypeController.delete(1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(leaveTypeService)
                .delete(1L);
    }
}
