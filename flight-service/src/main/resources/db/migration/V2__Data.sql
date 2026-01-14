-- =====================================================
-- Airline Ticketing System - Initial Data
-- V2__Data.sql
-- =====================================================

-- =====================================================
-- SAMPLE FLIGHTS
-- =====================================================
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES
    -- Istanbul to New York
    ('TK001', 'IST', 'JFK', '2026-01-15 08:00:00', '2026-01-15 14:30:00', 899.99, 250, 45, 'SCHEDULED'),
    ('TK002', 'IST', 'JFK', '2026-01-15 22:00:00', '2026-01-16 04:30:00', 799.99, 250, 120, 'SCHEDULED'),
    
    -- Istanbul to London
    ('TK011', 'IST', 'LHR', '2026-01-15 06:00:00', '2026-01-15 09:00:00', 349.99, 180, 90, 'SCHEDULED'),
    ('TK012', 'IST', 'LHR', '2026-01-15 14:00:00', '2026-01-15 17:00:00', 399.99, 180, 150, 'SCHEDULED'),
    
    -- Istanbul to Paris
    ('TK021', 'IST', 'CDG', '2026-01-15 07:30:00', '2026-01-15 10:30:00', 329.99, 180, 60, 'SCHEDULED'),
    
    -- Istanbul to Dubai
    ('TK031', 'IST', 'DXB', '2026-01-15 01:00:00', '2026-01-15 07:00:00', 449.99, 300, 200, 'SCHEDULED'),
    
    -- New York to Istanbul
    ('TK101', 'JFK', 'IST', '2026-01-16 18:00:00', '2026-01-17 12:30:00', 949.99, 250, 80, 'SCHEDULED'),
    
    -- London to Istanbul
    ('TK111', 'LHR', 'IST', '2026-01-16 10:00:00', '2026-01-16 16:00:00', 379.99, 180, 45, 'SCHEDULED'),
    
    -- Past flights (for scheduler testing)
    ('TK901', 'IST', 'JFK', '2026-01-10 08:00:00', '2026-01-10 14:30:00', 899.99, 250, 200, 'SCHEDULED'),
    ('TK902', 'IST', 'LHR', '2026-01-10 06:00:00', '2026-01-10 09:00:00', 349.99, 180, 150, 'SCHEDULED');

-- =====================================================
-- PARTNER AIRLINES
-- =====================================================
INSERT INTO partner_airlines (name, code, api_key, is_active, rate_limit)
VALUES
    ('Star Alliance Partner', 'SAP', 'sap_secret_key_99', TRUE, 1000),
    ('SkyTeam Partner', 'STP', 'stp_secret_key_88', TRUE, 500),
    ('OneWorld Partner', 'OWP', 'owp_secret_key_77', TRUE, 750),
    ('Inactive Partner', 'INP', 'inp_secret_key_66', FALSE, 100);

-- =====================================================
-- SAMPLE MILES ACCOUNTS
-- =====================================================
INSERT INTO miles_accounts (azure_user_id, member_number, balance, tier)
VALUES
    ('azure-user-001', 'ML100001', 15000, 'GOLD'),
    ('azure-user-002', 'ML100002', 5000, 'SILVER'),
    ('azure-user-003', 'ML100003', 1000, 'BRONZE'),
    ('azure-user-004', 'ML100004', 50000, 'PLATINUM');

-- =====================================================
-- SAMPLE MILES TRANSACTIONS
-- =====================================================
INSERT INTO miles_transactions (account_id, amount, type, description, reference_id, transaction_date)
VALUES
    (1, 5000, 'EARN', 'Flight IST-JFK', 'TK001-2025', '2025-12-01 10:00:00'),
    (1, 3000, 'EARN', 'Flight JFK-IST', 'TK101-2025', '2025-12-10 15:00:00'),
    (1, 2000, 'BONUS', 'Welcome bonus', 'WELCOME-001', '2025-11-01 09:00:00'),
    (1, -1000, 'BURN', 'Upgrade to Business', 'UPG-001', '2025-12-15 12:00:00'),
    (2, 3000, 'EARN', 'Flight IST-LHR', 'TK011-2025', '2025-12-05 08:00:00'),
    (2, 1500, 'EARN', 'Flight LHR-IST', 'TK111-2025', '2025-12-12 11:00:00'),
    (3, 1000, 'BONUS', 'Sign-up bonus', 'SIGNUP-003', '2026-01-01 00:00:00');

-- =====================================================
-- SAMPLE BOOKINGS (for testing)
-- =====================================================
INSERT INTO bookings (ref, flight_id, user_id, user_email, status, price_paid, passenger_count)
VALUES
    ('BK001001', 1, 'azure-user-001', 'user1@example.com', 'CONFIRMED', 899.99, 1),
    ('BK001002', 1, 'azure-user-002', 'user2@example.com', 'CONFIRMED', 1799.98, 2),
    ('BK002001', 2, 'azure-user-003', 'user3@example.com', 'PENDING', 799.99, 1);

-- =====================================================
-- SAMPLE PASSENGERS
-- =====================================================
INSERT INTO passengers (booking_id, first_name, last_name, passport_no, date_of_birth, nationality)
VALUES
    (1, 'John', 'Doe', 'US123456789', '1985-05-15', 'USA'),
    (2, 'Jane', 'Smith', 'UK987654321', '1990-08-22', 'UK'),
    (2, 'Bob', 'Smith', 'UK987654322', '1988-03-10', 'UK'),
    (3, 'Alice', 'Johnson', 'DE456789123', '1995-11-30', 'Germany');
