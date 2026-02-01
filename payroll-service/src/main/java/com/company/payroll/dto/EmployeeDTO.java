package com.company.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String jobTitle;
    private BigDecimal salary;
    private Long departmentId;
    private String departmentName;
    private LocalDate hireDate;
    private String status;
}
