package com.ust.lms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class EmployeeLeaveBalance extends CommonFields {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer balance;

    @Column(name = "leave_year_start", nullable = false)
    private LocalDate leaveYearStart;

    @Column(name = "leave_year_end", nullable = false)
    private LocalDate leaveYearEnd;
}