DROP DATABASE IF EXISTS `dbtickets`;
CREATE DATABASE IF NOT EXISTS `dbtickets`;
USE `dbtickets`;

-- EVENTS table
CREATE TABLE IF  NOT EXISTS Events (
    event_id INT PRIMARY KEY,
    event_name VARCHAR(30) NOT NULL,
    venue VARCHAR(100) NOT NULL,
    event_date DATE,
    start_time TIME,
    end_time TIME,
    event_status ENUM('Scheduled', 'Ongoing', 'Completed', 'Cancelled') NOT NULL
);

INSERT INTO Events (event_id, event_name, venue, event_date, start_time, end_time, event_status) VALUES 
(1, 'Le Sserafim Tour in Manila', 'SM Mall of Asia Arena', '2025-08-02', '19:30:00', '21:00:00', 'Scheduled'),
(2, 'BLACKPINK Deadline World Tour', 'Philippine Arena', '2025-11-22', '19:00:00', '21:00:00', 'Scheduled'),
(3, 'Justin Bieber SWAG Album Tour', 'Philippine Arena', '2025-11-28', '18:00:00', '20:00:00', 'Cancelled'),
(4, 'IU H.E.R. Tour', 'Araneta Coliseum', '2025-07-14', '18:30:00', '20:30:00', 'Completed'),
(5, 'EXO Reunion Concert', 'Rizal Memorial Stadium', '2025-01-10', '20:00:00', '22:30:00', 'Completed'),
(6, 'SEVENTEEN Follow Again Tour', 'Smart Araneta Coliseum', '2025-12-01', '19:00:00', '21:30:00', 'Scheduled'),
(7, 'NewJeans Bunnies World', 'MOA Arena', '2025-12-18', '18:00:00', '20:30:00', 'Scheduled'),
(8, 'Taylor Swift The Eras Tour', 'Philippine Arena', '2026-01-15', '20:00:00', '23:00:00', 'Scheduled'),
(9, 'SB19 Pagtatag Finale', 'New Frontier Theater', '2025-11-08', '19:00:00', '21:00:00', 'Scheduled'),
(10, 'NCT Nation', 'Cebu Convention Center', '2025-10-28', '18:30:00', '21:00:00', 'Scheduled'),
(11, 'ENHYPEN Fate Plus', 'Clark International Stadium', '2025-12-22', '19:00:00', '21:30:00', 'Scheduled'),
(12, 'Twice Ready to Be Tour', 'SMX Convention Center', '2025-12-29', '18:00:00', '20:00:00', 'Scheduled'),
(13, 'Ben&Ben Live in the Park', 'Ayala Triangle Gardens', '2025-09-20', '17:00:00', '19:00:00', 'Scheduled');

-- CUSTOMERS table
CREATE TABLE IF  NOT EXISTS Customers (
    customer_id INT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    phone_number VARCHAR(11) NOT NULL
);

INSERT INTO Customers (customer_id, last_name, first_name, email, phone_number) VALUES
(100, 'Snow', 'Jon', 'jonsnow@gmail.com', '09369339160'),
(101, 'Tesfaye', 'Abel', 'weeknd@gmail.com', '09456574696'),
(102, 'Zefanya', 'Nicole', 'niki@gmail.com', '09953218112'),
(103, 'Clara', 'Maria', 'maria.clara@gmail.com', '09876543211'),
(104, 'Garcia', 'Alex', 'alex.garcia@email.com', '09171234568'),
(105, 'Lee', 'Minji', 'minji.lee@email.com', '09181234567'),
(106, 'Reyes', 'Carlos', 'carlos.reyes@email.com', '09181230001'),
(107, 'Tan', 'Bianca', 'bianca.tan@email.com', '09221230002'),
(108, 'Dela Cruz', 'Miguel', 'miguel.dc@email.com', '09181230003'),
(109, 'Villanueva', 'Sofia', 'sofia.v@email.com', '09221230004'),
(110, 'Torres', 'Marco', 'marco.t@email.com', '09391230005'),
(111, 'Gomez', 'Isabelle', 'isabelle.g@email.com', '09171230006'),
(112, 'Navarro', 'Jake', 'jake.n@email.com', '09221230007'),
(113, 'Lopez', 'Cheska', 'cheska.l@email.com', '09181230008'),
(114, 'Santos', 'Nathan', 'nathan.s@email.com', '09391230009'),
(115, 'Lim', 'Kyla', 'kyla.lim@email.com', '09221230010');

-- TICKETS table
CREATE TABLE IF  NOT EXISTS Tickets (
    ticket_id INT AUTO_INCREMENT PRIMARY KEY,
    event_id INT NOT NULL,
    ticket_type VARCHAR(30) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    seat_number VARCHAR(11) NOT NULL,
    ticket_status ENUM('Available', 'Sold', 'Cancelled') NOT NULL,
    FOREIGN KEY (event_id) REFERENCES Events(event_id)
);

