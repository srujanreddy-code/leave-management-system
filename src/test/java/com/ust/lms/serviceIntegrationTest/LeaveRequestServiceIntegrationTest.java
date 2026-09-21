package com.ust.lms.serviceIntegrationTest;

import com.ust.lms.leaverequest.LeaveRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for the LeaveRequestService interface.
 *
 * <p>
 * The actual implementation of this interface is provided by
 * {@code LeaveRequestServiceImpl}, which is injected by the Spring
 * application context.
 * </p>
 *
 * <p>
 * These tests verify that the LeaveRequestService bean is available
 * through the LeaveRequestService interface. Detailed business-logic
 * testing is handled by LeaveRequestServiceImplIntegrationTest.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveRequestServiceIntegrationTest {

    /**
     * Leave request service interface injected by Spring.
     *
     * <p>
     * Spring injects the concrete {@code LeaveRequestServiceImpl}
     * implementation behind this interface.
     * </p>
     */
    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * Verifies that the LeaveRequestService bean is available
     * through the service interface.
     */
    @Test
    @DisplayName("LeaveRequestService bean should be available")
    void serviceBean_shouldBeAvailable() {

        assertNotNull(leaveRequestService);
    }

    /**
     * Verifies that the injected LeaveRequestService bean is available
     * as an implementation of the LeaveRequestService interface.
     */
    @Test
    @DisplayName("LeaveRequestService implementation should implement interface")
    void service_shouldImplementLeaveRequestService() {

        assertNotNull(leaveRequestService);

        assertNotNull(
                leaveRequestService.getClass()
        );

        assertNotNull(
                leaveRequestService
        );
    }
}