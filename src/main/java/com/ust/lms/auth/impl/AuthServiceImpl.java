package com.ust.lms.auth.impl;

import com.ust.lms.auth.AuthService;
import com.ust.lms.common.CommonService;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.AuthResponseDto;
import com.ust.lms.dto.LoginRequestDto;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
/**
 * Service implementation for user registration and authentication.
 */
@Service
@Slf4j
public class AuthServiceImpl extends CommonService implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    /**
     * Registers a new user after validating that the email is not already registered.
     *
     * @param dto user registration details
     * @return registered user's response details
     * @throws BadRequestException if the email is already registered
     */
    @Override
    public UserResponseDto register(UserRequestDto dto) {

        log.info("Registering user with email: {}", dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        User user = modelMapper.map(dto, User.class);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        setAuditFields(user, true);
        User saved = userRepository.save(user);

        log.info("User registered successfully with id: {}", saved.getId());

        return modelMapper.map(saved, UserResponseDto.class);
    }
    /**
     * Authenticates a user and generates a JWT token upon successful authentication.
     *
     * @param dto login credentials
     * @return authentication response containing the generated JWT token
     * @throws ResourceNotFoundException if the authenticated user cannot be found
     */
    @Override
    public AuthResponseDto login(LoginRequestDto dto) {

        log.info("Login attempt for email: {}", dto.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        log.info("User logged in successfully with email: {}", user.getEmail());

        return new AuthResponseDto(token, user.getEmail(), user.getRole());
    }
}