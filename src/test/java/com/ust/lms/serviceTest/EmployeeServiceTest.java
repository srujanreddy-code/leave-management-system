package com.ust.lms.serviceTest;

import com.ust.lms.employee.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit test for the EmployeeService interface contract.
 *
 * <p>
 * This test verifies that the Spring application context provides a valid
 * implementation of the {@link EmployeeService} interface.
 * </p>
 */
@SpringBootTest
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    /**
     * Verifies that an EmployeeService implementation is available
     * through the EmployeeService interface.
     */
    @Test
    void shouldInjectEmployeeServiceImplementation() {

        assertNotNull(employeeService);

        assertTrue(
                employeeService instanceof EmployeeService,
                "Injected bean should implement EmployeeService"
        );
    }
}
