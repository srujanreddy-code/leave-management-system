package com.ust.lms.department.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.DepartmentNotFoundException;
import com.ust.lms.common.exception.DuplicateDepartmentException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.department.DepartmentService;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing departments, including creation,
 * retrieval, updating, and deletion of department records.
 */
@Service
@Slf4j
public class DepartmentServiceImpl extends CommonService implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new department.
     *
     * @param dto department details
     * @return response containing the created department
     * @throws DuplicateDepartmentException if a department with the same name already exists
     */
    @Override
    public DepartmentResponseDto create(DepartmentRequestDto dto) {
        log.info("Creating department with name: {}", dto.getName());
        departmentRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            throw new DuplicateDepartmentException("Department name already exists");
        });

        Department department = modelMapper.map(dto, Department.class);
        setAuditFields(department, true);
        Department saved = departmentRepository.save(department);
        log.info("Department created successfully with id: {}", saved.getId());
        return modelMapper.map(saved, DepartmentResponseDto.class);
    }

    /**
     * Retrieves a department by its ID.
     *
     * @param id department ID
     * @return response containing the requested department
     * @throws DepartmentNotFoundException if the department does not exist or has been deleted
     */
    @Override
    public DepartmentResponseDto getById(Long id) {
        log.info("Fetching department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));
        return modelMapper.map(department, DepartmentResponseDto.class);
    }

    /**
     * Retrieves all active departments using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing active departments
     */
    @Override
    public PageResponseDto<DepartmentResponseDto> getAll(Pageable pageable) {
        Page<Department> result = departmentRepository.findByDeletedFalse(pageable);
        return new PageResponseDto<>(
                result.getContent().stream().map(d -> modelMapper.map(d, DepartmentResponseDto.class)).toList(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * Updates an existing department.
     *
     * @param id  department ID
     * @param dto updated department details
     * @return response containing the updated department
     * @throws DepartmentNotFoundException if the department does not exist or has been deleted
     * @throws DuplicateDepartmentException if the updated department name already exists
     */
    @Override
    public DepartmentResponseDto update(Long id, DepartmentRequestDto dto) {
        log.info("Updating department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() ->new DepartmentNotFoundException("Department not found"));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(department.getName())) {
            departmentRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
                throw new DuplicateDepartmentException("Department name already exists");
            });
        }

        modelMapper.map(dto, department);
        setAuditFields(department, false);
        Department updated = departmentRepository.save(department);
        log.info("Department updated successfully with id: {}", id);
        return modelMapper.map(updated, DepartmentResponseDto.class);
    }

    /**
     * Soft deletes an existing department.
     *
     * @param id department ID
     * @throws DepartmentNotFoundException if the department does not exist or has been deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Deleting department with id: {}", id);
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));
        softDelete(department);
        setAuditFields(department, false);
        departmentRepository.save(department);
        log.info("Department deleted successfully with id: {}", id);
    }
}