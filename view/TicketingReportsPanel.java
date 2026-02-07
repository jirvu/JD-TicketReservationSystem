import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class TicketingReportsPanel extends JPanel {
    private final Connection connection;

    public TicketingReportsPanel(Connection conn) {
        this.connection = conn;
        setLayout(new BorderLayout());

        // --- Back Button Panel ---
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> {
            // find the window that contains this panel and close it
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);
        // ----------------------------

        // now add your tabbed pane
        JTabbedPane tabs = new JTabbedPane();

        // Customer Transactions Tab
        tabs.addTab("Customer Transactions", buildCustomerTransactionsTab());

        // Sales Report Tab
        tabs.addTab("Sales Report", buildSalesReportTab());

        // Event Summary Tab
        tabs.addTab("Event Summary", buildEventSummaryTab());

        // Payment Breakdown Tab
        tabs.addTab("Payment Breakdown", buildPaymentBreakdownTab());

        add(tabs, BorderLayout.CENTER);
    }

    // 1. Customer Transactions Report Tab
    private JPanel buildCustomerTransactionsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Customer ID:"));
        JTextField customerIdField = new JTextField(10);
        JButton generateBtn = new JButton("Generate");
        inputPanel.add(customerIdField);
        inputPanel.add(generateBtn);

        JTextArea resultArea = new JTextArea(10, 60);
        resultArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(resultArea);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            resultArea.setText(""); // clear previous
            String text = customerIdField.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter a Customer ID.");
                return;
            }
            try {
                int customerId = Integer.parseInt(text);
                StringBuilder sb = new StringBuilder();

                // --- Query: Total transactions & spent
                String summarySql = "SELECT COUNT(*) AS total_transactions, SUM(amount_paid) AS total_spent FROM Transactions WHERE customer_id = ? AND transaction_status = 'Confirmed'";
                try (PreparedStatement stmt = connection.prepareStatement(summarySql)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            sb.append("Total Transactions: ").append(rs.getInt("total_transactions")).append("\n");
                            sb.append("Total Amount Spent: ").append(rs.getDouble("total_spent")).append("\n");
                        }
                    }
                }

                // --- Query: Most Tickets Bought on Date
                String topTimeSql = "SELECT DATE(purchase_date) AS date, COUNT(*) AS ticket_count FROM Transactions WHERE customer_id = ? AND transaction_status = 'Confirmed' GROUP BY DATE(purchase_date) ORDER BY ticket_count DESC LIMIT 1";
                try (PreparedStatement stmt = connection.prepareStatement(topTimeSql)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            sb.append("Most Tickets Bought on: ").append(rs.getDate("date")).append(" (").append(rs.getInt("ticket_count")).append(" tickets)\n");
                        }
                    }
                }

                // --- Query: Most Tickets Bought for Event
                String topEventSql = "SELECT e.event_name, COUNT(*) AS tickets_bought FROM Transactions t JOIN Tickets tk ON t.ticket_id = tk.ticket_id JOIN Events e ON tk.event_id = e.event_id WHERE t.customer_id = ? AND t.transaction_status = 'Confirmed' GROUP BY e.event_name ORDER BY tickets_bought DESC LIMIT 1";
                try (PreparedStatement stmt = connection.prepareStatement(topEventSql)) {
                    stmt.setInt(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            sb.append("Most Tickets Bought for Event: ").append(rs.getString("event_name")).append(" (").append(rs.getInt("tickets_bought")).append(" tickets)\n");
                        }
                    }
                }

                resultArea.setText(sb.toString());
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // 2. Sales Report Tab
    private JPanel buildSalesReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        JTextField dayField = new JTextField(2);
        JTextField monthField = new JTextField(2);
        JTextField yearField = new JTextField(4);
        JButton generateBtn = new JButton("Generate");

        inputPanel.add(new JLabel("Day (0=skip):"));
        inputPanel.add(dayField);
        inputPanel.add(new JLabel("Month (0=skip):"));
        inputPanel.add(monthField);
        inputPanel.add(new JLabel("Year (0=skip):"));
        inputPanel.add(yearField);
        inputPanel.add(generateBtn);

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Event ID","Event Name","Total Revenue","Sold Tickets","Cancelled","Refunded"}, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            model.setRowCount(0);
            // Parse date fields
            String day = dayField.getText().trim();
            String month = monthField.getText().trim();
            String year = yearField.getText().trim();
            int d = day.isEmpty() ? 0 : Integer.parseInt(day);
            int m = month.isEmpty() ? 0 : Integer.parseInt(month);
            int y = year.isEmpty() ? 0 : Integer.parseInt(year);

            String yearStr = (y == 0) ? "%" : String.format("%04d", y);
            String monthStr = (m == 0) ? "%" : String.format("%02d", m);
            String dayStr = (d == 0) ? "%" : String.format("%02d", d);
            String dateFilter = String.format("%s-%s-%s", yearStr, monthStr, dayStr);

            String query = """
                    SELECT e.event_id, e.event_name,
                      SUM(CASE WHEN t.transaction_status = 'Confirmed' THEN t.amount_paid ELSE 0 END) AS total_revenue,
                      COUNT(CASE WHEN t.transaction_status = 'Confirmed' THEN 1 END) AS sold_tickets,
                      COUNT(CASE WHEN t.transaction_status = 'Cancelled' THEN 1 END) AS cancelled_tickets,
                      COUNT(CASE WHEN t.transaction_status = 'Refunded' THEN 1 END) AS refunded_tickets
                    FROM Events e
                    LEFT JOIN Tickets tk ON e.event_id = tk.event_id
                    LEFT JOIN Transactions t ON tk.ticket_id = t.ticket_id
                    WHERE DATE(t.purchase_date) LIKE ?
                    GROUP BY e.event_id, e.event_name
                    """;
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, dateFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getDouble("total_revenue"),
                            rs.getInt("sold_tickets"),
                            rs.getInt("cancelled_tickets"),
                            rs.getInt("refunded_tickets")
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // 3. Event Summary Report Tab
    private JPanel buildEventSummaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton generateBtn = new JButton("Generate");

        inputPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        inputPanel.add(endDateField);
        inputPanel.add(generateBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID","Event Name","Status","Total","Sold","Cancelled","Revenue","Attendance %"}, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            model.setRowCount(0);
            String start = startDateField.getText().trim();
            String end = endDateField.getText().trim();
            boolean hasRange = !start.isEmpty() && !end.isEmpty();

            StringBuilder query = new StringBuilder(
                "SELECT e.event_id, e.event_name, e.event_status, " +
                "COUNT(tk.ticket_id) AS total_tickets, " +
                "SUM(CASE WHEN tr.transaction_status = 'Confirmed' THEN 1 ELSE 0 END) AS sold_tickets, " +
                "SUM(CASE WHEN tr.transaction_status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled_tickets, " +
                "SUM(CASE WHEN tr.transaction_status = 'Confirmed' THEN tr.amount_paid ELSE 0 END) AS total_revenue " +
                "FROM Events e " +
                "LEFT JOIN Tickets tk ON e.event_id = tk.event_id " +
                "LEFT JOIN Transactions tr ON tk.ticket_id = tr.ticket_id ");
            if (hasRange) query.append("WHERE e.event_date BETWEEN ? AND ? ");
            query.append("GROUP BY e.event_id, e.event_name, e.event_status ORDER BY e.event_date");

            try (PreparedStatement ps = connection.prepareStatement(query.toString())) {
                if (hasRange) {
                    ps.setDate(1, java.sql.Date.valueOf(LocalDate.parse(start)));
                    ps.setDate(2, java.sql.Date.valueOf(LocalDate.parse(end)));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int total = rs.getInt("total_tickets");
                        int sold = rs.getInt("sold_tickets");
                        double attendance = total == 0 ? 0 : (sold * 100.0 / total);
                        model.addRow(new Object[]{
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("event_status"),
                            total,
                            sold,
                            rs.getInt("cancelled_tickets"),
                            rs.getDouble("total_revenue"),
                            String.format("%.2f", attendance)
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    // 4. Payment Method Breakdown Tab
    private JPanel buildPaymentBreakdownTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel();
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton generateBtn = new JButton("Generate");
        inputPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        inputPanel.add(endDateField);
        inputPanel.add(generateBtn);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Payment","Date","Revenue","Txns","Avg Tx","Event Name","Status","Type"}, 0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        generateBtn.addActionListener(e -> {
            model.setRowCount(0);
            String start = startDateField.getText().trim();
            String end = endDateField.getText().trim();
            boolean hasRange = !start.isEmpty() && !end.isEmpty();

            String query = "SELECT t.payment_method, DATE(t.purchase_date) AS date, " +
                    "COUNT(*) AS total_transactions, SUM(t.amount_paid) AS total_revenue, AVG(t.amount_paid) AS avg_transaction, " +
                    "e.event_status, e.event_name, e.event_type " +
                    "FROM Transactions t " +
                    "JOIN Tickets tk ON t.ticket_id = tk.ticket_id " +
                    "JOIN Events e ON tk.event_id = e.event_id " +
                    "WHERE t.transaction_status = 'Confirmed' " +
                    (hasRange ? "AND t.purchase_date BETWEEN ? AND ? " : "") +
                    "GROUP BY t.payment_method, DATE(t.purchase_date), e.event_status, e.event_name, e.event_type " +
                    "ORDER BY t.payment_method, date";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                if (hasRange) {
                    ps.setDate(1, java.sql.Date.valueOf(LocalDate.parse(start)));
                    ps.setDate(2, java.sql.Date.valueOf(LocalDate.parse(end)));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("payment_method"),
                            rs.getString("date"),
                            rs.getDouble("total_revenue"),
                            rs.getInt("total_transactions"),
                            rs.getDouble("avg_transaction"),
                            rs.getString("event_name"),
                            rs.getString("event_status"),
                            rs.getString("event_type")
                        });
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

}
