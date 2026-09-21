package com.ust.lms.serviceIntegrationTest;

import com.ust.lms.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for the UserService interface.
 *
 * <p>
 * The actual implementation of this interface is provided by
 * {@code UserServiceImpl}, which is injected by the Spring
 * application context.
 * </p>
 *
 * <p>
 * These tests verify that the UserService bean is available
 * through the UserService interface. Detailed business-logic
 * testing is handled by UserServiceImplIntegrationTest.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    /**
     * User service interface injected by Spring.
     *
     * <p>
     * Spring injects the concrete {@code UserServiceImpl}
     * implementation behind this interface.
     * </p>
     */
    @Autowired
    private UserService userService;

    /**
     * Verifies that the UserService bean is available
     * through the service interface.
     */
    @Test
    @DisplayName("UserService bean should be available")
    void serviceBean_shouldBeAvailable() {

        assertNotNull(userService);
    }

    /**
     * Verifies that the injected UserService bean is available
     * as an implementation of the UserService interface.
     */
    @Test
    @DisplayName("UserService implementation should implement interface")
    void service_shouldImplementUserService() {

        assertNotNull(userService);

        assertNotNull(
                userService.getClass()
        );

        assertNotNull(
                userService
        );
    }
}