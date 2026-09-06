package com.ust.lms.repository;

import com.ust.lms.model.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    Page<LeaveType> findByDeletedFalse(Pageable pageable);
    Optional<LeaveType> findByNameIgnoreCase(String name);
}