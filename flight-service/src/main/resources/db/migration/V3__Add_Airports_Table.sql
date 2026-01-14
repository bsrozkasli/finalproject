-- =====================================================
-- Airline Ticketing System - Database Schema
-- V2__Add_Airports_Table.sql
-- =====================================================

CREATE TABLE airports (
    code VARCHAR(3) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL
);

-- Insert some initial data
INSERT INTO airports (code, name, city, country) VALUES
('IST', 'Istanbul Airport', 'Istanbul', 'Turkey'),
('SAW', 'Sabiha Gokcen Airport', 'Istanbul', 'Turkey'),
('JFK', 'John F. Kennedy International Airport', 'New York', 'USA'),
('LHR', 'Heathrow Airport', 'London', 'UK'),
('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
('DXB', 'Dubai International Airport', 'Dubai', 'UAE'),
('FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany'),
('AMS', 'Schiphol Airport', 'Amsterdam', 'Netherlands'),
('MUC', 'Munich Airport', 'Munich', 'Germany'),
('BJS', 'Beijing Capital International Airport', 'Beijing', 'China');
