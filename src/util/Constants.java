package util;

/**
 * Constants.java
 * ---------------
 * Centralized location for all application-wide constant values.
 * Keeping constants in one place avoids "magic numbers/strings" scattered
 * across the codebase and makes future changes (e.g. minimum balance rules)
 * a one-line edit instead of a hunt through every class.
 */
public class Constants {

    // ---------- Database configuration ----------
    // NOTE: Update these values to match your local MySQL installation.
    public static final String DB_URL = "jdbc:mysql://localhost:3306/bank_management";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root"; // change to your MySQL password

    // ---------- Business rules ----------
    public static final double MINIMUM_OPENING_BALANCE = 500.00;
    public static final double MINIMUM_TRANSACTION_AMOUNT = 0.01;

    // ---------- Transaction type labels ----------
    public static final String TXN_DEPOSIT = "DEPOSIT";
    public static final String TXN_WITHDRAW = "WITHDRAW";
    public static final String TXN_TRANSFER_DEBIT = "TRANSFER_DEBIT";
    public static final String TXN_TRANSFER_CREDIT = "TRANSFER_CREDIT";

    // ---------- Admin credentials (Bonus feature: admin login) ----------
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin@123";
    public static final int MAX_LOGIN_ATTEMPTS = 3;

    // ---------- Console color codes (ANSI) ----------
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    private Constants() {
        // Private constructor prevents instantiation of a pure constants class.
    }
}
