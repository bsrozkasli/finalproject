-- =====================================================
-- Airline Ticketing System - Extended Data
-- V6__Add_More_Airports_And_Flights.sql
-- =====================================================

-- ADDITIONAL AIRPORTS
INSERT INTO airports (code, name, city, country) VALUES
-- B ile başlayanlar
('BCN', 'Barcelona-El Prat Airport', 'Barcelona', 'Spain'),
('BER', 'Berlin Brandenburg Airport', 'Berlin', 'Germany'),
('BKK', 'Suvarnabhumi Airport', 'Bangkok', 'Thailand'),
('BOM', 'Chhatrapati Shivaji Airport', 'Mumbai', 'India'),
('BRU', 'Brussels Airport', 'Brussels', 'Belgium'),
('BUD', 'Budapest Ferenc Liszt Airport', 'Budapest', 'Hungary'),
-- L ile başlayanlar
('LAX', 'Los Angeles International Airport', 'Los Angeles', 'USA'),
('LIS', 'Lisbon Portela Airport', 'Lisbon', 'Portugal'),
('LED', 'Pulkovo Airport', 'Saint Petersburg', 'Russia'),
-- Diğer önemli havalimanları
('SIN', 'Changi Airport', 'Singapore', 'Singapore'),
('HKG', 'Hong Kong International Airport', 'Hong Kong', 'China'),
('NRT', 'Narita International Airport', 'Tokyo', 'Japan'),
('ICN', 'Incheon International Airport', 'Seoul', 'South Korea'),
('SYD', 'Sydney Kingsford Smith Airport', 'Sydney', 'Australia'),
('MEL', 'Melbourne Airport', 'Melbourne', 'Australia'),
('YYZ', 'Toronto Pearson Airport', 'Toronto', 'Canada'),
('ORD', 'O''Hare International Airport', 'Chicago', 'USA'),
('MIA', 'Miami International Airport', 'Miami', 'USA'),
('ATL', 'Hartsfield-Jackson Atlanta Airport', 'Atlanta', 'USA'),
('SFO', 'San Francisco International Airport', 'San Francisco', 'USA'),
('SEA', 'Seattle-Tacoma Airport', 'Seattle', 'USA'),
('DOH', 'Hamad International Airport', 'Doha', 'Qatar'),
('AUH', 'Abu Dhabi International Airport', 'Abu Dhabi', 'UAE'),
('MAD', 'Adolfo Suárez Madrid–Barajas Airport', 'Madrid', 'Spain'),
('FCO', 'Leonardo da Vinci–Fiumicino Airport', 'Rome', 'Italy'),
('VIE', 'Vienna International Airport', 'Vienna', 'Austria'),
('ZRH', 'Zurich Airport', 'Zurich', 'Switzerland'),
('CPH', 'Copenhagen Airport', 'Copenhagen', 'Denmark'),
('ARN', 'Stockholm Arlanda Airport', 'Stockholm', 'Sweden'),
('OSL', 'Oslo Gardermoen Airport', 'Oslo', 'Norway'),
('HEL', 'Helsinki-Vantaa Airport', 'Helsinki', 'Finland'),
('WAW', 'Warsaw Chopin Airport', 'Warsaw', 'Poland'),
('PRG', 'Václav Havel Airport Prague', 'Prague', 'Czech Republic'),
('ATH', 'Athens International Airport', 'Athens', 'Greece'),
('CAI', 'Cairo International Airport', 'Cairo', 'Egypt'),
('JNB', 'O.R. Tambo International Airport', 'Johannesburg', 'South Africa'),
('GRU', 'São Paulo–Guarulhos Airport', 'São Paulo', 'Brazil'),
('EZE', 'Ministro Pistarini Airport', 'Buenos Aires', 'Argentina'),
('MEX', 'Mexico City International Airport', 'Mexico City', 'Mexico'),
('DEL', 'Indira Gandhi International Airport', 'Delhi', 'India'),
('PVG', 'Shanghai Pudong Airport', 'Shanghai', 'China'),
('CAN', 'Guangzhou Baiyun Airport', 'Guangzhou', 'China'),
('KUL', 'Kuala Lumpur International Airport', 'Kuala Lumpur', 'Malaysia'),
('CGK', 'Soekarno-Hatta Airport', 'Jakarta', 'Indonesia');

-- =====================================================
-- FLIGHTS FROM ISTANBUL (IST)
-- =====================================================

