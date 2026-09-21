package com.ust.lms.employee;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.EmployeeLeaveBalanceResponseDto;
import com.ust.lms.dto.EmployeeRequestDto;
import com.ust.lms.dto.EmployeeResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Defines operations for managing employees.
 */
public interface EmployeeService {

    /**
     * Creates a new employee.
     *
     * @param dto employee details
     * @return response containing the created employee
     */
    EmployeeResponseDto create(EmployeeRequestDto dto);

    /**
     * Retrieves an employee by their ID.
     *
     * @param id employee ID
     * @return response containing the requested employee
     */
    EmployeeResponseDto getById(Long id);

    /**
     * Retrieves all active employees using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing employees
     */
    PageResponseDto<EmployeeResponseDto> getAll(Pageable pageable);

    /**
     * Retrieves active employees belonging to a specific department.
     *
     * @param departmentId department ID
     * @param pageable pagination and sorting information
     * @return paginated response containing employees in the department
     */
    PageResponseDto<EmployeeResponseDto> getByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * Retrieves the employee ID associated with a user ID.
     *
     * @param userId user ID
     * @return employee ID associated with the user
     */
    Long getIdByUserId(Long userId);

    /**
     * Updates an existing employee.
     *
     * @param id employee ID
     * @param dto updated employee details
     * @return response containing the updated employee
     */
    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);

    /**
     * Soft deletes an employee by their ID.
     *
     * @param id employee ID
     */
    void delete(Long id);
    /**
     * Retrieves all leave balances for an employee.
     *
     * @param employeeId employee ID
     * @return list of leave balances with their corresponding leave types
     */
    List<EmployeeLeaveBalanceResponseDto> getLeaveBalances(Long employeeId);
}