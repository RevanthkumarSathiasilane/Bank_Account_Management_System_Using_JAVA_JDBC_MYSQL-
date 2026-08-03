package exception;

/**
 * InvalidAmountException.java
 * -----------------------------
 * Thrown when a monetary amount supplied by the user fails a basic
 * sanity/business rule check, such as:
 *  - Deposit/withdraw/transfer amount <= 0
 *  - Opening balance below the minimum required balance
 */
public class InvalidAmountException extends Exception {

    public InvalidAmountException(String message) {
        super(message);
    }
}
