package com.ust.lms.serviceTest;

import com.ust.lms.user.UserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * JUnit unit test for the UserService interface contract.
 *
 * <p>
 * This test verifies that the UserService interface can be used
 * as a service contract independently of the Spring application context.
 * </p>
 */
class UserServiceTest {

    /**
     * Verifies that a UserService mock can be created successfully
     * from the UserService interface.
     */
    @Test
    void shouldCreateUserServiceMock() {

        UserService userService = mock(UserService.class);

        assertNotNull(userService);

        assertTrue(
                userService instanceof UserService,
                "Mock should implement UserService"
        );
    }
}