-- IST -> BCN (Barcelona)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1851', 'IST', 'BCN', '2026-01-14 08:00:00', '2026-01-14 11:30:00', 299.00, 180, 30, 'SCHEDULED'),
('TK1853', 'IST', 'BCN', '2026-01-15 08:00:00', '2026-01-15 11:30:00', 289.00, 180, 15, 'SCHEDULED'),
('TK1855', 'IST', 'BCN', '2026-01-17 08:00:00', '2026-01-17 11:30:00', 279.00, 180, 0, 'SCHEDULED'),
('TK1857', 'IST', 'BCN', '2026-01-20 08:00:00', '2026-01-20 11:30:00', 309.00, 180, 45, 'SCHEDULED');

-- IST -> BER (Berlin)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1721', 'IST', 'BER', '2026-01-14 06:30:00', '2026-01-14 09:00:00', 199.00, 180, 50, 'SCHEDULED'),
('TK1723', 'IST', 'BER', '2026-01-14 14:30:00', '2026-01-14 17:00:00', 219.00, 180, 20, 'SCHEDULED'),
('TK1725', 'IST', 'BER', '2026-01-15 06:30:00', '2026-01-15 09:00:00', 209.00, 180, 10, 'SCHEDULED'),
('TK1727', 'IST', 'BER', '2026-01-16 06:30:00', '2026-01-16 09:00:00', 189.00, 180, 0, 'SCHEDULED'),
('TK1729', 'IST', 'BER', '2026-01-18 06:30:00', '2026-01-18 09:00:00', 229.00, 180, 60, 'SCHEDULED');

-- IST -> BJS (Beijing)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK024', 'IST', 'BJS', '2026-01-14 01:00:00', '2026-01-14 15:00:00', 749.00, 300, 80, 'SCHEDULED'),
('TK026', 'IST', 'BJS', '2026-01-16 01:00:00', '2026-01-16 15:00:00', 699.00, 300, 45, 'SCHEDULED'),
('TK028', 'IST', 'BJS', '2026-01-19 01:00:00', '2026-01-19 15:00:00', 779.00, 300, 120, 'SCHEDULED');

-- IST -> BKK (Bangkok)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK068', 'IST', 'BKK', '2026-01-14 23:30:00', '2026-01-15 13:30:00', 599.00, 300, 100, 'SCHEDULED'),
('TK070', 'IST', 'BKK', '2026-01-16 23:30:00', '2026-01-17 13:30:00', 579.00, 300, 70, 'SCHEDULED'),
('TK072', 'IST', 'BKK', '2026-01-18 23:30:00', '2026-01-19 13:30:00', 619.00, 300, 90, 'SCHEDULED');

-- IST -> LAX (Los Angeles)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK6010', 'IST', 'LAX', '2026-01-14 10:00:00', '2026-01-14 18:00:00', 999.00, 300, 150, 'SCHEDULED'),
('TK6012', 'IST', 'LAX', '2026-01-15 10:00:00', '2026-01-15 18:00:00', 1049.00, 300, 100, 'SCHEDULED'),
('TK6014', 'IST', 'LAX', '2026-01-17 10:00:00', '2026-01-17 18:00:00', 979.00, 300, 80, 'SCHEDULED'),
('TK6016', 'IST', 'LAX', '2026-01-20 10:00:00', '2026-01-20 18:00:00', 1029.00, 300, 200, 'SCHEDULED');

-- IST -> SIN (Singapore)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK054', 'IST', 'SIN', '2026-01-14 22:00:00', '2026-01-15 14:00:00', 649.00, 300, 110, 'SCHEDULED'),
('TK056', 'IST', 'SIN', '2026-01-16 22:00:00', '2026-01-17 14:00:00', 629.00, 300, 85, 'SCHEDULED'),
('TK058', 'IST', 'SIN', '2026-01-19 22:00:00', '2026-01-20 14:00:00', 669.00, 300, 130, 'SCHEDULED');

-- IST -> DOH (Doha)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK780', 'IST', 'DOH', '2026-01-14 03:00:00', '2026-01-14 07:00:00', 399.00, 250, 60, 'SCHEDULED'),
('TK782', 'IST', 'DOH', '2026-01-14 15:00:00', '2026-01-14 19:00:00', 419.00, 250, 40, 'SCHEDULED'),
('TK784', 'IST', 'DOH', '2026-01-15 03:00:00', '2026-01-15 07:00:00', 389.00, 250, 25, 'SCHEDULED'),
('TK786', 'IST', 'DOH', '2026-01-17 03:00:00', '2026-01-17 07:00:00', 409.00, 250, 70, 'SCHEDULED');

