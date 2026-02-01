package com.company.hr.repository;

import com.company.hr.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeId(Long employeeId);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.date = :date")
    List<Attendance> findByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.status = :status AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    long countByEmployeeIdAndStatusAndMonth(@Param("employeeId") Long employeeId,
                                             @Param("status") Attendance.AttendanceStatus status,
                                             @Param("month") int month,
                                             @Param("year") int year);
}
