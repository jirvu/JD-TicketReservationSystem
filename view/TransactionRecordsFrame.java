import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TransactionRecordsFrame extends JFrame {
    private final Connection connection;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public TransactionRecordsFrame(Connection connection) {
        this.connection = connection;
        setTitle("Transaction Records Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Table setup
        String[] columns = {"Transaction ID","Customer ID","Ticket ID","Purchase Date","Payment Method","Amount Paid","Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        initComponents();
        loadAllTransactions();
    }

    private void initComponents() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnAdd    = new JButton("[1] Add Transaction");
        JButton btnRemove = new JButton("[2] Remove/Cancel Transaction");
        JButton btnView   = new JButton("[3] View Details");
        JButton btnStats  = new JButton("[4] View Transaction Statistics");
        JButton btnBack   = new JButton("[5] Back");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(btnView);
        buttonPanel.add(btnStats);
        buttonPanel.add(btnBack);

        JScrollPane scrollPane = new JScrollPane(table);
        setLayout(new BorderLayout(10, 10));
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> { addTransaction(); loadAllTransactions(); });
        btnRemove.addActionListener(e -> { removeTransaction(); loadAllTransactions(); });
        btnView.addActionListener(e -> viewTransactionDetails());
        btnStats.addActionListener(e -> viewTransactionStatistics());
        btnBack.addActionListener(e -> dispose());
    }

    private void loadAllTransactions() {
        tableModel.setRowCount(0);
        String sql = "SELECT transaction_id, customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status FROM Transactions";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getInt("customer_id"),
                    rs.getInt("ticket_id"),
                    rs.getDate("purchase_date"),
                    rs.getString("payment_method"),
                    rs.getDouble("amount_paid"),
                    rs.getString("transaction_status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTransaction() {
        try {
            String cust = JOptionPane.showInputDialog(this, "Customer ID:"); if(cust==null) return;
            int customerId = Integer.parseInt(cust);
            String ticket = JOptionPane.showInputDialog(this, "Ticket ID:"); if(ticket==null) return;
            int ticketId = Integer.parseInt(ticket);
            // Using current date
            String method = JOptionPane.showInputDialog(this, "Payment Method:"); if(method==null) return;
            String amt = JOptionPane.showInputDialog(this, "Amount Paid:"); if(amt==null) return;
            double amountPaid = Double.parseDouble(amt);
            String[] statuses = {"Pending","Confirmed","Cancelled","Failed","Refunded"};
            String status = (String) JOptionPane.showInputDialog(this, "Select Status:", "Status",
                    JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[1]);
            if(status==null) return;

            String sql = "INSERT INTO Transactions(customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status) " +
                         "VALUES(?, ?, CURRENT_DATE, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                ps.setInt(2, ticketId);
                ps.setString(3, method);
                ps.setDouble(4, amountPaid);
                ps.setString(5, status);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Transaction added.");
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTransaction() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "Enter Transaction ID to cancel:"); if(idStr==null) return;
            int id = Integer.parseInt(idStr);
            String sql = "UPDATE Transactions SET transaction_status = 'Cancelled' WHERE transaction_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Transaction cancelled.");
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling transaction: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTransactionDetails() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Transaction ID:"); if(idStr==null) return;
        try {
            int id = Integer.parseInt(idStr);
            String sql = "SELECT * FROM Transactions WHERE transaction_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String msg = String.format(
                            "Transaction ID: %d\nCustomer ID: %d\nTicket ID: %d\nDate: %s\nPayment: %s\nAmount: %.2f\nStatus: %s",
                            rs.getInt("transaction_id"), rs.getInt("customer_id"), rs.getInt("ticket_id"),
                            rs.getDate("purchase_date"), rs.getString("payment_method"), rs.getDouble("amount_paid"), rs.getString("transaction_status")
                        );
                        JOptionPane.showMessageDialog(this, msg, "Transaction Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Transaction not found.");
                    }
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error viewing details: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTransactionStatistics() {
        String[] cols = {"Customer ID","Ticket ID","Failed","Pending","Confirmed","Cancelled","Refunded"};
        DefaultTableModel statsModel = new DefaultTableModel(cols, 0);
        String sql = "SELECT customer_id, ticket_id, failed_tickets, pending_tickets, confirmed_tickets, cancelled_tickets, refunded_tickets FROM Transaction_Statistics";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                statsModel.addRow(new Object[]{
                    rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7)
                });
            }
            JTable statsTable = new JTable(statsModel);
            JOptionPane.showMessageDialog(this, new JScrollPane(statsTable), "Transaction Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading statistics: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
