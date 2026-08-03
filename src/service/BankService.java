package service;

import config.DatabaseConnection;
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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * BankService.java
 * ------------------
 * The SERVICE layer (business logic layer) in our MVC-inspired
 * architecture:
 *
 *      Menu (View/Controller)  -->  BankService (business rules)  -->  DAO (persistence)
 *
 * INTERVIEW CONCEPT - MVC Architecture (adapted for a console app):
 *   Model      -> Account.java, Transaction.java
 *   View       -> Menu.java (prints prompts/results to the console)
 *   Controller -> Menu.java also plays controller here since there is no
 *                 separate framework; in a real MVC web app the Controller
 *                 (e.g. a Servlet/Spring Controller) would call this exact
 *                 same BankService instead.
 *
 * BankService NEVER writes raw SQL. It only calls DAO methods and applies
 * business rules (validation, exception throwing, orchestrating multi-step
 * operations like Transfer). This separation is what makes the DAO Pattern
 * valuable: the persistence technology could change entirely (MySQL ->
 * PostgreSQL, or JDBC -> JPA) without a single line of this class changing.
 */
public class BankService {

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final TransactionDAO transactionDAO = new TransactionDAOImpl();

    // ==================================================================
    // CREATE ACCOUNT
    // ==================================================================
    public Account createAccount(String name, String phone, String email, BigDecimal openingBalance)
            throws InvalidAmountException, SQLException {

        if (!InputValidator.isValidName(name)) {
            throw new InvalidAmountException("Customer name must be at least 2 characters.");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new InvalidAmountException("Invalid phone number. Must be a 10-digit number starting 6-9.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidAmountException("Invalid email format.");
        }
        if (!InputValidator.isValidOpeningBalance(openingBalance)) {
            throw new InvalidAmountException(
                    "Opening balance must be at least Rs. " + Constants.MINIMUM_OPENING_BALANCE);
        }
        if (accountDAO.isEmailTaken(email)) {
            throw new InvalidAmountException("An account with this email already exists.");
        }

        Account account = new Account(null, name.trim(), phone.trim(), email.trim(), openingBalance);
        String accountNumber = accountDAO.createAccount(account);
        account.setAccountNumber(accountNumber);
        return account;
    }

    // ==================================================================
    // VIEW ACCOUNT / VIEW ALL / SEARCH
    // ==================================================================
    public Account getAccount(String accountNumber) throws AccountNotFoundException, SQLException {
        Account account = accountDAO.getAccountByNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("No account found with number: " + accountNumber);
        }
        return account;
    }

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.getAllAccounts();
    }

    public List<Account> searchByName(String name) throws SQLException {
        return accountDAO.searchByCustomerName(name);
    }

    public List<Account> getTopRichestCustomers(int limit) throws SQLException {
        return accountDAO.getTopRichestCustomers(limit);
    }

    public List<Account> searchByBalanceRange(BigDecimal min, BigDecimal max) throws SQLException {
        return accountDAO.searchByBalanceRange(min, max);
    }

    // ==================================================================
    // DEPOSIT
    // ==================================================================
    public Account deposit(String accountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, SQLException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Deposit amount must be greater than zero.");
        }

        Account account = getAccount(accountNumber); // throws AccountNotFoundException if missing

        BigDecimal newBalance = account.getBalance().add(amount);
        accountDAO.updateBalance(accountNumber, newBalance);

        transactionDAO.addTransaction(new Transaction(
                accountNumber, Constants.TXN_DEPOSIT, amount, "Cash deposit"));

        account.setBalance(newBalance);
        return account;
    }

    // ==================================================================
    // WITHDRAW
    // ==================================================================
    public Account withdraw(String accountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException, SQLException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero.");
        }

        Account account = getAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: Rs. " + account.getBalance());
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        accountDAO.updateBalance(accountNumber, newBalance);

        transactionDAO.addTransaction(new Transaction(
                accountNumber, Constants.TXN_WITHDRAW, amount, "Cash withdrawal"));

        account.setBalance(newBalance);
        return account;
    }

    // ==================================================================
    // TRANSFER  (demonstrates JDBC Transaction Management)
    // ==================================================================
    /**
     * Transfers money from one account to another as a single atomic
     * database transaction.
     *
     * INTERVIEW CONCEPT - Transaction Management / ACID:
     *   A transfer touches FOUR rows (debit sender balance, credit
     *   receiver balance, insert debit transaction row, insert credit
     *   transaction row). If the application crashed or an error occurred
     *   after only 2 of those 4 writes, the bank's books would be
     *   inconsistent (money would vanish or be duplicated).
     *
     *   To guarantee ATOMICITY (the "A" in ACID - all-or-nothing), we:
     *     1. Obtain a single Connection for the whole operation.
     *     2. Turn OFF auto-commit: conn.setAutoCommit(false).
     *     3. Perform all four writes using that SAME connection.
     *     4. If every write succeeds -> conn.commit() makes them permanent.
     *     5. If ANY exception occurs -> conn.rollback() undoes every
     *        change made so far in this transaction, as if none of it
     *        ever happened.
     *     6. finally: restore auto-commit and close the connection.
     */
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException, SQLException {

        if (!InputValidator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Transfer amount must be greater than zero.");
        }
        if (fromAccountNumber.equalsIgnoreCase(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // ---- BEGIN TRANSACTION ----

            Account fromAccount = accountDAO.getAccountByNumber(conn, fromAccountNumber);
            if (fromAccount == null) {
                throw new AccountNotFoundException("Sender account not found: " + fromAccountNumber);
            }
            Account toAccount = accountDAO.getAccountByNumber(conn, toAccountNumber);
            if (toAccount == null) {
                throw new AccountNotFoundException("Receiver account not found: " + toAccountNumber);
            }
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Available: Rs. " + fromAccount.getBalance());
            }

            // Debit sender
            BigDecimal newFromBalance = fromAccount.getBalance().subtract(amount);
            accountDAO.updateBalance(conn, fromAccountNumber, newFromBalance);

            // Credit receiver
            BigDecimal newToBalance = toAccount.getBalance().add(amount);
            accountDAO.updateBalance(conn, toAccountNumber, newToBalance);

            // Record both legs of the transfer
            transactionDAO.addTransaction(conn, new Transaction(
                    fromAccountNumber, Constants.TXN_TRANSFER_DEBIT, amount,
                    "Transfer to " + toAccountNumber));
            transactionDAO.addTransaction(conn, new Transaction(
                    toAccountNumber, Constants.TXN_TRANSFER_CREDIT, amount,
                    "Transfer from " + fromAccountNumber));

            conn.commit(); // ---- COMMIT: all four writes become permanent ----

        } catch (AccountNotFoundException | InsufficientBalanceException | SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ---- ROLLBACK: undo everything in this transaction ----
                } catch (SQLException rollbackEx) {
                    // Log and swallow - the original exception is more important to the caller.
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e; // Re-throw so the UI layer can display the correct message.
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // restore default behaviour before returning connection
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    // ==================================================================
    // TRANSACTION HISTORY / MINI STATEMENT / REPORTS
    // ==================================================================
    public List<Transaction> getTransactionHistory(String accountNumber)
            throws AccountNotFoundException, SQLException {
        getAccount(accountNumber); // validates existence first
        return transactionDAO.getTransactionHistory(accountNumber);
    }

    public List<Transaction> getMiniStatement(String accountNumber, int limit)
            throws AccountNotFoundException, SQLException {
        getAccount(accountNumber);
        return transactionDAO.getMiniStatement(accountNumber, limit);
    }

    public List<Transaction> getDailyReport(String date) throws SQLException {
        return transactionDAO.getDailyReport(date);
    }

    public List<Transaction> getMonthlyReport(int month, int year) throws SQLException {
        return transactionDAO.getMonthlyReport(month, year);
    }

    // ==================================================================
    // DELETE ACCOUNT
    // ==================================================================
    public void deleteAccount(String accountNumber)
            throws AccountNotFoundException, InvalidAmountException, SQLException {

        Account account = getAccount(accountNumber);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidAmountException(
                    "Cannot delete account. Balance must be zero (current: Rs. " + account.getBalance() + ").");
        }
        boolean deleted = accountDAO.deleteAccount(accountNumber);
        if (!deleted) {
            throw new AccountNotFoundException("Account could not be deleted (not found or balance not zero).");
        }
    }

    // ==================================================================
    // BONUS: SIMPLE INTEREST CALCULATOR
    // ==================================================================
    /**
     * Calculates simple interest = (Principal * Rate * Time) / 100.
     * A pure utility calculation - does not touch the database.
     */
    public BigDecimal calculateSimpleInterest(BigDecimal principal, double ratePercent, double years) {
        BigDecimal rate = BigDecimal.valueOf(ratePercent);
        BigDecimal time = BigDecimal.valueOf(years);
        return principal.multiply(rate).multiply(time)
                .divide(BigDecimal.valueOf(100));
    }
}
