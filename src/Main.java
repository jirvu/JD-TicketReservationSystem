
import java.sql.*;
import util.DatabaseConnection;
import util.Utilities;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("Database Connected!");

            boolean programRun = true;
            while (programRun) {
                System.out.println("\n=== Main Menu ===");
                System.out.println("[1] Ticketing Records");
                System.out.println("[2] Ticketing Transactions");
                System.out.println("[3] Ticketing Reports");
                System.out.println("[4] Exit");

                int choice = Utilities.getUserInput("Choice: ");

                switch (choice) {
                    case 1 -> {
                        System.out.println("Launching Ticketing Records...");
                        new TicketingRecordsMgt(connection).showRecordsMenu();
                    }
                    case 2 -> {
                        System.out.println("Launching Ticketing Transactions...");
                        new TicketingTransactions(connection).showTransactionsMenu();
                    }
                    case 3 -> {
                        System.out.println("Launching Ticketing Reports...");
                        new TicketingReports(connection).showReportsMenu();
                    }
                    case 4 -> {
                        System.out.println("Exiting System...");
                        programRun = false;
                    }
                    default ->
                        System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
