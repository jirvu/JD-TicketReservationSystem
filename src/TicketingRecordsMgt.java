import java.sql.*;
import java.util.*;

public class TicketingRecordsMgt {

    private final Scanner scanner = new Scanner(System.in);
    private final Connection connection;

    public TicketingRecordsMgt(Connection connection) {
        this.connection = connection;
    }

    public void showRecordsMenu() {
        boolean programRun = true;
        while (programRun) {
            System.out.println("\n=== Ticketing Records Management System ===");
            System.out.println("[1] Manage Customer Records");
            System.out.println("[2] Manage Ticket Records");
            System.out.println("[3] Manage Transaction Records");
            System.out.println("[4] Manage Event Records");
            System.out.println("[5] Exit");
            System.out.print("Choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    try {
                        new CustomerRecords(connection).showMenu();
                    } catch (Exception e) {
                        System.err.println("Error launching Customer Records: " + e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        new TicketRecords(connection).showMenu();
                    } catch (SQLException e) {
                        System.err.println("Error launching Ticket Records: " + e.getMessage());
                    }
                }
                case 3 -> {
                    try {
                        new TransactionRecords(connection).showMenu();
                    } catch (SQLException e) {
                        System.err.println("Error launching Ticket Records: " + e.getMessage());
                    }
                }
                case 4 -> {
                    try {
                        new EventRecords(connection).showMenu();
                    } catch (SQLException e) {
                        System.err.println("Error managing Event Records: " + e.getMessage());
                    }
                }
                case 5 -> {
                    System.out.println("Exiting System...");
                    programRun = false;
                }
                default ->
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}



