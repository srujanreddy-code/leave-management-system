package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveTypeResponseDto extends CommonResponseDto {
    private String name;
    private Integer maxDays;
}