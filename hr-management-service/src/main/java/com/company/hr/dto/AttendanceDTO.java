package com.company.hr.dto;

import com.company.hr.entity.Attendance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceDTO {

    private Long id;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkIn;

    private LocalTime checkOut;

    private String notes;

    @NotNull(message = "Status is required")
    private Attendance.AttendanceStatus status;

    private Integer workHours;

    private LocalDateTime createdAt;
}
