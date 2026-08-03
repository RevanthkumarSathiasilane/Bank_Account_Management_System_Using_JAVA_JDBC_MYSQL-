package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Transaction.java
 * -----------------
 * POJO representing a single row of the "transactions" table.
 * Every deposit, withdrawal, or transfer leg is stored as a Transaction
 * record, giving the application a full audit trail (mini statement /
 * transaction history feature).
 */
public class Transaction {

    private int id;
    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAW, TRANSFER_DEBIT, TRANSFER_CREDIT
    private BigDecimal amount;
    private String description;
    private Timestamp transactionTime;

    public Transaction() {
    }

    public Transaction(String accountNumber, String transactionType,
                       BigDecimal amount, String description) {
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
    }

    // ---------------- Getters and Setters ----------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Timestamp transactionTime) {
        this.transactionTime = transactionTime;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-16s Rs. %-12s %-25s on %s",
                accountNumber, transactionType, amount, description, transactionTime);
    }
}
