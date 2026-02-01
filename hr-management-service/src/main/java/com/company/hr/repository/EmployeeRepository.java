package com.company.hr.repository;

import com.company.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    List<Employee> findByDepartmentId(Long departmentId);

    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    List<Employee> findByManagerId(Long managerId);

    @Query("SELECT e FROM Employee e WHERE e.status = :status")
    List<Employee> findByStatus(@Param("status") Employee.EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId AND e.status = 'ACTIVE'")
    long countActiveEmployeesByDepartment(@Param("departmentId") Long departmentId);
}
