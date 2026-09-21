package com.ust.lms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a department within the organization.
 */
@Getter
@Setter
@Entity
public class Department extends CommonFields {
    @Column(nullable = false)
    private String name;

    private String description;
}