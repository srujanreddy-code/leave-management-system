package com.ust.lms.user;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponseDto create(UserRequestDto dto);
    UserResponseDto getById(Long id);
    PageResponseDto<UserResponseDto> getAll(Pageable pageable);
    UserResponseDto update(Long id, UserRequestDto dto);
    void delete(Long id);
    Long getIdByEmail(String email);
}