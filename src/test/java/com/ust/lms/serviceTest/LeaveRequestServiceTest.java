package com.ust.lms.serviceTest;

import com.ust.lms.leaverequest.LeaveRequestService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * JUnit unit test for the LeaveRequestService interface contract.
 *
 * <p>
 * This test verifies that the LeaveRequestService interface can be used
 * as a service contract independently of the Spring application context.
 * </p>
 */
class LeaveRequestServiceTest {

    /**
     * Verifies that a LeaveRequestService mock can be created
     * successfully from the LeaveRequestService interface.
     */
    @Test
    void shouldCreateLeaveRequestServiceMock() {

        LeaveRequestService leaveRequestService =
                mock(LeaveRequestService.class);

        assertNotNull(leaveRequestService);

        assertTrue(
                leaveRequestService instanceof LeaveRequestService,
                "Mock should implement LeaveRequestService"
        );
    }
}