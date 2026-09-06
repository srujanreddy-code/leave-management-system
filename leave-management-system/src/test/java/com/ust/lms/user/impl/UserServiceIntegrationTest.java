package com.ust.lms.user.impl;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.common.exception.BadRequestException;
import com.ust.lms.common.exception.ResourceNotFoundException;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.model.User;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldCreateSuccessfully() {
        UserRequestDto request = new UserRequestDto();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(Role.EMPLOYEE);

        UserResponseDto response = userService.create(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.EMPLOYEE, response.getRole());

        assertEquals(1, userRepository.count());

        User savedUser = userRepository.findById(response.getId()).orElseThrow();

        assertNotEquals("password123", savedUser.getPassword());
        assertTrue(
                passwordEncoder.matches(
                        "password123",
                        savedUser.getPassword()
                )
        );
    }

    @Test
    void getById_shouldReturnUser() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        UserResponseDto response = userService.getById(saved.getId());

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.EMPLOYEE, response.getRole());
    }

    @Test
    void getById_shouldThrowExceptionWhenUserDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getById(999999L)
        );
    }

    @Test
    void getById_shouldThrowExceptionForDeletedUser() {
        User user = new User();
        user.setName("Deleted User");
        user.setEmail("deleted@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(true);

        User saved = userRepository.save(user);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getById(saved.getId())
        );
    }

    @Test
    void getAll_shouldReturnOnlyNonDeletedUsers() {
        User user1 = new User();
        user1.setName("John");
        user1.setEmail("john@example.com");
        user1.setPassword("password");
        user1.setRole(Role.EMPLOYEE);

        User user2 = new User();
        user2.setName("Jane");
        user2.setEmail("jane@example.com");
        user2.setPassword("password");
        user2.setRole(Role.MANAGER);

        User deletedUser = new User();
        deletedUser.setName("Deleted");
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setPassword("password");
        deletedUser.setRole(Role.EMPLOYEE);
        deletedUser.setDeleted(true);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(deletedUser);

        Pageable pageable = PageRequest.of(0, 10);

        PageResponseDto<UserResponseDto> response =
                userService.getAll(pageable);

        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(2, response.getContent().size());
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword(
                passwordEncoder.encode("oldPassword")
        );
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        UserRequestDto request = new UserRequestDto();
        request.setName("John Updated");
        request.setEmail("john.updated@example.com");
        request.setPassword("newPassword123");
        request.setRole(Role.MANAGER);

        UserResponseDto response =
                userService.update(saved.getId(), request);

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        assertEquals("John Updated", response.getName());
        assertEquals("john.updated@example.com", response.getEmail());
        assertEquals(Role.MANAGER, response.getRole());

        User updated =
                userRepository.findById(saved.getId()).orElseThrow();

        assertEquals("John Updated", updated.getName());
        assertEquals("john.updated@example.com", updated.getEmail());

        assertTrue(
                passwordEncoder.matches(
                        "newPassword123",
                        updated.getPassword()
                )
        );
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserDoesNotExist() {
        UserRequestDto request = new UserRequestDto();
        request.setName("John");
        request.setEmail("john@example.com");
        request.setPassword("password");
        request.setRole(Role.EMPLOYEE);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.update(999999L, request)
        );
    }

    @Test
    void updateUser_shouldRejectDuplicateEmail() {
        User user1 = new User();
        user1.setName("John");
        user1.setEmail("john@example.com");
        user1.setPassword("password");
        user1.setRole(Role.EMPLOYEE);

        User user2 = new User();
        user2.setName("Jane");
        user2.setEmail("jane@example.com");
        user2.setPassword("password");
        user2.setRole(Role.EMPLOYEE);

        User savedUser1 = userRepository.save(user1);
        userRepository.save(user2);

        UserRequestDto request = new UserRequestDto();
        request.setName("John Updated");
        request.setEmail("jane@example.com");
        request.setPassword("password");
        request.setRole(Role.EMPLOYEE);

        assertThrows(
                BadRequestException.class,
                () -> userService.update(savedUser1.getId(), request)
        );
    }

    @Test
    void updateUser_shouldAllowSameEmail() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        UserRequestDto request = new UserRequestDto();
        request.setName("John Updated");
        request.setEmail("john@example.com");
        request.setPassword("newPassword");
        request.setRole(Role.EMPLOYEE);

        UserResponseDto response =
                userService.update(saved.getId(), request);

        assertNotNull(response);
        assertEquals("John Updated", response.getName());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    void updateUser_shouldNotChangePasswordWhenPasswordIsBlank() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword(
                passwordEncoder.encode("oldPassword")
        );
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);
        String originalPassword = saved.getPassword();

        UserRequestDto request = new UserRequestDto();
        request.setName("John Updated");
        request.setEmail("john@example.com");
        request.setPassword("   ");
        request.setRole(Role.EMPLOYEE);

        userService.update(saved.getId(), request);

        User updated =
                userRepository.findById(saved.getId()).orElseThrow();

        assertEquals(originalPassword, updated.getPassword());
    }

    @Test
    void deleteUser_shouldSoftDeleteSuccessfully() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        userService.delete(saved.getId());

        User deleted =
                userRepository.findById(saved.getId()).orElseThrow();

        assertTrue(deleted.isDeleted());
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenUserDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.delete(999999L)
        );
    }

    @Test
    void deleteUser_shouldThrowExceptionWhenAlreadyDeleted() {
        User user = new User();
        user.setName("Deleted User");
        user.setEmail("deleted@example.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(true);

        User saved = userRepository.save(user);

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.delete(saved.getId())
        );
    }

    @Test
    void getIdByEmail_shouldReturnUserId() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);

        User saved = userRepository.save(user);

        Long userId =
                userService.getIdByEmail("john@example.com");

        assertEquals(saved.getId(), userId);
    }

    @Test
    void getIdByEmail_shouldThrowExceptionWhenEmailDoesNotExist() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getIdByEmail("notfound@example.com")
        );
    }
}