package com.dao;
import config.DatabaseConnection;
import dao.AccountDAO;
import model.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public void createAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (account_number, customer_name, phone, email, balance) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountNumber());
            ps.setString(2, account.getCustomerName());
            ps.setString(3, account.getPhone());
            ps.setString(4, account.getEmail());
            ps.setDouble(5, account.getBalance());

            ps.executeUpdate();
        }
    }

    @Override
    public Account getAccountByNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null; // Not found - caller (service layer) decides how to react
    }

    @Override
    public List<Account> getAllAccounts() throws SQLException {
        String sql = "SELECT * FROM accounts ORDER BY id ASC";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        }
        return accounts;
    }

    @Override
    public List<Account> searchByCustomerName(String name) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE customer_name LIKE ? ORDER BY customer_name";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    public List<Account> getAccountsByBalanceRange(double min, double max) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE balance BETWEEN ? AND ? ORDER BY balance DESC";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, min);
            ps.setDouble(2, max);

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
        String sql = "SELECT * FROM accounts ORDER BY balance DESC LIMIT ?";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAccountNumberExists(String accountNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public void updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteAccount(String accountNumber) throws SQLException {
        String sql = "DELETE FROM accounts WHERE account_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ps.executeUpdate();
        }
    }


    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getString("account_number"),
                rs.getString("customer_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getDouble("balance"),
                rs.getTimestamp("created_at")
        );
    }
}