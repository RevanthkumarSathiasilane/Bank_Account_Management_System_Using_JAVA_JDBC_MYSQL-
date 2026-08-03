package util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * InputValidator.java
 * ----------------------
 * Pure, stateless validation helper methods. Keeping validation logic
 * separate from BankService (business logic) and Menu (UI) means each
 * class has a single, clear responsibility (Single Responsibility
 * Principle) and validation rules can be unit-tested in isolation.
 */
public class InputValidator {

    // Simple, practical patterns - good enough for an interview project.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$"); // 10-digit Indian mobile number starting 6-9

    private InputValidator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() >= 2;
    }

    /** Opening balance must be a positive number and meet the minimum balance rule. */
    public static boolean isValidOpeningBalance(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.valueOf(Constants.MINIMUM_OPENING_BALANCE)) >= 0;
    }

    /** Deposit/withdraw/transfer amount must simply be greater than zero. */
    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Attempts to parse a String into a BigDecimal.
     * Returns null if the input is not a valid number, allowing the
     * calling UI code to re-prompt the user instead of crashing.
     */
    public static BigDecimal parseAmount(String input) {
        try {
            return new BigDecimal(input.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
