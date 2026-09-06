package com.ust.lms.department.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.department.DepartmentService;
import com.ust.lms.department.impl.DepartmentServiceImpl;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DepartmentServiceIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
    }

    @Test
    void createDepartment_shouldCreateSuccessfully() {

        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setName("Human Resources");
        request.setDescription("HR Department");

        DepartmentResponseDto response =
                departmentService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Human Resources", response.getName());
        assertEquals("HR Department", response.getDescription());

        assertEquals(1, departmentRepository.count());
    }

    @Test
    void createDepartment_shouldRejectDuplicateName() {

        DepartmentRequestDto firstRequest = new DepartmentRequestDto();
        firstRequest.setName("Finance");
        firstRequest.setDescription("Finance Department");

        departmentService.create(firstRequest);

        DepartmentRequestDto duplicateRequest = new DepartmentRequestDto();
        duplicateRequest.setName("Finance");
        duplicateRequest.setDescription("Another Finance Department");

        assertThrows(
                BadRequestException.class,
                () -> departmentService.create(duplicateRequest)
        );

        assertEquals(1, departmentRepository.count());
    }

    @Test
    void createDepartment_shouldRejectDuplicateNameIgnoringCase() {

        DepartmentRequestDto firstRequest = new DepartmentRequestDto();
        firstRequest.setName("Engineering");

        departmentService.create(firstRequest);

        DepartmentRequestDto duplicateRequest = new DepartmentRequestDto();
        duplicateRequest.setName("engineering");

        assertThrows(
                BadRequestException.class,
                () -> departmentService.create(duplicateRequest)
        );

        assertEquals(1, departmentRepository.count());
    }

    @Test
    void getById_shouldReturnDepartment() {

        Department department = new Department();
        department.setName("IT");
        department.setDescription("Information Technology");

        Department saved =
                departmentRepository.save(department);

        DepartmentResponseDto response =
                departmentService.getById(saved.getId());

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals("IT", response.getName());
        assertEquals("Information Technology", response.getDescription());
    }

    @Test
    void getById_shouldThrowExceptionWhenDepartmentDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getById(999999L)
        );
    }

    @Test
    void getById_shouldThrowExceptionForDeletedDepartment() {

        Department department = new Department();
        department.setName("Deleted Department");
        department.setDescription("Deleted");
        department.setDeleted(true);

        Department saved =
                departmentRepository.save(department);

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getById(saved.getId())
        );
    }

    @Test
    void getAll_shouldReturnOnlyNonDeletedDepartments() {

        Department department1 = new Department();
        department1.setName("IT");
        department1.setDescription("IT Department");

        Department department2 = new Department();
        department2.setName("HR");
        department2.setDescription("HR Department");

        Department deletedDepartment = new Department();
        deletedDepartment.setName("Deleted");
        deletedDepartment.setDescription("Deleted Department");
        deletedDepartment.setDeleted(true);

        departmentRepository.save(department1);
        departmentRepository.save(department2);
        departmentRepository.save(deletedDepartment);

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<DepartmentResponseDto> response =
                departmentService.getAll(pageable);

        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(2, response.getContent().size());
    }

    @Test
    void updateDepartment_shouldUpdateSuccessfully() {

        Department department = new Department();
        department.setName("IT");
        department.setDescription("Old Description");

        Department saved =
                departmentRepository.save(department);

        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setName("Information Technology");
        request.setDescription("New Description");

        DepartmentResponseDto response =
                departmentService.update(saved.getId(), request);

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals("Information Technology", response.getName());
        assertEquals("New Description", response.getDescription());

        Department updated =
                departmentRepository.findById(saved.getId())
                        .orElseThrow();

        assertEquals("Information Technology", updated.getName());
        assertEquals("New Description", updated.getDescription());
    }

    @Test
    void updateDepartment_shouldThrowExceptionWhenDepartmentDoesNotExist() {

        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setName("IT");

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.update(999999L, request)
        );
    }

    @Test
    void updateDepartment_shouldRejectDuplicateName() {

        Department department1 = new Department();
        department1.setName("IT");

        Department department2 = new Department();
        department2.setName("HR");

        Department savedDepartment1 =
                departmentRepository.save(department1);

        departmentRepository.save(department2);

        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setName("HR");

        assertThrows(
                BadRequestException.class,
                () -> departmentService.update(
                        savedDepartment1.getId(),
                        request
                )
        );
    }

    @Test
    void updateDepartment_shouldAllowSameName() {

        Department department = new Department();
        department.setName("IT");
        department.setDescription("Old Description");

        Department saved =
                departmentRepository.save(department);

        DepartmentRequestDto request = new DepartmentRequestDto();
        request.setName("IT");
        request.setDescription("Updated Description");

        DepartmentResponseDto response =
                departmentService.update(saved.getId(), request);

        assertNotNull(response);
        assertEquals("IT", response.getName());
        assertEquals("Updated Description", response.getDescription());
    }

    @Test
    void deleteDepartment_shouldSoftDeleteSuccessfully() {

        Department department = new Department();
        department.setName("IT");
        department.setDescription("IT Department");

        Department saved =
                departmentRepository.save(department);

        departmentService.delete(saved.getId());

        Department deleted =
                departmentRepository.findById(saved.getId())
                        .orElseThrow();

        assertTrue(deleted.isDeleted());
    }

    @Test
    void deleteDepartment_shouldThrowExceptionWhenDepartmentDoesNotExist() {

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.delete(999999L)
        );
    }

    @Test
    void deleteDepartment_shouldThrowExceptionWhenAlreadyDeleted() {

        Department department = new Department();
        department.setName("Deleted Department");
        department.setDeleted(true);

        Department saved =
                departmentRepository.save(department);

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.delete(saved.getId())
        );
    }
}