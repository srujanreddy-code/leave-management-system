package com.ust.lms.serviceImplTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.DepartmentNotFoundException;
import com.ust.lms.common.exception.DuplicateDepartmentException;
import com.ust.lms.department.impl.DepartmentServiceImpl;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DepartmentServiceImpl}.
 *
 * <p>
 * Mockito is used to mock the repository so that the tests
 * verify only the business logic of the service implementation.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentServiceImpl departmentService;

    /**
     * Initializes the service before each test.
     */
    @BeforeEach
    void setUp() {
        departmentService =
                new DepartmentServiceImpl(
                        departmentRepository,
                        new ModelMapper()
                );
    }

    /**
     * Verifies successful department creation.
     */
    @Test
    void create_withNewName_savesSuccessfully() {

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");
        dto.setDescription("Development team");

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.empty());

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> {
                    Department department = invocation.getArgument(0);
                    department.setId(1L);
                    return department;
                });

        DepartmentResponseDto result =
                departmentService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());
        assertEquals("Development team", result.getDescription());

        verify(departmentRepository)
                .findByNameIgnoreCase("Engineering");

        verify(departmentRepository)
                .save(any(Department.class));
    }

    /**
     * Verifies that duplicate department names are rejected.
     */
    @Test
    void create_withDuplicateName_throwsDuplicateDepartmentException() {

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");

        Department existing = new Department();
        existing.setId(1L);
        existing.setName("Engineering");

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                DuplicateDepartmentException.class,
                () -> departmentService.create(dto)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    /**
     * Verifies that duplicate names are rejected without considering case.
     */
    @Test
    void create_withDuplicateNameIgnoringCase_throwsDuplicateDepartmentException() {

        Department dtoExisting = new Department();
        dtoExisting.setId(1L);
        dtoExisting.setName("Engineering");

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("engineering");

        when(departmentRepository.findByNameIgnoreCase("engineering"))
                .thenReturn(Optional.of(dtoExisting));

        assertThrows(
                DuplicateDepartmentException.class,
                () -> departmentService.create(dto)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    /**
     * Verifies successful retrieval of an active department.
     */
    @Test
    void getById_withExistingDepartment_returnsDepartment() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDescription("Development team");
        department.setDeleted(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        DepartmentResponseDto result =
                departmentService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Engineering", result.getName());
        assertEquals("Development team", result.getDescription());

        verify(departmentRepository)
                .findById(1L);
    }

    /**
     * Verifies that an unknown department throws
     * {@link DepartmentNotFoundException}.
     */
    @Test
    void getById_withUnknownId_throwsDepartmentNotFoundException() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.getById(99L)
        );
    }

    /**
     * Verifies that a deleted department cannot be retrieved.
     */
    @Test
    void getById_withDeletedDepartment_throwsDepartmentNotFoundException() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Deleted");
        department.setDeleted(true);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.getById(1L)
        );
    }

    /**
     * Verifies that active departments are returned with pagination.
     */
    @Test
    void getAll_returnsActiveDepartments() {

        Department department1 = new Department();
        department1.setId(1L);
        department1.setName("Engineering");

        Department department2 = new Department();
        department2.setId(2L);
        department2.setName("HR");

        Pageable pageable = PageRequest.of(0, 10);

        when(departmentRepository.findByDeletedFalse(pageable))
                .thenReturn(
                        new PageImpl<>(
                                List.of(department1, department2),
                                pageable,
                                2
                        )
                );

        PageResponseDto<DepartmentResponseDto> result =
                departmentService.getAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getContent().size());

        verify(departmentRepository)
                .findByDeletedFalse(pageable);
    }

    /**
     * Verifies successful department update.
     */
    @Test
    void update_withValidDepartment_updatesSuccessfully() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDescription("Old description");
        department.setDeleted(false);

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Information Technology");
        dto.setDescription("New description");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository
                .findByNameIgnoreCase("Information Technology"))
                .thenReturn(Optional.empty());

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        DepartmentResponseDto result =
                departmentService.update(1L, dto);

        assertNotNull(result);
        assertEquals(
                "Information Technology",
                result.getName()
        );
        assertEquals(
                "New description",
                result.getDescription()
        );

        verify(departmentRepository)
                .save(department);
    }

    /**
     * Verifies that updating an unknown department throws
     * {@link DepartmentNotFoundException}.
     */
    @Test
    void update_withUnknownId_throwsDepartmentNotFoundException() {

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.update(99L, dto)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    /**
     * Verifies that updating a department with a duplicate name
     * throws {@link DuplicateDepartmentException}.
     */
    @Test
    void update_withDuplicateName_throwsDuplicateDepartmentException() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDeleted(false);

        Department existing = new Department();
        existing.setId(2L);
        existing.setName("HR");

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("HR");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.findByNameIgnoreCase("HR"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                DuplicateDepartmentException.class,
                () -> departmentService.update(1L, dto)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    /**
     * Verifies that updating a department with the same name is allowed.
     */
    @Test
    void update_withSameName_updatesSuccessfully() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDescription("Old description");
        department.setDeleted(false);

        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");
        dto.setDescription("New description");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        DepartmentResponseDto result =
                departmentService.update(1L, dto);

        assertNotNull(result);
        assertEquals("Engineering", result.getName());
        assertEquals("New description", result.getDescription());

        verify(departmentRepository, never())
                .findByNameIgnoreCase(anyString());

        verify(departmentRepository)
                .save(department);
    }

    /**
     * Verifies successful soft deletion of a department.
     */
    @Test
    void delete_withExistingDepartment_softDeletesSuccessfully() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");
        department.setDeleted(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        departmentService.delete(1L);

        assertTrue(department.isDeleted());

        verify(departmentRepository)
                .findById(1L);

        verify(departmentRepository)
                .save(department);
    }

    /**
     * Verifies that deleting an unknown department throws
     * {@link DepartmentNotFoundException}.
     */
    @Test
    void delete_withUnknownId_throwsDepartmentNotFoundException() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.delete(99L)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    /**
     * Verifies that an already deleted department cannot be deleted.
     */
    @Test
    void delete_withAlreadyDeletedDepartment_throwsDepartmentNotFoundException() {

        Department department = new Department();
        department.setId(1L);
        department.setName("Deleted");
        department.setDeleted(true);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        assertThrows(
                DepartmentNotFoundException.class,
                () -> departmentService.delete(1L)
        );

        verify(departmentRepository, never())
                .save(any(Department.class));
    }
}