package com.ust.lms.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommonResponseDto {
    private Long id;
    private LocalDateTime createdOn;
}