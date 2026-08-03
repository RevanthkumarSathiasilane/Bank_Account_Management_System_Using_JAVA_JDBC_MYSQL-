package com.util;
import java.util.regex.Pattern;
public class InputValidator {
    private static final Pattern PHONE_PATTERN = Pattern.compile(Constants.PHONE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(Constants.EMAIL_REGEX);
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    public static boolean isValidOpeningBalance(double balance) {
        return balance >= Constants.MIN_OPENING_BALANCE;
    }
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    private InputValidator() {
    }
}