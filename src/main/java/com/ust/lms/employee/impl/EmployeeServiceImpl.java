package com.ust.lms.employee.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.DepartmentNotFoundException;
import com.ust.lms.common.exception.EmployeeNotFoundException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.common.exception.UserNotFoundException;
import com.ust.lms.dto.EmployeeLeaveBalanceResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import com.ust.lms.employee.EmployeeService;
import com.ust.lms.model.*;
import com.ust.lms.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing employees and their leave balances,
 * including creation, retrieval, updating, and deletion of employee records.
 */
@Service
@Slf4j
public class EmployeeServiceImpl extends CommonService implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, ModelMapper modelMapper, EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository,
                               LeaveTypeRepository leaveTypeRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.modelMapper = modelMapper;
        this.employeeLeaveBalanceRepository = employeeLeaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    /**
     * Creates a new employee and initializes leave balances for all active leave types.
     *
     * @param dto employee details
     * @return response containing the created employee
     * @throws UserNotFoundException if the specified user does not exist
     * @throws DepartmentNotFoundException if the specified department does not exist
     */
    @Override
    public EmployeeResponseDto create(EmployeeRequestDto dto) {
        log.info("Creating employee for userId: {}", dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setLeaveBalance(0);
        setAuditFields(employee, true);

        Employee saved = employeeRepository.save(employee);
        createLeaveBalances(saved);

        log.info("Employee created successfully with id: {}", saved.getId());

        return toResponseDto(saved);
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id employee ID
     * @return response containing the requested employee
     * @throws EmployeeNotFoundException if the employee does not exist or has been deleted
     */
    @Override
    @Transactional
    public EmployeeResponseDto getById(Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return toResponseDto(employee);
    }

    /**
     * Retrieves all active employees using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing employees
     */
    @Override
    @Transactional
    public PageResponseDto<EmployeeResponseDto> getAll(Pageable pageable) {
        Page<Employee> result = employeeRepository.findByDeletedFalse(pageable);
        return toPageResponseDto(result);
    }

    /**
     * Retrieves all active employees belonging to a specific department.
     *
     * @param departmentId department ID
     * @param pageable pagination and sorting information
     * @return paginated response containing employees in the department
     */
    @Override
    @Transactional
    public PageResponseDto<EmployeeResponseDto> getByDepartmentId(Long departmentId, Pageable pageable) {
        Page<Employee> result = employeeRepository.findByDepartmentIdAndDeletedFalse(departmentId, pageable);
        return toPageResponseDto(result);
    }

    /**
     * Retrieves an employee ID using the associated user ID.
     *
     * @param userId user ID
     * @return employee ID associated with the user
     * @throws EmployeeNotFoundException if no employee record exists for the user
     */
    @Override
    @Transactional
    public Long getIdByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee record not found for this user"))
                .getId();
    }

    /**
     * Updates an existing employee's department or joining date.
     *
     * @param id employee ID
     * @param dto updated employee details
     * @return response containing the updated employee
     * @throws EmployeeNotFoundException if the employee does not exist or has been deleted
     * @throws DepartmentNotFoundException if the specified department does not exist
     */
    @Override
    @Transactional
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {
        log.info("Updating employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));
            employee.setDepartment(department);
        }
        if (dto.getJoiningDate() != null) {
            employee.setJoiningDate(dto.getJoiningDate());
        }

        setAuditFields(employee, false);

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully with id: {}", id);
        return toResponseDto(updated);
    }

    /**
     * Soft deletes an existing employee.
     *
     * @param id employee ID
     * @throws EmployeeNotFoundException if the employee does not exist or has been deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Deleting employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        softDelete(employee);
        setAuditFields(employee, false);
        employeeRepository.save(employee);
        log.info("Employee deleted successfully with id: {}", id);
    }

    /**
     * Converts an employee entity to its response DTO.
     *
     * @param employee employee entity
     * @return mapped employee response DTO
     */
    private EmployeeResponseDto toResponseDto(Employee employee) {
        EmployeeResponseDto dto = modelMapper.map(employee, EmployeeResponseDto.class);
        dto.setUserId(employee.getUser().getId());
        dto.setUserName(employee.getUser().getName());
        dto.setDepartmentId(employee.getDepartment().getId());
        dto.setDepartmentName(employee.getDepartment().getName());
        return dto;
    }

    /**
     * Converts a page of employee entities to a paginated response DTO.
     *
     * @param pageResult page containing employee entities
     * @return paginated employee response DTO
     */
    private PageResponseDto<EmployeeResponseDto> toPageResponseDto(Page<Employee> pageResult) {
        return new PageResponseDto<>(
                pageResult.getContent().stream().map(this::toResponseDto).toList(),
                pageResult.getNumber() + 1,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    /**
     * Creates leave balance records for all active leave types for an employee.
     *
     * @param employee employee for whom leave balances are created
     */
    private void createLeaveBalances(Employee employee) {

        LocalDate leaveYearStart = employee.getJoiningDate();
        LocalDate leaveYearEnd = leaveYearStart.plusYears(1).minusDays(1);

        List<LeaveType> leaveTypes = leaveTypeRepository.findByDeletedFalse();

        int totalBalance = 0;

        for (LeaveType leaveType : leaveTypes) {

            EmployeeLeaveBalance balance = new EmployeeLeaveBalance();
            balance.setEmployee(employee);
            balance.setLeaveType(leaveType);
            balance.setBalance(leaveType.getMaxDays());
            balance.setLeaveYearStart(leaveYearStart);
            balance.setLeaveYearEnd(leaveYearEnd);

            setAuditFields(balance, true);

            employeeLeaveBalanceRepository.save(balance);

            totalBalance += leaveType.getMaxDays();
        }

        employee.setLeaveBalance(totalBalance);
        employeeRepository.save(employee);
    }
    /**
     * Retrieves all active leave balances for an employee.
     *
     * @param employeeId employee ID
     * @return list of leave balance details including leave type information
     * @throws EmployeeNotFoundException if the employee does not exist or has been deleted
     */
    @Override
    @Transactional
    public List<EmployeeLeaveBalanceResponseDto> getLeaveBalances(Long employeeId) {
        log.info("Fetching leave balances for employee with id: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        return employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream()
                .map(this::toLeaveBalanceResponseDto)
                .toList();
    }
    /**
     * Converts an employee leave balance entity to its response DTO.
     *
     * @param balance employee leave balance entity
     * @return leave balance response DTO
     */
    private EmployeeLeaveBalanceResponseDto toLeaveBalanceResponseDto(
            EmployeeLeaveBalance balance) {

        EmployeeLeaveBalanceResponseDto dto =
                modelMapper.map(balance, EmployeeLeaveBalanceResponseDto.class);

        dto.setEmployeeId(balance.getEmployee().getId());
        dto.setLeaveTypeId(balance.getLeaveType().getId());
        dto.setLeaveTypeName(balance.getLeaveType().getName());

        return dto;
    }
}