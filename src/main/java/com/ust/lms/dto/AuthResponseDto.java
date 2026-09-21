package com.ust.lms.dto;

import com.ust.lms.common.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the response returned after successful user authentication.
 */
@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String email;
    private Role role;
}