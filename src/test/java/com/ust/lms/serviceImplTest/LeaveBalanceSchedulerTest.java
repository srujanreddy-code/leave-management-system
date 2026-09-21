package com.ust.lms.serviceImplTest;
import com.ust.lms.employee.impl.LeaveBalanceScheduler;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveBalanceScheduler}.
 *
 * <p>
 * This test class verifies the leave balance reset logic without loading
 * the Spring application context or connecting to the database.
 * Repository dependencies are mocked using Mockito.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class LeaveBalanceSchedulerTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @InjectMocks
    private LeaveBalanceScheduler leaveBalanceScheduler;

    private Employee employee;
    private LeaveType sickLeave;
    private LeaveType earnedLeave;
    private EmployeeLeaveBalance sickBalance;
    private EmployeeLeaveBalance earnedBalance;

    /**
     * Creates the common test data used by the test cases.
     */
    @BeforeEach
    void setUp() {

        employee = new Employee();
        employee.setId(1L);

        /*
         * Joining date is deliberately chosen so that the current
         * leave year can be calculated by the scheduler.
         */
        employee.setJoiningDate(LocalDate.of(2025, 1, 1));
        employee.setLeaveBalance(5);

        sickLeave = new LeaveType();
        sickLeave.setId(1L);
        sickLeave.setMaxDays(6);

        earnedLeave = new LeaveType();
        earnedLeave.setId(2L);
        earnedLeave.setMaxDays(8);

        sickBalance = new EmployeeLeaveBalance();
        sickBalance.setId(1L);
        sickBalance.setEmployee(employee);
        sickBalance.setLeaveType(sickLeave);

        earnedBalance = new EmployeeLeaveBalance();
        earnedBalance.setId(2L);
        earnedBalance.setEmployee(employee);
        earnedBalance.setLeaveType(earnedLeave);
    }

    /**
     * Verifies that expired leave balances are reset to the maximum
     * number of days configured for each leave type.
     */
    @Test
    void shouldResetLeaveBalancesWhenLeaveYearHasEnded() {

        LocalDate today = LocalDate.now();

        /*
         * Set the stored leave year end to a date before today
         * so that the scheduler considers the leave year expired.
         */
        sickBalance.setLeaveYearEnd(today.minusDays(1));
        earnedBalance.setLeaveYearEnd(today.minusDays(1));

        when(employeeRepository.findByDeletedFalse())
                .thenReturn(List.of(employee));

        when(leaveTypeRepository.findByDeletedFalse())
                .thenReturn(List.of(sickLeave, earnedLeave));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(employee.getId()))
                .thenReturn(List.of(sickBalance, earnedBalance));

        leaveBalanceScheduler.resetLeaveBalances();

        assertEquals(6, sickBalance.getBalance());
        assertEquals(8, earnedBalance.getBalance());

        assertEquals(14, employee.getLeaveBalance());

        verify(employeeLeaveBalanceRepository, times(1))
                .save(sickBalance);

        verify(employeeLeaveBalanceRepository, times(1))
                .save(earnedBalance);

        verify(employeeRepository, times(1))
                .save(employee);
    }

    /**
     * Verifies that an employee is skipped when no leave balances exist.
     */
    @Test
    void shouldSkipEmployeeWhenNoLeaveBalancesExist() {

        when(employeeRepository.findByDeletedFalse())
                .thenReturn(List.of(employee));

        when(leaveTypeRepository.findByDeletedFalse())
                .thenReturn(List.of(sickLeave, earnedLeave));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(employee.getId()))
                .thenReturn(List.of());

        leaveBalanceScheduler.resetLeaveBalances();

        verify(employeeLeaveBalanceRepository, never())
                .save(any(EmployeeLeaveBalance.class));

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    /**
     * Verifies that leave balances are not reset when the current
     * leave year has not yet ended.
     */
    @Test
    void shouldNotResetLeaveBalancesWhenLeaveYearHasNotEnded() {

        LocalDate today = LocalDate.now();

        sickBalance.setBalance(3);
        earnedBalance.setBalance(5);

        sickBalance.setLeaveYearEnd(today.plusDays(10));
        earnedBalance.setLeaveYearEnd(today.plusDays(10));

        when(employeeRepository.findByDeletedFalse())
                .thenReturn(List.of(employee));

        when(leaveTypeRepository.findByDeletedFalse())
                .thenReturn(List.of(sickLeave, earnedLeave));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(employee.getId()))
                .thenReturn(List.of(sickBalance, earnedBalance));

        leaveBalanceScheduler.resetLeaveBalances();

        assertEquals(3, sickBalance.getBalance());
        assertEquals(5, earnedBalance.getBalance());

        assertEquals(5, employee.getLeaveBalance());

        verify(employeeLeaveBalanceRepository, never())
                .save(any(EmployeeLeaveBalance.class));

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    /**
     * Verifies that multiple employees are processed independently.
     */
    @Test
    void shouldResetBalancesForMultipleEmployees() {

        Employee secondEmployee = new Employee();
        secondEmployee.setId(2L);
        secondEmployee.setJoiningDate(LocalDate.of(2025, 1, 1));
        secondEmployee.setLeaveBalance(0);

        EmployeeLeaveBalance secondBalance = new EmployeeLeaveBalance();
        secondBalance.setId(3L);
        secondBalance.setEmployee(secondEmployee);
        secondBalance.setLeaveType(sickLeave);
        secondBalance.setLeaveYearEnd(LocalDate.now().minusDays(1));

        sickBalance.setLeaveYearEnd(LocalDate.now().minusDays(1));
        earnedBalance.setLeaveYearEnd(LocalDate.now().minusDays(1));

        when(employeeRepository.findByDeletedFalse())
                .thenReturn(List.of(employee, secondEmployee));

        when(leaveTypeRepository.findByDeletedFalse())
                .thenReturn(List.of(sickLeave, earnedLeave));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(1L))
                .thenReturn(List.of(sickBalance, earnedBalance));

        when(employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(2L))
                .thenReturn(List.of(secondBalance));

        leaveBalanceScheduler.resetLeaveBalances();

        assertEquals(6, sickBalance.getBalance());
        assertEquals(8, earnedBalance.getBalance());
        assertEquals(14, employee.getLeaveBalance());

        assertEquals(6, secondBalance.getBalance());
        assertEquals(6, secondEmployee.getLeaveBalance());

        verify(employeeLeaveBalanceRepository, times(1))
                .save(sickBalance);

        verify(employeeLeaveBalanceRepository, times(1))
                .save(earnedBalance);

        verify(employeeLeaveBalanceRepository, times(1))
                .save(secondBalance);

        verify(employeeRepository, times(1))
                .save(employee);

        verify(employeeRepository, times(1))
                .save(secondEmployee);
    }
}