package exception;

/**
 * AccountNotFoundException.java
 * -------------------------------
 * Thrown when an operation refers to an account number that does not
 * exist in the database (e.g. deposit, withdraw, transfer, balance
 * inquiry, or delete on a non-existent account).
 */
public class AccountNotFoundException extends Exception {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
