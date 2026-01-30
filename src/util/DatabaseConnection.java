package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/dbtickets";
    private static final String USER = "root";
    private static final String PASSWORD = "user"; // Update this with your actual MySQL root password
    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);
                System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found. Please add the connector JAR to your project.");
                e.printStackTrace();
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                handleSQLException(e);
                throw e;
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
                throw new SQLException("Failed to initialize database connection", e);
            }
        }
        return connection;
    }

    private static void handleSQLException(SQLException e) {
        String message = e.getMessage();
        if (message.contains("Access denied")) {
            System.err.println("""
                    Database Connection Failed
                    Possible cause: Incorrect MySQL password.

                    Current Settings:
                    - Username: %s
                    - Password: %s

                    To fix:
                    1. Open DatabaseConnection.java
                    2. Update the PASSWORD field with your MySQL root password
                    3. Restart the application
                    """.formatted(USER, PASSWORD.isEmpty() ? "(empty)" : "(set)"));
        } else if (message.contains("Unknown database")) {
            System.err.println("""
                    Database 'dbtickets' not found.
                    To fix:
                    1. Open MySQL Workbench or Terminal
                    2. Run: CREATE DATABASE dbtickets;
                    3. Import your database schema.
                    """);
        } else {
            System.err.println("Database Error: " + message);
        }
        e.printStackTrace();
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