INSERT INTO Tickets (ticket_id, event_id, ticket_type, price, seat_number, ticket_status) VALUES
(1000, 1, 'VIP Standing', 10000.00, 'A1', 'Sold'), -- Sold to customer 103
(1001, 1, 'General Admission', 3500.00, 'F15', 'Available'), 
(1002, 2, 'Upper Box', 7000.00, 'D12', 'Sold'), -- Sold to customer 101
(1003, 2, 'Lower Box', 8000.00, 'C3', 'Cancelled'), -- Refunded to customer 100
(1004, 3, 'VIP', 9200.00, 'A10', 'Available'),
(1005, 3, 'General Admission', 4200.00, 'C15', 'Available'),
(1006, 4, 'VIP', 8800.00, 'A05', 'Available'),
(1007, 5, 'General Admission', 3900.00, 'B20', 'Available'),
(1008, 6, 'VIP', 15000.00, 'V01', 'Available'),
(1009, 7, 'General Admission', 7500.00, 'G12', 'Available'),
(1010, 8, 'VIP', 6800.00, 'F03', 'Available'),
(1011, 9, 'General Admission', 3200.00, 'J09', 'Available'),
(1012, 10, 'VIP', 7900.00, 'D08', 'Available'),
(1013, 11, 'General Admission', 3400.00, 'L11', 'Available'),
(1014, 12, 'VIP', 8700.00, 'E17', 'Available'),
(1015, 13, 'General Admission', 4100.00, 'M04', 'Available'),
(1016, 13, 'VIP', 9100.00, 'B01', 'Available'),
(1017, 12, 'General Admission', 4300.00, 'P22', 'Available'),
(1018, 11, 'VIP', 5000.00, 'H07', 'Available'),
(1019, 10, 'General Admission', 2000.00, 'S10', 'Available');

-- TRANSACTIONS table
CREATE TABLE IF  NOT EXISTS Transactions (
    transaction_id INT PRIMARY KEY,
    customer_id INT NOT NULL,
    ticket_id INT NOT NULL,
    purchase_date DATE,
    payment_method VARCHAR(11) NOT NULL,
    amount_paid DECIMAL(10,2),
    transaction_status ENUM('Pending', 'Confirmed', 'Cancelled', 'Failed', 'Refunded') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id),
    FOREIGN KEY (ticket_id) REFERENCES Tickets(ticket_id)
);

INSERT INTO Transactions (transaction_id, customer_id, ticket_id, purchase_date, payment_method, amount_paid, transaction_status) VALUES
(500, 100, 1003, '2025-07-10', 'credit', 8000.00, 'Refunded'), -- Refunded
(501, 101, 1002, '2025-06-24', 'e-wallet', 7000.00, 'Confirmed'),
(502, 103, 1000, '2025-04-01', 'e-wallet', 10000.00, 'Confirmed'),
(503, 102, 1004, '2025-08-01', 'e-wallet', 9200.00, 'Confirmed'),
(504, 104, 1005, '2025-08-01', 'credit', 4200.00, 'Confirmed'),
(505, 105, 1006, '2025-08-02', 'bank', 3900.00, 'Pending'),
(506, 106, 1007, '2025-08-03', 'credit', 7500.00, 'Confirmed'),
(507, 107, 1008, '2025-08-04', 'e-wallet', 6800.00, 'Cancelled'),
(508, 108, 1009, '2025-08-05', 'e-wallet', 3400.00, 'Confirmed'),
(509, 109, 1010, '2025-08-05', 'bank', 4100.00, 'Failed'),
(510, 110, 1011, '2025-08-06', 'credit', 9100.00, 'Confirmed'),
(511, 111, 1012, '2025-08-07', 'e-wallet', 2000.00, 'Refunded'),
(512, 112, 1013, '2025-08-07', 'bank', 5000.00, 'Confirmed'),
(513, 113, 1014, '2025-08-04', 'e-wallet', 6800.00, 'Cancelled'),
(514, 114, 1015, '2025-08-05', 'e-wallet', 3400.00, 'Confirmed'),
(515, 115, 1016, '2025-08-05', 'bank', 4100.00, 'Failed'),
(516, 115, 1017, '2025-08-06', 'credit', 9100.00, 'Confirmed'),
(517, 114, 1018, '2025-08-07', 'e-wallet', 2000.00, 'Refunded'),
(518, 113, 1019, '2025-08-07', 'bank', 5000.00, 'Confirmed');

