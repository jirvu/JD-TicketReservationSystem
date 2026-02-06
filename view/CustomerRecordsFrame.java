import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerRecordsFrame extends JFrame {
    private Connection connection;
    private DefaultTableModel customerTableModel;
    private JTable customerTable;
    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JScrollPane historyScroll;

    public CustomerRecordsFrame(Connection connection) {
        this.connection = connection;
        setTitle("Manage Customer Records");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnViewAll  = new JButton("[1] View All Customers");
        JButton btnViewById = new JButton("[2] View Customer by ID");
        JButton btnBack     = new JButton("[3] Return to Records Menu");
        buttonPanel.add(btnViewAll);
        buttonPanel.add(btnViewById);
        buttonPanel.add(btnBack);

        // Customer table setup with event columns
        String[] customerColumns = {"Customer ID", "First Name", "Last Name", "Email", "Phone", "Event ID", "Event Name"};
        customerTableModel = new DefaultTableModel(customerColumns, 0);
        customerTable = new JTable(customerTableModel);
        JScrollPane customerScroll = new JScrollPane(customerTable);

        // History table setup
        String[] historyColumns = {"Transaction ID", "Customer ID"};
        historyTableModel = new DefaultTableModel(historyColumns, 0);
        historyTable = new JTable(historyTableModel);
        historyScroll = new JScrollPane(historyTable);
        historyScroll.setVisible(false);

        // Split pane to hold both tables
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, customerScroll, historyScroll);
        splitPane.setResizeWeight(0.8);
        splitPane.setDividerSize(5);

        // Layout
        setLayout(new BorderLayout(10, 10));
        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Actions
        btnViewAll.addActionListener(e -> {
            loadAllCustomers();
            historyTableModel.setRowCount(0);
            historyScroll.setVisible(false);
            splitPane.setDividerLocation(0.8);
            revalidate(); repaint();
        });
        btnViewById.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter Customer ID:");
            if (input == null || input.trim().isEmpty()) return;
            try {
                int id = Integer.parseInt(input);
                customerTableModel.setRowCount(0);
                historyTableModel.setRowCount(0);
                loadCustomerById(id);
                loadTransactionHistory(id);
                if (historyTableModel.getRowCount() > 0) {
                    historyScroll.setVisible(true);
                    splitPane.setDividerLocation(0.5);
                }
                revalidate(); repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnBack.addActionListener(e -> dispose());
    }

    private void loadAllCustomers() {
        customerTableModel.setRowCount(0);
        String sql = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone_number, " +
                     "t.event_id, e.event_name " +
                     "FROM Customers c " +
                     "LEFT JOIN Transactions tr ON c.customer_id = tr.customer_id " +
                     "LEFT JOIN Tickets t ON tr.ticket_id = t.ticket_id " +
                     "LEFT JOIN Events e ON t.event_id = e.event_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customerTableModel.addRow(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getInt("event_id"),
                    rs.getString("event_name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerById(int id) {
        String sqlCust = "SELECT c.customer_id, c.first_name, c.last_name, c.email, c.phone_number, " +
                         "t.event_id, e.event_name " +
                         "FROM Customers c " +
                         "LEFT JOIN Transactions tr ON c.customer_id = tr.customer_id " +
                         "LEFT JOIN Tickets t ON tr.ticket_id = t.ticket_id " +
                         "LEFT JOIN Events e ON t.event_id = e.event_id " +
                         "WHERE c.customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sqlCust)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    JOptionPane.showMessageDialog(this, "No customer found with ID " + id, "Not Found", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                while (rs.next()) {
                    customerTableModel.addRow(new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone_number"),
                        rs.getInt("event_id"),
                        rs.getString("event_name")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching customer: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionHistory(int id) {
        String sqlHist = "SELECT transaction_id, customer_id FROM Transactions WHERE customer_id = ?";
        try (PreparedStatement stmt2 = connection.prepareStatement(sqlHist)) {
            stmt2.setInt(1, id);
            try (ResultSet rs2 = stmt2.executeQuery()) {
                while (rs2.next()) {
                    historyTableModel.addRow(new Object[]{
                        rs2.getInt("transaction_id"),
                        rs2.getInt("customer_id")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transaction history: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
