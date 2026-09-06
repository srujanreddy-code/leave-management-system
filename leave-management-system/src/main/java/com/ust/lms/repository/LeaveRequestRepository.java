package com.ust.lms.repository;

import com.ust.lms.common.LeaveStatus;
import com.ust.lms.model.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByDeletedFalse(Pageable pageable);
    Page<LeaveRequest> findByEmployeeIdAndDeletedFalse(Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByLeaveStatusAndDeletedFalse(LeaveStatus leaveStatus, Pageable pageable);
    Page<LeaveRequest> findByEmployeeDepartmentIdAndDeletedFalse(Long departmentId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.deleted = false " +
            "AND lr.leaveStatus IN ('PENDING', 'APPROVED') " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlapping(@Param("employeeId") Long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.id <> :excludeId AND lr.deleted = false " +
            "AND lr.leaveStatus IN ('PENDING', 'APPROVED') " +
            "AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findOverlappingExcludingId(@Param("employeeId") Long employeeId,
                                                  @Param("excludeId") Long excludeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}