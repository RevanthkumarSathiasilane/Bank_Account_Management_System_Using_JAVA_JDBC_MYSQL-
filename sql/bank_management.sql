CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;
-- Drop tables if re-running the script during development
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;
-- Accounts table
CREATE TABLE accounts (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20)  UNIQUE NOT NULL,
    customer_name  VARCHAR(100) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    email          VARCHAR(100) UNIQUE NOT NULL,
    balance        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
-- account_number references accounts.account_number (soft FK relationship,
-- enforced with an explicit FOREIGN KEY constraint below).
CREATE TABLE transactions (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    account_number    VARCHAR(20)   NOT NULL,
    transaction_type  VARCHAR(30)   NOT NULL,
    amount            DECIMAL(12,2) NOT NULL,
    description       VARCHAR(255),
    transaction_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_number) REFERENCES accounts(account_number)
        ON DELETE CASCADE
);
-- Helpful indexes for common lookups
CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_transactions_account_number ON transactions(account_number);
CREATE INDEX idx_transactions_time ON transactions(transaction_time);
-- Sample data 
INSERT INTO accounts (account_number, customer_name, phone, email, balance, created_at) VALUES
('ACC1000000001', 'Arjun Sharma', '9876543210', 'arjun.sharma@example.com', 15000.00, NOW()),
('ACC1000000002', 'Priya Nair',   '9123456780', 'priya.nair@example.com',   8500.50,  NOW()),
('ACC1000000003', 'Rahul Verma',  '9988776655', 'rahul.verma@example.com',  52000.75, NOW());

-- Sample transactions 
INSERT INTO transactions (account_number, transaction_type, amount, description, transaction_time) VALUES
('ACC1000000001', 'DEPOSIT',  15000.00, 'Initial deposit', NOW()),
('ACC1000000002', 'DEPOSIT',  8500.50,  'Initial deposit', NOW()),
('ACC1000000003', 'DEPOSIT',  52000.75, 'Initial deposit', NOW());

-- SELECT * FROM accounts;
-- SELECT * FROM transactions ORDER BY transaction_time DESC;
