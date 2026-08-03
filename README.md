# Bank Account Management System (Java + JDBC + MySQL)

A console-based banking application built with **Core Java, JDBC, and MySQL only**
(no Spring, no Hibernate, no build tool required). Designed to be a clean,
interview-ready demonstration of JDBC, SQL, exception handling, transaction
management, and layered OOP architecture.

---

## Features

| # | Feature |
|---|---------|
| 1 | Create Account (with validation + duplicate email check) |
| 2 | View Account |
| 3 | View All Accounts |
| 4 | Deposit Money |
| 5 | Withdraw Money (custom `InsufficientBalanceException`) |
| 6 | Transfer Money (atomic JDBC transaction: commit/rollback) |
| 7 | Check Balance |
| 8 | Transaction History (latest first) |
| 9 | Delete Account (only if balance is zero) |
| 10 | Search Customer (by account number or name) |
| 11 | **Bonus:** Interest Calculator, Mini Statement, Top 5 Richest Customers, Balance Range Search, Daily/Monthly Reports |
| — | Admin login with limited attempts (before menu is shown) |

---

## Technology Stack

- Java 17+ (Core Java only)
- JDBC (`java.sql.*`)
- MySQL 8
- MySQL Connector/J (JDBC driver jar - see setup below)
- IntelliJ IDEA / Eclipse
- Git

---

## Folder Structure

```
BankManagementSystem/
├── src/
│   ├── config/
│   │   └── DatabaseConnection.java     # Creates JDBC connections
│   ├── dao/
│   │   ├── AccountDAO.java             # DAO interface (abstraction)
│   │   └── TransactionDAO.java
│   ├── daoimpl/
│   │   ├── AccountDAOImpl.java         # JDBC implementation (PreparedStatement)
│   │   └── TransactionDAOImpl.java
│   ├── model/
│   │   ├── Account.java                # Entity / POJO
│   │   └── Transaction.java
│   ├── service/
│   │   └── BankService.java            # Business logic + transaction mgmt
│   ├── util/
│   │   ├── InputValidator.java         # Validation helpers
│   │   ├── Menu.java                   # Console UI (View + Controller)
│   │   └── Constants.java              # DB config, business constants
│   ├── exception/
│   │   ├── InsufficientBalanceException.java
│   │   ├── AccountNotFoundException.java
│   │   └── InvalidAmountException.java
│   └── main/
│       └── Main.java                   # Entry point
├── sql/
│   └── bank_management.sql             # CREATE DATABASE/TABLE + sample data
├── lib/                                 # Put mysql-connector-j-x.x.x.jar here
└── README.md
```

> **Where does the "database file" go?**
> MySQL is a server-based database, not a single file like SQLite, so there is
> no `.db` file to place inside the project. Instead:
> 1. The **schema/setup script** lives at `sql/bank_management.sql` — run it
     >    once against your MySQL server to create the `bank_management` database.
> 2. The **JDBC driver jar** (`mysql-connector-j-x.x.x.jar`), which is the file
     >    your Java code actually needs at compile/run time, goes in the `lib/`
     >    folder of this project and is added to the classpath (see "How to Run").

---

## Database Setup

1. Install and start MySQL 8 locally.
2. Run the setup script:
   ```bash
   mysql -u root -p < sql/bank_management.sql
   ```
   This creates the `bank_management` database, the `accounts` and
   `transactions` tables, and inserts 3 sample customers.
3. Update credentials in `src/util/Constants.java` if your MySQL
   username/password differ from `root` / `root`:
   ```java
   public static final String DB_URL = "jdbc:mysql://localhost:3306/bank_management";
   public static final String DB_USER = "root";
   public static final String DB_PASSWORD = "root";
   ```

---

## How to Run