-- IST -> MAD (Madrid)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1301', 'IST', 'MAD', '2026-01-14 07:00:00', '2026-01-14 11:00:00', 349.00, 180, 45, 'SCHEDULED'),
('TK1303', 'IST', 'MAD', '2026-01-15 07:00:00', '2026-01-15 11:00:00', 339.00, 180, 30, 'SCHEDULED'),
('TK1305', 'IST', 'MAD', '2026-01-18 07:00:00', '2026-01-18 11:00:00', 359.00, 180, 55, 'SCHEDULED');

-- IST -> FCO (Rome)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1861', 'IST', 'FCO', '2026-01-14 09:00:00', '2026-01-14 11:30:00', 249.00, 180, 65, 'SCHEDULED'),
('TK1863', 'IST', 'FCO', '2026-01-14 17:00:00', '2026-01-14 19:30:00', 269.00, 180, 40, 'SCHEDULED'),
('TK1865', 'IST', 'FCO', '2026-01-15 09:00:00', '2026-01-15 11:30:00', 259.00, 180, 50, 'SCHEDULED'),
('TK1867', 'IST', 'FCO', '2026-01-17 09:00:00', '2026-01-17 11:30:00', 239.00, 180, 20, 'SCHEDULED');

-- =====================================================
-- FLIGHTS FROM LHR (London)
-- =====================================================

-- LHR -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1972', 'LHR', 'IST', '2026-01-14 07:00:00', '2026-01-14 13:00:00', 299.00, 180, 70, 'SCHEDULED'),
('TK1974', 'LHR', 'IST', '2026-01-14 15:00:00', '2026-01-14 21:00:00', 319.00, 180, 50, 'SCHEDULED'),
('TK1976', 'LHR', 'IST', '2026-01-15 07:00:00', '2026-01-15 13:00:00', 289.00, 180, 35, 'SCHEDULED'),
('TK1978', 'LHR', 'IST', '2026-01-17 07:00:00', '2026-01-17 13:00:00', 309.00, 180, 80, 'SCHEDULED');

-- LHR -> JFK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA115', 'LHR', 'JFK', '2026-01-14 08:00:00', '2026-01-14 11:00:00', 599.00, 280, 120, 'SCHEDULED'),
('BA117', 'LHR', 'JFK', '2026-01-14 14:00:00', '2026-01-14 17:00:00', 649.00, 280, 90, 'SCHEDULED'),
('BA119', 'LHR', 'JFK', '2026-01-15 08:00:00', '2026-01-15 11:00:00', 579.00, 280, 75, 'SCHEDULED'),
('BA121', 'LHR', 'JFK', '2026-01-16 08:00:00', '2026-01-16 11:00:00', 619.00, 280, 100, 'SCHEDULED'),
('BA123', 'LHR', 'JFK', '2026-01-18 08:00:00', '2026-01-18 11:00:00', 559.00, 280, 60, 'SCHEDULED');

-- LHR -> LAX
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA269', 'LHR', 'LAX', '2026-01-14 10:00:00', '2026-01-14 14:00:00', 749.00, 280, 140, 'SCHEDULED'),
('BA271', 'LHR', 'LAX', '2026-01-15 10:00:00', '2026-01-15 14:00:00', 729.00, 280, 110, 'SCHEDULED'),
('BA273', 'LHR', 'LAX', '2026-01-17 10:00:00', '2026-01-17 14:00:00', 769.00, 280, 160, 'SCHEDULED');

-- LHR -> SIN
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA015', 'LHR', 'SIN', '2026-01-14 21:00:00', '2026-01-15 17:00:00', 699.00, 300, 130, 'SCHEDULED'),
('BA017', 'LHR', 'SIN', '2026-01-16 21:00:00', '2026-01-17 17:00:00', 679.00, 300, 95, 'SCHEDULED'),
('BA019', 'LHR', 'SIN', '2026-01-19 21:00:00', '2026-01-20 17:00:00', 719.00, 300, 150, 'SCHEDULED');

