-- V1__Initial_Schema.sql

-- Create departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT
);

-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    date_of_birth DATE NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    employment_type VARCHAR(20) NOT NULL,
    job_title VARCHAR(100),
    salary DECIMAL(12, 2),
    department_id BIGINT,
    manager_id BIGINT,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version BIGINT,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (manager_id) REFERENCES employees(id)
);

-- Create leaves table
CREATE TABLE leaves (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number_of_days INTEGER NOT NULL,
    reason VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    approver_comments VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id)
);

-- Create attendances table
CREATE TABLE attendances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    check_in TIME NOT NULL,
    check_out TIME,
    notes VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    work_hours INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    UNIQUE (employee_id, date)
);

-- Create indexes
CREATE INDEX idx_email ON employees(email);
CREATE INDEX idx_employee_id ON employees(employee_id);
CREATE INDEX idx_department ON employees(department_id);
CREATE INDEX idx_manager ON employees(manager_id);
CREATE INDEX idx_leave_employee ON leaves(employee_id);
CREATE INDEX idx_leave_status ON leaves(status);
CREATE INDEX idx_attendance_employee ON attendances(employee_id);
CREATE INDEX idx_attendance_date ON attendances(date);

-- Insert sample departments
INSERT INTO departments (name, description, code) VALUES
('Engineering', 'Software Development and Engineering', 'ENG'),
('Human Resources', 'HR and People Operations', 'HR'),
('Sales', 'Sales and Business Development', 'SALES'),
('Marketing', 'Marketing and Brand Management', 'MKT'),
('Finance', 'Finance and Accounting', 'FIN');
