package com.ust.lms.user.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alice Smith");
        user.setEmail("alice@example.com");
        user.setPassword("encodedPass");
        user.setDeleted(false);
    }

    @Test
    void create_success() {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Alice Smith");
        dto.setEmail("alice@example.com");
        dto.setPassword("rawPass");

        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDto result = userService.create(dto);

        assertNotNull(result);
        assertEquals("Alice Smith", result.getName());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void getById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
    }

    @Test
    void getAll_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findByDeletedFalse(pageable)).thenReturn(page);

        PageResponseDto<UserResponseDto> result = userService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void update_duplicateEmail_throwsBadRequest() {
        UserRequestDto dto = new UserRequestDto();
        dto.setEmail("taken@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(BadRequestException.class, () -> userService.update(1L, dto));
    }

    @Test
    void update_withNewPassword_encodesAndSaves() {
        UserRequestDto dto = new UserRequestDto();
        dto.setPassword("newRawPass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newRawPass")).thenReturn("newEncodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDto result = userService.update(1L, dto);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("newRawPass");
    }

    @Test
    void delete_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        assertTrue(user.isDeleted());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getIdByEmail_success() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        Long id = userService.getIdByEmail("alice@example.com");

        assertEquals(1L, id);
    }

    @Test
    void getIdByEmail_notFound_throwsResourceNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getIdByEmail("unknown@example.com"));
    }
}