-- LHR -> DXB
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA107', 'LHR', 'DXB', '2026-01-14 09:00:00', '2026-01-14 19:00:00', 499.00, 280, 100, 'SCHEDULED'),
('BA109', 'LHR', 'DXB', '2026-01-14 21:00:00', '2026-01-15 07:00:00', 479.00, 280, 85, 'SCHEDULED'),
('BA111', 'LHR', 'DXB', '2026-01-15 09:00:00', '2026-01-15 19:00:00', 489.00, 280, 70, 'SCHEDULED'),
('BA113', 'LHR', 'DXB', '2026-01-17 09:00:00', '2026-01-17 19:00:00', 509.00, 280, 110, 'SCHEDULED');

-- LHR -> BJS (Beijing)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA039', 'LHR', 'BJS', '2026-01-14 11:00:00', '2026-01-15 05:00:00', 799.00, 300, 140, 'SCHEDULED'),
('BA041', 'LHR', 'BJS', '2026-01-16 11:00:00', '2026-01-17 05:00:00', 779.00, 300, 100, 'SCHEDULED'),
('BA043', 'LHR', 'BJS', '2026-01-19 11:00:00', '2026-01-20 05:00:00', 819.00, 300, 160, 'SCHEDULED');

-- =====================================================
-- FLIGHTS FROM BJS (Beijing)
-- =====================================================

-- BJS -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK025', 'BJS', 'IST', '2026-01-15 02:00:00', '2026-01-15 09:00:00', 729.00, 300, 90, 'SCHEDULED'),
('TK027', 'BJS', 'IST', '2026-01-17 02:00:00', '2026-01-17 09:00:00', 709.00, 300, 65, 'SCHEDULED'),
('TK029', 'BJS', 'IST', '2026-01-20 02:00:00', '2026-01-20 09:00:00', 749.00, 300, 110, 'SCHEDULED');

-- BJS -> LHR
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA040', 'BJS', 'LHR', '2026-01-15 08:00:00', '2026-01-15 12:00:00', 789.00, 300, 130, 'SCHEDULED'),
('BA042', 'BJS', 'LHR', '2026-01-17 08:00:00', '2026-01-17 12:00:00', 769.00, 300, 95, 'SCHEDULED'),
('BA044', 'BJS', 'LHR', '2026-01-20 08:00:00', '2026-01-20 12:00:00', 809.00, 300, 150, 'SCHEDULED');

-- BJS -> SIN
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('CA969', 'BJS', 'SIN', '2026-01-14 10:00:00', '2026-01-14 16:00:00', 399.00, 280, 100, 'SCHEDULED'),
('CA971', 'BJS', 'SIN', '2026-01-15 10:00:00', '2026-01-15 16:00:00', 389.00, 280, 75, 'SCHEDULED'),
('CA973', 'BJS', 'SIN', '2026-01-17 10:00:00', '2026-01-17 16:00:00', 409.00, 280, 120, 'SCHEDULED');

-- BJS -> BKK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('CA979', 'BJS', 'BKK', '2026-01-14 12:00:00', '2026-01-14 17:00:00', 349.00, 280, 85, 'SCHEDULED'),
('CA981', 'BJS', 'BKK', '2026-01-15 12:00:00', '2026-01-15 17:00:00', 339.00, 280, 60, 'SCHEDULED'),
('CA983', 'BJS', 'BKK', '2026-01-18 12:00:00', '2026-01-18 17:00:00', 359.00, 280, 100, 'SCHEDULED');

-- =====================================================
-- FLIGHTS FROM DXB (Dubai)
-- =====================================================

-- DXB -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK761', 'DXB', 'IST', '2026-01-14 08:00:00', '2026-01-14 12:00:00', 429.00, 300, 80, 'SCHEDULED'),
('TK763', 'DXB', 'IST', '2026-01-14 20:00:00', '2026-01-15 00:00:00', 409.00, 300, 60, 'SCHEDULED'),
('TK765', 'DXB', 'IST', '2026-01-15 08:00:00', '2026-01-15 12:00:00', 419.00, 300, 45, 'SCHEDULED'),
('TK767', 'DXB', 'IST', '2026-01-17 08:00:00', '2026-01-17 12:00:00', 439.00, 300, 95, 'SCHEDULED');

