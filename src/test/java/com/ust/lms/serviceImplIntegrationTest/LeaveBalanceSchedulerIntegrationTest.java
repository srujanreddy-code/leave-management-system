package com.ust.lms.serviceImplIntegrationTest;

import com.ust.lms.employee.impl.LeaveBalanceScheduler;
import com.ust.lms.model.Department;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveType;
import com.ust.lms.model.User;
import com.ust.lms.repository.DepartmentRepository;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import com.ust.lms.repository.UserRepository;
import com.ust.lms.common.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Integration tests for {@link LeaveBalanceScheduler}.
 *
 * <p>
 * This test class verifies the leave balance reset process using the
 * Spring application context and the actual repositories connected to
 * the test database.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaveBalanceSchedulerIntegrationTest {

    @Autowired
    private LeaveBalanceScheduler leaveBalanceScheduler;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Employee employee;
    private LeaveType sickLeave;
    private LeaveType earnedLeave;

    /**
     * Creates the employee, department, user and leave types required
     * for the integration tests.
     */
    @BeforeEach
    void setUp() {

        User user = new User();
        user.setName("Scheduler Test User");
        user.setEmail("scheduler@test.com");
        user.setPassword("password");
        user.setRole(Role.EMPLOYEE);

        user = userRepository.save(user);

        Department department = new Department();
        department.setName("Scheduler Department");
        department.setDescription("Department for scheduler testing");

        department = departmentRepository.save(department);

        employee = new Employee();
        employee.setUser(user);
        employee.setDepartment(department);
        employee.setJoiningDate(LocalDate.of(2025, 1, 1));
        employee.setLeaveBalance(5);

        employee = employeeRepository.save(employee);

        sickLeave = new LeaveType();
        sickLeave.setName("Scheduler Sick Leave");
        sickLeave.setMaxDays(6);

        sickLeave = leaveTypeRepository.save(sickLeave);

        earnedLeave = new LeaveType();
        earnedLeave.setName("Scheduler Earned Leave");
        earnedLeave.setMaxDays(8);

        earnedLeave = leaveTypeRepository.save(earnedLeave);
    }

    /**
     * Verifies that expired leave balances are reset to the maximum
     * days configured for their respective leave types.
     */
    @Test
    void shouldResetExpiredLeaveBalances() {

        LocalDate expiredDate = LocalDate.now().minusDays(1);

        EmployeeLeaveBalance sickBalance = new EmployeeLeaveBalance();
        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);
        sickBalance.setBalance(2);
        sickBalance.setLeaveYearStart(
                expiredDate.minusYears(1).plusDays(1)
        );
        sickBalance.setLeaveYearEnd(expiredDate);

        sickBalance = employeeLeaveBalanceRepository.save(sickBalance);

        EmployeeLeaveBalance earnedBalance = new EmployeeLeaveBalance();
        earnedBalance.setEmployee(employee);
        earnedBalance.setLeaveType(earnedLeave);
        earnedBalance.setBalance(3);
        earnedBalance.setLeaveYearStart(
                expiredDate.minusYears(1).plusDays(1)
        );
        earnedBalance.setLeaveYearEnd(expiredDate);

        earnedBalance = employeeLeaveBalanceRepository.save(earnedBalance);

        leaveBalanceScheduler.resetLeaveBalances();

        EmployeeLeaveBalance updatedSickBalance =
                employeeLeaveBalanceRepository.findById(sickBalance.getId())
                        .orElseThrow();

        EmployeeLeaveBalance updatedEarnedBalance =
                employeeLeaveBalanceRepository.findById(earnedBalance.getId())
                        .orElseThrow();

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId())
                        .orElseThrow();

        assertEquals(6, updatedSickBalance.getBalance());
        assertEquals(8, updatedEarnedBalance.getBalance());

        assertEquals(14, updatedEmployee.getLeaveBalance());

        assertFalse(
                updatedSickBalance.getLeaveYearEnd().isBefore(LocalDate.now())
        );

        assertFalse(
                updatedEarnedBalance.getLeaveYearEnd().isBefore(LocalDate.now())
        );
    }

    /**
     * Verifies that balances are not reset when the stored leave year
     * has not yet expired.
     */
    @Test
    void shouldNotResetActiveLeaveBalances() {

        EmployeeLeaveBalance sickBalance = new EmployeeLeaveBalance();
        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);
        sickBalance.setBalance(2);
        sickBalance.setLeaveYearStart(LocalDate.now().minusMonths(6));
        sickBalance.setLeaveYearEnd(LocalDate.now().plusDays(10));

        sickBalance = employeeLeaveBalanceRepository.save(sickBalance);

        EmployeeLeaveBalance earnedBalance = new EmployeeLeaveBalance();
        earnedBalance.setEmployee(employee);
        earnedBalance.setLeaveType(earnedLeave);
        earnedBalance.setBalance(3);
        earnedBalance.setLeaveYearStart(LocalDate.now().minusMonths(6));
        earnedBalance.setLeaveYearEnd(LocalDate.now().plusDays(10));

        earnedBalance = employeeLeaveBalanceRepository.save(earnedBalance);

        leaveBalanceScheduler.resetLeaveBalances();

        EmployeeLeaveBalance updatedSickBalance =
                employeeLeaveBalanceRepository.findById(sickBalance.getId())
                        .orElseThrow();

        EmployeeLeaveBalance updatedEarnedBalance =
                employeeLeaveBalanceRepository.findById(earnedBalance.getId())
                        .orElseThrow();

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId())
                        .orElseThrow();

        assertEquals(2, updatedSickBalance.getBalance());
        assertEquals(3, updatedEarnedBalance.getBalance());

        assertEquals(5, updatedEmployee.getLeaveBalance());
    }

    /**
     * Verifies that an employee without leave balances is skipped
     * without causing an exception.
     */
    @Test
    void shouldSkipEmployeeWithoutLeaveBalances() {

        leaveBalanceScheduler.resetLeaveBalances();

        Employee updatedEmployee =
                employeeRepository.findById(employee.getId())
                        .orElseThrow();

        assertEquals(5, updatedEmployee.getLeaveBalance());

        List<EmployeeLeaveBalance> balances =
                employeeLeaveBalanceRepository
                        .findByEmployeeIdAndDeletedFalse(employee.getId());

        assertEquals(0, balances.size());
    }
}
