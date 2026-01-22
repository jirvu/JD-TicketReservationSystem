import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import util.Utilities;

public class TicketingTransactions {

    private final Scanner scanner = new Scanner(System.in);
    private final Connection connection;

    public TicketingTransactions(Connection connection) {
        this.connection = connection;
    }

    public void showTransactionsMenu() {
        boolean runProgram = true;

        while (runProgram) {
            System.out.println("\nTicket Transaction Queries");
            System.out.println("[1] Place an Order");
            System.out.println("[2] Refund or Cancel an Order");
            System.out.println("[3] Transfer Tickets");
            System.out.println("[4] Manage Event Status");
            System.out.println("[5] Exit Transactions");

            try {
                int choice = Utilities.getUserInput("Choice: ");
                switch (choice) {
                    case 1 -> placeOrder();
                    case 2 -> refundCancelOrder();
                    case 3 -> transferTickets();
                    case 4 -> manageEventStatus();
                    case 5 -> {
                        System.out.println("Exiting System...");
                        runProgram = false;
                    }
                    default ->
                        System.out.println("Invalid input. Please try again.");
                }

            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public void placeOrder() {
    String availableTicketsQuery = """
        SELECT t.ticket_id, t.event_id, t.ticket_type, t.price
        FROM Tickets t
        WHERE t.ticket_status = 'Available'
          AND t.ticket_id NOT IN (
              SELECT ticket_id FROM Transactions WHERE customer_id = ?
          )
    """;
    String selectTicketQuery = "SELECT event_id, price, ticket_status, ticket_type FROM Tickets WHERE ticket_id = ?";
    String insertTransactionQuery = "INSERT INTO Transactions (customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status) VALUES (?, ?, ?, ?, ?, ?)";
    String updateTicketQuery = "UPDATE Tickets SET ticket_status = 'Sold' WHERE ticket_id = ?";
    String updateStatsQuery = "UPDATE Ticket_Statistics SET sold_tickets = sold_tickets + 1, available_tickets = available_tickets - 1 WHERE event_id = ? AND ticket_type = ?";
    String insertHistoryQuery = "INSERT INTO Transaction_History (transaction_id, customer_id) VALUES (?, ?)";
    String insertTransStatQuery = "INSERT INTO Transaction_Statistics (customer_id, ticket_id, failed_tickets, pending_tickets, confirmed_tickets, cancelled_tickets, refunded_tickets) VALUES (?, ?, 0, 0, 1, 0, 0)";

    try (
        PreparedStatement availableStmt = connection.prepareStatement(availableTicketsQuery);
        PreparedStatement ticketStmt = connection.prepareStatement(selectTicketQuery);
        PreparedStatement transactionStmt = connection.prepareStatement(insertTransactionQuery, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement updateTicketStmt = connection.prepareStatement(updateTicketQuery);
        PreparedStatement updateStatsStmt = connection.prepareStatement(updateStatsQuery);
        PreparedStatement insertHistoryStmt = connection.prepareStatement(insertHistoryQuery);
        PreparedStatement insertTransStatStmt = connection.prepareStatement(insertTransStatQuery)) {

        int customerId = Utilities.getUserInput("Enter Customer ID: ");
        
        // 1. Display available tickets
        availableStmt.setInt(1, customerId);
        ResultSet availableRs = availableStmt.executeQuery();

        System.out.println("\n=== Tickets Available For You ===");
        Set<Integer> validTicketIds = new HashSet<>();
        boolean hasTickets = false;
        while (availableRs.next()) {
            hasTickets = true;
            int tId = availableRs.getInt("ticket_id");
            validTicketIds.add(tId);
            int eId = availableRs.getInt("event_id");
            String tType = availableRs.getString("ticket_type");
            double price = availableRs.getDouble("price");
            System.out.printf("Ticket ID: %d | Event ID: %d | Type: %s | Price: PHP %.2f%n", tId, eId, tType, price);
        }

        if (!hasTickets) {
            System.out.println("No available tickets for this customer.");
            return;
        }

        // 2. Ensure ticket exists in displayed list
        int ticketId;
        while (true) {
            ticketId = Utilities.getUserInput("Enter Ticket ID to purchase from the list above: ");
            if (validTicketIds.contains(ticketId)) break;
            System.out.println("Invalid Ticket ID. Please select from the list.");
        }

        // 3. Validate payment method
        String paymentMethod;
        while (true) {
            paymentMethod = Utilities.getUserText("Enter Payment Method (E-Wallet, Bank, Credit): ").toLowerCase();
            if (paymentMethod.equals("e-wallet") || paymentMethod.equals("bank") || paymentMethod.equals("credit")) break;
            System.out.println("Invalid payment method. Please choose from E-Wallet, Bank, or Credit.");
        }

        LocalDate purchaseDate = LocalDate.now();

        // 4. Validate ticket details again (final verification before processing)
        ticketStmt.setInt(1, ticketId);
        ResultSet rs = ticketStmt.executeQuery();
        if (!rs.next()) {
            System.out.println("Ticket record disappeared. Try again.");
            return;
        }

        String ticketStatus = rs.getString("ticket_status");
        if (!ticketStatus.equalsIgnoreCase("Available")) {
            System.out.println("Ticket is no longer available.");
            return;
        }

        int eventId = rs.getInt("event_id");
        double price = rs.getDouble("price");
        String ticketType = rs.getString("ticket_type");

        // 5. Process transaction
        transactionStmt.setInt(1, customerId);
        transactionStmt.setInt(2, ticketId);
        transactionStmt.setDate(3, java.sql.Date.valueOf(purchaseDate));
        transactionStmt.setString(4, paymentMethod);
        transactionStmt.setDouble(5, price);
        transactionStmt.setString(6, "Confirmed");
        transactionStmt.executeUpdate();

        int transactionId;
        try (ResultSet keys = transactionStmt.getGeneratedKeys()) {
            if (keys.next()) {
                transactionId = keys.getInt(1);
            } else {
                System.out.println("Transaction failed, no transaction ID generated.");
                return;
            }
        }

        // 6. Update ticket status
        updateTicketStmt.setInt(1, ticketId);
        updateTicketStmt.executeUpdate();

        // 7. Update ticket statistics
        updateStatsStmt.setInt(1, eventId);
        updateStatsStmt.setString(2, ticketType);
        updateStatsStmt.executeUpdate();

        // 8. Record in history and transaction stats
        insertHistoryStmt.setInt(1, transactionId);
        insertHistoryStmt.setInt(2, customerId);
        insertHistoryStmt.executeUpdate();

        insertTransStatStmt.setInt(1, customerId);
        insertTransStatStmt.setInt(2, ticketId);
        insertTransStatStmt.executeUpdate();

        System.out.println("Purchase successful! Transaction ID: " + transactionId);

        } catch (SQLException e) {
            System.out.println("Error during transaction: " + e.getMessage());
        }
    }

    private void refundCancelOrder() {
        try {
            System.out.println("\n=== Cancel and Refund Transaction ===");

            int customerId = Utilities.getUserInput("Enter Customer ID: ");

            // Fetch transactions for the customer
            String fetchQuery = "SELECT transaction_id, ticket_id, amount_paid, transaction_status FROM Transactions WHERE customer_id = ? AND transaction_status = 'Confirmed'";

            List<Integer> validTransactionIds = new ArrayList<>();

            try (PreparedStatement fetchStmt = connection.prepareStatement(fetchQuery)) {
                fetchStmt.setInt(1, customerId);
                try (ResultSet rs = fetchStmt.executeQuery()) {
                    System.out.println("\nYour Confirmed Transactions:");
                    System.out.println("ID       | Ticket ID | Amount Paid   | Status");
                    while (rs.next()) {
                        int txId = rs.getInt("transaction_id");
                        int tkId = rs.getInt("ticket_id");
                        double paid = rs.getDouble("amount_paid");
                        String status = rs.getString("transaction_status");
                        System.out.printf("%-8d | %-9d | PHP %-10.2f | %s\n", txId, tkId, paid, status);
                        validTransactionIds.add(txId);
                    }
                }
            }

            if (validTransactionIds.isEmpty()) {
                System.out.println("No confirmed transactions found for this customer.");
                return;
            }

            int transactionId = Utilities.getUserInput("Enter Transaction ID to cancel and refund: ");

            if (!validTransactionIds.contains(transactionId)) {
                System.out.println("Invalid transaction ID or not eligible for refund.");
                return;
            }

            // Update transaction status to Refunded
            String updateTransaction = "UPDATE Transactions SET transaction_status = 'Refunded' WHERE transaction_id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateTransaction)) {
                updateStmt.setInt(1, transactionId);
                int rows = updateStmt.executeUpdate();
                if (rows == 0) {
                    System.out.println("Failed to update transaction status.");
                    return;
                }
            }

            // Fetch the ticket ID for the refunded transaction
            int ticketId = -1;
            String getTicketIdQuery = "SELECT ticket_id FROM Transactions WHERE transaction_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(getTicketIdQuery)) {
                stmt.setInt(1, transactionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        ticketId = rs.getInt("ticket_id");
                    }
                }
            }

            if (ticketId == -1) {
                System.out.println("Error retrieving ticket for transaction.");
                return;
            }

            // Update ticket status back to Available
            String updateTicket = "UPDATE Tickets SET ticket_status = 'Available' WHERE ticket_id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateTicket)) {
                updateStmt.setInt(1, ticketId);
                updateStmt.executeUpdate();
            }

            System.out.println("Transaction cancelled and ticket refunded successfully.");

        } catch (SQLException e) {
            System.err.println("SQL error while processing refund/cancellation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private void transferTickets() {
        boolean programRun = true;

        while (programRun) {
            try {
                int senderId = Utilities.getUserInput("Enter sender customer ID: ");

                // Display confirmed transactions for this customer
                String displayTicketsQuery = """
                SELECT t.ticket_id, e.event_name, tk.seat_number, tk.ticket_type, t.amount_paid
                FROM Transactions t
                JOIN Tickets tk ON t.ticket_id = tk.ticket_id
                JOIN Events e ON tk.event_id = e.event_id
                WHERE t.customer_id = ? AND t.transaction_status = 'Confirmed'
            """;

                try (PreparedStatement pstmt = connection.prepareStatement(displayTicketsQuery)) {
                    pstmt.setInt(1, senderId);
                    ResultSet rs = pstmt.executeQuery();

                    System.out.println("\nYour Confirmed Transactions:");
                    boolean hasTickets = false;
                    while (rs.next()) {
                        hasTickets = true;
                        System.out.printf(
                                "Ticket ID: %d | Event: %s | Seat: %s | Type: %s | Paid: %.2f%n",
                                rs.getInt("ticket_id"),
                                rs.getString("event_name"),
                                rs.getString("seat_number"),
                                rs.getString("ticket_type"),
                                rs.getDouble("amount_paid")
                        );
                    }
                    if (!hasTickets) {
                        System.out.println("No confirmed tickets found for this customer.");
                        continue; // Ask for another customer ID
                    }
                }

                int ticketId = Utilities.getUserInput("Enter ticket ID to transfer: ");

                // Validate sender owns this ticket and transaction is confirmed
                String validateQuery = """
                SELECT transaction_id
                FROM Transactions
                WHERE customer_id = ? AND ticket_id = ? AND transaction_status = 'Confirmed'
            """;
                try (PreparedStatement pstmt = connection.prepareStatement(validateQuery)) {
                    pstmt.setInt(1, senderId);
                    pstmt.setInt(2, ticketId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        System.out.println("No valid confirmed ticket found for this customer.");
                        continue;
                    }
                }

                // Get recipient ID or create new customer account
                int recipientId = Utilities.getUserInput("Enter recipient customer ID (0 if new): ");
                if (recipientId == 0) {
                    String name = Utilities.getUserText("Enter recipient name: ");
                    String insertRecipient = "INSERT INTO Customers (customer_id, last_name, first_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertRecipient)) {
                        // Generate new customer ID (max + 1)
                        int newCustomerId = getNextCustomerId();
                        insertStmt.setInt(1, newCustomerId);
                        insertStmt.setString(2, name); // last_name placeholder
                        insertStmt.setString(3, "");   // first_name placeholder
                        insertStmt.setString(4, name.toLowerCase() + "@email.com"); // temp email
                        insertStmt.setString(5, "00000000000"); // temp phone
                        insertStmt.executeUpdate();
                        recipientId = newCustomerId;
                        System.out.println("New recipient account created with ID: " + recipientId);
                    }
                } else {
                    // Validate recipient exists
                    String checkRecipient = "SELECT customer_id FROM Customers WHERE customer_id = ?";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkRecipient)) {
                        checkStmt.setInt(1, recipientId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (!rs.next()) {
                            System.out.println("Recipient customer ID does not exist.");
                            continue;
                        }
                    }
                }

                // Begin transaction
                connection.setAutoCommit(false);

                // Step 1: Update sender's Transaction_Statistics (decrement)
                String updateSenderStats = """
                UPDATE Transaction_Statistics
                SET confirmed_tickets = confirmed_tickets - 1
                WHERE customer_id = ? AND ticket_id = ?
            """;
                try (PreparedStatement updateStats = connection.prepareStatement(updateSenderStats)) {
                    updateStats.setInt(1, senderId);
                    updateStats.setInt(2, ticketId);
                    int rows = updateStats.executeUpdate();
                    if (rows == 0) {
                        System.out.println("Sender has no tickets to transfer.");
                        connection.rollback();
                        continue;
                    }
                }

                // Step 2: Update recipient's Transaction_Statistics
                String upsertRecipientStats = """
                SELECT confirmed_tickets FROM Transaction_Statistics
                WHERE customer_id = ? AND ticket_id = ?
            """;
                try (PreparedStatement checkStats = connection.prepareStatement(upsertRecipientStats)) {
                    checkStats.setInt(1, recipientId);
                    checkStats.setInt(2, ticketId);
                    ResultSet rs = checkStats.executeQuery();
                    if (rs.next()) {
                        String updateStats = """
                        UPDATE Transaction_Statistics
                        SET confirmed_tickets = confirmed_tickets + 1
                        WHERE customer_id = ? AND ticket_id = ?
                    """;
                        try (PreparedStatement update = connection.prepareStatement(updateStats)) {
                            update.setInt(1, recipientId);
                            update.setInt(2, ticketId);
                            update.executeUpdate();
                        }
                    } else {
                        String insertStats = """
                        INSERT INTO Transaction_Statistics (customer_id, ticket_id, failed_tickets, pending_tickets, confirmed_tickets, cancelled_tickets, refunded_tickets)
                        VALUES (?, ?, 0, 0, 1, 0, 0)
                    """;
                        try (PreparedStatement insert = connection.prepareStatement(insertStats)) {
                            insert.setInt(1, recipientId);
                            insert.setInt(2, ticketId);
                            insert.executeUpdate();
                        }
                    }
                }

                // Step 3: Insert transfer record in Transactions table
                String insertTransaction = """
                INSERT INTO Transactions (transaction_id, customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status)
                VALUES (?, ?, ?, NOW(), ?, ?, 'Confirmed')
            """;
                try (PreparedStatement insertTrans = connection.prepareStatement(insertTransaction)) {
                    int nextId = getNextTransactionId();
                    insertTrans.setInt(1, nextId);
                    insertTrans.setInt(2, recipientId);
                    insertTrans.setInt(3, ticketId);
                    insertTrans.setString(4, "Transfer");
                    insertTrans.setDouble(5, 0.00);
                    insertTrans.executeUpdate();
                }

                connection.commit();
                System.out.println("Ticket transferred successfully.");

            } catch (SQLException e) {
                System.out.println("Transfer failed. Rolling back... Error: " + e.getMessage());
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("Rollback failed: " + rollbackEx.getMessage());
                }
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Could not reset auto-commit.");
                }
            }

