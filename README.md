# JD-TicketReservationSystem

A java-based ticketing management system that handles customer, tickets, transactions, and events management.

## Features

### Records Management
- **Customer Records**  
  - Manage customer information and contact details.  
  - View booked events and transaction history.

- **Ticket Records**  
  - Add, remove, and view ticket data including seat number, type, and price.  
  - Update ticket statuses: `Available`, `Sold`, `Cancelled`.  
  - Monitor statistics for each ticket type per event.

- **Transaction Records**  
  - Record all purchases, cancellations, and refunds.  
  - Track customer, ticket, payment method, and transaction status.  
  - View analytics on confirmed, pending, failed, or refunded transactions.

- **Event Records**  
  - Display and filter event details by date, status, and availability.  
  - Manage event time, venue, and status (`Scheduled`, `Ongoing`, `Completed`, `Cancelled`).

### Transaction Management
- **Placing Orders**  
  - Checks ticket availability and updates records after purchase.  
  - Links transaction to customer and event.

- **Refunding / Cancelling Orders**  
  - Validates ticket ownership and eligibility.  
  - Restores ticket availability and updates transaction logs.

- **Transferring Tickets**  
  - Transfers ticket ownership between customers.  
  - Logs actions for traceability and customer support.

- **Event Status Handling**  
  - Automatically updates event status based on date and time.  
  - Blocks transactions for cancelled or completed events.  
  - Propagates changes across ticket and transaction records.
 
### Report Management
- **Customer Transactions Report**  
  - Total transactions, spending, and purchase trends by date or event.

- **Sales Report**  
  - Revenue per event, ticket sales, and refund stats per time period.

- **Event Summary Report**  
  - Lists events by status, ticket distribution, and attendance rate.

- **Payment Method Breakdown Report**  
  - Shows revenue by payment type and links behavior to event performance.  
  - Supports insights for financial reporting and marketing optimization.

--- 

## Getting Started
1. Ensure you have Java 17 or later installed
2. Set up MYSQL database
3. Run the `JD-TicketReservationSystem.sql` script to create the database
4. Update the database connection details in `DatabaseConnection.java` (Change password)
5. Compile and run the application:
  ```cli
  javac -cp ".;mysql-connector-j-9.3.0.jar" -d out src/util/*.java src/*.java view/*.java
  java -cp ".;mysql-connector-j-9.3.0.jar;out" MainFrame
  ```

