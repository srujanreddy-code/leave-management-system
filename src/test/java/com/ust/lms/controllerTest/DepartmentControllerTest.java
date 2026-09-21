package com.ust.lms.controllerTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.controller.DepartmentController;
import com.ust.lms.department.DepartmentService;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DepartmentController}.
 *
 * <p>This class tests the controller in isolation by mocking the
 * {@link DepartmentService}. It verifies HTTP response status codes,
 * response bodies, service interactions, pagination handling, and
 * exception propagation.</p>
 */
@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    /**
     * Mocked department service used by the controller.
     */
    @Mock
    private DepartmentService departmentService;

    /**
     * Controller under test.
     */
    @InjectMocks
    private DepartmentController departmentController;

    /**
     * Common department request used by the test cases.
     */
    private DepartmentRequestDto requestDto;

    /**
     * Common department response used by the test cases.
     */
    private DepartmentResponseDto responseDto;

    /**
     * Initializes common test data before each test.
     */
    @BeforeEach
    void setUp() {
        requestDto = new DepartmentRequestDto();
        requestDto.setName("Engineering");
        requestDto.setDescription("Engineering Department");

        responseDto = new DepartmentResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Engineering");
        responseDto.setDescription("Engineering Department");
    }

    /**
     * Verifies that creating a department returns HTTP 201 CREATED
     * along with the created department details.
     */
    @Test
    void create_shouldReturnCreatedDepartment() {

        when(departmentService.create(any(DepartmentRequestDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<DepartmentResponseDto> response =
                departmentController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Engineering", response.getBody().getName());
        assertEquals(
                "Engineering Department",
                response.getBody().getDescription()
        );

        verify(departmentService).create(requestDto);
    }

    /**
     * Verifies that retrieving a department by ID returns HTTP 200 OK
     * and the expected department details.
     */
    @Test
    void getById_shouldReturnDepartment() {

        when(departmentService.getById(1L))
                .thenReturn(responseDto);

        ResponseEntity<DepartmentResponseDto> response =
                departmentController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Engineering", response.getBody().getName());

        verify(departmentService).getById(1L);
    }

    /**
     * Verifies that retrieving all departments returns HTTP 200 OK
     * with the paginated response returned by the service.
     */
    @Test
    void getAll_shouldReturnPaginatedDepartments() {

        PageResponseDto<DepartmentResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        1,
                        10,
                        1,
                        1
                );

        when(departmentService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        ResponseEntity<PageResponseDto<DepartmentResponseDto>> response =
                departmentController.getAll(
                        1,
                        10,
                        "asc",
                        new String[]{"name"}
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getContent().size());

        verify(departmentService).getAll(any(Pageable.class));
    }

    /**
     * Verifies that the controller converts the one-based page number
     * received from the request into a zero-based page number before
     * passing the pageable object to the service.
     */
    @Test
    void getAll_shouldConvertPageToZeroBasedPage() {

        PageResponseDto<DepartmentResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        2,
                        10,
                        11,
                        2
                );

        when(departmentService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        departmentController.getAll(
                3,
                10,
                "asc",
                new String[]{"name"}
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(departmentService).getAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(
                "name",
                pageable.getSort().getOrderFor("name").getProperty()
        );
    }

    /**
     * Verifies that a page number less than one is converted to page zero
     * instead of producing a negative page number.
     */
    @Test
    void getAll_shouldUseZeroForPageLessThanOne() {

        PageResponseDto<DepartmentResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        1,
                        10,
                        1,
                        1
                );

        when(departmentService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        departmentController.getAll(
                0,
                10,
                "asc",
                new String[]{"name"}
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(departmentService).getAll(pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
    }

    /**
     * Verifies that updating a department returns HTTP 200 OK
     * with the updated department details.
     */
    @Test
    void update_shouldReturnUpdatedDepartment() {

        DepartmentResponseDto updatedResponse =
                new DepartmentResponseDto();

        updatedResponse.setId(1L);
        updatedResponse.setName("Information Technology");
        updatedResponse.setDescription("Updated Department");

        when(departmentService.update(1L, requestDto))
                .thenReturn(updatedResponse);

        ResponseEntity<DepartmentResponseDto> response =
                departmentController.update(1L, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(
                "Information Technology",
                response.getBody().getName()
        );
        assertEquals(
                "Updated Department",
                response.getBody().getDescription()
        );

        verify(departmentService).update(1L, requestDto);
    }

    /**
     * Verifies that deleting a department returns HTTP 204 NO CONTENT
     * after the service successfully performs the deletion.
     */
    @Test
    void delete_shouldReturnNoContent() {

        doNothing()
                .when(departmentService)
                .delete(1L);

        ResponseEntity<Void> response =
                departmentController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(departmentService).delete(1L);
    }

    /**
     * Verifies that exceptions thrown by the create service method
     * are propagated by the controller.
     */
    @Test
    void create_shouldPropagateServiceException() {

        when(departmentService.create(any(DepartmentRequestDto.class)))
                .thenThrow(new RuntimeException("Department creation failed"));

        assertThrows(
                RuntimeException.class,
                () -> departmentController.create(requestDto)
        );

        verify(departmentService).create(requestDto);
    }

    /**
     * Verifies that exceptions thrown by the getById service method
     * are propagated by the controller.
     */
    @Test
    void getById_shouldPropagateServiceException() {

        when(departmentService.getById(1L))
                .thenThrow(new RuntimeException("Department not found"));

        assertThrows(
                RuntimeException.class,
                () -> departmentController.getById(1L)
        );

        verify(departmentService).getById(1L);
    }

    /**
     * Verifies that exceptions thrown by the update service method
     * are propagated by the controller.
     */
    @Test
    void update_shouldPropagateServiceException() {

        when(departmentService.update(1L, requestDto))
                .thenThrow(new RuntimeException("Update failed"));

        assertThrows(
                RuntimeException.class,
                () -> departmentController.update(1L, requestDto)
        );

        verify(departmentService).update(1L, requestDto);
    }

    /**
     * Verifies that exceptions thrown by the delete service method
     * are propagated by the controller.
     */
    @Test
    void delete_shouldPropagateServiceException() {

        doThrow(new RuntimeException("Delete failed"))
                .when(departmentService)
                .delete(1L);

        assertThrows(
                RuntimeException.class,
                () -> departmentController.delete(1L)
        );

        verify(departmentService).delete(1L);
    }
}
