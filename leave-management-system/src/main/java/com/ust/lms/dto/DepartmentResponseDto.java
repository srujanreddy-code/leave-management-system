package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentResponseDto extends CommonResponseDto {
    private String name;
    private String description;
}