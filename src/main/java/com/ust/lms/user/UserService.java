package com.ust.lms.user;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for managing users.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param dto user details
     * @return response containing the created user
     */
    UserResponseDto create(UserRequestDto dto);

    /**
     * Retrieves a user by its ID.
     *
     * @param id user ID
     * @return response containing the requested user
     */
    UserResponseDto getById(Long id);

    /**
     * Retrieves all active users using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing users
     */
    PageResponseDto<UserResponseDto> getAll(Pageable pageable);

    /**
     * Updates an existing user.
     *
     * @param id user ID
     * @param dto updated user details
     * @return response containing the updated user
     */
    UserResponseDto update(Long id, UserRequestDto dto);

    /**
     * Soft deletes an existing user.
     *
     * @param id user ID
     */
    void delete(Long id);

    /**
     * Retrieves a user's ID using their email address.
     *
     * @param email user's email address
     * @return ID of the user
     */
    Long getIdByEmail(String email);
}