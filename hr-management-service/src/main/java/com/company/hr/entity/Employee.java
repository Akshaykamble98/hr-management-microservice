package com.company.hr.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_employee_id", columnList = "employeeId"),
    @Index(name = "idx_department", columnList = "department_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    @Column(length = 100)
    private String jobTitle;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Employee> subordinates = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Leave> leaves = new HashSet<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Attendance> attendances = new HashSet<>();

    @Embedded
    private Address address;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    @Version
    private Long version;

    public enum EmploymentStatus {
        ACTIVE, INACTIVE, TERMINATED, ON_LEAVE, SUSPENDED
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN, TEMPORARY
    }
}
