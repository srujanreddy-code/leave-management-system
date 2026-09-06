package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeResponseDto extends CommonResponseDto {
    private Long userId;
    private String userName;
    private Long departmentId;
    private String departmentName;
    private LocalDate joiningDate;
    private Integer leaveBalance;
}