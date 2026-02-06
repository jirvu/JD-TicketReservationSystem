import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TicketingRecordsFrame extends JFrame {
    private Connection connection;
    private DefaultTableModel tableModel;
    private JTable table;

    public TicketingRecordsFrame(Connection connection) {
        this.connection = connection;
        setTitle("Ticket Records Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
        loadAllTickets();
    }

    private void initComponents() {
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnAdd    = new JButton("[1] Add Ticket");
        JButton btnRemove = new JButton("[2] Cancel Ticket");
        JButton btnView   = new JButton("[3] View Details");
        JButton btnStats  = new JButton("[4] View Statistics");
        JButton btnBack   = new JButton("[5] Back");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnRemove);
        buttonPanel.add(btnView);
        buttonPanel.add(btnStats);
        buttonPanel.add(btnBack);

        // Table setup for Tickets
        String[] columns = {"Ticket ID", "Event ID", "Ticket Type", "Price", "Seat Number", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Layout
        setLayout(new BorderLayout(10, 10));
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        btnAdd.addActionListener(e -> { addTicket(); loadAllTickets(); });
        btnRemove.addActionListener(e -> { removeTicket(); loadAllTickets(); });
        btnView.addActionListener(e -> viewTicketDetails());
        btnStats.addActionListener(e -> viewTicketStatistics());
        btnBack.addActionListener(e -> dispose());
    }

    private void loadAllTickets() {
        tableModel.setRowCount(0);
        String sql = "SELECT ticket_id, event_id, ticket_type, price, seat_number, ticket_status FROM Tickets";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("ticket_id"),
                    rs.getInt("event_id"),
                    rs.getString("ticket_type"),
                    rs.getDouble("price"),
                    rs.getString("seat_number"),
                    rs.getString("ticket_status")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading tickets: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTicket() {
        try {
            String evStr = JOptionPane.showInputDialog(this, "Event ID:"); if (evStr==null) return;
            int eventId = Integer.parseInt(evStr);
            String type = JOptionPane.showInputDialog(this, "Ticket Type:"); if (type==null) return;
            String priceStr = JOptionPane.showInputDialog(this, "Price:"); if (priceStr==null) return;
            double price = Double.parseDouble(priceStr);
            String seat = JOptionPane.showInputDialog(this, "Seat Number:"); if (seat==null) return;
            // Insert ticket
            String ins = "INSERT INTO Tickets(event_id, ticket_type, price, seat_number, ticket_status) VALUES(?,?,?,?, 'Available')";
            try (PreparedStatement ps = connection.prepareStatement(ins)) {
                ps.setInt(1, eventId);
                ps.setString(2, type);
                ps.setDouble(3, price);
                ps.setString(4, seat);
                ps.executeUpdate();
            }
            // Update statistics
            String upd = "UPDATE Ticket_Statistics SET total_tickets = total_tickets+1, available_tickets = available_tickets+1 WHERE event_id = ? AND ticket_type = ?";
            try (PreparedStatement ps = connection.prepareStatement(upd)) {
                ps.setInt(1, eventId);
                ps.setString(2, type);
                if (ps.executeUpdate() == 0) {
                    String insStats = "INSERT INTO Ticket_Statistics(event_id, ticket_type, total_tickets, sold_tickets, cancelled_tickets, available_tickets) VALUES(?, ?, 1, 0, 0, 1)";
                    try (PreparedStatement ps2 = connection.prepareStatement(insStats)) {
                        ps2.setInt(1, eventId);
                        ps2.setString(2, type);
                        ps2.executeUpdate();
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "Ticket added successfully.");
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error adding ticket: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTicket() {
        try {
            String idStr = JOptionPane.showInputDialog(this, "Enter Ticket ID to cancel:"); if (idStr==null) return;
            int ticketId = Integer.parseInt(idStr);
            // Fetch details
            String sel = "SELECT event_id, ticket_type, ticket_status FROM Tickets WHERE ticket_id = ?";
            int eventId; String type, status;
            try (PreparedStatement ps = connection.prepareStatement(sel)) {
                ps.setInt(1, ticketId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { JOptionPane.showMessageDialog(this, "Ticket not found."); return; }
                    eventId = rs.getInt("event_id");
                    type = rs.getString("ticket_type");
                    status = rs.getString("ticket_status");
                }
            }
            if (!"Available".equals(status)) {
                JOptionPane.showMessageDialog(this, "Only available tickets can be cancelled."); return;
            }
            String updTkt = "UPDATE Tickets SET ticket_status = 'Cancelled' WHERE ticket_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(updTkt)) {
                ps.setInt(1, ticketId); ps.executeUpdate();
            }
            String updStats = "UPDATE Ticket_Statistics SET cancelled_tickets = cancelled_tickets+1, available_tickets = available_tickets-1 WHERE event_id = ? AND ticket_type = ?";
            try (PreparedStatement ps = connection.prepareStatement(updStats)) {
                ps.setInt(1, eventId);
                ps.setString(2, type);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Ticket cancelled.");
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error cancelling ticket: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTicketDetails() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Ticket ID:"); if (idStr==null) return;
        try {
            int ticketId = Integer.parseInt(idStr);
            String sel = "SELECT * FROM Tickets WHERE ticket_id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sel)) {
                ps.setInt(1, ticketId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String msg = String.format("ID: %d\nEvent: %d\nType: %s\nPrice: %.2f\nSeat: %s\nStatus: %s",
                            rs.getInt("ticket_id"), rs.getInt("event_id"), rs.getString("ticket_type"),
                            rs.getDouble("price"), rs.getString("seat_number"), rs.getString("ticket_status"));
                        JOptionPane.showMessageDialog(this, msg, "Ticket Details", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Ticket not found.");
                    }
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error viewing details: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTicketStatistics() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Event ID","Ticket Type","Total","Sold","Cancelled","Available"}, 0);
        String sel = "SELECT event_id, ticket_type, total_tickets, sold_tickets, cancelled_tickets, available_tickets FROM Ticket_Statistics";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sel)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6)
                });
            }
            JTable statTable = new JTable(model);
            JOptionPane.showMessageDialog(this, new JScrollPane(statTable), "Ticket Statistics", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
