package com.company.hr.mapper;

import com.company.hr.dto.DepartmentDTO;
import com.company.hr.dto.LeaveDTO;
import com.company.hr.dto.AttendanceDTO;
import com.company.hr.entity.Department;
import com.company.hr.entity.Leave;
import com.company.hr.entity.Attendance;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface DepartmentMapper {

    @Mapping(expression = "java(department.getEmployees() != null ? department.getEmployees().size() : 0)", target = "employeeCount")
    DepartmentDTO toDTO(Department department);

    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Department toEntity(DepartmentDTO departmentDTO);

    List<DepartmentDTO> toDTOList(List<Department> departments);
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface LeaveMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(expression = "java(leave.getEmployee() != null ? leave.getEmployee().getFirstName() + \" \" + leave.getEmployee().getLastName() : null)", target = "employeeName")
    @Mapping(source = "approvedBy.id", target = "approvedById")
    @Mapping(expression = "java(leave.getApprovedBy() != null ? leave.getApprovedBy().getFirstName() + \" \" + leave.getApprovedBy().getLastName() : null)", target = "approvedByName")
    LeaveDTO toDTO(Leave leave);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Leave toEntity(LeaveDTO leaveDTO);

    List<LeaveDTO> toDTOList(List<Leave> leaves);
}

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface AttendanceMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(expression = "java(attendance.getEmployee() != null ? attendance.getEmployee().getFirstName() + \" \" + attendance.getEmployee().getLastName() : null)", target = "employeeName")
    AttendanceDTO toDTO(Attendance attendance);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Attendance toEntity(AttendanceDTO attendanceDTO);

    List<AttendanceDTO> toDTOList(List<Attendance> attendances);
}
