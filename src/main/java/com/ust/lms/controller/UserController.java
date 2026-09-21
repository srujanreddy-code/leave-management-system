package com.ust.lms.controller;

import com.ust.lms.common.PageResponseDto;
import com.ust.lms.common.PaginationConstants;
import com.ust.lms.dto.UserRequestDto;
import com.ust.lms.dto.UserResponseDto;
import com.ust.lms.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles REST API requests for user management.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all active users with pagination and sorting.
     *
     * @param page          page number, starting from 1
     * @param limit         number of records per page
     * @param sortDirection sorting direction
     * @param sort          fields to sort by
     * @return paginated list of users
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAll(
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_LIMIT) int limit,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDirection,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_FIELD) String... sort) {

        int zeroBasedPage = Math.max(page - 1, 0);
        Pageable pageable = getPageable(zeroBasedPage, limit, sortDirection, sort);
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id user ID
     * @return response containing the requested user
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /**
     * Updates an existing user.
     *
     * @param id  user ID
     * @param dto updated user details
     * @return response containing the updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id, @Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    /**
     * Soft deletes a user by ID.
     *
     * @param id user ID
     * @return response with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}