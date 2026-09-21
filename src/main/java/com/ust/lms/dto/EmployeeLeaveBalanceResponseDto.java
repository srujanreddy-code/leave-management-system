package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents an employee's leave balance details for a specific leave type and leave year.
 */
@Getter
@Setter
public class EmployeeLeaveBalanceResponseDto {
    private Long id;
    private Long employeeId;
    private Long leaveTypeId;
    private String leaveTypeName;
    private Integer balance;
    private LocalDate leaveYearStart;
    private LocalDate leaveYearEnd;
}