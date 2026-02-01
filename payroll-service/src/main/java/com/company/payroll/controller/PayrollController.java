package com.company.payroll.controller;

import com.company.payroll.dto.PayrollDTO;
import com.company.payroll.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "APIs for managing payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @Operation(summary = "Create a new payroll")
    public ResponseEntity<Map<String, Object>> createPayroll(@Valid @RequestBody PayrollDTO payrollDTO) {
        PayrollDTO created = payrollService.createPayroll(payrollDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(success("Payroll created successfully", created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payroll by ID")
    public ResponseEntity<Map<String, Object>> getPayrollById(@PathVariable Long id) {
        PayrollDTO payroll = payrollService.getPayrollById(id);
        return ResponseEntity.ok(success(payroll));
    }

    @GetMapping
    @Operation(summary = "Get all payrolls with pagination")
    public ResponseEntity<Map<String, Object>> getAllPayrolls(Pageable pageable) {
        Page<PayrollDTO> payrolls = payrollService.getAllPayrolls(pageable);
        return ResponseEntity.ok(success(payrolls));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get payrolls by employee ID")
    public ResponseEntity<Map<String, Object>> getPayrollsByEmployeeId(@PathVariable Long employeeId) {
        List<PayrollDTO> payrolls = payrollService.getPayrollsByEmployeeId(employeeId);
        return ResponseEntity.ok(success(payrolls));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve payroll")
    public ResponseEntity<Map<String, Object>> approvePayroll(@PathVariable Long id) {
        PayrollDTO approved = payrollService.approvePayroll(id);
        return ResponseEntity.ok(success("Payroll approved successfully", approved));
    }

    @PutMapping("/{id}/pay")
    @Operation(summary = "Process payroll payment")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Long id) {
        PayrollDTO paid = payrollService.processPayment(id);
        return ResponseEntity.ok(success("Payment processed successfully", paid));
    }

    private Map<String, Object> success(Object data) {
        return success(null, data);
    }

    private Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        if (message != null) {
            response.put("message", message);
        }
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
}
