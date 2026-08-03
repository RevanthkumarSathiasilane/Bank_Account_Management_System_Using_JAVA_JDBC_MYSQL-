package daoimpl;

import config.DatabaseConnection;
import dao.AccountDAO;
import model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAOImpl.java
 * ---------------------
 * Concrete JDBC implementation of the AccountDAO contract.
 *
 * INTERVIEW CONCEPT - PreparedStatement vs Statement:
 *   We NEVER use java.sql.Statement with string-concatenated SQL because
 *   that is vulnerable to SQL Injection (e.g. an attacker typing
 *   `' OR '1'='1` into a name field could alter the query's logic).
 *   PreparedStatement:
 *     - Precompiles the SQL once; placeholders (?) are bound as typed
 *       parameters, so user input is NEVER interpreted as SQL syntax.
 *     - Is also faster when the same query is executed repeatedly,
 *       because the database can cache the compiled execution plan.
 *
 * INTERVIEW CONCEPT - try-with-resources:
 *   Connection, PreparedStatement, and ResultSet all implement
 *   AutoCloseable. Declaring them in a try(...) block guarantees they are
 *   closed (even if an exception is thrown) without needing a manual
 *   finally block - this prevents connection/resource leaks.
 */
public class AccountDAOImpl implements AccountDAO {

    // ---------- SQL statements (kept as constants for readability & reuse) ----------
    private static final String INSERT_ACCOUNT =
            "INSERT INTO accounts (account_number, customer_name, phone, email, balance, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_NUMBER =
            "SELECT * FROM accounts WHERE account_number = ?";

    private static final String SELECT_ALL =
            "SELECT * FROM accounts ORDER BY id";

    private static final String UPDATE_BALANCE =
            "UPDATE accounts SET balance = ? WHERE account_number = ?";

    private static final String DELETE_ACCOUNT =
            "DELETE FROM accounts WHERE account_number = ? AND balance = 0";

    private static final String EMAIL_EXISTS =
            "SELECT COUNT(*) FROM accounts WHERE email = ?";

    private static final String SEARCH_BY_NAME =
            "SELECT * FROM accounts WHERE customer_name LIKE ? ORDER BY id";

    private static final String TOP_RICHEST =
            "SELECT * FROM accounts ORDER BY balance DESC LIMIT ?";

    private static final String BALANCE_RANGE =
            "SELECT * FROM accounts WHERE balance BETWEEN ? AND ? ORDER BY balance DESC";

    /**
     * Generates a unique account number in the form ACCyyyyMMddHHmmssSSS
     * (timestamp based) so we never rely on a separate sequence table.
     * Simple, readable, and effectively unique for a demo/interview project.
     */
    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }

    @Override
    public String createAccount(Account account) throws SQLException {
        String accountNumber = generateAccountNumber();

        // try-with-resources: Connection + PreparedStatement auto-closed.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ACCOUNT)) {

            ps.setString(1, accountNumber);
            ps.setString(2, account.getCustomerName());
            ps.setString(3, account.getPhone());
            ps.setString(4, account.getEmail());
            ps.setBigDecimal(5, account.getBalance());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
        }
        return accountNumber;
    }

    @Override
    public Account getAccountByNumber(String accountNumber) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return getAccountByNumber(conn, accountNumber);
        }
    }

    @Override
    public Account getAccountByNumber(Connection conn, String accountNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_NUMBER)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null; // Not found - caller decides how to handle (usually throws AccountNotFoundException)
    }

    @Override
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        }
        return accounts;
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal newBalance) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateBalance(conn, accountNumber, newBalance);
        }
    }

    @Override
    public void updateBalance(Connection conn, String accountNumber, BigDecimal newBalance) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_BALANCE)) {
            ps.setBigDecimal(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean deleteAccount(String accountNumber) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ACCOUNT)) {
            ps.setString(1, accountNumber);
            int rows = ps.executeUpdate();
            return rows > 0; // false means account not found OR balance was not zero
        }
    }

    @Override
    public boolean isEmailTaken(String email) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EMAIL_EXISTS)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public List<Account> searchByCustomerName(String name) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH_BY_NAME)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }
        }
        return accounts;
    }

    @Override
    public List<Account> getTopRichestCustomers(int limit) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(TOP_RICHEST)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }
        }
        return accounts;
    }

    @Override
    public List<Account> searchByBalanceRange(BigDecimal min, BigDecimal max) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(BALANCE_RANGE)) {
            ps.setBigDecimal(1, min);
            ps.setBigDecimal(2, max);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }
        }
        return accounts;
    }

    /**
     * Maps the current row of a ResultSet to an Account object.
     * Extracted into a private helper to avoid duplicating this mapping
     * logic in every query method above (DRY principle).
     */
    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setCustomerName(rs.getString("customer_name"));
        account.setPhone(rs.getString("phone"));
        account.setEmail(rs.getString("email"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setCreatedAt(rs.getTimestamp("created_at"));
        return account;
    }
}
