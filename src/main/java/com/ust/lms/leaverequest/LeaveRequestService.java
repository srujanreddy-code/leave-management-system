package com.ust.lms.leaverequest;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.LeaveRequestRequestDto;
import com.ust.lms.dto.LeaveRequestResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for managing leave requests.
 */
public interface LeaveRequestService {

    /**
     * Creates a new leave request.
     *
     * @param dto leave request details
     * @return response containing the created leave request
     */
    LeaveRequestResponseDto apply(LeaveRequestRequestDto dto);

    /**
     * Retrieves a leave request by its ID.
     *
     * @param id leave request ID
     * @return response containing the requested leave request
     */
    LeaveRequestResponseDto getById(Long id);

    /**
     * Retrieves all active leave requests using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing leave requests
     */
    PageResponseDto<LeaveRequestResponseDto> getAll(Pageable pageable);

    /**
     * Retrieves active leave requests for a specific employee.
     *
     * @param employeeId employee ID
     * @param pageable pagination and sorting information
     * @return paginated response containing the employee's leave requests
     */
    PageResponseDto<LeaveRequestResponseDto> getByEmployeeId(Long employeeId, Pageable pageable);

    /**
     * Retrieves active leave requests filtered by leave status.
     *
     * @param status leave status
     * @param pageable pagination and sorting information
     * @return paginated response containing matching leave requests
     */
    PageResponseDto<LeaveRequestResponseDto> getByStatus(LeaveStatus status, Pageable pageable);

    /**
     * Retrieves active leave requests for a specific department.
     *
     * @param departmentId department ID
     * @param pageable pagination and sorting information
     * @return paginated response containing leave requests in the department
     */
    PageResponseDto<LeaveRequestResponseDto> getByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * Updates an existing pending leave request.
     *
     * @param id leave request ID
     * @param dto updated leave request details
     * @return response containing the updated leave request
     */
    LeaveRequestResponseDto update(Long id, LeaveRequestRequestDto dto);

    /**
     * Cancels an existing leave request.
     *
     * @param id leave request ID
     */
    void cancel(Long id);

    /**
     * Approves a pending leave request.
     *
     * @param id leave request ID
     * @param approverId ID of the user approving the request
     * @return response containing the approved leave request
     */
    LeaveRequestResponseDto approve(Long id, Long approverId);

    /**
     * Rejects a pending leave request.
     *
     * @param id leave request ID
     * @param approverId ID of the user rejecting the request
     * @return response containing the rejected leave request
     */
    LeaveRequestResponseDto reject(Long id, Long approverId);
}