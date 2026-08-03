package com.dao;
import model.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {

    void addTransaction(Connection conn, Transaction transaction) throws SQLException;

    List<Transaction> getTransactionsByAccount(String accountNumber) throws SQLException;
}
