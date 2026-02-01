-- V1__Initial_Schema.sql

-- Create payrolls table
CREATE TABLE payrolls (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(200) NOT NULL,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    basic_salary DECIMAL(12, 2) NOT NULL,
    allowances DECIMAL(12, 2) DEFAULT 0,
    bonuses DECIMAL(12, 2) DEFAULT 0,
    overtime_pay DECIMAL(12, 2) DEFAULT 0,
    deductions DECIMAL(12, 2) DEFAULT 0,
    tax DECIMAL(12, 2) DEFAULT 0,
    gross_salary DECIMAL(12, 2) NOT NULL,
    net_salary DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_date DATE,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT
);

-- Create indexes
CREATE INDEX idx_employee_id ON payrolls(employee_id);
CREATE INDEX idx_pay_period ON payrolls(pay_period_start, pay_period_end);
CREATE INDEX idx_status ON payrolls(status);
CREATE INDEX idx_payment_date ON payrolls(payment_date);

-- Insert sample data
INSERT INTO payrolls (employee_id, employee_name, pay_period_start, pay_period_end, 
                      basic_salary, allowances, bonuses, overtime_pay, deductions, tax,
                      gross_salary, net_salary, status)
VALUES 
(1, 'John Doe', '2024-01-01', '2024-01-31', 75000.00, 5000.00, 2000.00, 1000.00, 
 1500.00, 15000.00, 83000.00, 66500.00, 'PAID');
