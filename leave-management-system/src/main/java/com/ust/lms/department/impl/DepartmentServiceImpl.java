package com.ust.lms.department.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.department.DepartmentService;
import com.ust.lms.dto.DepartmentRequestDto;
import com.ust.lms.dto.DepartmentResponseDto;
import com.ust.lms.model.Department;
import com.ust.lms.repository.DepartmentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends CommonService implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, ModelMapper modelMapper) {
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public DepartmentResponseDto create(DepartmentRequestDto dto) {
        departmentRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
            throw new BadRequestException("Department name already exists");
        });

        Department department = modelMapper.map(dto, Department.class);
        setAuditFields(department, true);
        Department saved = departmentRepository.save(department);
        return modelMapper.map(saved, DepartmentResponseDto.class);
    }

    @Override
    public DepartmentResponseDto getById(Long id) {
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return modelMapper.map(department, DepartmentResponseDto.class);
    }

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

    @Override
    public DepartmentResponseDto update(Long id, DepartmentRequestDto dto) {
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (dto.getName() != null && !dto.getName().equalsIgnoreCase(department.getName())) {
            departmentRepository.findByNameIgnoreCase(dto.getName()).ifPresent(existing -> {
                throw new BadRequestException("Department name already exists");
            });
        }

        modelMapper.map(dto, department);
        setAuditFields(department, false);
        Department updated = departmentRepository.save(department);
        return modelMapper.map(updated, DepartmentResponseDto.class);
    }

    @Override
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .filter(d -> !d.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        softDelete(department);
        setAuditFields(department, false);
        departmentRepository.save(department);
    }
}