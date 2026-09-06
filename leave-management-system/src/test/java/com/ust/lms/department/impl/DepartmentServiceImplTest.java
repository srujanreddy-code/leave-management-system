package com.ust.lms.department.impl;

import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentServiceImpl(departmentRepository, new ModelMapper());
    }

    @Test
    void create_withNewName_savesSuccessfully() {
        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");
        dto.setDescription("Dev team");

        when(departmentRepository.findByNameIgnoreCase("Engineering")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DepartmentResponseDto result = departmentService.create(dto);

        assertEquals("Engineering", result.getName());
    }

    @Test
    void create_withDuplicateName_throwsBadRequest() {
        DepartmentRequestDto dto = new DepartmentRequestDto();
        dto.setName("Engineering");

        when(departmentRepository.findByNameIgnoreCase("Engineering"))
                .thenReturn(Optional.of(new Department()));

        assertThrows(BadRequestException.class, () -> departmentService.create(dto));
    }

    @Test
    void getById_withUnknownId_throwsResourceNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getById(99L));
    }

    @Test
    void getById_withSoftDeletedRecord_throwsResourceNotFound() {
        Department deleted = new Department();
        deleted.setId(1L);
        deleted.setDeleted(true);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(deleted));

        assertThrows(ResourceNotFoundException.class, () -> departmentService.getById(1L));
    }
}