-- TRANSACTION HISTORY table
CREATE TABLE IF  NOT EXISTS Transaction_History (
    transaction_id INT NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES Transactions(transaction_id),
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

INSERT INTO Transaction_History (transaction_id, customer_id) VALUES
(500, 100),
(501, 101),
(502, 103),
(503,  102),
(504,  104),
(505,  105),
(506,  106),
(507,  107),
(508,  108),
(509,  109),
(510,  110),
(511, 111),
(512,112),
(513,113),
(514,114),
(515, 115),
(516,115),
(517,114),
(518,113);

-- TICKET STATISTICS table
CREATE TABLE IF  NOT EXISTS Ticket_Statistics (
    event_id INT NOT NULL,
    ticket_type VARCHAR(30) NOT NULL,
    total_tickets INT NOT NULL,
    sold_tickets INT NOT NULL,
    cancelled_tickets INT NOT NULL,
    available_tickets INT NOT NULL,
    FOREIGN KEY (event_id) REFERENCES Events(event_id)
);

INSERT INTO Ticket_Statistics (event_id, ticket_type, total_tickets, sold_tickets, cancelled_tickets, available_tickets) VALUES
(1, 'VIP Standing', 50, 45, 1, 4),
(1, 'VIP Sitting', 50, 35, 2, 13),
(1, 'Lower Box', 70, 60, 5, 5),
(1, 'Upper Box', 100, 70, 10, 20),
(1, 'General Admission', 200, 180, 5, 15),
(2, 'VIP Standing', 40, 35, 0, 5),
(2, 'VIP Sitting', 60, 55, 1, 4),
(2, 'Lower Box', 25, 3, 1, 21),
(2, 'Upper Box', 70, 60, 2, 8),
(2, 'General Admission', 150, 130, 3, 17),
(3, 'VIP', 70, 60, 2, 8),
(3, 'General Admission', 150, 130, 3, 17),
(3, 'VIP', 70, 60, 2, 8),
(3, 'General Admission', 150, 130, 3, 17),
(4, 'VIP', 70, 60, 2, 8),
(4, 'General Admission', 150, 130, 3, 17),
(5, 'VIP', 70, 60, 2, 8),
(5, 'General Admission', 150, 130, 3, 17),
(6, 'VIP', 70, 60, 2, 8),
(6, 'General Admission', 150, 130, 3, 17),
(7, 'VIP', 70, 60, 2, 8),
(7, 'General Admission', 150, 130, 3, 17),
(8, 'VIP', 70, 60, 2, 8),
(8, 'General Admission', 150, 130, 3, 17),
(9, 'VIP', 70, 60, 2, 8),
(9, 'General Admission', 150, 130, 3, 17),
(10, 'VIP', 70, 60, 2, 8),
(10, 'General Admission', 150, 130, 3, 17),
(11, 'VIP', 70, 60, 2, 8),
(11, 'General Admission', 150, 130, 3, 17),
(12, 'VIP', 70, 60, 2, 8),
(12, 'General Admission', 150, 130, 3, 17),
(13, 'VIP', 70, 60, 2, 8),
(13, 'General Admission', 150, 130, 3, 17);

-- TRANSACTION STATISTICS table
CREATE TABLE IF NOT EXISTS Transaction_Statistics (
    customer_id INT NOT NULL,
    ticket_id INT NOT NULL,
    failed_tickets INT NOT NULL,
    pending_tickets INT NOT NULL,
    confirmed_tickets INT NOT NULL,
    cancelled_tickets INT NOT NULL,
    refunded_tickets INT NOT NULL,
    FOREIGN KEY (customer_id)
        REFERENCES Customers (customer_id),
    FOREIGN KEY (ticket_id)
        REFERENCES Tickets (ticket_id) ON DELETE CASCADE
);

INSERT INTO Transaction_Statistics (customer_id, ticket_id, failed_tickets, pending_tickets, confirmed_tickets, cancelled_tickets, refunded_tickets) VALUES
(100, 1003, 0, 0, 0, 0, 1),  -- Refunded
(101, 1002, 0, 0, 1, 0, 0),  -- Confirmed
(102, 1004, 0, 0, 1, 0, 0),  -- Confirmed
(103, 1000, 0, 0, 1, 0, 0),  -- Confirmed
(104, 1005, 0, 0, 1, 0, 0),  -- Confirmed
(105, 1006, 0, 1, 0, 0, 0),  -- Pending
(106, 1007, 0, 0, 1, 0, 0),  -- Confirmed
(107, 1008, 0, 0, 0, 1, 0),  -- Cancelled
(108, 1009, 0, 0, 1, 0, 0),  -- Confirmed
(109, 1010, 1, 0, 0, 0, 0),  -- Failed
(110, 1011, 0, 0, 1, 0, 0),  -- Confirmed
(111, 1012, 0, 0, 0, 0, 1),  -- Refunded
(112, 1013, 0, 0, 1, 0, 0),  -- Confirmed
(113, 1014, 0, 0, 0, 1, 0),  -- Cancelled
(114, 1015, 0, 0, 1, 0, 0),  -- Confirmed
(115, 1016, 1, 0, 0, 0, 0),  -- Failed
(115, 1017, 0, 0, 1, 0, 0),  -- Confirmed
(114, 1018, 0, 0, 0, 0, 1),  -- Refunded
(113, 1019, 0, 0, 1, 0, 0);  -- Confirmed
