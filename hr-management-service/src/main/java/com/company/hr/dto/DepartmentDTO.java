package com.company.hr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    private Long id;

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;

    private String code;

    private Integer employeeCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
