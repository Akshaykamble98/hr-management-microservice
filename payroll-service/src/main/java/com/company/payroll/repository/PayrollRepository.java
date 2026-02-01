package com.company.payroll.repository;

import com.company.payroll.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);

    List<Payroll> findByStatus(Payroll.PayrollStatus status);

    @Query("SELECT p FROM Payroll p WHERE p.employeeId = :employeeId AND " +
           "p.payPeriodStart = :startDate AND p.payPeriodEnd = :endDate")
    Optional<Payroll> findByEmployeeIdAndPayPeriod(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payroll p WHERE p.payPeriodStart >= :startDate AND p.payPeriodEnd <= :endDate")
    List<Payroll> findByPayPeriodRange(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(p.netSalary) FROM Payroll p WHERE p.status = 'PAID' AND " +
           "YEAR(p.paymentDate) = :year AND MONTH(p.paymentDate) = :month")
    BigDecimal getTotalPayrollForMonth(@Param("year") int year, @Param("month") int month);
}
