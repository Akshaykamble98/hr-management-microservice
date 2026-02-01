package com.company.hr.repository;

import com.company.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByCode(String code);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