-- DXB -> LHR
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('EK001', 'DXB', 'LHR', '2026-01-14 07:30:00', '2026-01-14 11:30:00', 549.00, 350, 150, 'SCHEDULED'),
('EK003', 'DXB', 'LHR', '2026-01-14 15:30:00', '2026-01-14 19:30:00', 529.00, 350, 120, 'SCHEDULED'),
('EK005', 'DXB', 'LHR', '2026-01-15 07:30:00', '2026-01-15 11:30:00', 539.00, 350, 100, 'SCHEDULED'),
('EK007', 'DXB', 'LHR', '2026-01-16 07:30:00', '2026-01-16 11:30:00', 559.00, 350, 170, 'SCHEDULED'),
('EK009', 'DXB', 'LHR', '2026-01-18 07:30:00', '2026-01-18 11:30:00', 519.00, 350, 90, 'SCHEDULED');

-- DXB -> SIN
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('EK404', 'DXB', 'SIN', '2026-01-14 03:00:00', '2026-01-14 14:30:00', 449.00, 350, 130, 'SCHEDULED'),
('EK406', 'DXB', 'SIN', '2026-01-15 03:00:00', '2026-01-15 14:30:00', 439.00, 350, 95, 'SCHEDULED'),
('EK408', 'DXB', 'SIN', '2026-01-17 03:00:00', '2026-01-17 14:30:00', 459.00, 350, 145, 'SCHEDULED');

-- DXB -> BKK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('EK376', 'DXB', 'BKK', '2026-01-14 02:30:00', '2026-01-14 12:00:00', 379.00, 350, 110, 'SCHEDULED'),
('EK378', 'DXB', 'BKK', '2026-01-15 02:30:00', '2026-01-15 12:00:00', 369.00, 350, 85, 'SCHEDULED'),
('EK380', 'DXB', 'BKK', '2026-01-18 02:30:00', '2026-01-18 12:00:00', 389.00, 350, 130, 'SCHEDULED');

-- =====================================================
-- FLIGHTS FROM JFK (New York)
-- =====================================================

-- JFK -> LHR
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA116', 'JFK', 'LHR', '2026-01-14 19:00:00', '2026-01-15 07:00:00', 629.00, 280, 130, 'SCHEDULED'),
('BA118', 'JFK', 'LHR', '2026-01-15 19:00:00', '2026-01-16 07:00:00', 609.00, 280, 100, 'SCHEDULED'),
('BA120', 'JFK', 'LHR', '2026-01-17 19:00:00', '2026-01-18 07:00:00', 649.00, 280, 150, 'SCHEDULED');

-- JFK -> CDG (Paris)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('AF001', 'JFK', 'CDG', '2026-01-14 18:00:00', '2026-01-15 07:00:00', 579.00, 280, 95, 'SCHEDULED'),
('AF003', 'JFK', 'CDG', '2026-01-15 18:00:00', '2026-01-16 07:00:00', 559.00, 280, 70, 'SCHEDULED'),
('AF005', 'JFK', 'CDG', '2026-01-17 18:00:00', '2026-01-18 07:00:00', 599.00, 280, 120, 'SCHEDULED');

-- JFK -> LAX
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('AA001', 'JFK', 'LAX', '2026-01-14 08:00:00', '2026-01-14 11:30:00', 299.00, 180, 80, 'SCHEDULED'),
('AA003', 'JFK', 'LAX', '2026-01-14 14:00:00', '2026-01-14 17:30:00', 319.00, 180, 60, 'SCHEDULED'),
('AA005', 'JFK', 'LAX', '2026-01-15 08:00:00', '2026-01-15 11:30:00', 289.00, 180, 45, 'SCHEDULED'),
('AA007', 'JFK', 'LAX', '2026-01-16 08:00:00', '2026-01-16 11:30:00', 309.00, 180, 90, 'SCHEDULED'),
('AA009', 'JFK', 'LAX', '2026-01-18 08:00:00', '2026-01-18 11:30:00', 279.00, 180, 30, 'SCHEDULED');

-- =====================================================
-- FLIGHTS FROM SIN (Singapore)
-- =====================================================

-- SIN -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK055', 'SIN', 'IST', '2026-01-15 23:00:00', '2026-01-16 06:00:00', 659.00, 300, 115, 'SCHEDULED'),
('TK057', 'SIN', 'IST', '2026-01-17 23:00:00', '2026-01-18 06:00:00', 639.00, 300, 90, 'SCHEDULED'),
('TK059', 'SIN', 'IST', '2026-01-20 23:00:00', '2026-01-21 06:00:00', 679.00, 300, 140, 'SCHEDULED');

