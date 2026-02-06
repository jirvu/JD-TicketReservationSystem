import javax.swing.*;   
import java.awt.*;
import java.sql.Connection;

public class RecordsFrame extends JFrame {
    private final Connection connection;

    public RecordsFrame(Connection connection) {
        this.connection = connection;
        setTitle("Ticket Reservation System");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel("Ticketing Records Management", SwingConstants.CENTER);
        JButton btnCustomer = new JButton("Customer Records");
        JButton btnTicket = new JButton("Ticket Records");
        JButton btnTransaction = new JButton("Transaction Records");
        JButton btnEvent = new JButton("Event Records");
        JButton btnBack = new JButton("Return to Main Menu");

        panel.add(title);
        panel.add(btnCustomer);
        panel.add(btnTicket);
        panel.add(btnTransaction);
        panel.add(btnEvent);
        panel.add(btnBack);

        add(panel);

        // Button actions
        btnCustomer.addActionListener(e -> new CustomerRecordsFrame(connection).setVisible(true));
        btnTicket.addActionListener(e -> new TicketingRecordsFrame(connection).setVisible(true));
        btnTransaction.addActionListener(e -> new TransactionRecordsFrame(connection).setVisible(true));
        btnEvent.addActionListener(e -> new EventRecordsFrame(connection).setVisible(true));
        btnBack.addActionListener(e -> dispose());
    }
}
