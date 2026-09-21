package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the response data returned for a department.
 */
@Getter
@Setter
public class DepartmentResponseDto extends CommonResponseDto {
    private String name;
    private String description;
}