package com.ust.lms.serviceTest;

import com.ust.lms.department.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test for the DepartmentService interface contract.
 *
 * <p>
 * This test verifies that the Spring application context provides a valid
 * implementation of the {@link DepartmentService} interface.
 * </p>
 */
@SpringBootTest
class DepartmentServiceTest {

    /**
     * Department service injected by Spring.
     */
    @Autowired
    private DepartmentService departmentService;

    /**
     * Verifies that a DepartmentService implementation is available
     * through the DepartmentService interface.
     */
    @Test
    void shouldInjectDepartmentServiceImplementation() {

        assertNotNull(departmentService);

        assertTrue(
                departmentService instanceof DepartmentService,
                "Injected bean should implement DepartmentService"
        );
    }
}
