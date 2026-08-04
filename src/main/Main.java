package main;
import util.Menu;
/**
 * Main.java
 * -----------
 * Application entry point. Kept intentionally tiny - its only job is to
 * hand control over to the Menu (UI layer). This is good practice: the
 * "main" method should orchestrate, not implement, business logic.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(Constants.DB_PASSWORD);
        Menu menu = new Menu();
        menu.start();
    }
}
