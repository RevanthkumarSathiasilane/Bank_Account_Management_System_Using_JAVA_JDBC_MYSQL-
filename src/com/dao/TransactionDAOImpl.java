package com.dao;
import config.DatabaseConnection;
import dao.TransactionDAO;
import model.Transaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public void addTransaction(Connection conn, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, description) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transaction.getAccountNumber());
            ps.setString(2, transaction.getTransactionType());
            ps.setDouble(3, transaction.getAmount());
            ps.setString(4, transaction.getDescription());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccount(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_time DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getString("account_number"),
                            rs.getString("transaction_type"),
                            rs.getDouble("amount"),
                            rs.getString("description"),
                            rs.getTimestamp("transaction_time")
                    ));
                }
            }
        }
        return transactions;
    }
}