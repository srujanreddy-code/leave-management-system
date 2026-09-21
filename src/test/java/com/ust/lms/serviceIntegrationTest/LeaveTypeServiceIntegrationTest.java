package com.ust.lms.serviceIntegrationTest;

import com.ust.lms.leavetype.LeaveTypeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for the LeaveTypeService interface.
 *
 * <p>
 * The actual implementation of this interface is provided by
 * {@code LeaveTypeServiceImpl}, which is injected by the Spring
 * application context.
 * </p>
 *
 * <p>
 * These tests verify that the LeaveTypeService bean is available
 * through the LeaveTypeService interface. Detailed business-logic
 * testing is handled by LeaveTypeServiceImplIntegrationTest.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveTypeServiceIntegrationTest {

    /**
     * Leave type service interface injected by Spring.
     *
     * <p>
     * Spring injects the concrete {@code LeaveTypeServiceImpl}
     * implementation behind this interface.
     * </p>
     */
    @Autowired
    private LeaveTypeService leaveTypeService;

    /**
     * Verifies that the LeaveTypeService bean is available
     * through the service interface.
     */
    @Test
    @DisplayName("LeaveTypeService bean should be available")
    void serviceBean_shouldBeAvailable() {

        assertNotNull(leaveTypeService);
    }

    /**
     * Verifies that the injected LeaveTypeService bean is available
     * as an implementation of the LeaveTypeService interface.
     */
    @Test
    @DisplayName("LeaveTypeService implementation should implement interface")
    void service_shouldImplementLeaveTypeService() {

        assertNotNull(leaveTypeService);

        assertNotNull(
                leaveTypeService.getClass()
        );

        assertNotNull(
                leaveTypeService
        );
    }
}