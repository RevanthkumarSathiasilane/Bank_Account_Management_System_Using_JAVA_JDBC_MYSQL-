package dao;

import model.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * AccountDAO.java
 * -----------------
 * DAO (Data Access Object) Pattern - Interface (Abstraction layer).
 *
 * INTERVIEW CONCEPT - DAO Pattern:
 *   The DAO pattern separates the "what" (business operations on data)
 *   from the "how" (raw JDBC/SQL code). The service layer talks only to
 *   this interface, never to JDBC directly. This gives us:
 *     - Loose coupling: swap AccountDAOImpl for a different persistence
 *       technology later without touching BankService.
 *     - Testability: a mock implementation can be injected for unit tests.
 *
 * OOP Principle demonstrated: ABSTRACTION
 *   Callers depend on this interface's contract, not on implementation
 *   details of how rows are fetched/updated in MySQL.
 */
public interface AccountDAO {

    /** Inserts a new account row. Returns the generated account number. */
    String createAccount(Account account) throws SQLException;

    /** Fetches a single account by account number, or null if not found. */
    Account getAccountByNumber(String accountNumber) throws SQLException;

    /** Fetches a single account by account number using an existing
     *  connection - used inside multi-step transactions (e.g. transfer)
     *  so all statements share the same Connection/transaction. */
    Account getAccountByNumber(Connection conn, String accountNumber) throws SQLException;

    /** Returns all accounts ordered by id. */
    List<Account> getAllAccounts() throws SQLException;

    /** Updates only the balance column for the given account number. */
    void updateBalance(String accountNumber, java.math.BigDecimal newBalance) throws SQLException;

    /** Updates the balance using a caller-supplied connection (for transactions). */
    void updateBalance(Connection conn, String accountNumber, java.math.BigDecimal newBalance) throws SQLException;

    /** Deletes an account permanently. Caller must have already validated balance == 0. */
    boolean deleteAccount(String accountNumber) throws SQLException;

    /** Checks whether an email already exists (used to prevent duplicate emails). */
    boolean isEmailTaken(String email) throws SQLException;

    /** Searches accounts whose customer_name contains the given (partial) name. */
    List<Account> searchByCustomerName(String name) throws SQLException;

    /** Returns the top N accounts ordered by balance descending. */
    List<Account> getTopRichestCustomers(int limit) throws SQLException;

    /** Returns accounts whose balance falls within [min, max]. */
    List<Account> searchByBalanceRange(java.math.BigDecimal min, java.math.BigDecimal max) throws SQLException;
}
