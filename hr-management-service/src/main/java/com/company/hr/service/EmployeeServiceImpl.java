package com.company.hr.service;

import com.company.hr.dto.EmployeeDTO;
import com.company.hr.entity.Department;
import com.company.hr.entity.Employee;
import com.company.hr.event.EmployeeEventPublisher;
import com.company.hr.exception.ResourceNotFoundException;
import com.company.hr.exception.DuplicateResourceException;
import com.company.hr.mapper.EmployeeMapper;
import com.company.hr.repository.DepartmentRepository;
import com.company.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee with ID: {}", employeeDTO.getEmployeeId());

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        if (employeeRepository.existsByEmployeeId(employeeDTO.getEmployeeId())) {
            throw new DuplicateResourceException("Employee with ID " + employeeDTO.getEmployeeId() + " already exists");
        }

        Employee employee = employeeMapper.toEntity(employeeDTO);

        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));
            employee.setDepartment(department);
        }

        if (employeeDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + employeeDTO.getManagerId()));
            employee.setManager(manager);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        
        // Publish event
        eventPublisher.publishEmployeeCreatedEvent(savedEmployee);
        
        log.info("Employee created successfully with ID: {}", savedEmployee.getId());
        return employeeMapper.toDTO(savedEmployee);
    }

    @Override
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        log.info("Updating employee with ID: {}", id);

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check for duplicate email if email is being changed
        if (!existingEmployee.getEmail().equals(employeeDTO.getEmail()) 
                && employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DuplicateResourceException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        employeeMapper.updateEntityFromDTO(employeeDTO, existingEmployee);

        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));
            existingEmployee.setDepartment(department);
        }

        if (employeeDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(employeeDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + employeeDTO.getManagerId()));
            existingEmployee.setManager(manager);
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        
        // Publish event
        eventPublisher.publishEmployeeUpdatedEvent(updatedEmployee);
        
        log.info("Employee updated successfully with ID: {}", updatedEmployee.getId());
        return employeeMapper.toDTO(updatedEmployee);
    }

    @Override
    @Cacheable(value = "employees", key = "#id")
    public EmployeeDTO getEmployeeById(Long id) {
        log.info("Fetching employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return employeeMapper.toDTO(employee);
    }

    @Override
    @Cacheable(value = "employees", key = "#employeeId")
    public EmployeeDTO getEmployeeByEmployeeId(String employeeId) {
        log.info("Fetching employee with employee ID: {}", employeeId);
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with employee ID: " + employeeId));
        return employeeMapper.toDTO(employee);
    }

    @Override
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        log.info("Fetching all employees with pagination");
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDTO);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
        log.info("Fetching employees for department ID: {}", departmentId);
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        return employeeMapper.toDTOList(employees);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByManager(Long managerId) {
        log.info("Fetching employees for manager ID: {}", managerId);
        List<Employee> employees = employeeRepository.findByManagerId(managerId);
        return employeeMapper.toDTOList(employees);
    }

    @Override
    public List<EmployeeDTO> getEmployeesByStatus(Employee.EmploymentStatus status) {
        log.info("Fetching employees with status: {}", status);
        List<Employee> employees = employeeRepository.findByStatus(status);
        return employeeMapper.toDTOList(employees);
    }

    @Override
    public Page<EmployeeDTO> searchEmployees(String keyword, Pageable pageable) {
        log.info("Searching employees with keyword: {}", keyword);
        return employeeRepository.searchEmployees(keyword, pageable)
                .map(employeeMapper::toDTO);
    }

    @Override
    @Transactional
    @CacheEvict(value = "employees", allEntries = true)
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        
        // Publish event before deletion
        eventPublisher.publishEmployeeDeletedEvent(employee);
        
        employeeRepository.delete(employee);
        log.info("Employee deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return employeeRepository.existsByEmployeeId(employeeId);
    }
}
