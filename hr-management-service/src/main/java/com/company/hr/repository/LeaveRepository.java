package com.company.hr.repository;

import com.company.hr.entity.Leave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByEmployeeId(Long employeeId);

    Page<Leave> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Leave> findByStatus(Leave.LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId AND l.status = :status")
    List<Leave> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, 
                                           @Param("status") Leave.LeaveStatus status);

    @Query("SELECT l FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate")
    List<Leave> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(l.numberOfDays) FROM Leave l WHERE l.employee.id = :employeeId " +
           "AND l.status = 'APPROVED' AND YEAR(l.startDate) = :year")
    Integer getTotalLeaveDaysByEmployeeAndYear(@Param("employeeId") Long employeeId, 
                                                @Param("year") int year);
}