-- SIN -> LHR
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('SQ321', 'SIN', 'LHR', '2026-01-14 23:00:00', '2026-01-15 06:00:00', 749.00, 350, 160, 'SCHEDULED'),
('SQ323', 'SIN', 'LHR', '2026-01-15 23:00:00', '2026-01-16 06:00:00', 729.00, 350, 125, 'SCHEDULED'),
('SQ325', 'SIN', 'LHR', '2026-01-17 23:00:00', '2026-01-18 06:00:00', 769.00, 350, 180, 'SCHEDULED');

-- SIN -> SYD (Sydney)
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('SQ211', 'SIN', 'SYD', '2026-01-14 08:00:00', '2026-01-14 18:00:00', 549.00, 350, 140, 'SCHEDULED'),
('SQ213', 'SIN', 'SYD', '2026-01-15 08:00:00', '2026-01-15 18:00:00', 529.00, 350, 105, 'SCHEDULED'),
('SQ215', 'SIN', 'SYD', '2026-01-17 08:00:00', '2026-01-17 18:00:00', 569.00, 350, 160, 'SCHEDULED');

-- SIN -> BKK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('SQ970', 'SIN', 'BKK', '2026-01-14 07:00:00', '2026-01-14 08:30:00', 149.00, 180, 70, 'SCHEDULED'),
('SQ972', 'SIN', 'BKK', '2026-01-14 14:00:00', '2026-01-14 15:30:00', 159.00, 180, 55, 'SCHEDULED'),
('SQ974', 'SIN', 'BKK', '2026-01-15 07:00:00', '2026-01-15 08:30:00', 139.00, 180, 40, 'SCHEDULED'),
('SQ976', 'SIN', 'BKK', '2026-01-17 07:00:00', '2026-01-17 08:30:00', 169.00, 180, 85, 'SCHEDULED');

-- =====================================================
-- ADDITIONAL RETURN FLIGHTS
-- =====================================================

-- BCN -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1852', 'BCN', 'IST', '2026-01-14 12:30:00', '2026-01-14 18:00:00', 289.00, 180, 35, 'SCHEDULED'),
('TK1854', 'BCN', 'IST', '2026-01-15 12:30:00', '2026-01-15 18:00:00', 279.00, 180, 20, 'SCHEDULED'),
('TK1856', 'BCN', 'IST', '2026-01-17 12:30:00', '2026-01-17 18:00:00', 299.00, 180, 50, 'SCHEDULED');

-- BER -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1722', 'BER', 'IST', '2026-01-14 10:00:00', '2026-01-14 14:30:00', 189.00, 180, 45, 'SCHEDULED'),
('TK1724', 'BER', 'IST', '2026-01-14 18:00:00', '2026-01-14 22:30:00', 209.00, 180, 30, 'SCHEDULED'),
('TK1726', 'BER', 'IST', '2026-01-15 10:00:00', '2026-01-15 14:30:00', 199.00, 180, 15, 'SCHEDULED');

-- LAX -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK6011', 'LAX', 'IST', '2026-01-14 20:00:00', '2026-01-15 18:00:00', 989.00, 300, 145, 'SCHEDULED'),
('TK6013', 'LAX', 'IST', '2026-01-16 20:00:00', '2026-01-17 18:00:00', 969.00, 300, 110, 'SCHEDULED'),
('TK6015', 'LAX', 'IST', '2026-01-18 20:00:00', '2026-01-19 18:00:00', 1009.00, 300, 170, 'SCHEDULED');

-- LAX -> JFK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('AA002', 'LAX', 'JFK', '2026-01-14 13:00:00', '2026-01-14 21:30:00', 309.00, 180, 75, 'SCHEDULED'),
('AA004', 'LAX', 'JFK', '2026-01-15 13:00:00', '2026-01-15 21:30:00', 299.00, 180, 55, 'SCHEDULED'),
('AA006', 'LAX', 'JFK', '2026-01-17 13:00:00', '2026-01-17 21:30:00', 319.00, 180, 95, 'SCHEDULED');

-- LAX -> LHR
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('BA270', 'LAX', 'LHR', '2026-01-14 16:00:00', '2026-01-15 10:00:00', 759.00, 280, 135, 'SCHEDULED'),
('BA272', 'LAX', 'LHR', '2026-01-16 16:00:00', '2026-01-17 10:00:00', 739.00, 280, 100, 'SCHEDULED'),
('BA274', 'LAX', 'LHR', '2026-01-18 16:00:00', '2026-01-19 10:00:00', 779.00, 280, 155, 'SCHEDULED');

