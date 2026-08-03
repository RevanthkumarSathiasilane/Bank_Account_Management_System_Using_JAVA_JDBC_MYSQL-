package util;

import exception.AccountNotFoundException;
import exception.InsufficientBalanceException;
import exception.InvalidAmountException;
import model.Account;
import model.Transaction;
import service.BankService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Menu.java
 * -----------
 * The VIEW + CONTROLLER layer of our console MVC design.
 * Responsible ONLY for:
 *   - Printing prompts and menus
 *   - Reading user input
 *   - Calling the appropriate BankService method
 *   - Catching exceptions and printing friendly, colored messages
 *
 * It contains NO SQL and NO direct JDBC calls - all business logic lives
 * in BankService. This keeps the UI layer thin and swappable (e.g. this
 * could be replaced by a JavaFX GUI or a REST controller later without
 * changing BankService at all).
 */
public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final BankService bankService = new BankService();

    public void start() {
        if (!adminLogin()) {
            System.out.println(Constants.RED + "Too many failed attempts. Exiting..." + Constants.RESET);
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter Choice: ");
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> viewAccount();
                case 3 -> viewAllAccounts();
                case 4 -> deposit();
                case 5 -> withdraw();
                case 6 -> transfer();
                case 7 -> checkBalance();
                case 8 -> transactionHistory();
                case 9 -> deleteAccount();
                case 10 -> searchCustomer();
                case 11 -> bonusMenu();
                case 12 -> {
                    running = false;
                    System.out.println(Constants.CYAN + "Thank you for using Bank Account Management System!" + Constants.RESET);
                }
                default -> System.out.println(Constants.RED + "Invalid choice. Try again." + Constants.RESET);
            }
        }
        scanner.close();
    }

    // ==================================================================
    // ADMIN LOGIN (Bonus feature)
    // ==================================================================
    private boolean adminLogin() {
        System.out.println(Constants.BOLD + Constants.CYAN +
                "======================================\n" +
                "   BANK MANAGEMENT SYSTEM - LOGIN\n" +
                "======================================" + Constants.RESET);

        int attempts = 0;
        while (attempts < Constants.MAX_LOGIN_ATTEMPTS) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (username.equals(Constants.ADMIN_USERNAME) && password.equals(Constants.ADMIN_PASSWORD)) {
                System.out.println(Constants.GREEN + "Login successful!\n" + Constants.RESET);
                return true;
            }
            attempts++;
            System.out.println(Constants.RED + "Invalid credentials. Attempts left: " +
                    (Constants.MAX_LOGIN_ATTEMPTS - attempts) + Constants.RESET);
        }
        return false;
    }

    // ==================================================================
    // MENU DISPLAY
    // ==================================================================
    private void printMainMenu() {
        System.out.println(Constants.BOLD + Constants.CYAN +
                "\n======================================\n" +
                "     BANK ACCOUNT MANAGEMENT SYSTEM\n" +
                "======================================" + Constants.RESET);
        System.out.println("""
                 1  Create Account
                 2  View Account
                 3  View All Accounts
                 4  Deposit Money
                 5  Withdraw Money
                 6  Transfer Money
                 7  Check Balance
                 8  Transaction History
                 9  Delete Account
                10  Search Customer
                11  Bonus Features
                12  Exit""");
        System.out.println(Constants.CYAN + "======================================" + Constants.RESET);
    }

    // ==================================================================
    // 1. CREATE ACCOUNT
    // ==================================================================
    private void createAccount() {
        try {
            System.out.print("Customer Name: ");
            String name = scanner.nextLine();
            System.out.print("Phone (10 digits): ");
            String phone = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Opening Balance (min Rs. " + Constants.MINIMUM_OPENING_BALANCE + "): ");
            BigDecimal balance = InputValidator.parseAmount(scanner.nextLine());

            if (balance == null) {
                System.out.println(Constants.RED + "Invalid number format for balance." + Constants.RESET);
                return;
            }

            Account account = bankService.createAccount(name, phone, email, balance);
            System.out.println(Constants.GREEN + "Account created successfully!" + Constants.RESET);
            System.out.println(account);

        } catch (InvalidAmountException e) {
            System.out.println(Constants.RED + "Error: " + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 2. VIEW ACCOUNT
    // ==================================================================
    private void viewAccount() {
        try {
            String accNum = promptAccountNumber();
            Account account = bankService.getAccount(accNum);
            System.out.println(account);
        } catch (AccountNotFoundException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 3. VIEW ALL ACCOUNTS
    // ==================================================================
    private void viewAllAccounts() {
        try {
            List<Account> accounts = bankService.getAllAccounts();
            if (accounts.isEmpty()) {
                System.out.println(Constants.YELLOW + "No accounts found." + Constants.RESET);
                return;
            }
            System.out.printf("%-15s %-20s %-12s %-25s %-12s%n",
                    "AccNumber", "Name", "Phone", "Email", "Balance");
            System.out.println("-".repeat(90));
            for (Account a : accounts) {
                System.out.printf("%-15s %-20s %-12s %-25s Rs.%-10s%n",
                        a.getAccountNumber(), a.getCustomerName(), a.getPhone(), a.getEmail(), a.getBalance());
            }
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 4. DEPOSIT
    // ==================================================================
    private void deposit() {
        try {
            String accNum = promptAccountNumber();
            System.out.print("Deposit Amount: ");
            BigDecimal amount = InputValidator.parseAmount(scanner.nextLine());
            if (amount == null) {
                System.out.println(Constants.RED + "Invalid amount format." + Constants.RESET);
                return;
            }
            Account account = bankService.deposit(accNum, amount);
            System.out.println(Constants.GREEN + "Deposit successful! New balance: Rs. " +
                    account.getBalance() + Constants.RESET);
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 5. WITHDRAW
    // ==================================================================
    private void withdraw() {
        try {
            String accNum = promptAccountNumber();
            System.out.print("Withdraw Amount: ");
            BigDecimal amount = InputValidator.parseAmount(scanner.nextLine());
            if (amount == null) {
                System.out.println(Constants.RED + "Invalid amount format." + Constants.RESET);
                return;
            }
            Account account = bankService.withdraw(accNum, amount);
            System.out.println(Constants.GREEN + "Withdrawal successful! New balance: Rs. " +
                    account.getBalance() + Constants.RESET);
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientBalanceException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 6. TRANSFER
    // ==================================================================
    private void transfer() {
        try {
            System.out.print("From Account Number: ");
            String from = scanner.nextLine();
            System.out.print("To Account Number: ");
            String to = scanner.nextLine();
            System.out.print("Transfer Amount: ");
            BigDecimal amount = InputValidator.parseAmount(scanner.nextLine());
            if (amount == null) {
                System.out.println(Constants.RED + "Invalid amount format." + Constants.RESET);
                return;
            }
            bankService.transfer(from, to, amount);
            System.out.println(Constants.GREEN + "Transfer of Rs. " + amount +
                    " from " + from + " to " + to + " completed successfully." + Constants.RESET);
        } catch (AccountNotFoundException | InvalidAmountException | InsufficientBalanceException e) {
            System.out.println(Constants.RED + "Transfer failed: " + e.getMessage() +
                    " (transaction rolled back)" + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 7. CHECK BALANCE
    // ==================================================================
    private void checkBalance() {
        try {
            String accNum = promptAccountNumber();
            Account account = bankService.getAccount(accNum);
            System.out.println(Constants.GREEN + "Current Balance: Rs. " + account.getBalance() + Constants.RESET);
        } catch (AccountNotFoundException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 8. TRANSACTION HISTORY
    // ==================================================================
    private void transactionHistory() {
        try {
            String accNum = promptAccountNumber();
            List<Transaction> history = bankService.getTransactionHistory(accNum);
            if (history.isEmpty()) {
                System.out.println(Constants.YELLOW + "No transactions found for this account." + Constants.RESET);
                return;
            }
            System.out.println(Constants.CYAN + "Transaction History (latest first):" + Constants.RESET);
            for (Transaction t : history) {
                System.out.println(t);
            }
        } catch (AccountNotFoundException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 9. DELETE ACCOUNT
    // ==================================================================
    private void deleteAccount() {
        try {
            String accNum = promptAccountNumber();
            bankService.deleteAccount(accNum);
            System.out.println(Constants.GREEN + "Account deleted successfully." + Constants.RESET);
        } catch (AccountNotFoundException | InvalidAmountException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 10. SEARCH CUSTOMER
    // ==================================================================
    private void searchCustomer() {
        System.out.println("Search by: 1) Account Number  2) Customer Name");
        int choice = readInt("Enter choice: ");
        try {
            if (choice == 1) {
                String accNum = promptAccountNumber();
                Account account = bankService.getAccount(accNum);
                System.out.println(account);
            } else if (choice == 2) {
                System.out.print("Enter customer name (or part of it): ");
                String name = scanner.nextLine();
                List<Account> results = bankService.searchByName(name);
                if (results.isEmpty()) {
                    System.out.println(Constants.YELLOW + "No matching customers found." + Constants.RESET);
                } else {
                    results.forEach(System.out::println);
                }
            } else {
                System.out.println(Constants.RED + "Invalid choice." + Constants.RESET);
            }
        } catch (AccountNotFoundException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    // ==================================================================
    // 11. BONUS FEATURES SUBMENU
    // ==================================================================
    private void bonusMenu() {
        System.out.println(Constants.CYAN + """
                --- Bonus Features ---
                1 Interest Calculator
                2 Mini Statement
                3 Top 5 Richest Customers
                4 Search by Balance Range
                5 Daily Transaction Report
                6 Monthly Transaction Report
                7 Back to Main Menu""" + Constants.RESET);
        int choice = readInt("Enter choice: ");
        try {
            switch (choice) {
                case 1 -> interestCalculator();
                case 2 -> miniStatement();
                case 3 -> topRichestCustomers();
                case 4 -> searchByBalanceRange();
                case 5 -> dailyReport();
                case 6 -> monthlyReport();
                case 7 -> { /* back to main menu */ }
                default -> System.out.println(Constants.RED + "Invalid choice." + Constants.RESET);
            }
        } catch (AccountNotFoundException e) {
            System.out.println(Constants.RED + e.getMessage() + Constants.RESET);
        } catch (SQLException e) {
            printDbError(e);
        }
    }

    private void interestCalculator() {
        System.out.print("Principal Amount: ");
        BigDecimal principal = InputValidator.parseAmount(scanner.nextLine());
        System.out.print("Annual Interest Rate (%): ");
        double rate = readDouble();
        System.out.print("Time Period (years): ");
        double years = readDouble();

        if (principal == null) {
            System.out.println(Constants.RED + "Invalid principal amount." + Constants.RESET);
            return;
        }
        BigDecimal interest = bankService.calculateSimpleInterest(principal, rate, years);
        System.out.println(Constants.GREEN + "Simple Interest = Rs. " + interest + Constants.RESET);
    }

    private void miniStatement() throws AccountNotFoundException, SQLException {
        String accNum = promptAccountNumber();
        List<Transaction> txns = bankService.getMiniStatement(accNum, 5);
        if (txns.isEmpty()) {
            System.out.println(Constants.YELLOW + "No transactions found." + Constants.RESET);
            return;
        }
        System.out.println(Constants.CYAN + "Last " + txns.size() + " transactions:" + Constants.RESET);
        txns.forEach(System.out::println);
    }

    private void topRichestCustomers() throws SQLException {
        List<Account> top = bankService.getTopRichestCustomers(5);
        System.out.println(Constants.CYAN + "Top 5 Richest Customers:" + Constants.RESET);
        int rank = 1;
        for (Account a : top) {
            System.out.println(rank++ + ". " + a.getCustomerName() + " - Rs. " + a.getBalance() +
                    " (" + a.getAccountNumber() + ")");
        }
    }

    private void searchByBalanceRange() throws SQLException {
        System.out.print("Minimum Balance: ");
        BigDecimal min = InputValidator.parseAmount(scanner.nextLine());
        System.out.print("Maximum Balance: ");
        BigDecimal max = InputValidator.parseAmount(scanner.nextLine());
        if (min == null || max == null) {
            System.out.println(Constants.RED + "Invalid amount format." + Constants.RESET);
            return;
        }
        List<Account> results = bankService.searchByBalanceRange(min, max);
        if (results.isEmpty()) {
            System.out.println(Constants.YELLOW + "No accounts in this balance range." + Constants.RESET);
        } else {
            results.forEach(System.out::println);
        }
    }

    private void dailyReport() throws SQLException {
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        List<Transaction> txns = bankService.getDailyReport(date);
        if (txns.isEmpty()) {
            System.out.println(Constants.YELLOW + "No transactions on this date." + Constants.RESET);
        } else {
            txns.forEach(System.out::println);
        }
    }

    private void monthlyReport() throws SQLException {
        System.out.print("Enter month (1-12): ");
        int month = readInt("");
        System.out.print("Enter year (e.g. 2026): ");
        int year = readInt("");
        List<Transaction> txns = bankService.getMonthlyReport(month, year);
        if (txns.isEmpty()) {
            System.out.println(Constants.YELLOW + "No transactions in this month." + Constants.RESET);
        } else {
            txns.forEach(System.out::println);
        }
    }

    // ==================================================================
    // HELPER METHODS
    // ==================================================================
    private String promptAccountNumber() {
        System.out.print("Enter Account Number: ");
        return scanner.nextLine().trim();
    }

    private void printDbError(SQLException e) {
        System.out.println(Constants.RED + "Database error occurred: " + e.getMessage() + Constants.RESET);
    }

    /** Robust integer reader that re-prompts on non-numeric input instead of crashing. */
    private int readInt(String prompt) {
        while (true) {
            if (!prompt.isEmpty()) {
                System.out.print(prompt);
            }
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                System.out.println(Constants.RED + "Please enter a valid number." + Constants.RESET);
            }
        }
    }

    private double readDouble() {
        while (true) {
            String line = scanner.nextLine();
            try {
                return Double.parseDouble(line.trim());
            } catch (NumberFormatException e) {
                System.out.print(Constants.RED + "Please enter a valid number: " + Constants.RESET);
            }
        }
    }
}
