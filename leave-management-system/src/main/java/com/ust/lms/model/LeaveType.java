package com.ust.lms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LeaveType extends CommonFields {
    @Column(nullable = false)
    private String name;

    @Column(name = "max_days", nullable = false)
    private Integer maxDays;
}