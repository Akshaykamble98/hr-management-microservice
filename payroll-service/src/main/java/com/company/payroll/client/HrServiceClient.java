package com.company.payroll.client;

import com.company.payroll.dto.EmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hr-management-service", path = "/api/v1")
public interface HrServiceClient {

    @GetMapping("/employees/{id}")
    ApiResponse<EmployeeDTO> getEmployeeById(@PathVariable Long id);

    @GetMapping("/employees/employee-id/{employeeId}")
    ApiResponse<EmployeeDTO> getEmployeeByEmployeeId(@PathVariable String employeeId);
}
