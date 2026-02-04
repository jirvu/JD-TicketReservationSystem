import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import javax.swing.*;
import util.DatabaseConnection;

public class MainFrame extends JFrame {

    private Connection connection;

    public MainFrame() {
        setTitle("Ticket Reservation System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center the frame

        try {
            connection = DatabaseConnection.getConnection();
            System.out.println("Database Connected!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // GUI components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton btnRecords = new JButton("Ticketing Records");
        JButton btnTransactions = new JButton("Ticketing Transactions");
        JButton btnReports = new JButton("Ticketing Reports");
        JButton btnExit = new JButton("Exit");

        // Button actions
        btnRecords.addActionListener((ActionEvent e) -> {
            new RecordsFrame(connection).setVisible(true);
        });

        btnTransactions.addActionListener((ActionEvent e) -> {
        JFrame transactionsFrame = new JFrame("Ticketing Transactions");
        transactionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        transactionsFrame.setSize(600, 400);
        transactionsFrame.setLocationRelativeTo(this);
        transactionsFrame.add(new TicketingTransactionsPanel(connection));
        transactionsFrame.setVisible(true);
        });

        btnReports.addActionListener((ActionEvent e) -> {
            JFrame reportsFrame = new JFrame("Ticketing Reports");
            reportsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            reportsFrame.setSize(800, 600);
            reportsFrame.setLocationRelativeTo(this);
            reportsFrame.add(new TicketingReportsPanel(connection));
            reportsFrame.setVisible(true);
        });

        btnExit.addActionListener((ActionEvent e) -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseConnection.closeConnection();
                dispose(); // close frame
            }
        });

        // Add buttons to panel
        panel.add(new JLabel("Main Menu", SwingConstants.CENTER));
        panel.add(btnRecords);
        panel.add(btnTransactions);
        panel.add(btnReports);
        panel.add(btnExit);

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
