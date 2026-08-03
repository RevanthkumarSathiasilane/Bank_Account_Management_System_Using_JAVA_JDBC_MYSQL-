package daoimpl;

import config.DatabaseConnection;
import dao.TransactionDAO;
import model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAOImpl.java
 * --------------------------
 * Concrete JDBC implementation of TransactionDAO.
 * Every method uses PreparedStatement and try-with-resources, matching
 * the same defensive JDBC style used in AccountDAOImpl.
 */
public class TransactionDAOImpl implements TransactionDAO {

    private static final String INSERT_TXN =
            "INSERT INTO transactions (account_number, transaction_type, amount, description, transaction_time) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_HISTORY =
            "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC";

    private static final String SELECT_MINI_STATEMENT =
            "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC LIMIT ?";

    private static final String SELECT_DAILY =
            "SELECT * FROM transactions WHERE DATE(transaction_time) = ? ORDER BY transaction_time DESC";

    private static final String SELECT_MONTHLY =
            "SELECT * FROM transactions WHERE MONTH(transaction_time) = ? AND YEAR(transaction_time) = ? " +
                    "ORDER BY transaction_time DESC";

    @Override
    public void addTransaction(Transaction transaction) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            addTransaction(conn, transaction);
        }
    }

    @Override
    public void addTransaction(Connection conn, Transaction transaction) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_TXN)) {
            ps.setString(1, transaction.getAccountNumber());
            ps.setString(2, transaction.getTransactionType());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_HISTORY)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> getMiniStatement(String accountNumber, int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_MINI_STATEMENT)) {
            ps.setString(1, accountNumber);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> getDailyReport(String date) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DAILY)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Transaction> getMonthlyReport(int month, int year) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_MONTHLY)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getInt("id"));
        t.setAccountNumber(rs.getString("account_number"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setDescription(rs.getString("description"));
        t.setTransactionTime(rs.getTimestamp("transaction_time"));
        return t;
    }
}
