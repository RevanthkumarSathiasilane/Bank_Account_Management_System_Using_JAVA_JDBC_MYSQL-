package com.dao;
import model.Account;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {

        void createAccount(Account account) throws SQLException;

        Account getAccountByNumber(String accountNumber) throws SQLException;

        List<Account> getAllAccounts() throws SQLException;

        List<Account> searchByCustomerName(String name) throws SQLException;

        List<Account> getAccountsByBalanceRange(double min, double max) throws SQLException;

        List<Account> getTopRichestCustomers(int limit) throws SQLException;

        boolean isEmailExists(String email) throws SQLException;

        boolean isAccountNumberExists(String accountNumber) throws SQLException;

        void updateBalance(Connection conn, String accountNumber, double newBalance) throws SQLException;

        void deleteAccount(String accountNumber) throws SQLException;
    }
}
