package exception;
/**
 * InsufficientBalanceException.java
 * ----------------------------------
 * Custom checked exception thrown when a withdrawal or transfer is
 * attempted for an amount greater than the account's available balance.
 *
 * Why a custom exception instead of a generic RuntimeException?
 * - It makes the API self-documenting: any method signature that declares
 *   "throws InsufficientBalanceException" tells the caller exactly what
 *   business rule might fail.
 * - It allows the calling code (Menu/UI layer) to catch this specific
 *   exception and display a friendly, business-relevant message instead
 *   of a generic stack trace.
 */
public class InsufficientBalanceException extends Exception {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
