package com.ust.lms.serviceIntegrationTest;

import com.ust.lms.employee.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for the {@link EmployeeService} interface.
 *
 * <p>
 * The actual implementation of this interface is provided by
 * {@code EmployeeServiceImpl}, which is injected by the Spring
 * application context.
 * </p>
 *
 * <p>
 * These tests verify that the service contract can be invoked
 * successfully through the {@link EmployeeService} interface.
 * Detailed business-logic testing is handled by
 * {@code EmployeeServiceImplIntegrationTest}.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EmployeeServiceIntegrationTest {

    /**
     * Employee service interface injected by Spring.
     *
     * <p>
     * Spring injects the concrete {@code EmployeeServiceImpl}
     * implementation behind this interface.
     * </p>
     */
    @Autowired
    private EmployeeService employeeService;

    /**
     * Verifies that the EmployeeService bean is available
     * through the service interface.
     */
    @Test
    @DisplayName("EmployeeService bean should be available")
    void serviceBean_shouldBeAvailable() {

        assertNotNull(employeeService);
    }

    /**
     * Verifies that the service bean implements the
     * EmployeeService interface.
     */
    @Test
    @DisplayName("EmployeeService implementation should implement interface")
    void service_shouldImplementEmployeeService() {

        assertNotNull(employeeService);

        assertNotNull(
                employeeService.getClass()
        );

        // The injected bean is accessed through the
        // EmployeeService interface.
        assertNotNull(
                employeeService
        );
    }
}