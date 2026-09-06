package com.ust.lms.auth;

import com.ust.lms.dto.AuthResponseDto;
import com.ust.lms.dto.LoginRequestDto;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;

public interface AuthService {
    UserResponseDto register(UserRequestDto dto);
    AuthResponseDto login(LoginRequestDto dto);
}