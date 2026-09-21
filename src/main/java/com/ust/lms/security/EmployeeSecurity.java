package com.ust.lms.security;

import com.ust.lms.common.Role;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.model.Employee;
import com.ust.lms.model.User;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Provides authorization checks for employee-related operations.
 */
@Component("employeeSecurity")
public class EmployeeSecurity {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeSecurity(EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Checks whether the authenticated user is the owner of the employee record
     * or has manager or administrator privileges.
     *
     * @param employeeId    employee ID to check ownership for
     * @param authentication current authenticated user's authentication details
     * @return true if the user is the employee owner, manager, or administrator;
     *         false otherwise
     * @throws ResourceNotFoundException if the current user or employee cannot be found
     */
    public boolean isOwnerOrManager(Long employeeId, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return employee.getUser().getId().equals(currentUser.getId());
    }
}