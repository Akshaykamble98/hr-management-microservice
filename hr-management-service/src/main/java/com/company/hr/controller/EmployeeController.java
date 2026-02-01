package com.company.hr.controller;

import com.company.hr.dto.ApiResponse;
import com.company.hr.dto.EmployeeDTO;
import com.company.hr.entity.Employee;
import com.company.hr.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create a new employee")
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createdEmployee));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employee")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping("/employee-id/{employeeId}")
    @Operation(summary = "Get employee by employee ID")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        EmployeeDTO employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping
    @Operation(summary = "Get all employees with pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> getAllEmployees(Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get employees by department")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/manager/{managerId}")
    @Operation(summary = "Get employees by manager")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByManager(@PathVariable Long managerId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get employees by status")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByStatus(
            @PathVariable Employee.EmploymentStatus status) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees")
    public ResponseEntity<ApiResponse<Page<EmployeeDTO>>> searchEmployees(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<EmployeeDTO> employees = employeeService.searchEmployees(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an employee")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
}
