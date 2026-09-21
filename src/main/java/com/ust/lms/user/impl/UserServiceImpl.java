package com.ust.lms.user.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.common.exception.UserNotFoundException;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.user.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementation for managing users, including creation, retrieval,
 * updating, and deletion operations.
 */
@Service
@Slf4j
public class UserServiceImpl extends CommonService implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user and securely encodes the user's password.
     *
     * @param dto user details
     * @return response containing the created user
     */
    @Override
    public UserResponseDto create(UserRequestDto dto) {
        log.info("Creating user with email: {}", dto.getEmail());

        User user = modelMapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        setAuditFields(user, true);
        User saved = userRepository.save(user);

        log.info("User created successfully with id: {}", saved.getId());

        return modelMapper.map(saved, UserResponseDto.class);
    }

    /**
     * Retrieves a user by its ID.
     *
     * @param id user ID
     * @return response containing the requested user
     * @throws UserNotFoundException if the user does not exist or has been deleted
     */
    @Override
    public UserResponseDto getById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return modelMapper.map(user, UserResponseDto.class);
    }

    /**
     * Retrieves all active users using pagination.
     *
     * @param pageable pagination and sorting information
     * @return paginated response containing users
     */
    @Override
    public PageResponseDto<UserResponseDto> getAll(Pageable pageable) {
        Page<User> result = userRepository.findByDeletedFalse(pageable);
        return new PageResponseDto<>(
                result.getContent().stream().map(u -> modelMapper.map(u, UserResponseDto.class)).toList(),
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * Updates an existing user and securely encodes a new password when provided.
     *
     * @param id user ID
     * @param dto updated user details
     * @return response containing the updated user
     * @throws UserNotFoundException if the user does not exist or has been deleted
     * @throws BadRequestException if the email is already in use
     */
    @Override
    public UserResponseDto update(Long id, UserRequestDto dto) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                throw new BadRequestException("Email already in use");
            });
        }

        String rawPassword = dto.getPassword();
        dto.setPassword(null);
        modelMapper.map(dto, user);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        setAuditFields(user, false);
        User updated = userRepository.save(user);
        log.info("User updated successfully with id: {}", id);
        return modelMapper.map(updated, UserResponseDto.class);
    }

    /**
     * Soft deletes an existing user.
     *
     * @param id user ID
     * @throws UserNotFoundException if the user does not exist or has already been deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        softDelete(user);
        setAuditFields(user, false);
        userRepository.save(user);
        log.info("User deleted successfully with id: {}", id);
    }

    /**
     * Retrieves a user's ID using their email address.
     *
     * @param email user's email address
     * @return ID of the user
     * @throws UserNotFoundException if no user is found with the specified email
     */
    @Override
    public Long getIdByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getId();
    }
}