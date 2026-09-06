package com.ust.lms.repository;

import com.ust.lms.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByUserId(Long userId);
    Page<Employee> findByDeletedFalse(Pageable pageable);
    Page<Employee> findByDepartmentIdAndDeletedFalse(Long departmentId, Pageable pageable);
}