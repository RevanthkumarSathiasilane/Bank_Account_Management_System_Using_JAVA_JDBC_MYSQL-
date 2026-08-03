package com.util;
public class Menu {

    public static void printHeader() {
        System.out.println("========================================");
        System.out.println("     BANK ACCOUNT MANAGEMENT SYSTEM");
        System.out.println("========================================");
    }
    public static void printMainMenu() {
        System.out.println("\n----------------------------------------");
        System.out.println(" 1.  Create Account");
        System.out.println(" 2.  View Account");
        System.out.println(" 3.  View All Accounts");
        System.out.println(" 4.  Deposit Money");
        System.out.println(" 5.  Withdraw Money");
        System.out.println(" 6.  Transfer Money");
        System.out.println(" 7.  Check Balance");
        System.out.println(" 8.  Transaction History");
        System.out.println(" 9.  Delete Account");
        System.out.println("10.  Search Customer");
        System.out.println("11.  Bonus: Top 5 Richest Customers");
        System.out.println("12.  Bonus: Search by Balance Range");
        System.out.println("13.  Bonus: Export Transaction History to CSV");
        System.out.println("14.  Bonus: Interest Calculator");
        System.out.println(" 0.  Exit");
        System.out.println("----------------------------------------");
        System.out.print("Enter Choice: ");
    }

    public static void printLine() {
        System.out.println("----------------------------------------");
    }

    private Menu() {
    }
}