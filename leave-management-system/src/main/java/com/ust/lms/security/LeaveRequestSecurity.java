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