package com.company.hr.service;

import com.company.hr.dto.EmployeeDTO;
import com.company.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO);

    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO getEmployeeByEmployeeId(String employeeId);

    Page<EmployeeDTO> getAllEmployees(Pageable pageable);

    List<EmployeeDTO> getEmployeesByDepartment(Long departmentId);

    List<EmployeeDTO> getEmployeesByManager(Long managerId);

    List<EmployeeDTO> getEmployeesByStatus(Employee.EmploymentStatus status);

    Page<EmployeeDTO> searchEmployees(String keyword, Pageable pageable);

    void deleteEmployee(Long id);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);
}
