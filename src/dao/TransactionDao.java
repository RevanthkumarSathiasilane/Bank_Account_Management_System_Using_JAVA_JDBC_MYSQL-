package dao;

import model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * TransactionDAO.java
 * ---------------------
 * DAO interface for all operations on the "transactions" table
 * (the audit trail of every deposit, withdrawal, and transfer leg).
 */
public interface TransactionDAO {

    /** Inserts a transaction record using its own new connection/commit. */
    void addTransaction(Transaction transaction) throws SQLException;

    /** Inserts a transaction record using an existing connection - used so
     *  that a transfer's debit + credit rows are part of the same
     *  database transaction as the balance updates. */
    void addTransaction(Connection conn, Transaction transaction) throws SQLException;

    /** Returns all transactions for an account, most recent first. */
    List<Transaction> getTransactionHistory(String accountNumber) throws SQLException;

    /** Returns only the last {@code limit} transactions (Mini Statement bonus feature). */
    List<Transaction> getMiniStatement(String accountNumber, int limit) throws SQLException;

    /** Returns all transactions that occurred on the given SQL date string 'YYYY-MM-DD'. */
    List<Transaction> getDailyReport(String date) throws SQLException;

    /** Returns all transactions that occurred in the given month/year. */
    List<Transaction> getMonthlyReport(int month, int year) throws SQLException;
}
