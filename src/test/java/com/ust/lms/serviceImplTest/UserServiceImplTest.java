package com.ust.lms.serviceImplTest;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.UserNotFoundException;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.user.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * <p>Verifies user creation, retrieval, pagination, updating,
 * soft deletion, password encoding, and email lookup behavior.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    /**
     * Creates common test data before each test.
     */
    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(false);
        user.setStatus(true);

        requestDto = new UserRequestDto();
        requestDto.setName("John Doe");
        requestDto.setEmail("john@example.com");
        requestDto.setPassword("password");
        requestDto.setRole(Role.EMPLOYEE);

        responseDto = new UserResponseDto();
        responseDto.setId(1L);
        responseDto.setName("John Doe");
        responseDto.setEmail("john@example.com");
        responseDto.setRole(Role.EMPLOYEE);
    }

    /**
     * Verifies that a user is created successfully and
     * the password is encoded before persistence.
     */
    @Test
    @DisplayName("Create user successfully")
    void create_success() {

        when(modelMapper.map(requestDto, User.class))
                .thenReturn(user);

        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.create(requestDto);

        assertNotNull(result);
        assertEquals(responseDto, result);
        assertEquals("encodedPassword", user.getPassword());

        verify(modelMapper)
                .map(requestDto, User.class);

        verify(passwordEncoder)
                .encode("password");

        verify(userRepository)
                .save(user);

        verify(modelMapper)
                .map(user, UserResponseDto.class);
    }

    /**
     * Verifies that an active user is retrieved successfully by ID.
     */
    @Test
    @DisplayName("Get user by ID successfully")
    void getById_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.getById(1L);

        assertNotNull(result);
        assertEquals(responseDto, result);

        verify(userRepository)
                .findById(1L);

        verify(modelMapper)
                .map(user, UserResponseDto.class);
    }

    /**
     * Verifies that UserNotFoundException is thrown when
     * the requested user does not exist.
     */
    @Test
    @DisplayName("Get user by ID - user not found")
    void getById_notFound_throwsUserNotFound() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(999L)
        );

        verify(userRepository)
                .findById(999L);

        verifyNoInteractions(modelMapper);
    }

    /**
     * Verifies that a soft-deleted user cannot be retrieved.
     */
    @Test
    @DisplayName("Get user by ID - soft deleted user")
    void getById_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(1L)
        );

        verify(userRepository)
                .findById(1L);

        verifyNoInteractions(modelMapper);
    }

    /**
     * Verifies that active users are retrieved with pagination.
     */
    @Test
    @DisplayName("Get all users successfully")
    void getAll_success() {

        Pageable pageable = PageRequest.of(0, 10);

        PageImpl<User> page =
                new PageImpl<>(
                        List.of(user),
                        pageable,
                        1
                );

        when(userRepository.findByDeletedFalse(pageable))
                .thenReturn(page);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        PageResponseDto<UserResponseDto> result =
                userService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(responseDto, result.getContent().get(0));

        verify(userRepository)
                .findByDeletedFalse(pageable);

        verify(modelMapper)
                .map(user, UserResponseDto.class);
    }

    /**
     * Verifies that a user is updated successfully when the email
     * remains unchanged.
     */
    @Test
    @DisplayName("Update user successfully with same email")
    void update_sameEmail_success() {

        requestDto.setEmail("john@example.com");
        requestDto.setPassword("newPassword");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        doAnswer(invocation -> {
            UserRequestDto source =
                    invocation.getArgument(0);

            User target =
                    invocation.getArgument(1);

            target.setName(source.getName());
            target.setEmail(source.getEmail());
            target.setRole(source.getRole());

            return null;
        }).when(modelMapper).map(requestDto, user);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncodedPassword");

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.update(1L, requestDto);

        assertNotNull(result);

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .findByEmail(anyString());

        verify(modelMapper)
                .map(requestDto, user);

        verify(passwordEncoder)
                .encode("newPassword");

        verify(userRepository)
                .save(user);

        verify(modelMapper)
                .map(user, UserResponseDto.class);

        assertEquals(
                "newEncodedPassword",
                user.getPassword()
        );
    }

    /**
     * Verifies that a user's email can be changed when
     * the new email is not already in use.
     */
    @Test
    @DisplayName("Update user successfully with different email")
    void update_differentEmail_success() {

        requestDto.setEmail("newjohn@example.com");
        requestDto.setPassword(null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("newjohn@example.com"))
                .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            UserRequestDto source =
                    invocation.getArgument(0);

            User target =
                    invocation.getArgument(1);

            target.setName(source.getName());
            target.setEmail(source.getEmail());
            target.setRole(source.getRole());

            return null;
        }).when(modelMapper).map(requestDto, user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        UserResponseDto result =
                userService.update(1L, requestDto);

        assertNotNull(result);

        verify(userRepository)
                .findByEmail("newjohn@example.com");

        verify(modelMapper)
                .map(requestDto, user);

        verify(userRepository)
                .save(user);

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    /**
     * Verifies that updating a user with an email already in use
     * throws BadRequestException.
     */
    @Test
    @DisplayName("Update user - duplicate email")
    void update_duplicateEmail_throwsBadRequest() {

        requestDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                BadRequestException.class,
                () -> userService.update(1L, requestDto)
        );

        verify(userRepository)
                .findByEmail("existing@example.com");

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(modelMapper);
    }

    /**
     * Verifies that a new password is not encoded when
     * the supplied password is null.
     */
    @Test
    @DisplayName("Update user - null password keeps existing password")
    void update_nullPassword_keepsExistingPassword() {

        requestDto.setPassword(null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        doAnswer(invocation -> null)
                .when(modelMapper)
                .map(requestDto, user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        String originalPassword = user.getPassword();

        userService.update(1L, requestDto);

        assertEquals(
                originalPassword,
                user.getPassword()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository)
                .save(user);
    }

    /**
     * Verifies that a blank password is not encoded.
     */
    @Test
    @DisplayName("Update user - blank password keeps existing password")
    void update_blankPassword_keepsExistingPassword() {

        requestDto.setPassword("   ");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        doAnswer(invocation -> null)
                .when(modelMapper)
                .map(requestDto, user);

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(user, UserResponseDto.class))
                .thenReturn(responseDto);

        String originalPassword = user.getPassword();

        userService.update(1L, requestDto);

        assertEquals(
                originalPassword,
                user.getPassword()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository)
                .save(user);
    }

    /**
     * Verifies that updating a non-existent user throws
     * UserNotFoundException.
     */
    @Test
    @DisplayName("Update user - user not found")
    void update_notFound_throwsUserNotFound() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(999L, requestDto)
        );

        verify(userRepository)
                .findById(999L);

        verify(userRepository, never())
                .save(any(User.class));
    }

    /**
     * Verifies that a soft-deleted user cannot be updated.
     */
    @Test
    @DisplayName("Update user - soft deleted user")
    void update_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(1L, requestDto)
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .save(any(User.class));
    }

    /**
     * Verifies that a user is soft deleted successfully.
     */
    @Test
    @DisplayName("Delete user successfully")
    void delete_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        userService.delete(1L);

        assertTrue(user.isDeleted());
        assertFalse(user.isStatus());

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .save(user);
    }

    /**
     * Verifies that deleting a non-existent user throws
     * UserNotFoundException.
     */
    @Test
    @DisplayName("Delete user - user not found")
    void delete_notFound_throwsUserNotFound() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.delete(999L)
        );

        verify(userRepository)
                .findById(999L);

        verify(userRepository, never())
                .save(any(User.class));
    }

    /**
     * Verifies that a soft-deleted user cannot be deleted again.
     */
    @Test
    @DisplayName("Delete user - already deleted")
    void delete_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserNotFoundException.class,
                () -> userService.delete(1L)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    /**
     * Verifies that a user's ID is retrieved successfully by email.
     */
    @Test
    @DisplayName("Get user ID by email successfully")
    void getIdByEmail_success() {

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        Long result =
                userService.getIdByEmail("john@example.com");

        assertEquals(1L, result);

        verify(userRepository)
                .findByEmail("john@example.com");
    }

    /**
     * Verifies that UserNotFoundException is thrown when
     * no user exists for the supplied email.
     */
    @Test
    @DisplayName("Get user ID by email - user not found")
    void getIdByEmail_notFound_throwsUserNotFound() {

        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getIdByEmail("missing@example.com")
        );

        verify(userRepository)
                .findByEmail("missing@example.com");
    }
}