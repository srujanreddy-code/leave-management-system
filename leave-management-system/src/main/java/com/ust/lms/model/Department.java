package com.ust.lms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Department extends CommonFields {
    @Column(nullable = false)
    private String name;

    private String description;
}