### Step 1 — Get the MySQL JDBC driver
Download **MySQL Connector/J** (`mysql-connector-j-8.x.x.jar`) from
[dev.mysql.com/downloads/connector/j](https://dev.mysql.com/downloads/connector/j/)
and place it inside the `lib/` folder of this project.

### Step 2 — Compile
From the project root:
```bash
javac -d out $(find src -name "*.java")
```

### Step 3 — Run (with the driver on the classpath)
**Linux/Mac:**
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" main.Main
```
**Windows:**
```bash
java -cp "out;lib/mysql-connector-j-8.x.x.jar" main.Main
```

### Using IntelliJ IDEA / Eclipse instead
1. Open the project folder as a new Java project (no build tool needed).
2. Mark `src` as the **Sources Root**.
3. Add `lib/mysql-connector-j-8.x.x.jar` to **Project Structure → Libraries**.
4. Run `main.Main`.

### First login
The console will prompt for an admin login before showing the menu:
- Username: `admin`
- Password: `admin@123`
  (Change these in `Constants.java` before sharing/demoing the project.)

---

## Interview Concepts Demonstrated

- **PreparedStatement vs Statement** — every query in `AccountDAOImpl` /
  `TransactionDAOImpl` uses `PreparedStatement` with `?` placeholders,
  preventing SQL Injection. `Statement` is never used.
- **JDBC Transaction Management (ACID)** — `BankService.transfer()` sets
  `conn.setAutoCommit(false)`, performs the debit + credit + two transaction
  inserts on one `Connection`, then calls `commit()` on success or
  `rollback()` on any failure, guaranteeing atomicity.
- **try-with-resources** — `Connection`, `PreparedStatement`, and `ResultSet`
  are declared in `try(...)` blocks so they are always closed automatically.
- **DAO Pattern** — `AccountDAO`/`TransactionDAO` interfaces decouple
  `BankService` (business logic) from the JDBC implementation classes.
- **MVC-style layering** — Model (`model/`), View/Controller (`util/Menu.java`),
  business logic (`service/BankService.java`).
- **OOP Principles** — Encapsulation (private fields + getters/setters in
  `Account`/`Transaction`), Abstraction (DAO interfaces), Polymorphism
  (`AccountDAO accountDAO = new AccountDAOImpl();`).
- **Custom Exceptions** — `InsufficientBalanceException`,
  `AccountNotFoundException`, `InvalidAmountException` make business rule
  failures explicit and give the UI layer friendly error messages instead of
  raw stack traces.

---

## Future Improvements

- Replace the manual `Connection`-per-call pattern with a real connection
  pool (e.g. HikariCP) for production use.
- Add a JavaFX or Swing GUI on top of the existing `BankService` layer
  without changing any business logic.
- Add JUnit test coverage for `InputValidator` and `BankService` using a
  mocked/in-memory DAO.
- Add pagination for `View All Accounts` when the customer base grows large.
# Bank Account Management System (Java + JDBC + MySQL)

A console-based banking application built with **Core Java, JDBC, and MySQL only**
(no Spring, no Hibernate, no build tool required). Designed to be a clean,
interview-ready demonstration of JDBC, SQL, exception handling, transaction
management, and layered OOP architecture.

---

## Features

| # | Feature |
|---|---------|
| 1 | Create Account (with validation + duplicate email check) |
| 2 | View Account |
| 3 | View All Accounts |
| 4 | Deposit Money |
| 5 | Withdraw Money (custom `InsufficientBalanceException`) |
| 6 | Transfer Money (atomic JDBC transaction: commit/rollback) |
| 7 | Check Balance |
| 8 | Transaction History (latest first) |
| 9 | Delete Account (only if balance is zero) |
| 10 | Search Customer (by account number or name) |
| 11 | **Bonus:** Interest Calculator, Mini Statement, Top 5 Richest Customers, Balance Range Search, Daily/Monthly Reports |
| — | Admin login with limited attempts (before menu is shown) |

---

## Technology Stack

- Java 17+ (Core Java only)
- JDBC (`java.sql.*`)
- MySQL 8
- MySQL Connector/J (JDBC driver jar - see setup below)
- IntelliJ IDEA / Eclipse
- Git

---

## Folder Structure

```
BankManagementSystem/
├── src/
│   ├── config/
│   │   └── DatabaseConnection.java     # Creates JDBC connections
│   ├── dao/
│   │   ├── AccountDAO.java             # DAO interface (abstraction)
│   │   └── TransactionDAO.java
│   ├── daoimpl/
│   │   ├── AccountDAOImpl.java         # JDBC implementation (PreparedStatement)
│   │   └── TransactionDAOImpl.java
│   ├── model/
│   │   ├── Account.java                # Entity / POJO
│   │   └── Transaction.java
│   ├── service/
│   │   └── BankService.java            # Business logic + transaction mgmt
│   ├── util/
│   │   ├── InputValidator.java         # Validation helpers
│   │   ├── Menu.java                   # Console UI (View + Controller)
│   │   └── Constants.java              # DB config, business constants
│   ├── exception/
│   │   ├── InsufficientBalanceException.java
│   │   ├── AccountNotFoundException.java
│   │   └── InvalidAmountException.java
│   └── main/
│       └── Main.java                   # Entry point
├── sql/
│   └── bank_management.sql             # CREATE DATABASE/TABLE + sample data
├── lib/                                 # Put mysql-connector-j-x.x.x.jar here
└── README.md
```

> **Where does the "database file" go?**
> MySQL is a server-based database, not a single file like SQLite, so there is
> no `.db` file to place inside the project. Instead:
> 1. The **schema/setup script** lives at `sql/bank_management.sql` — run it
     >    once against your MySQL server to create the `bank_management` database.
> 2. The **JDBC driver jar** (`mysql-connector-j-x.x.x.jar`), which is the file
     >    your Java code actually needs at compile/run time, goes in the `lib/`
     >    folder of this project and is added to the classpath (see "How to Run").

---

## Database Setup

1. Install and start MySQL 8 locally.
2. Run the setup script:
   ```bash
   mysql -u root -p < sql/bank_management.sql
   ```
   This creates the `bank_management` database, the `accounts` and
   `transactions` tables, and inserts 3 sample customers.
3. Update credentials in `src/util/Constants.java` if your MySQL
   username/password differ from `root` / `root`:
   ```java
   public static final String DB_URL = "jdbc:mysql://localhost:3306/bank_management";
   public static final String DB_USER = "root";
   public static final String DB_PASSWORD = "root";
   ```

---

## How to Run

### Step 1 — Get the MySQL JDBC driver
Download **MySQL Connector/J** (`mysql-connector-j-8.x.x.jar`) from
[dev.mysql.com/downloads/connector/j](https://dev.mysql.com/downloads/connector/j/)
and place it inside the `lib/` folder of this project.

### Step 2 — Compile
From the project root:
```bash
javac -d out $(find src -name "*.java")
```

### Step 3 — Run (with the driver on the classpath)
**Linux/Mac:**
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" main.Main
```
**Windows:**
```bash
java -cp "out;lib/mysql-connector-j-8.x.x.jar" main.Main
```

### Using IntelliJ IDEA / Eclipse instead
1. Open the project folder as a new Java project (no build tool needed).
2. Mark `src` as the **Sources Root**.
3. Add `lib/mysql-connector-j-8.x.x.jar` to **Project Structure → Libraries**.
4. Run `main.Main`.

### First login
The console will prompt for an admin login before showing the menu:
- Username: `admin`
- Password: `admin@123`
  (Change these in `Constants.java` before sharing/demoing the project.)

---

## Interview Concepts Demonstrated

- **PreparedStatement vs Statement** — every query in `AccountDAOImpl` /
  `TransactionDAOImpl` uses `PreparedStatement` with `?` placeholders,
  preventing SQL Injection. `Statement` is never used.
- **JDBC Transaction Management (ACID)** — `BankService.transfer()` sets
  `conn.setAutoCommit(false)`, performs the debit + credit + two transaction
  inserts on one `Connection`, then calls `commit()` on success or
  `rollback()` on any failure, guaranteeing atomicity.
- **try-with-resources** — `Connection`, `PreparedStatement`, and `ResultSet`
  are declared in `try(...)` blocks so they are always closed automatically.
- **DAO Pattern** — `AccountDAO`/`TransactionDAO` interfaces decouple
  `BankService` (business logic) from the JDBC implementation classes.
- **MVC-style layering** — Model (`model/`), View/Controller (`util/Menu.java`),
  business logic (`service/BankService.java`).
- **OOP Principles** — Encapsulation (private fields + getters/setters in
  `Account`/`Transaction`), Abstraction (DAO interfaces), Polymorphism
  (`AccountDAO accountDAO = new AccountDAOImpl();`).
- **Custom Exceptions** — `InsufficientBalanceException`,
  `AccountNotFoundException`, `InvalidAmountException` make business rule
  failures explicit and give the UI layer friendly error messages instead of
  raw stack traces.

---

## Future Improvements

- Replace the manual `Connection`-per-call pattern with a real connection
  pool (e.g. HikariCP) for production use.
- Add a JavaFX or Swing GUI on top of the existing `BankService` layer
  without changing any business logic.
- Add JUnit test coverage for `InputValidator` and `BankService` using a
  mocked/in-memory DAO.
- Add pagination for `View All Accounts` when the customer base grows large.
