package com.company.payroll.service;

import com.company.payroll.client.HrServiceClient;
import com.company.payroll.dto.EmployeeDTO;
import com.company.payroll.dto.PayrollDTO;
import com.company.payroll.entity.Payroll;
import com.company.payroll.repository.PayrollRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final HrServiceClient hrServiceClient;

    @Transactional
    @CacheEvict(value = "payrolls", allEntries = true)
    @CircuitBreaker(name = "hrService", fallbackMethod = "createPayrollFallback")
    public PayrollDTO createPayroll(PayrollDTO payrollDTO) {
        log.info("Creating payroll for employee ID: {}", payrollDTO.getEmployeeId());

        // Fetch employee details from HR Service
        EmployeeDTO employee = hrServiceClient.getEmployeeById(payrollDTO.getEmployeeId()).getData();

        Payroll payroll = Payroll.builder()
                .employeeId(payrollDTO.getEmployeeId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .payPeriodStart(payrollDTO.getPayPeriodStart())
                .payPeriodEnd(payrollDTO.getPayPeriodEnd())
                .basicSalary(employee.getSalary())
                .allowances(payrollDTO.getAllowances() != null ? payrollDTO.getAllowances() : BigDecimal.ZERO)
                .bonuses(payrollDTO.getBonuses() != null ? payrollDTO.getBonuses() : BigDecimal.ZERO)
                .overtimePay(payrollDTO.getOvertimePay() != null ? payrollDTO.getOvertimePay() : BigDecimal.ZERO)
                .deductions(payrollDTO.getDeductions() != null ? payrollDTO.getDeductions() : BigDecimal.ZERO)
                .tax(payrollDTO.getTax() != null ? payrollDTO.getTax() : BigDecimal.ZERO)
                .status(Payroll.PayrollStatus.DRAFT)
                .notes(payrollDTO.getNotes())
                .build();

        // Calculate gross and net salary
        calculateSalary(payroll);

        Payroll savedPayroll = payrollRepository.save(payroll);
        log.info("Payroll created successfully with ID: {}", savedPayroll.getId());

        return mapToDTO(savedPayroll);
    }

    private void calculateSalary(Payroll payroll) {
        // Gross Salary = Basic Salary + Allowances + Bonuses + Overtime Pay
        BigDecimal grossSalary = payroll.getBasicSalary()
                .add(payroll.getAllowances())
                .add(payroll.getBonuses())
                .add(payroll.getOvertimePay());
        payroll.setGrossSalary(grossSalary);

        // Net Salary = Gross Salary - Deductions - Tax
        BigDecimal netSalary = grossSalary
                .subtract(payroll.getDeductions())
                .subtract(payroll.getTax());
        payroll.setNetSalary(netSalary);
    }

    @Cacheable(value = "payrolls", key = "#id")
    public PayrollDTO getPayrollById(Long id) {
        log.info("Fetching payroll with ID: {}", id);
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));
        return mapToDTO(payroll);
    }

    public Page<PayrollDTO> getAllPayrolls(Pageable pageable) {
        log.info("Fetching all payrolls with pagination");
        return payrollRepository.findAll(pageable).map(this::mapToDTO);
    }

    public List<PayrollDTO> getPayrollsByEmployeeId(Long employeeId) {
        log.info("Fetching payrolls for employee ID: {}", employeeId);
        return payrollRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "payrolls", allEntries = true)
    public PayrollDTO approvePayroll(Long id) {
        log.info("Approving payroll with ID: {}", id);
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        payroll.setStatus(Payroll.PayrollStatus.APPROVED);
        Payroll updatedPayroll = payrollRepository.save(payroll);

        return mapToDTO(updatedPayroll);
    }

    @Transactional
    @CacheEvict(value = "payrolls", allEntries = true)
    public PayrollDTO processPayment(Long id) {
        log.info("Processing payment for payroll ID: {}", id);
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payroll not found with id: " + id));

        if (payroll.getStatus() != Payroll.PayrollStatus.APPROVED) {
            throw new RuntimeException("Payroll must be approved before payment");
        }

        payroll.setStatus(Payroll.PayrollStatus.PAID);
        payroll.setPaymentDate(LocalDate.now());
        Payroll updatedPayroll = payrollRepository.save(payroll);

        return mapToDTO(updatedPayroll);
    }

    private PayrollDTO mapToDTO(Payroll payroll) {
        return PayrollDTO.builder()
                .id(payroll.getId())
                .employeeId(payroll.getEmployeeId())
                .employeeName(payroll.getEmployeeName())
                .payPeriodStart(payroll.getPayPeriodStart())
                .payPeriodEnd(payroll.getPayPeriodEnd())
                .basicSalary(payroll.getBasicSalary())
                .allowances(payroll.getAllowances())
                .bonuses(payroll.getBonuses())
                .overtimePay(payroll.getOvertimePay())
                .deductions(payroll.getDeductions())
                .tax(payroll.getTax())
                .grossSalary(payroll.getGrossSalary())
                .netSalary(payroll.getNetSalary())
                .status(payroll.getStatus())
                .paymentDate(payroll.getPaymentDate())
                .notes(payroll.getNotes())
                .createdAt(payroll.getCreatedAt())
                .updatedAt(payroll.getUpdatedAt())
                .build();
    }

    // Fallback method for Circuit Breaker
    public PayrollDTO createPayrollFallback(PayrollDTO payrollDTO, Exception ex) {
        log.error("HR Service is unavailable. Using fallback method.", ex);
        throw new RuntimeException("HR Service is currently unavailable. Please try again later.");
    }
}
