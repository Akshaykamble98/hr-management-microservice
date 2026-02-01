package com.company.payroll.dto;

import com.company.payroll.entity.Payroll;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;

    @NotNull(message = "Pay period start date is required")
    private LocalDate payPeriodStart;

    @NotNull(message = "Pay period end date is required")
    private LocalDate payPeriodEnd;

    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal bonuses;
    private BigDecimal overtimePay;
    private BigDecimal deductions;
    private BigDecimal tax;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;

    private Payroll.PayrollStatus status;
    private LocalDate paymentDate;
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