            boolean validChoice = false;
            while (!validChoice) {
                int choice = Utilities.getUserInput("Transfer another ticket? (1 - Yes, 2 - No): ");
                switch (choice) {
                    case 1 ->
                        validChoice = true;
                    case 2 -> {
                        System.out.println("Exiting transfer menu...");
                        programRun = false;
                        validChoice = true;
                    }
                    default ->
                        System.out.println("Invalid choice. Enter 1 or 2.");
                }
            }
        }
    }

// Helper method to get next transaction_id
    private int getNextTransactionId() throws SQLException {
        String query = "SELECT IFNULL(MAX(transaction_id), 0) + 1 AS next_id FROM Transactions";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 1;
    }

// Helper method to get next customer_id
    private int getNextCustomerId() throws SQLException {
        String query = "SELECT IFNULL(MAX(customer_id), 0) + 1 AS next_id FROM Customers";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 100; // default starting id
    }


    private void manageEventStatus() {
        try {
            String updateQuery = "UPDATE Events SET event_status = 'Completed' WHERE event_status = 'Scheduled' AND event_date < CURDATE()";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                int updated = pstmt.executeUpdate();
                System.out.println("Auto-updated " + updated + " events to 'Completed' based on current date.");
            }

            // Optional: View all current events with their status
            String selectQuery = "SELECT event_id, event_name, event_status, event_date FROM Events ORDER BY event_date";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectQuery)) {
                System.out.println("\nCurrent Event Statuses:");
                while (rs.next()) {
                    System.out.printf("ID: %-4d | Name: %-30s | Status: %-10s | Date: %s\n",
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("event_status"),
                            rs.getDate("event_date").toString());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error managing event statuses: " + e.getMessage());
        }
    }
}
