-- =========================================================
-- Bank Account Management System - Database Setup Script
-- =========================================================

-- 1. CREATE DATABASE
DROP DATABASE IF EXISTS bank_management;
CREATE DATABASE bank_management;
USE bank_management;

-- 2. CREATE TABLE: accounts
CREATE TABLE accounts (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    account_number  VARCHAR(20) UNIQUE NOT NULL,
    customer_name   VARCHAR(100) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(100) UNIQUE NOT NULL,
    balance         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. CREATE TABLE: transactions
CREATE TABLE transactions (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    account_number      VARCHAR(20) NOT NULL,
    transaction_type    VARCHAR(30) NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    description         VARCHAR(255),
    transaction_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        ON DELETE CASCADE
);

-- Helpful index for the most common lookup pattern (history by account, newest first)
CREATE INDEX idx_transactions_account_time ON transactions(account_number, transaction_time DESC);

-- 4. INSERT SAMPLE DATA
INSERT INTO accounts (account_number, customer_name, phone, email, balance) VALUES
('1000000001', 'Arun Kumar',     '9876543210', 'arun.kumar@example.com',     15000.00),
('1000000002', 'Priya Sharma',   '9876543211', 'priya.sharma@example.com',   8500.50),
('1000000003', 'Rohit Verma',    '9876543212', 'rohit.verma@example.com',    500.00),
('1000000004', 'Sneha Reddy',    '9876543213', 'sneha.reddy@example.com',    250000.00),
('1000000005', 'Karthik Iyer',   '9876543214', 'karthik.iyer@example.com',   32000.75);

INSERT INTO transactions (account_number, transaction_type, amount, description) VALUES
('1000000001', 'DEPOSIT',  15000.00, 'Initial deposit'),
('1000000002', 'DEPOSIT',  8500.50,  'Initial deposit'),
('1000000003', 'DEPOSIT',  500.00,   'Initial deposit'),
('1000000004', 'DEPOSIT',  250000.00,'Initial deposit'),
('1000000005', 'DEPOSIT',  32000.75, 'Initial deposit');

-- 5. Sanity check queries (optional, run manually to verify)
-- SELECT * FROM accounts;
-- SELECT * FROM transactions ORDER BY transaction_time DESC;