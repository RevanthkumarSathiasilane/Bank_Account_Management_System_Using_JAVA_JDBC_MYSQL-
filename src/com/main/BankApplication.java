package com.main;
import exception.AccountNotFoundException;
import exception.InsufficientBalanceException;
import exception.InvalidAmountException;
import model.Account;
import model.Transaction;
import service.BankService;
import util.Menu;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
public class BankApplication {
        private static final Scanner scanner = new Scanner(System.in);
        private static final BankService bankService = new BankService();

        public static void main(String[] args) {
            Menu.printHeader();

            boolean running = true;
            while (running) {
                Menu.printMainMenu();
                int choice = readInt();

                try {
                    switch (choice) {
                        case 1 -> handleCreateAccount();
                        case 2 -> handleViewAccount();
                        case 3 -> handleViewAllAccounts();
                        case 4 -> handleDeposit();
                        case 5 -> handleWithdraw();
                        case 6 -> handleTransfer();
                        case 7 -> handleCheckBalance();
                        case 8 -> handleTransactionHistory();
                        case 9 -> handleDeleteAccount();
                        case 10 -> handleSearchCustomer();
                        case 11 -> handleTopRichest();
                        case 12 -> handleBalanceRangeSearch();
                        case 13 -> handleExportCSV();
                        case 14 -> handleInterestCalculator();
                        case 0 -> {
                            running = false;
                            System.out.println("Thank you for using the Bank Management System. Goodbye!");
                        }
                        default -> System.out.println("Invalid choice. Please try again.");
                    }
                } catch (AccountNotFoundException | InvalidAmountException | InsufficientBalanceException e) {
                    // These are our CUSTOM checked exceptions - always business-rule
                    // violations, so a clean message is enough (no stack trace needed).
                    System.out.println("Error: " + e.getMessage());
                } catch (SQLException e) {
                    // Database-level failure (connection lost, constraint violation, etc.)
                    System.out.println("Database error occurred: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                }
            }

            scanner.close();
        }

        // ================= HANDLERS =================

        private static void handleCreateAccount() throws SQLException, InvalidAmountException {
            System.out.print("Enter Customer Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Phone (10 digits): ");
            String phone = scanner.nextLine();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Opening Balance (min 500): ");
            double balance = readDouble();

            Account account = bankService.createAccount(name, phone, email, balance);
            Menu.printLine();
            System.out.println("Account created successfully!");
            System.out.println(account);
            Menu.printLine();
        }

        private static void handleViewAccount() throws SQLException, AccountNotFoundException {
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine();
            Account account = bankService.getAccount(accNo);
            Menu.printLine();
            System.out.println(account);
            Menu.printLine();
        }

        private static void handleViewAllAccounts() throws SQLException {
            List<Account> accounts = bankService.getAllAccounts();
            Menu.printLine();
            if (accounts.isEmpty()) {
                System.out.println("No accounts found.");
            } else {
                for (Account a : accounts) {
                    System.out.println(a);
                    Menu.printLine();
                }
            }
        }

        private static void handleDeposit() throws SQLException, AccountNotFoundException, InvalidAmountException {
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine();
            System.out.print("Enter Amount to Deposit: ");
            double amount = readDouble();

            bankService.deposit(accNo, amount);
            System.out.println("Deposit successful! New balance: Rs. " + bankService.getAccount(accNo).getBalance());
        }

        private static void handleWithdraw() throws SQLException, AccountNotFoundException,
                InvalidAmountException, InsufficientBalanceException {
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine();
            System.out.print("Enter Amount to Withdraw: ");
            double amount = readDouble();

            bankService.withdraw(accNo, amount);
            System.out.println("Withdrawal successful! New balance: Rs. " + bankService.getAccount(accNo).getBalance());
        }

        private static void handleTransfer() throws SQLException, AccountNotFoundException,
                InvalidAmountException, InsufficientBalanceException {
            System.out.print("Enter Source Account Number: ");
            String fromAcc = scanner.nextLine();
            System.out.print("Enter Destination Account Number: ");
            String toAcc = scanner.nextLine();
            System.out.print("Enter Amount to Transfer: ");
            double amount = readDouble();

            bankService.transfer(fromAcc, toAcc, amount);
            System.out.println("Transfer successful!");
        }

        private static void handleCheckBalance() throws SQLException, AccountNotFoundException {
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine();
            Account account = bankService.getAccount(accNo);
            System.out.println("Current Balance: Rs. " + account.getBalance());
        }

        private static void handleTransactionHistory() throws SQLException, AccountNotFoundException {
            System.out.print("Enter Account Number: ");
            String accNo = scanner.nextLine();
            List<Transaction> transactions = bankService.getTransactionHistory(accNo);

            Menu.printLine();
            if (transactions.isEmpty()) {
                System.out.println("No transactions found for this account.");
            } else {
                for (Transaction t : transactions) {
                    System.out.println(t);
                }
            }
            Menu.printLine();
        }

        private static void handleDeleteAccount() throws SQLException, AccountNotFoundException, InsufficientBalanceException {
            System.out.print("Enter Account Number to Delete: ");
            String accNo = scanner.nextLine();
            bankService.deleteAccount(accNo);
            System.out.println("Account deleted successfully.");
        }

        private static void handleSearchCustomer() throws SQLException {
            System.out.print("Enter Customer Name (or part of it): ");
            String name = scanner.nextLine();
            List<Account> results = bankService.searchByName(name);

            Menu.printLine();
            if (results.isEmpty()) {
                System.out.println("No matching customers found.");
            } else {
                for (Account a : results) {
                    System.out.println(a);
                    Menu.printLine();
                }
            }
        }

        private static void handleTopRichest() throws SQLException {
            List<Account> accounts = bankService.getTopRichestCustomers(5);
            Menu.printLine();
            System.out.println("Top 5 Richest Customers:");
            int rank = 1;
            for (Account a : accounts) {
                System.out.println(rank++ + ". " + a.getCustomerName() + " - Rs. " + a.getBalance());
            }
            Menu.printLine();
        }

        private static void handleBalanceRangeSearch() throws SQLException {
            System.out.print("Enter Minimum Balance: ");
            double min = readDouble();
            System.out.print("Enter Maximum Balance: ");
            double max = readDouble();

            List<Account> accounts = bankService.getAccountsByBalanceRange(min, max);
            Menu.printLine();
            if (accounts.isEmpty()) {
                System.out.println("No accounts found in this balance range.");
            } else {
                for (Account a : accounts) {
                    System.out.println(a);
                    Menu.printLine();
                }
            }
        }

        private static void handleExportCSV() {
            try {
                System.out.print("Enter Account Number: ");
                String accNo = scanner.nextLine();
                String fileName = "transactions_" + accNo + ".csv";
                String path = bankService.exportTransactionsToCSV(accNo, fileName);
                System.out.println("Exported successfully to: " + path);
            } catch (Exception e) {
                System.out.println("Export failed: " + e.getMessage());
            }
        }

        private static void handleInterestCalculator() {
            System.out.print("Enter Principal Amount: ");
            double principal = readDouble();
            System.out.print("Enter Annual Interest Rate (%): ");
            double rate = readDouble();
            System.out.print("Enter Number of Years: ");
            int years = readInt();

            double interest = bankService.calculateInterest(principal, rate, years);
            System.out.println("Simple Interest: Rs. " + interest);
            System.out.println("Total Amount after " + years + " years: Rs. " + (principal + interest));
        }

        // ================= INPUT HELPERS =================

        private static int readInt() {
            while (true) {
                try {
                    int value = Integer.parseInt(scanner.nextLine().trim());
                    return value;
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a valid whole number: ");
                }
            }
        }

        private static double readDouble() {
            while (true) {
                try {
                    return Double.parseDouble(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.print("Please enter a valid number: ");
                }
            }
        }
    }
}
