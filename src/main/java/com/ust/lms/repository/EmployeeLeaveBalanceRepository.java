package com.ust.lms.repository;

import com.ust.lms.model.EmployeeLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for performing database operations on employee leave balances.
 */
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, Long> {

    Optional<EmployeeLeaveBalance> findByEmployeeIdAndLeaveTypeIdAndLeaveYearStart(
            Long employeeId,
            Long leaveTypeId,
            LocalDate leaveYearStart
    );

    List<EmployeeLeaveBalance> findByEmployeeIdAndDeletedFalse(Long employeeId);
}