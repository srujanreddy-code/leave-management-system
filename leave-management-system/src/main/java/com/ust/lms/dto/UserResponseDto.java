package com.ust.lms.dto;

import com.ust.lms.common.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDto extends CommonResponseDto {
    private String name;
    private String email;
    private Role role;
}