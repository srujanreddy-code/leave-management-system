package com.ust.lms.serviceImplIntegrationTest;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserServiceImpl}.
 *
 * <p>Verifies user service behavior using the actual Spring context,
 * repositories, password encoder, ModelMapper, and test database.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    /**
     * Creates common test user data before each test.
     */
    @BeforeEach
    void setUp() {

        user = new User();
        user.setName("John Doe");
        user.setEmail(
                "john" + System.nanoTime() + "@example.com"
        );
        user.setPassword(
                passwordEncoder.encode("password")
        );
        user.setRole(Role.EMPLOYEE);
        user.setDeleted(false);
        user.setStatus(true);

        user = userRepository.save(user);
    }

    /**
     * Verifies that a user is created successfully and
     * the password is stored in encoded form.
     */
    @Test
    @DisplayName("Create user successfully")
    void create_success() {

        String rawPassword = "newPassword";

        UserRequestDto dto = new UserRequestDto();
        dto.setName("New User");
        dto.setEmail(
                "newuser" + System.nanoTime() + "@example.com"
        );
        dto.setPassword(rawPassword);
        dto.setRole(Role.EMPLOYEE);

        UserResponseDto result =
                userService.create(dto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New User", result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(Role.EMPLOYEE, result.getRole());

        User savedUser =
                userRepository.findById(result.getId())
                        .orElseThrow();

        assertNotEquals(
                rawPassword,
                savedUser.getPassword()
        );

        assertTrue(
                passwordEncoder.matches(
                        rawPassword,
                        savedUser.getPassword()
                )
        );

        assertFalse(savedUser.isDeleted());
        assertTrue(savedUser.isStatus());
    }

    /**
     * Verifies that an active user is retrieved successfully.
     */
    @Test
    @DisplayName("Get user by ID successfully")
    void getById_success() {

        UserResponseDto result =
                userService.getById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole(), result.getRole());
    }

    /**
     * Verifies that UserNotFoundException is thrown for
     * a non-existent user.
     */
    @Test
    @DisplayName("Get user by ID - user not found")
    void getById_notFound_throwsUserNotFound() {

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(999999L)
        );
    }

    /**
     * Verifies that a soft-deleted user cannot be retrieved.
     */
    @Test
    @DisplayName("Get user by ID - soft deleted")
    void getById_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);
        userRepository.save(user);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(user.getId())
        );
    }

    /**
     * Verifies that active users are returned with pagination.
     */
    @Test
    @DisplayName("Get all users successfully")
    void getAll_success() {

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail(
                "second" + System.nanoTime() + "@example.com"
        );
        secondUser.setPassword(
                passwordEncoder.encode("password")
        );
        secondUser.setRole(Role.MANAGER);
        secondUser.setDeleted(false);
        secondUser.setStatus(true);

        userRepository.save(secondUser);

        Pageable pageable =
                PageRequest.of(0, 10);

        PageResponseDto<UserResponseDto> result =
                userService.getAll(pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertEquals(1, result.getCurrentPage());
        assertEquals(10, result.getPageSize());

        assertTrue(
                result.getContent()
                        .stream()
                        .anyMatch(dto ->
                                dto.getId().equals(user.getId()))
        );
    }

    /**
     * Verifies that a user's details are updated successfully
     * and a new password is encoded.
     */
    @Test
    @DisplayName("Update user successfully")
    void update_success() {

        String newPassword = "newPassword";

        UserRequestDto dto = new UserRequestDto();
        dto.setName("Updated Name");
        dto.setEmail(
                "updated" + System.nanoTime() + "@example.com"
        );
        dto.setPassword(newPassword);
        dto.setRole(Role.MANAGER);

        UserResponseDto result =
                userService.update(user.getId(), dto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(Role.MANAGER, result.getRole());

        User updatedUser =
                userRepository.findById(user.getId())
                        .orElseThrow();

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(dto.getEmail(), updatedUser.getEmail());
        assertEquals(Role.MANAGER, updatedUser.getRole());

        assertTrue(
                passwordEncoder.matches(
                        newPassword,
                        updatedUser.getPassword()
                )
        );
    }

    /**
     * Verifies that updating a user with an email already
     * assigned to another user throws BadRequestException.
     */
    @Test
    @DisplayName("Update user - duplicate email")
    void update_duplicateEmail_throwsBadRequest() {

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail(
                "second" + System.nanoTime() + "@example.com"
        );
        secondUser.setPassword(
                passwordEncoder.encode("password")
        );
        secondUser.setRole(Role.EMPLOYEE);
        secondUser.setDeleted(false);
        secondUser.setStatus(true);

        secondUser = userRepository.save(secondUser);

        UserRequestDto dto = new UserRequestDto();
        dto.setName("Updated Name");
        dto.setEmail(secondUser.getEmail());
        dto.setPassword("password");
        dto.setRole(Role.EMPLOYEE);

        assertThrows(
                BadRequestException.class,
                () -> userService.update(user.getId(), dto)
        );
    }

    /**
     * Verifies that a user can be updated without changing
     * the existing password.
     */
    @Test
    @DisplayName("Update user - null password")
    void update_nullPassword_keepsPassword() {

        String originalPassword =
                user.getPassword();

        UserRequestDto dto = new UserRequestDto();
        dto.setName("Updated Name");
        dto.setEmail(user.getEmail());
        dto.setPassword(null);
        dto.setRole(Role.MANAGER);

        UserResponseDto result =
                userService.update(user.getId(), dto);

        assertNotNull(result);

        User updatedUser =
                userRepository.findById(user.getId())
                        .orElseThrow();

        assertEquals(
                originalPassword,
                updatedUser.getPassword()
        );

        assertEquals(
                "Updated Name",
                updatedUser.getName()
        );

        assertEquals(
                Role.MANAGER,
                updatedUser.getRole()
        );
    }

    /**
     * Verifies that a non-existent user cannot be updated.
     */
    @Test
    @DisplayName("Update user - user not found")
    void update_notFound_throwsUserNotFound() {

        UserRequestDto dto = new UserRequestDto();
        dto.setName("Updated");
        dto.setEmail("updated@example.com");
        dto.setPassword("password");
        dto.setRole(Role.EMPLOYEE);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(999999L, dto)
        );
    }

    /**
     * Verifies that a soft-deleted user cannot be updated.
     */
    @Test
    @DisplayName("Update user - soft deleted")
    void update_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);
        userRepository.save(user);

        UserRequestDto dto = new UserRequestDto();
        dto.setName("Updated");
        dto.setEmail(user.getEmail());
        dto.setPassword("password");
        dto.setRole(Role.EMPLOYEE);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.update(user.getId(), dto)
        );
    }

    /**
     * Verifies that a user is soft deleted successfully.
     */
    @Test
    @DisplayName("Delete user successfully")
    void delete_success() {

        userService.delete(user.getId());

        User deletedUser =
                userRepository.findById(user.getId())
                        .orElseThrow();

        assertTrue(deletedUser.isDeleted());
        assertFalse(deletedUser.isStatus());
    }

    /**
     * Verifies that deleting a non-existent user throws
     * UserNotFoundException.
     */
    @Test
    @DisplayName("Delete user - user not found")
    void delete_notFound_throwsUserNotFound() {

        assertThrows(
                UserNotFoundException.class,
                () -> userService.delete(999999L)
        );
    }

    /**
     * Verifies that an already deleted user cannot be deleted again.
     */
    @Test
    @DisplayName("Delete user - soft deleted")
    void delete_softDeleted_throwsUserNotFound() {

        user.setDeleted(true);
        userRepository.save(user);

        assertThrows(
                UserNotFoundException.class,
                () -> userService.delete(user.getId())
        );
    }

    /**
     * Verifies that a user's ID is retrieved successfully
     * using their email.
     */
    @Test
    @DisplayName("Get user ID by email successfully")
    void getIdByEmail_success() {

        Long result =
                userService.getIdByEmail(user.getEmail());

        assertEquals(user.getId(), result);
    }

    /**
     * Verifies that UserNotFoundException is thrown when
     * no user exists for the specified email.
     */
    @Test
    @DisplayName("Get user ID by email - user not found")
    void getIdByEmail_notFound_throwsUserNotFound() {

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getIdByEmail(
                        "missing" + System.nanoTime() + "@example.com"
                )
        );
    }
}