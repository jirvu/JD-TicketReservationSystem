import java.sql.*;
import java.util.*;
import util.Utilities;

public class TransactionRecords {
    private final Connection connection;
    private final Scanner scanner = new Scanner(System.in);

    public TransactionRecords(Connection connection) {
        this.connection = connection;
    }

public void showMenu() throws SQLException {
    boolean running = true;
    while (running) {
        System.out.println("\n=== Transaction Records Management ===");
        System.out.println("[1] Add transaction details");
        System.out.println("[2] Remove transaction details");
        System.out.println("[3] View transaction details");
        System.out.println("[4] Exit");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> addTransactionDetails();
            case 2 -> removeTransactionDetails();
            case 3 -> viewTransactionDetails();
            case 4 -> running = false;
            default -> System.out.println("Invalid choice. Try again.");
        }
    }
}

    private void addTransactionDetails() {
    try {
        System.out.println("\n=== Add New Transaction ===");

        int customerId = Utilities.getUserInput("Customer ID: ");
        int ticketId = Utilities.getUserInput("Ticket ID: ");
        scanner.nextLine(); // consume leftover newline

        System.out.print("Purchase Date (YYYY-MM-DD): ");
        String purchaseDate = scanner.nextLine().trim();

        System.out.print("Payment Method (e.g., Cash, Credit): ");
        String paymentMethod = scanner.nextLine().trim();

        double amountPaid;
        while (true) {
            try {
                System.out.print("Amount Paid: ");
                amountPaid = Double.parseDouble(scanner.nextLine().trim());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Try again.");
            }
        }

        System.out.print("Transaction Status (e.g., Confirmed, Pending): ");
        String transactionStatus = scanner.nextLine().trim();

        String insertQuery = """
            INSERT INTO Transaction (customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status)
            VALUES (?, ?, ?, ?, ?, ?);
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, ticketId);
            pstmt.setString(3, purchaseDate);
            pstmt.setString(4, paymentMethod);
            pstmt.setDouble(5, amountPaid);
            pstmt.setString(6, transactionStatus);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Transaction added successfully.");

                // Insert default transaction statistics
                String statsInsert = """
                    INSERT INTO Transaction_Statistics 
                    (customer_id, ticket_id, failed_tickets, pending_tickets, confirmed_tickets, cancelled_tickets, refunded_tickets)
                    VALUES (?, ?, 0, 0, 0, 0, 0);
                """;

                try (PreparedStatement statStmt = connection.prepareStatement(statsInsert)) {
                    statStmt.setInt(1, customerId);
                    statStmt.setInt(2, ticketId);
                    statStmt.executeUpdate();
                    System.out.println("Default transaction statistics added.");
                } catch (SQLException e) {
                    System.out.println("Warning: Could not insert statistics. " + e.getMessage());
                }

            } else {
                System.out.println("Failed to add transaction.");
            }
        }

    } catch (SQLException e) {
        System.out.println("SQL error while adding transaction: " + e.getMessage());
    }
}


    private void removeTransactionDetails() {
    try {
        System.out.println("\n=== Remove Transaction ===");

        int transactionId = Utilities.getUserInput("Enter Transaction ID to remove: ");

        // Retrieve customer_id and ticket_id before deleting
        String fetchQuery = "SELECT customer_id, ticket_id FROM Transaction WHERE transaction_id = ?";
        int customerId = -1;
        int ticketId = -1;

        try (PreparedStatement fetchStmt = connection.prepareStatement(fetchQuery)) {
            fetchStmt.setInt(1, transactionId);
            try (ResultSet rs = fetchStmt.executeQuery()) {
                if (rs.next()) {
                    customerId = rs.getInt("customer_id");
                    ticketId = rs.getInt("ticket_id");
                } else {
                    System.out.println("Transaction ID not found.");
                    return;
                }
            }
        }

        // Delete from Transaction_Statistics
        String deleteStats = "DELETE FROM Transaction_Statistics WHERE customer_id = ? AND ticket_id = ?";
        try (PreparedStatement statsStmt = connection.prepareStatement(deleteStats)) {
            statsStmt.setInt(1, customerId);
            statsStmt.setInt(2, ticketId);
            statsStmt.executeUpdate();
            System.out.println("Associated statistics removed.");
        }

        // Delete from Transaction
        String deleteQuery = "DELETE FROM Transaction WHERE transaction_id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, transactionId);

            int rowsDeleted = deleteStmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Transaction deleted successfully.");
            } else {
                System.out.println("Failed to delete transaction.");
            }
        }

    } catch (SQLException e) {
        System.out.println("SQL error while removing transaction: " + e.getMessage());
    }
}


    private void viewTransactionDetails() {
        boolean programRun = true;
        while (programRun) {
            String query = "SELECT transaction_id, customer_id, ticket_id FROM Transaction;";

            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet resultSet = pstmt.executeQuery()) {

                List<Integer> transactionIds = new ArrayList<>();

                System.out.println("List of all transactions:");
                while (resultSet.next()) {
                    int id = resultSet.getInt("transaction_id");
                    int customerId = resultSet.getInt("customer_id");
                    int ticketId = resultSet.getInt("ticket_id");

                    transactionIds.add(id);
                    System.out.printf("[%d] Customer ID: %d | Ticket ID: %d\n", id, customerId, ticketId);
                }

                int id;
                boolean validInput = false;
                while (!validInput) {
                    try {
                        id = Utilities.getUserInput("Transaction ID to view (Enter 0 to go back): ");

                        if (id == 0) {
                            System.out.println("Returning to previous menu...");
                            programRun = false;
                            break;
                        }

                        if (transactionIds.contains(id)) {
                            validInput = true;

                            String detailQuery = """
                                SELECT t.transaction_id, t.customer_id, t.ticket_id, t.purchase_date, t.payment_method,
                                       t.amount_paid, t.transaction_status,
                                       ts.failed_tickets, ts.pending_tickets, ts.confirmed_tickets,
                                       ts.cancelled_tickets, ts.refunded_tickets
                                FROM Transaction t
                                LEFT JOIN Transaction_Statistics ts
                                ON t.customer_id = ts.customer_id AND t.ticket_id = ts.ticket_id
                                WHERE t.transaction_id = ?;
                                """;

                            try (PreparedStatement detailStmt = connection.prepareStatement(detailQuery)) {
                                detailStmt.setInt(1, id);

                                try (ResultSet detailResult = detailStmt.executeQuery()) {
                                    System.out.println("\nTransaction Details:");
                                    System.out.println("-".repeat(100));
                                    System.out.printf("%-15s %-12s %-10s %-12s %-15s %-12s %-12s %-8s %-8s %-8s %-8s %-8s\n",
                                            "Transaction ID", "Customer ID", "Ticket ID", "Purchase", "Payment", "Amount",
                                            "Status", "Failed", "Pending", "Confirmed", "Cancelled", "Refunded");
                                    System.out.println("-".repeat(100));

                                    boolean hasRecords = false;

                                    while (detailResult.next()) {
                                        hasRecords = true;

                                        System.out.printf("%-15d %-12d %-10d %-12s %-15s %-12.2f %-12s %-8d %-8d %-8d %-8d %-8d\n",
                                                detailResult.getInt("transaction_id"),
                                                detailResult.getInt("customer_id"),
                                                detailResult.getInt("ticket_id"),
                                                detailResult.getString("purchase_date"),
                                                detailResult.getString("payment_method"),
                                                detailResult.getDouble("amount_paid"),
                                                detailResult.getString("transaction_status"),
                                                detailResult.getInt("failed_tickets"),
                                                detailResult.getInt("pending_tickets"),
                                                detailResult.getInt("confirmed_tickets"),
                                                detailResult.getInt("cancelled_tickets"),
                                                detailResult.getInt("refunded_tickets")
                                        );
                                    }

                                    if (!hasRecords) {
                                        System.out.println("No records found for this transaction.");
                                    }

                                    System.out.println("-".repeat(100));
                                }
                            }

                        } else {
                            System.out.println("Transaction ID not found.");
                        }
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please try again.");
                        scanner.nextLine(); // clear buffer
                    }
                }

            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
                if (!confirmContinue()) {
                    programRun = false;
                }
            }

            if (programRun && !confirmContinue()) {
                System.out.println("Exiting view transactions menu...");
                programRun = false;
            }
        }
    }

    private boolean confirmContinue() {
        while (true) {
            int choice = Utilities.getUserInput("Continue viewing transactions? (1 - yes, 2 - no): ");
            switch (choice) {
                case 1 -> { return true; }
                case 2 -> { return false; }
                default -> System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
}
