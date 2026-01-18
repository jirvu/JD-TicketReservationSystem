import java.sql.*;
import java.util.Scanner;

public class CustomerRecords {
    private final Connection connection;
    private final Scanner scanner = new Scanner(System.in);

    public CustomerRecords(Connection connection) {
        this.connection = connection;
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Customer Records Management ===");
            System.out.println("[1] View All Customers");
            System.out.println("[2] View Customer by ID");
            System.out.println("[3] Back to Main Menu");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAllCustomers();
                case 2 -> viewCustomerById();
                case 3 -> running = false;
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void viewAllCustomers() {
        String sql = "SELECT * FROM Customers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- Customer List ---");
            while (rs.next()) {
                System.out.printf("ID: %d | Name: %s %s | Email: %s | Phone: %s%n",
                        rs.getInt("customer_id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("email"), rs.getString("phone_number"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
        }
    }

    private void viewCustomerById() {
        System.out.print("Enter Customer ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        String customerSql = "SELECT * FROM Customers WHERE customer_id = ?";
        String historySql = "SELECT t.transaction_id, t.ticket_id, t.transaction_status, t.amount_paid, e.event_name " +
                "FROM Transactions t " +
                "JOIN Tickets tk ON t.ticket_id = tk.ticket_id " +
                "JOIN Events e ON tk.event_id = e.event_id " +
                "WHERE t.customer_id = ?";

        try (PreparedStatement customerStmt = connection.prepareStatement(customerSql)) {
            customerStmt.setInt(1, id);
            try (ResultSet rs = customerStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("\nCustomer ID: %d\nName: %s %s\nEmail: %s\nPhone: %s\n",
                            rs.getInt("customer_id"), rs.getString("first_name"), rs.getString("last_name"),
                            rs.getString("email"), rs.getString("phone_number"));

                    System.out.println("\n--- Transaction History ---");
                    try (PreparedStatement historyStmt = connection.prepareStatement(historySql)) {
                        historyStmt.setInt(1, id);
                        try (ResultSet histRs = historyStmt.executeQuery()) {
                            while (histRs.next()) {
                                System.out.printf("Transaction ID: %d | Ticket ID: %d | Event: %s | Status: %s | Amount: %.2f%n",
                                        histRs.getInt("transaction_id"),
                                        histRs.getInt("ticket_id"),
                                        histRs.getString("event_name"),
                                        histRs.getString("transaction_status"),
                                        histRs.getDouble("amount_paid"));
                            }
                        }
                    }
                } else {
                    System.out.println("Customer not found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving customer: " + e.getMessage());
        }
    }
}
