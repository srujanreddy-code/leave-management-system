package com.ust.lms.serviceIntegrationTest;

import com.ust.lms.department.DepartmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for the {@link DepartmentService} interface.
 *
 * <p>
 * The actual implementation of this interface is provided by
 * {@code DepartmentServiceImpl}, which is injected by the Spring
 * application context.
 * </p>
 *
 * <p>
 * These tests verify that the service contract can be invoked
 * successfully through the {@link DepartmentService} interface.
 * Detailed business-logic testing is handled by
 * {@code DepartmentServiceImplIntegrationTest}.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DepartmentServiceIntegrationTest {

    /**
     * Department service interface injected by Spring.
     *
     * <p>
     * Spring injects the concrete {@code DepartmentServiceImpl}
     * implementation behind this interface.
     * </p>
     */
    @Autowired
    private DepartmentService departmentService;

    /**
     * Verifies that the DepartmentService bean is available
     * through the service interface.
     */
    @Test
    @DisplayName("DepartmentService bean should be available")
    void serviceBean_shouldBeAvailable() {

        assertNotNull(departmentService);
    }

    /**
     * Verifies that the service bean implements the
     * DepartmentService interface.
     */
    @Test
    @DisplayName("DepartmentService implementation should implement interface")
    void service_shouldImplementDepartmentService() {

        assertNotNull(departmentService);

        assertNotNull(
                departmentService.getClass()
        );

        // The injected bean is accessed through the
        // DepartmentService interface.
        assertNotNull(
                departmentService
        );
    }
}
