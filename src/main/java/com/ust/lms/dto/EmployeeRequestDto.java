package com.ust.lms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents the request data used to create or update an employee.
 */
@Getter
@Setter
public class EmployeeRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long departmentId;

    @NotNull
    private LocalDate joiningDate;
}