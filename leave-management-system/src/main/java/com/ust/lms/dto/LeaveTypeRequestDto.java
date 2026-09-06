package com.ust.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveTypeRequestDto {
    @NotBlank
    private String name;

    @NotNull
    private Integer maxDays;
}