-- BKK -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK069', 'BKK', 'IST', '2026-01-15 15:00:00', '2026-01-15 21:00:00', 589.00, 300, 95, 'SCHEDULED'),
('TK071', 'BKK', 'IST', '2026-01-17 15:00:00', '2026-01-17 21:00:00', 569.00, 300, 70, 'SCHEDULED'),
('TK073', 'BKK', 'IST', '2026-01-19 15:00:00', '2026-01-19 21:00:00', 609.00, 300, 115, 'SCHEDULED');

-- BKK -> SIN
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('SQ971', 'BKK', 'SIN', '2026-01-14 09:30:00', '2026-01-14 13:00:00', 155.00, 180, 65, 'SCHEDULED'),
('SQ973', 'BKK', 'SIN', '2026-01-14 16:30:00', '2026-01-14 20:00:00', 165.00, 180, 50, 'SCHEDULED'),
('SQ975', 'BKK', 'SIN', '2026-01-15 09:30:00', '2026-01-15 13:00:00', 145.00, 180, 35, 'SCHEDULED');

-- SYD -> SIN
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('SQ212', 'SYD', 'SIN', '2026-01-14 20:00:00', '2026-01-15 02:00:00', 559.00, 350, 145, 'SCHEDULED'),
('SQ214', 'SYD', 'SIN', '2026-01-16 20:00:00', '2026-01-17 02:00:00', 539.00, 350, 110, 'SCHEDULED'),
('SQ216', 'SYD', 'SIN', '2026-01-18 20:00:00', '2026-01-19 02:00:00', 579.00, 350, 165, 'SCHEDULED');

-- DOH -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK781', 'DOH', 'IST', '2026-01-14 08:00:00', '2026-01-14 12:00:00', 389.00, 250, 55, 'SCHEDULED'),
('TK783', 'DOH', 'IST', '2026-01-14 20:00:00', '2026-01-15 00:00:00', 409.00, 250, 35, 'SCHEDULED'),
('TK785', 'DOH', 'IST', '2026-01-15 08:00:00', '2026-01-15 12:00:00', 379.00, 250, 20, 'SCHEDULED');

-- MAD -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1302', 'MAD', 'IST', '2026-01-14 12:00:00', '2026-01-14 18:00:00', 339.00, 180, 40, 'SCHEDULED'),
('TK1304', 'MAD', 'IST', '2026-01-15 12:00:00', '2026-01-15 18:00:00', 329.00, 180, 25, 'SCHEDULED'),
('TK1306', 'MAD', 'IST', '2026-01-18 12:00:00', '2026-01-18 18:00:00', 349.00, 180, 50, 'SCHEDULED');

-- FCO -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1862', 'FCO', 'IST', '2026-01-14 12:30:00', '2026-01-14 17:00:00', 239.00, 180, 60, 'SCHEDULED'),
('TK1864', 'FCO', 'IST', '2026-01-14 20:30:00', '2026-01-15 01:00:00', 259.00, 180, 35, 'SCHEDULED'),
('TK1866', 'FCO', 'IST', '2026-01-15 12:30:00', '2026-01-15 17:00:00', 249.00, 180, 45, 'SCHEDULED');

-- CDG -> IST
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('TK1822', 'CDG', 'IST', '2026-01-14 11:30:00', '2026-01-14 17:30:00', 319.00, 180, 50, 'SCHEDULED'),
('TK1824', 'CDG', 'IST', '2026-01-15 11:30:00', '2026-01-15 17:30:00', 309.00, 180, 35, 'SCHEDULED'),
('TK1826', 'CDG', 'IST', '2026-01-17 11:30:00', '2026-01-17 17:30:00', 329.00, 180, 65, 'SCHEDULED');

-- CDG -> JFK
INSERT INTO flights (code, from_airport, to_airport, departure_time, arrival_time, price, capacity, booked_seats, status) VALUES
('AF002', 'CDG', 'JFK', '2026-01-14 10:00:00', '2026-01-14 12:00:00', 589.00, 280, 100, 'SCHEDULED'),
('AF004', 'CDG', 'JFK', '2026-01-15 10:00:00', '2026-01-15 12:00:00', 569.00, 280, 75, 'SCHEDULED'),
('AF006', 'CDG', 'JFK', '2026-01-17 10:00:00', '2026-01-17 12:00:00', 609.00, 280, 125, 'SCHEDULED');
