package com.ust.lms.repository;

import com.ust.lms.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for performing database operations on leave types.
 */
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Page<LeaveType> findByDeletedFalse(Pageable pageable);

    Optional<LeaveType> findByNameIgnoreCase(String name);

    List<LeaveType> findByDeletedFalse();
}