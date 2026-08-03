package config;

import util.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 * -------------------------
 * Single Responsibility: create and hand out JDBC Connection objects.
 *
 * INTERVIEW CONCEPT - JDBC Architecture:
 *   Java Application -> JDBC API -> JDBC Driver Manager -> JDBC Driver -> Database
 *
 *   1. DriverManager.getConnection(url, user, password) asks the
 *      DriverManager to find a registered Driver that understands the
 *      given URL ("jdbc:mysql://...") and returns a live Connection.
 *   2. Since JDBC 4.0, we no longer need Class.forName("...Driver") -
 *      the driver is auto-loaded via the META-INF/services mechanism
 *      (Service Provider Interface) as long as the MySQL Connector/J
 *      jar is on the classpath.
 *
 * We do NOT use a static/shared Connection field here on purpose:
 * each DAO call requests a *fresh* connection and closes it (via
 * try-with-resources) as soon as the operation is complete. This is the
 * simplest correct pattern for a console application. For a real
 * production web application you would instead use a CONNECTION POOL
 * (e.g. HikariCP) - see README.md "Connection Pooling" explanation.
 */
public class DatabaseConnection {

    /**
     * Opens and returns a new JDBC connection to the bank_management
     * database. The caller is responsible for closing the connection
     * (best done with try-with-resources).
     *
     * @return an open java.sql.Connection
     * @throws SQLException if the connection cannot be established
     *                       (wrong URL, DB not running, bad credentials, etc.)
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Constants.DB_URL,
                Constants.DB_USER,
                Constants.DB_PASSWORD
        );
    }
}
