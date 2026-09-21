package com.ust.lms.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.Role;
import com.ust.lms.controller.UserController;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link UserController}.
 *
 * <p>Verifies controller behavior using MockMvc and a mocked
 * UserService without starting the full Spring application context.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    /**
     * Configures MockMvc and common test data before each test.
     */
    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

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
     * Verifies that GET all users returns a paginated response.
     */
    @Test
    @DisplayName("GET /api/users - get all users")
    void getAll_success() throws Exception {

        PageResponseDto<UserResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        1,
                        10,
                        1,
                        1
                );

        when(userService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentPage")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.pageSize")
                                .value(10)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.totalPages")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.content[0].id")
                                .value(1)
                );

        verify(userService)
                .getAll(any(Pageable.class));
    }

    /**
     * Verifies that page=1 is converted to Spring's zero-based
     * page number 0.
     */
    @Test
    @DisplayName("GET /api/users - pagination parameters")
    void getAll_pagination_success() throws Exception {

        PageResponseDto<UserResponseDto> pageResponse =
                new PageResponseDto<>(
                        List.of(responseDto),
                        1,
                        1,
                        2,
                        2
                );

        when(userService.getAll(any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(
                        get("/api/users")
                                .param("page", "1")
                                .param("limit", "1")
                                .param("sortDirection", "asc")
                                .param("sort", "name")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.currentPage")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.pageSize")
                                .value(1)
                );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(userService)
                .getAll(pageableCaptor.capture());

        Pageable pageable =
                pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
        assertEquals(
                "name",
                pageable.getSort()
                        .iterator()
                        .next()
                        .getProperty()
        );
    }

    /**
     * Verifies that GET by ID returns the requested user.
     */
    @Test
    @DisplayName("GET /api/users/{id} - get user")
    void getById_success() throws Exception {

        when(userService.getById(1L))
                .thenReturn(responseDto);

        mockMvc.perform(
                        get("/api/users/{id}", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("John Doe")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("john@example.com")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("EMPLOYEE")
                );

        verify(userService)
                .getById(1L);
    }

    /**
     * Verifies that PUT updates a user successfully.
     */
    @Test
    @DisplayName("PUT /api/users/{id} - update user")
    void update_success() throws Exception {

        when(userService.update(
                eq(1L),
                any(UserRequestDto.class)
        )).thenReturn(responseDto);

        mockMvc.perform(
                        put("/api/users/{id}", 1L)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                requestDto
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("John Doe")
                );

        verify(userService)
                .update(
                        eq(1L),
                        any(UserRequestDto.class)
                );
    }

    /**
     * Verifies that DELETE soft deletes a user and returns
     * HTTP 204 No Content.
     */
    @Test
    @DisplayName("DELETE /api/users/{id} - delete user")
    void delete_success() throws Exception {

        doNothing()
                .when(userService)
                .delete(1L);

        mockMvc.perform(
                        delete("/api/users/{id}", 1L)
                )
                .andExpect(status().isNoContent());

        verify(userService)
                .delete(1L);
    }
}