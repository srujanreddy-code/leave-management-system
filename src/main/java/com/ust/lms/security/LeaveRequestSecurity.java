package com.ust.lms.security;

import com.ust.lms.common.Role;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.model.LeaveRequest;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveRequestRepository;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Provides authorization checks for leave request operations.
 */
@Component("leaveRequestSecurity")
public class LeaveRequestSecurity {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestSecurity(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository,EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Checks whether the authenticated user is the owner of the leave request
     * or has manager or administrator privileges.
     *
     * @param leaveRequestId leave request ID to check ownership for
     * @param authentication current authenticated user's authentication details
     * @return true if the user is the owner, manager, or administrator; false otherwise
     * @throws ResourceNotFoundException if the current user or leave request cannot be found
     */
    public boolean isOwnerOrManager(Long leaveRequestId, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        return leaveRequest.getEmployee().getUser().getId().equals(currentUser.getId());
    }

    /**
     * Checks whether the authenticated user can apply for leave on behalf
     * of the specified employee.
     *
     * @param employeeId employee ID for whom the leave is being applied
     * @param authentication current authenticated user's authentication details
     * @return true if the user can apply for the employee; false otherwise
     * @throws ResourceNotFoundException if the current user cannot be found
     */
    public boolean canApplyForEmployee(Long employeeId, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        return employeeRepository.findById(employeeId)
                .map(employee -> employee.getUser().getId().equals(currentUser.getId()))
                .orElse(false);
    }
}