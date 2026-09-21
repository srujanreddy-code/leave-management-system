package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents common response fields shared across response DTOs.
 */
@Getter
@Setter
public class CommonResponseDto {
    private Long id;
    private LocalDateTime createdOn;
}