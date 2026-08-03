package com.service;
import dao.AccountDAO;
import dao.TransactionDAO;
import daoimpl.AccountDAOImpl;
import daoimpl.TransactionDAOImpl;
import exception.AccountNotFoundException;
import exception.InsufficientBalanceException;
import exception.InvalidAmountException;
import model.Account;
import model.Transaction;
import util.Constants;
import util.InputValidator;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BankService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public BankService() {
        // Constructor injects DAO implementations. In a Spring app this
        // would be @Autowired; here we wire it manually - still valid
        // "Dependency Injection" (just manual, not framework-managed).
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    // ================= CREATE ACCOUNT =================

    public Account createAccount(String name, String phone, String email, double openingBalance)
            throws SQLException, InvalidAmountException {

        if (!InputValidator.isNotEmpty(name)) {
            throw new InvalidAmountException("Customer name cannot be empty.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new InvalidAmountException("Invalid phone number. Must be a 10-digit number starting with 6-9.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidAmountException("Invalid email format.");
        }
        if (accountDAO.isEmailExists(email)) {
            throw new InvalidAmountException("An account with this email already exists.");
        }
        if (!InputValidator.isValidOpeningBalance(openingBalance)) {
            throw new InvalidAmountException(
                    "Opening balance must be at least Rs. " + Constants.MIN_OPENING_BALANCE);
        }

        String accountNumber = generateUniqueAccountNumber();
        Account account = new Account(accountNumber, name, phone, email, openingBalance);
        accountDAO.createAccount(account);
        return account;
    }

    /**
     * Generates a random, unique 10-digit account number.
     * Loops (extremely rare collision) until an unused number is found.
     */
    private String generateUniqueAccountNumber() throws SQLException {
        String accountNumber;
        do {
            long randomPart = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
            accountNumber = String.valueOf(randomPart);
        } while (accountDAO.isAccountNumberExists(accountNumber));
        return accountNumber;
    }

    // ================= VIEW =================

    public Account getAccount(String accountNumber) throws SQLException, AccountNotFoundException {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("No account found with number: " + accountNumber);
        }
        return account;
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.getAllAccounts();
    }

    // ================= DEPOSIT =================

    public void deposit(String accountNumber, double amount)
            throws SQLException, AccountNotFoundException, InvalidAmountException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }

        Account account = getAccount(accountNumber); // throws AccountNotFoundException if missing
        double newBalance = account.getBalance() + amount;

        // Single-statement style op, but still routed the same way as
        // transfer for consistency: open one connection, do work, commit.
        try (Connection conn = config.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(conn, accountNumber, newBalance);
                transactionDAO.addTransaction(conn,
                        new Transaction(accountNumber, Constants.TXN_DEPOSIT, amount, "Cash deposit"));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ================= WITHDRAW =================

    public void withdraw(String accountNumber, double amount)
            throws SQLException, AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }

        Account account = getAccount(accountNumber);

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: Rs. " + account.getBalance());
        }

        double newBalance = account.getBalance() - amount;

        try (Connection conn = config.DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountDAO.updateBalance(conn, accountNumber, newBalance);
                transactionDAO.addTransaction(conn,
                        new Transaction(accountNumber, Constants.TXN_WITHDRAW, amount, "Cash withdrawal"));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ================= TRANSFER (CORE JDBC TRANSACTION DEMO) =================

    /**
     * Transfers money from one account to another as a SINGLE ATOMIC
     * database transaction:
     *   1. Debit source account
     *   2. Credit destination account
     *   3. Record a TRANSFER_DEBIT transaction row
     *   4. Record a TRANSFER_CREDIT transaction row
     *
     * If ANY of these four steps fails, ALL of them are rolled back -
     * this is exactly the "Atomicity" guarantee from ACID properties.
     * We achieve it manually with conn.setAutoCommit(false) / commit() /
     * rollback(), which is the #1 JDBC interview topic for this project.
     */
    public void transfer(String fromAccountNumber, String toAccountNumber, double amount)
            throws SQLException, AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Transfer amount must be greater than zero.");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }

        Account fromAccount = getAccount(fromAccountNumber);
        Account toAccount = getAccount(toAccountNumber);

        if (fromAccount.getBalance() < amount) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in source account. Available: Rs. " + fromAccount.getBalance());
        }

        Connection conn = null;
        try {
            conn = config.DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // START of manual transaction boundary

            double newFromBalance = fromAccount.getBalance() - amount;
            double newToBalance = toAccount.getBalance() + amount;

            accountDAO.updateBalance(conn, fromAccountNumber, newFromBalance);
            accountDAO.updateBalance(conn, toAccountNumber, newToBalance);

            transactionDAO.addTransaction(conn, new Transaction(
                    fromAccountNumber, Constants.TXN_TRANSFER_DEBIT, amount,
                    "Transfer to " + toAccountNumber));
            transactionDAO.addTransaction(conn, new Transaction(
                    toAccountNumber, Constants.TXN_TRANSFER_CREDIT, amount,
                    "Transfer from " + fromAccountNumber));

            conn.commit(); // All 4 statements succeeded -> make them permanent
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Undo everything done since setAutoCommit(false)
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // restore default behaviour
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    // ================= TRANSACTION HISTORY =================

    public List<Transaction> getTransactionHistory(String accountNumber)
            throws SQLException, AccountNotFoundException {
        getAccount(accountNumber); // validates existence
        return transactionDAO.getTransactionsByAccount(accountNumber);
    }

    // ================= DELETE ACCOUNT =================

    public void deleteAccount(String accountNumber)
            throws SQLException, AccountNotFoundException, InsufficientBalanceException {

        Account account = getAccount(accountNumber);
        if (account.getBalance() != 0.0) {
            throw new InsufficientBalanceException(
                    "Cannot delete account. Balance must be zero (current: Rs. " + account.getBalance() + ")");
        }
        accountDAO.deleteAccount(accountNumber);
    }

    // ================= SEARCH =================

    public List<Account> searchByName(String name) throws SQLException {
        return accountDAO.searchByCustomerName(name);
    }

    // ================= BONUS FEATURES =================

    public List<Account> getTopRichestCustomers(int limit) throws SQLException {
        return accountDAO.getTopRichestCustomers(limit);
    }

    public List<Account> getAccountsByBalanceRange(double min, double max) throws SQLException {
        return accountDAO.getAccountsByBalanceRange(min, max);
    }

    /** Simple simulated annual interest calculation (not persisted). */
    public double calculateInterest(double principal, double annualRatePercent, int years) {
        return principal * annualRatePercent * years / 100.0;
    }

    /** Exports an account's transaction history to a CSV file on disk. */
    public String exportTransactionsToCSV(String accountNumber, String filePath)
            throws SQLException, AccountNotFoundException, IOException {

        List<Transaction> transactions = getTransactionHistory(accountNumber);

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,Account Number,Type,Amount,Description,Time\n");
            for (Transaction t : transactions) {
                writer.append(String.format("%d,%s,%s,%.2f,%s,%s%n",
                        t.getId(), t.getAccountNumber(), t.getTransactionType(),
                        t.getAmount(), t.getDescription(), t.getTransactionTime()));
            }
        }
        return filePath;
    }
}
