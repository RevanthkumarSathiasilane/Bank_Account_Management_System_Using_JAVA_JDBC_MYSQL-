package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Account.java
 * -------------
 * POJO (Plain Old Java Object) / Entity class that represents a single row
 * of the "accounts" table.
 *
 * OOP Principle demonstrated: ENCAPSULATION
 *  - All fields are private.
 *  - Access is only permitted through public getters/setters.
 *  - This protects the internal state of the object from uncontrolled
 *    external modification and is the foundation of the DAO/Model pattern.
 */
public class Account {

    private int id;
    private String accountNumber;
    private String customerName;
    private String phone;
    private String email;
    private BigDecimal balance;
    private Timestamp createdAt;

    public Account() {
        // Default constructor required for building objects field-by-field
        // (e.g. when mapping a ResultSet row to an Account object).
    }

    public Account(String accountNumber, String customerName, String phone,
                   String email, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.balance = balance;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * A clean, formatted representation used by the console UI when
     * printing account details (used in "View Account" / "Balance Inquiry").
     */
    @Override
    public String toString() {
        return "----------------------------------------\n" +
                "Account Number : " + accountNumber + "\n" +
                "Customer Name  : " + customerName + "\n" +
                "Phone          : " + phone + "\n" +
                "Email          : " + email + "\n" +
                "Balance        : Rs. " + balance + "\n" +
                "Created At     : " + createdAt + "\n" +
                "----------------------------------------";
    }
}
