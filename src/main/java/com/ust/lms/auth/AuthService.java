package com.ust.lms.auth;

import com.ust.lms.dto.AuthResponseDto;
import com.ust.lms.dto.LoginRequestDto;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;

/**
 * Defines operations for user registration and authentication.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param dto user registration details
     * @return registered user's response details
     */
    UserResponseDto register(UserRequestDto dto);

    /**
     * Authenticates a user and generates an authentication token.
     *
     * @param dto user login credentials
     * @return authentication response containing the generated token
     */
    AuthResponseDto login(LoginRequestDto dto);
}