import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class EventRecords {
    private final Connection connection;
    private final Scanner scanner = new Scanner(System.in);

    public EventRecords(Connection connection) {
        this.connection = connection;
    }

    public void showMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== Event Records Menu ===");
            System.out.println("[1] View All Events");
            System.out.println("[2] Filter by Date Range");
            System.out.println("[3] Filter by Availability");
            System.out.println("[4] Filter by Status");
            System.out.println("[5] Back");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewAllEvents();
                case 2 -> filterByDateRange();
                case 3 -> filterByAvailability();
                case 4 -> filterByStatus();
                case 5 -> {
                    System.out.println("Returning to main menu...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void viewAllEvents() throws SQLException {
        String sql = "SELECT * FROM Events ORDER BY event_date, start_time";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            printEvents(rs);
        }
    }

    private void filterByDateRange() throws SQLException {
        System.out.println("\n[1] Today\n[2] This Week\n[3] This Month\n[4] This Year");
        System.out.print("Choose a range: ");
        int option = scanner.nextInt();
        scanner.nextLine();

        LocalDate now = LocalDate.now();
        LocalDate start = now, end = now;

        switch (option) {
            case 1 -> {}
            case 2 -> {
                start = now.with(java.time.DayOfWeek.MONDAY);
                end = now.with(java.time.DayOfWeek.SUNDAY);
            }
            case 3 -> {
                start = now.withDayOfMonth(1);
                end = now.withDayOfMonth(now.lengthOfMonth());
            }
            case 4 -> {
                start = now.withDayOfYear(1);
                end = now.withDayOfYear(now.lengthOfYear());
            }
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        String sql = "SELECT * FROM Events WHERE event_date BETWEEN ? AND ? ORDER BY event_date, start_time";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                printEvents(rs);
            }
        }
    }

    private void filterByAvailability() throws SQLException {
        String sql = "SELECT e.* FROM Events e JOIN Ticket_Statistics ts ON e.event_id = ts.event_id GROUP BY e.event_id HAVING SUM(ts.available_tickets) > 0";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            printEvents(rs);
        }
    }

    private void filterByStatus() throws SQLException {
        System.out.println("\nEnter event status to filter (Scheduled, Ongoing, Completed, Cancelled): ");
        String status = scanner.nextLine().trim();

        String sql = "SELECT * FROM Events WHERE event_status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                printEvents(rs);
            }
        }
    }

    private void printEvents(ResultSet rs) throws SQLException {
        System.out.println("\nEvent Listings:");
        while (rs.next()) {
            System.out.printf("ID: %d | Name: %s | Venue: %s | Date: %s | Time: %s - %s | Status: %s\n",
                    rs.getInt("event_id"),
                    rs.getString("event_name"),
                    rs.getString("venue"),
                    rs.getDate("event_date"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time"),
                    rs.getString("event_status"));
        }
    }
}

