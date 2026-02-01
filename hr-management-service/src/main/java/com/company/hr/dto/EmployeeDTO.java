package com.company.hr.dto;

import com.company.hr.entity.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class EmployeeDTO {

    private Long id;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotNull(message = "Status is required")
    private Employee.EmploymentStatus status;

    @NotNull(message = "Employment type is required")
    private Employee.EmploymentType employmentType;

    private String jobTitle;

    private BigDecimal salary;

    private Long departmentId;
    private String departmentName;

    private Long managerId;
    private String managerName;

    private AddressDTO address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
