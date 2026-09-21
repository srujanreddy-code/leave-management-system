package com.ust.lms.serviceTest;

import com.ust.lms.leavetype.LeaveTypeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * JUnit unit test for the LeaveTypeService interface contract.
 *
 * <p>
 * This test verifies that the LeaveTypeService interface can be used
 * as a service contract independently of the Spring application context.
 * </p>
 */
class LeaveTypeServiceTest {

    /**
     * Verifies that a LeaveTypeService mock can be created
     * successfully from the LeaveTypeService interface.
     */
    @Test
    void shouldCreateLeaveTypeServiceMock() {

        LeaveTypeService leaveTypeService =
                mock(LeaveTypeService.class);

        assertNotNull(leaveTypeService);

        assertTrue(
                leaveTypeService instanceof LeaveTypeService,
                "Mock should implement LeaveTypeService"
        );
    }
}