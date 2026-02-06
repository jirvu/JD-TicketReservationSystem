import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EventRecordsFrame extends JFrame {
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTable table;

    public EventRecordsFrame(Connection connection) {
        this.connection = connection;
        setTitle("Event Records Management");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnViewAll = new JButton("[1] View All Events");
        JButton btnViewById = new JButton("[2] View Event by ID");
        JButton btnBack = new JButton("[3] Return to Records Menu");
        buttonPanel.add(btnViewAll);
        buttonPanel.add(btnViewById);
        buttonPanel.add(btnBack);

        // Table setup
        String[] columns = {"Event ID", "Event Name", "Venue", "Date", "Start Time", "End Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        // Layout
        setLayout(new BorderLayout(10, 10));
        add(buttonPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Actions
        btnViewAll.addActionListener(e -> loadAllEvents());
        btnViewById.addActionListener(e -> showEventById());
        btnBack.addActionListener(e -> dispose());
    }

    private void loadAllEvents() {
        tableModel.setRowCount(0);
        String sql = "SELECT event_id, event_name, venue, event_date, start_time, end_time, event_status FROM Events";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("event_id"),
                    rs.getString("event_name"),
                    rs.getString("venue"),
                    rs.getDate("event_date"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("event_status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading events: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEventById() {
        String input = JOptionPane.showInputDialog(this, "Enter Event ID:");
        if (input == null || input.trim().isEmpty()) return;
        try {
            int id = Integer.parseInt(input);
            tableModel.setRowCount(0);
            String sql = "SELECT event_id, event_name, venue, event_date, start_time, end_time, event_status " +
                         "FROM Events WHERE event_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        tableModel.addRow(new Object[]{
                            rs.getInt("event_id"),
                            rs.getString("event_name"),
                            rs.getString("venue"),
                            rs.getDate("event_date"),
                            rs.getTime("start_time"),
                            rs.getTime("end_time"),
                            rs.getString("event_status")
                        });
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "No event found with ID " + id,
                            "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid ID format.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error fetching event: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
