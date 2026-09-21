package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the response data returned for a leave type.
 */
@Getter
@Setter
public class LeaveTypeResponseDto extends CommonResponseDto {
    private String name;
    private Integer maxDays;
}