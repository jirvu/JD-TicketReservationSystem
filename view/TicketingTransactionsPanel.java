import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TicketingTransactionsPanel extends JPanel {
    private final Connection connection;

    public TicketingTransactionsPanel(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());

        // --- Back Button Panel (same as TicketingReportsPanel) ---
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);
        // --------------------------------------------------------

        // Create Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Place Order", buildPlaceOrderTab());
        tabs.addTab("Refund/Cancel Order", buildRefundCancelTab());
        tabs.addTab("Transfer Tickets", buildTransferTab());
        tabs.addTab("Manage Event Status", buildManageStatusTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ---------------- TAB: PLACE ORDER ----------------
    private JPanel buildPlaceOrderTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Customer ID:"));
        JTextField customerIdField = new JTextField(10);
        inputPanel.add(customerIdField);

        inputPanel.add(new JLabel("Ticket ID:"));
        JTextField ticketIdField = new JTextField(10);
        inputPanel.add(ticketIdField);

        JButton placeOrderBtn = new JButton("Place Order");
        JTextArea resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);

        // ---- Table for Available Tickets ----
        JTable ticketsTable = new JTable();
        JScrollPane ticketsScroll = new JScrollPane(ticketsTable);
        loadAvailableTickets(ticketsTable);

        placeOrderBtn.addActionListener(e -> {
            String customerId = customerIdField.getText().trim();
            String ticketId = ticketIdField.getText().trim();
            if (customerId.isEmpty() || ticketId.isEmpty()) {
                resultArea.setText("Please fill in all fields.");
                return;
            }
            placeOrder(customerId, ticketId, resultArea);
            loadAvailableTickets(ticketsTable); // Refresh table
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(placeOrderBtn, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(ticketsScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------- TAB: REFUND/CANCEL ----------------
    private JPanel buildRefundCancelTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.add(new JLabel("Transaction ID:"));
        JTextField transIdField = new JTextField(10);
        inputPanel.add(transIdField);

        JButton refundBtn = new JButton("Refund/Cancel Order");
        JTextArea resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);

        // ---- Table for Active Transactions ----
        JTable transTable = new JTable();
        JScrollPane transScroll = new JScrollPane(transTable);
        loadActiveTransactions(transTable);

        refundBtn.addActionListener(e -> {
            String transId = transIdField.getText().trim();
            if (transId.isEmpty()) {
                resultArea.setText("Please enter a transaction ID.");
                return;
            }
            refundOrCancelOrder(transId, resultArea);
            loadActiveTransactions(transTable); // Refresh
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(refundBtn, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(transScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------- TAB: TRANSFER ----------------
    private JPanel buildTransferTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Transaction ID:"));
        JTextField transIdField = new JTextField(10);
        inputPanel.add(transIdField);

        inputPanel.add(new JLabel("New Customer ID:"));
        JTextField newCustomerIdField = new JTextField(10);
        inputPanel.add(newCustomerIdField);

        JButton transferBtn = new JButton("Transfer Tickets");
        JTextArea resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);

        // ---- Table for Active Transactions ----
        JTable transTable = new JTable();
        JScrollPane transScroll = new JScrollPane(transTable);
        loadActiveTransactions(transTable);

        transferBtn.addActionListener(e -> {
            String transId = transIdField.getText().trim();
            String newCustomerId = newCustomerIdField.getText().trim();
            if (transId.isEmpty() || newCustomerId.isEmpty()) {
                resultArea.setText("Please fill in all fields.");
                return;
            }
            transferTickets(transId, newCustomerId, resultArea);
            loadActiveTransactions(transTable); // Refresh
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(transferBtn, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(transScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------- TAB: MANAGE EVENT STATUS ----------------
    private JPanel buildManageStatusTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.add(new JLabel("Event ID:"));
        JTextField eventIdField = new JTextField(10);
        inputPanel.add(eventIdField);

        inputPanel.add(new JLabel("New Status:"));
        JTextField newStatusField = new JTextField(10);
        inputPanel.add(newStatusField);

        JButton manageBtn = new JButton("Update Status");
        JTextArea resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);

        // ---- Table for Events ----
        JTable eventsTable = new JTable();
        JScrollPane eventsScroll = new JScrollPane(eventsTable);
        loadEvents(eventsTable);

        manageBtn.addActionListener(e -> {
            String eventId = eventIdField.getText().trim();
            String newStatus = newStatusField.getText().trim();
            if (eventId.isEmpty() || newStatus.isEmpty()) {
                resultArea.setText("Please fill in all fields.");
                return;
            }
            manageEventStatus(eventId, newStatus, resultArea);
            loadEvents(eventsTable); // Refresh
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(manageBtn, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(eventsScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------- LOADERS ----------------
    private void loadAvailableTickets(JTable ticketsTable) {
        String query = "SELECT ticket_id, event_id, ticket_type, price FROM Tickets WHERE ticket_status='Available'";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Ticket ID", "Event ID", "Type", "Price"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("ticket_id"),
                        rs.getInt("event_id"),
                        rs.getString("ticket_type"),
                        rs.getDouble("price")
                });
            }
            ticketsTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading available tickets: " + ex.getMessage());
        }
    }

    private void loadActiveTransactions(JTable transTable) {
        String query = "SELECT transaction_id, customer_id, ticket_id, amount_paid, transaction_status " +
                       "FROM Transactions WHERE transaction_status='Confirmed'";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{"Trans ID", "Customer ID", "Ticket ID", "Amount", "Status"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("transaction_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("ticket_id"),
                        rs.getDouble("amount_paid"),
                        rs.getString("transaction_status")
                });
            }
            transTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage());
        }
    }

    private void loadEvents(JTable eventsTable) {
        String query = "SELECT event_id, event_name, venue, event_date, start_time, end_time, event_status FROM Events";
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            DefaultTableModel model = new DefaultTableModel(new String[]{
                    "Event ID", "Event Name", "Venue", "Date", "Start Time", "End Time", "Status"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getString("venue"),
                        rs.getDate("event_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("event_status")
                });
            }
            eventsTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
    }

    // ---------------- METHODS ----------------

    private void placeOrder(String customerId, String ticketId, JTextArea resultArea) {
        try {
            // Get payment method dynamically
            String[] methods = {"credit", "e-wallet", "bank"};
            String paymentMethod = (String) JOptionPane.showInputDialog(
                null,
                "Select payment method:",
                "Payment Method",
                JOptionPane.QUESTION_MESSAGE,
                null,
                methods,
                methods[0]
            );

            if (paymentMethod == null) {
                resultArea.setText("Payment method selection cancelled.");
                return;
            }

            // Fetch ticket price
            String priceQuery = "SELECT price FROM Tickets WHERE ticket_id = ? AND ticket_status = 'Available'";
            double price;
            try (PreparedStatement psPrice = connection.prepareStatement(priceQuery)) {
                psPrice.setInt(1, Integer.parseInt(ticketId));
                try (ResultSet rs = psPrice.executeQuery()) {
                    if (rs.next()) {
                        price = rs.getDouble("price");
                    } else {
                        resultArea.setText("Ticket not found or already sold.");
                        return;
                    }
                }
            }

            // Insert transaction with manually generated ID
            String insertTransactionQuery =
                "INSERT INTO Transactions (transaction_id, customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement psInsert = connection.prepareStatement(insertTransactionQuery)) {
                psInsert.setInt(1, getNextTransactionId());                        // transaction_id
                psInsert.setInt(2, Integer.parseInt(customerId));                  // customer_id
                psInsert.setInt(3, Integer.parseInt(ticketId));                    // ticket_id
                psInsert.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now())); // purchase_date
                psInsert.setString(5, paymentMethod);                              // payment_method
                psInsert.setDouble(6, price);                                      // amount_paid
                psInsert.setString(7, "Confirmed");                                // transaction_status
                psInsert.executeUpdate();
            }

            // Update ticket status
            String updateTicketQuery = "UPDATE Tickets SET ticket_status = 'Sold' WHERE ticket_id = ?";
            try (PreparedStatement psUpdate = connection.prepareStatement(updateTicketQuery)) {
                psUpdate.setInt(1, Integer.parseInt(ticketId));
                psUpdate.executeUpdate();
            }

            resultArea.setText("Order placed successfully!");
        } catch (Exception e) {
            resultArea.setText("Error placing order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getNextTransactionId() throws SQLException {
        String query = "SELECT IFNULL(MAX(transaction_id), 0) + 1 AS nextId FROM Transactions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("nextId");
            }
        }
        return 1; // Default if no records exist
    }


    private void refundOrCancelOrder(String transId, JTextArea resultArea) {
        String updateTransactionQuery = "UPDATE Transactions SET transaction_status='Cancelled' WHERE transaction_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(updateTransactionQuery)) {
            ps.setInt(1, Integer.parseInt(transId));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                resultArea.setText("Transaction " + transId + " cancelled successfully.");
            } else {
                resultArea.setText("Transaction not found.");
            }
        } catch (SQLException ex) {
            resultArea.setText("Error cancelling transaction: " + ex.getMessage());
        }
    }

    private void transferTickets(String transId, String newCustomerId, JTextArea resultArea) {
        String updateTransactionQuery = "UPDATE Transactions SET customer_id = ? WHERE transaction_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(updateTransactionQuery)) {
            ps.setInt(1, Integer.parseInt(newCustomerId));
            ps.setInt(2, Integer.parseInt(transId));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                resultArea.setText("Transaction " + transId + " transferred to Customer " + newCustomerId);
            } else {
                resultArea.setText("Transaction not found.");
            }
        } catch (SQLException ex) {
            resultArea.setText("Error transferring tickets: " + ex.getMessage());
        }
    }

    private void manageEventStatus(String eventId, String newStatus, JTextArea resultArea) {
        String updateEventQuery = "UPDATE Events SET event_status = ? WHERE event_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(updateEventQuery)) {
            ps.setString(1, newStatus);
            ps.setInt(2, Integer.parseInt(eventId));
            int rows = ps.executeUpdate();
            if (rows > 0) {
                resultArea.setText("Event " + eventId + " status updated to " + newStatus);
            } else {
                resultArea.setText("Event not found.");
            }
        } catch (SQLException ex) {
            resultArea.setText("Error updating event status: " + ex.getMessage());
        }
    }
}
