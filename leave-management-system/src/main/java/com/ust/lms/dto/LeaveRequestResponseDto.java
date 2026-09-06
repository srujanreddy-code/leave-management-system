package com.ust.lms.dto;

import com.ust.lms.common.LeaveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequestResponseDto extends CommonResponseDto {
    private Long employeeId;
    private String employeeName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus leaveStatus;
    private Long approvedById;
    private String approvedByName;
}