package com.ust.lms.employee.impl;

import com.ust.lms.common.CommonService;
import com.ust.lms.model.Employee;
import com.ust.lms.model.EmployeeLeaveBalance;
import com.ust.lms.model.LeaveType;
import com.ust.lms.repository.EmployeeLeaveBalanceRepository;
import com.ust.lms.repository.EmployeeRepository;
import com.ust.lms.repository.LeaveTypeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled component responsible for resetting employee leave balances
 * based on their joining-date anniversary.
 */
@Component
public class LeaveBalanceScheduler extends CommonService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveBalanceScheduler(
            EmployeeRepository employeeRepository,
            EmployeeLeaveBalanceRepository employeeLeaveBalanceRepository,
            LeaveTypeRepository leaveTypeRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeLeaveBalanceRepository = employeeLeaveBalanceRepository;
        this.leaveTypeRepository = leaveTypeRepository;
    }

    /**
     * Resets employee leave balances when the current leave year has ended.
     * Each leave type balance is restored to its configured maximum.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetLeaveBalances() {

        LocalDate today = LocalDate.now();

        List<Employee> employees = employeeRepository.findByDeletedFalse();
        List<LeaveType> leaveTypes = leaveTypeRepository.findByDeletedFalse();

        for (Employee employee : employees) {


            LocalDate anniversaryThisYear =
                    employee.getJoiningDate().withYear(today.getYear());

            LocalDate currentLeaveYearStart;

            if (today.isBefore(anniversaryThisYear)) {
                currentLeaveYearStart = anniversaryThisYear.minusYears(1);
            } else {
                currentLeaveYearStart = anniversaryThisYear;
            }

            LocalDate currentLeaveYearEnd =
                    currentLeaveYearStart.plusYears(1).minusDays(1);

            List<EmployeeLeaveBalance> balances =
                    employeeLeaveBalanceRepository.findByEmployeeIdAndDeletedFalse(employee.getId());

            if (balances.isEmpty()) {
                continue;
            }

            LocalDate storedLeaveYearEnd = balances.get(0).getLeaveYearEnd();

            if (today.isAfter(storedLeaveYearEnd)) {

                int totalBalance = 0;

                for (EmployeeLeaveBalance balance : balances) {

                    LeaveType leaveType = balance.getLeaveType();

                    balance.setBalance(leaveType.getMaxDays());
                    balance.setLeaveYearStart(currentLeaveYearStart);
                    balance.setLeaveYearEnd(currentLeaveYearEnd);

                    employeeLeaveBalanceRepository.save(balance);

                    totalBalance += leaveType.getMaxDays();
                }

                employee.setLeaveBalance(totalBalance);
                employeeRepository.save(employee);
            }
        }
    }
}