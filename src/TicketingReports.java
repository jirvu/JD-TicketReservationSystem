import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import util.Utilities;

public class TicketingReports {

    private final Connection connection;
    private final Statement statement;

    public TicketingReports(Connection connection) {
        this.connection = connection;
        try {
            this.statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void showReportsMenu() {
        boolean programRun = true;

        while (programRun) {
            System.out.println("\nTicket Report Queries");
            System.out.println("[1] View Customer Transactions Report");
            System.out.println("[2] View Sales Report");
            System.out.println("[3] Event Summary Report");
            System.out.println("[4] Payment Method Breakdown Report");
            System.out.println("[5] Exit");

            int choice = Utilities.getUserInput("Choice: ");
            switch (choice) {
                case 1 -> {
                    showCustomerTransactionsReport();
                    break;
                }
                case 2 -> {
                    showSalesReport();
                    break;
                }
                case 3 -> {
                    showEventSummaryReport();
                    break;
                }
                case 4 -> {
                    showPaymentMethodBreakdownReport();
                    break;
                }
                case 5 -> {
                    System.out.println("Exiting System...");
                    programRun = false;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showCustomerTransactionsReport() {
        System.out.println("\n---- Customer Transactions Report ----");
        try {
            int customerId = Utilities.getUserInput("Enter Customer ID to generate report: ");

            String summarySql = "SELECT COUNT(*) AS total_transactions, " +
                    "SUM(amount_paid) AS total_spent FROM Transactions " +
                    "WHERE customer_id = ? AND transaction_status = 'Confirmed'";

            PreparedStatement summaryStmt = connection.prepareStatement(summarySql);
            summaryStmt.setInt(1, customerId);
            ResultSet summaryRs = summaryStmt.executeQuery();

            if (summaryRs.next()) {
                System.out.printf("\nTotal Transactions: %d\nTotal Amount Spent: %.2f\n",
                        summaryRs.getInt("total_transactions"),
                        summaryRs.getDouble("total_spent"));
            }

            String topTimeSql = "SELECT DATE(purchase_date) AS date, COUNT(*) AS ticket_count FROM Transactions " +
                    "WHERE customer_id = ? AND transaction_status = 'Confirmed' " +
                    "GROUP BY DATE(purchase_date) ORDER BY ticket_count DESC LIMIT 1";

            PreparedStatement timeStmt = connection.prepareStatement(topTimeSql);
            timeStmt.setInt(1, customerId);
            ResultSet timeRs = timeStmt.executeQuery();

            if (timeRs.next()) {
                System.out.printf("\nMost Tickets Bought on: %s (%d tickets)\n",
                        timeRs.getDate("date"),
                        timeRs.getInt("ticket_count"));
            }

            String topEventSql = "SELECT e.event_name, COUNT(*) AS tickets_bought FROM Transactions t " +
                    "JOIN Tickets tk ON t.ticket_id = tk.ticket_id " +
                    "JOIN Events e ON tk.event_id = e.event_id " +
                    "WHERE t.customer_id = ? AND t.transaction_status = 'Confirmed' " +
                    "GROUP BY e.event_name ORDER BY tickets_bought DESC LIMIT 1";

            PreparedStatement eventStmt = connection.prepareStatement(topEventSql);
            eventStmt.setInt(1, customerId);
            ResultSet eventRs = eventStmt.executeQuery();

            if (eventRs.next()) {
                System.out.printf("\nMost Tickets Bought for Event: %s (%d tickets)\n",
                        eventRs.getString("event_name"),
                        eventRs.getInt("tickets_bought"));
            }

        } catch (SQLException e) {
            System.err.println("Error generating customer transaction report: " + e.getMessage());
        }
    }

    private void showSalesReport() {
        System.out.println("\n---- Sales Report ----");
        boolean programRun = true;

        while (programRun) {
            try {
                String dateFilter = "";
                boolean validInputs = false;

                while (!validInputs) {
                    int day = Utilities.getUserInput("Enter day (0 if skip): ");
                    int month = Utilities.getUserInput("Enter month (0 if skip): ");
                    int year = Utilities.getUserInput("Enter year (0 if skip): ");

                    if (day < 0 || month < 0 || year < 0)
                        throw new InputMismatchException();

                    String yearStr = (year == 0) ? "%" : String.format("%04d", year);
                    String monthStr = (month == 0) ? "%" : String.format("%02d", month);
                    String dayStr = (day == 0) ? "%" : String.format("%02d", day);

                    dateFilter = String.format("%s-%s-%s", yearStr, monthStr, dayStr);
                    validInputs = true;
                }

                String query = """
                        SELECT
                            e.event_id,
                            e.event_name,
                            SUM(CASE WHEN t.transaction_status = 'Confirmed' THEN t.amount_paid ELSE 0 END) AS total_revenue,
                            COUNT(CASE WHEN t.transaction_status = 'Confirmed' THEN 1 END) AS sold_tickets,
                            COUNT(CASE WHEN t.transaction_status = 'Cancelled' THEN 1 END) AS cancelled_tickets,
                            COUNT(CASE WHEN t.transaction_status = 'Refunded' THEN 1 END) AS refunded_tickets
                        FROM Events e
                        LEFT JOIN Tickets tk ON e.event_id = tk.event_id
                        LEFT JOIN Transactions t ON tk.ticket_id = t.ticket_id
                        WHERE DATE(t.purchase_date) LIKE ?
                        GROUP BY e.event_id, e.event_name;
                        """;

                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, dateFilter);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<EventReport> reports = new ArrayList<>();

                        while (rs.next()) {
                            EventReport report = new EventReport(
                                    rs.getInt("event_id"),
                                    rs.getString("event_name"),
                                    rs.getDouble("total_revenue"),
                                    rs.getInt("sold_tickets"),
                                    rs.getInt("cancelled_tickets"),
                                    rs.getInt("refunded_tickets"));
                            reports.add(report);
                        }

                        System.out.println("\nEvent Revenue and Ticket Breakdown Report");
                        System.out.println("-".repeat(80));

                        if (reports.isEmpty()) {
                            System.out.println("No records found for the selected date range.");
                        } else {
                            for (EventReport report : reports) {
                                System.out.println(report);
                            }
                        }

                        System.out.println("-".repeat(80));
                    }
                }
            } catch (SQLException e) {
                System.out.println("Query error: " + e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private void showEventSummaryReport() {
        System.out.println("\n---- Event Summary Report ----");
        String start = util.Utilities.getUserText("Enter Start Date (YYYY-MM-DD) or leave blank: ");
        String end = util.Utilities.getUserText("Enter End Date (YYYY-MM-DD) or leave blank: ");

        StringBuilder query = new StringBuilder(
                "SELECT e.event_id, e.event_name, e.event_status, "
                        + "COUNT(tk.ticket_id) AS total_tickets, "
                        + "SUM(CASE WHEN tr.transaction_status = 'Confirmed' THEN 1 ELSE 0 END) AS sold_tickets, "
                        + "SUM(CASE WHEN tr.transaction_status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled_tickets, "
                        + "SUM(CASE WHEN tr.transaction_status = 'Confirmed' THEN tr.amount_paid ELSE 0 END) AS total_revenue "
                        + "FROM Events e "
                        + "LEFT JOIN Tickets tk ON e.event_id = tk.event_id "
                        + "LEFT JOIN Transactions tr ON tk.ticket_id = tr.ticket_id ");

        boolean hasRange = !start.isBlank() && !end.isBlank();
        if (hasRange) {
            query.append("WHERE e.event_date BETWEEN ? AND ? ");
        }

        query.append("GROUP BY e.event_id, e.event_name, e.event_status ORDER BY e.event_date;");

        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            if (hasRange) {
                pstmt.setDate(1, java.sql.Date.valueOf(start));
                pstmt.setDate(2, java.sql.Date.valueOf(end));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println(
                        "\nID | Event Name                    | Status     | Total | Sold  | Canc  | Revenue     | Attendance");
                System.out.println(
                        "--------------------------------------------------------------------------------------------");

                while (rs.next()) {
                    int id = rs.getInt("event_id");
                    String name = rs.getString("event_name");
                    String status = rs.getString("event_status");
                    int total = rs.getInt("total_tickets");
                    int sold = rs.getInt("sold_tickets");
                    int cancelled = rs.getInt("cancelled_tickets");
                    double revenue = rs.getDouble("total_revenue");
                    double attendance = total == 0 ? 0 : (sold * 100.0 / total);

                    System.out.printf("%-3d | %-30s | %-10s | %-5d | %-5d | %-5d | %-11.2f | %6.2f%%%n",
                            id, name, status, total, sold, cancelled, revenue, attendance);
                }

                System.out.println(
                        "--------------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error generating event summary: " + e.getMessage());
        }
    }
    
    private void showPaymentMethodBreakdownReport() {
        System.out.println("\n--- Payment Method Breakdown Report ---");

        String start = util.Utilities.getUserText("Enter Start Date (YYYY-MM-DD) or leave blank: ").trim();
        String end = util.Utilities.getUserText("Enter End Date (YYYY-MM-DD) or leave blank: ").trim();
        boolean hasRange = !start.isEmpty() && !end.isEmpty();

        if (hasRange && (!isValidDate(start) || !isValidDate(end))) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }

        String query = """
        SELECT t.payment_method,
               DATE(t.purchase_date) AS purchase_date,
               COUNT(*) AS total_transactions,
               SUM(t.amount_paid) AS total_revenue,
               AVG(t.amount_paid) AS avg_transaction,
               e.event_status,
               e.event_name
        FROM Transactions t
        JOIN Tickets tk ON t.ticket_id = tk.ticket_id
        JOIN Events e ON tk.event_id = e.event_id
        WHERE t.transaction_status = 'Confirmed' """
                + (hasRange ? "AND t.purchase_date BETWEEN ? AND ? " : "")
                + """
        GROUP BY t.payment_method, DATE(t.purchase_date), e.event_status, e.event_name
        ORDER BY t.payment_method, purchase_date;
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            if (hasRange) {
                pstmt.setDate(1, java.sql.Date.valueOf(start));
                pstmt.setDate(2, java.sql.Date.valueOf(end));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                String currentMethod = "";
                double grandRevenue = 0;
                int grandTxns = 0;
                double methodRevenue = 0;
                int methodTxns = 0;

                while (rs.next()) {
                    String method = rs.getString("payment_method");
                    String date = rs.getString("purchase_date");
                    double revenue = rs.getDouble("total_revenue");
                    int txns = rs.getInt("total_transactions");
                    double avg = rs.getDouble("avg_transaction");
                    String eventName = rs.getString("event_name");
                    String status = rs.getString("event_status");

                    // New section per payment method
                    if (!method.equals(currentMethod)) {
                        // Print subtotal if switching method
                        if (!currentMethod.isEmpty()) {
                            System.out.printf("%n>> TOTAL for %s: PHP %.2f | Txns: %d%n", currentMethod, methodRevenue, methodTxns);
                            System.out.println("---------------------------------------------------------------------------------------------\n");
                        }

                        // Start new section
                        currentMethod = method;
                        methodRevenue = 0;
                        methodTxns = 0;

                        System.out.printf("---- %s Transactions ----%n", method.toUpperCase());
                        System.out.printf("%-12s %-15s PHP %-10s | Txns: %-5s Avg Tx: %-8s Event: %-28s Status: %-15s%n",
                                "Payment", "Date", "Revenue", "Count", "Amount", "Name", "State");
                        System.out.println("---------------------------------------------------------------------------------------------");
                    }

                    // Print row
                    System.out.printf("%-12s %-15s PHP %-10.2f | Txns: %-5d Avg Tx: %-8.2f Event: %-28s Status: %-15s%n",
                            method, date, revenue, txns, avg, eventName, status);

                    // Totals
                    methodRevenue += revenue;
                    methodTxns += txns;
                    grandRevenue += revenue;
                    grandTxns += txns;
                }

                // Final method total
                if (!currentMethod.isEmpty()) {
                    System.out.printf("%n>> TOTAL for %s: PHP %.2f | Txns: %d%n", currentMethod, methodRevenue, methodTxns);
                    System.out.println("---------------------------------------------------------------------------------------------\n");
                }

                // Grand totals
                System.out.println("==================== GRAND TOTAL ====================");
                System.out.printf("TOTAL Revenue: PHP %.2f | TOTAL Transactions: %d%n", grandRevenue, grandTxns);
                System.out.println("=====================================================");
            }
        } catch (SQLException e) {
            System.err.println("Error generating payment method breakdown report: " + e.getMessage());
        }
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

class EventReport {
    int eventId;
    String eventName;
    double totalRevenue;
    int sold;
    int cancelled;
    int refunded;

    public EventReport(int eventId, String eventName, double totalRevenue, int sold, int cancelled, int refunded) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.totalRevenue = totalRevenue;
        this.sold = sold;
        this.cancelled = cancelled;
        this.refunded = refunded;
    }

    @Override
    public String toString() {
        return String.format("%-10d %-30s PHP %-10.2f | Sold: %-5d Cancelled: %-5d Refunded: %-5d",
                eventId, eventName, totalRevenue, sold, cancelled, refunded);
    }
}