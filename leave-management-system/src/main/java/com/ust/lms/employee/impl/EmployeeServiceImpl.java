package com.ust.lms.employee.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.EmployeeService;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl extends CommonService implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, ModelMapper modelMapper) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public EmployeeResponseDto create(EmployeeRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setLeaveBalance(dto.getLeaveBalance());
        setAuditFields(employee, true);

        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
    }

    @Override
    @Transactional
    public EmployeeResponseDto getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toResponseDto(employee);
    }

    @Override
    @Transactional
    public PageResponseDto<EmployeeResponseDto> getAll(Pageable pageable) {
        Page<Employee> result = employeeRepository.findByDeletedFalse(pageable);
        return toPageResponseDto(result);
    }

    @Override
    @Transactional
    public PageResponseDto<EmployeeResponseDto> getByDepartmentId(Long departmentId, Pageable pageable) {
        Page<Employee> result = employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId, pageable);
        return toPageResponseDto(result);
    }

    @Override
    @Transactional
    public Long getIdByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee record not found for this user"))
                .getId();
    }

    @Override
    @Transactional
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            employee.setDepartment(department);
        }
        if (dto.getJoiningDate() != null) {
            employee.setJoiningDate(dto.getJoiningDate());
        }
        if (dto.getLeaveBalance() != null) {
            employee.setLeaveBalance(dto.getLeaveBalance());
        }

        setAuditFields(employee, false);

        Employee updated = employeeRepository.save(employee);
        return toResponseDto(updated);
    }

    @Override
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        softDelete(employee);
        setAuditFields(employee, false);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDto toResponseDto(Employee employee) {
        EmployeeResponseDto dto = modelMapper.map(employee, EmployeeResponseDto.class);
        dto.setUserId(employee.getUser().getId());
        dto.setUserName(employee.getUser().getName());
        dto.setDepartmentId(employee.getDepartment().getId());
        dto.setDepartmentName(employee.getDepartment().getName());
        return dto;
    }

    private PageResponseDto<EmployeeResponseDto> toPageResponseDto(Page<Employee> pageResult) {
        return new PageResponseDto<>(
                pageResult.getContent().stream().map(this::toResponseDto).toList(),
                pageResult.getNumber() + 1,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
}