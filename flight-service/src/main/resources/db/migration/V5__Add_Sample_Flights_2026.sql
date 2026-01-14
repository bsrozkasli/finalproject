-- =====================================================
-- Airline Ticketing System - Sample Data 2026
-- V5__Add_Sample_Flights_2026.sql
-- =====================================================

-- LHR (London) -> SAW (Istanbul)
-- Daily flights at 10:00 and 18:00 for Jan 2026
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES 
('TK1980', 'LHR', 'SAW', '2026-01-14 10:00:00', '2026-01-14 14:00:00', 150.00, 180, 0, 'SCHEDULED'),
('TK1982', 'LHR', 'SAW', '2026-01-14 18:00:00', '2026-01-14 22:00:00', 180.00, 180, 0, 'SCHEDULED'),
('TK1984', 'LHR', 'SAW', '2026-01-16 10:00:00', '2026-01-16 14:00:00', 160.00, 180, 0, 'SCHEDULED'),
('TK1986', 'LHR', 'SAW', '2026-01-18 10:00:00', '2026-01-18 14:00:00', 155.00, 180, 0, 'SCHEDULED'),
('TK1988', 'LHR', 'SAW', '2026-01-20 10:00:00', '2026-01-20 14:00:00', 150.00, 180, 0, 'SCHEDULED');

-- SAW (Istanbul) -> LHR (London)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES 
('TK1981', 'SAW', 'LHR', '2026-01-15 09:00:00', '2026-01-15 11:00:00', 145.00, 180, 0, 'SCHEDULED'),
('TK1983', 'SAW', 'LHR', '2026-01-17 09:00:00', '2026-01-17 11:00:00', 165.00, 180, 0, 'SCHEDULED');

-- IST (Istanbul) -> JFK (New York)
-- Long haul, expensive (Changed codes to TK5001, TK5003, TK5005 to avoid conflict with V2)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES 
('TK5001', 'IST', 'JFK', '2026-01-14 07:00:00', '2026-01-14 17:00:00', 850.00, 300, 50, 'SCHEDULED'),
('TK5003', 'IST', 'JFK', '2026-01-15 07:00:00', '2026-01-15 17:00:00', 900.00, 300, 20, 'SCHEDULED'),
('TK5005', 'IST', 'JFK', '2026-01-16 07:00:00', '2026-01-16 17:00:00', 870.00, 300, 0, 'SCHEDULED');

-- JFK -> IST (Changed codes to TK5002, TK5004 to avoid conflict with V2)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES 
('TK5002', 'JFK', 'IST', '2026-01-14 20:00:00', '2026-01-15 12:00:00', 850.00, 300, 0, 'SCHEDULED'),
('TK5004', 'JFK', 'IST', '2026-01-16 20:00:00', '2026-01-17 12:00:00', 880.00, 300, 0, 'SCHEDULED');

-- CDG (Paris) -> DXB (Dubai) (Changed codes to AF5100, AF5102)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status)
VALUES 
('AF5100', 'CDG', 'DXB', '2026-01-14 14:00:00', '2026-01-14 23:00:00', 450.00, 250, 0, 'SCHEDULED'),
('AF5102', 'CDG', 'DXB', '2026-01-15 14:00:00', '2026-01-15 23:00:00', 450.00, 250, 0, 'SCHEDULED');
