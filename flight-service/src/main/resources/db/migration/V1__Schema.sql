-- =====================================================
-- Airline Ticketing System - Database Schema
-- V1__Schema.sql
-- =====================================================

-- Enum type for flight status
CREATE TYPE flight_status AS ENUM ('SCHEDULED', 'BOARDING', 'DEPARTED', 'COMPLETED', 'CANCELLED', 'DELAYED');

-- Enum type for booking status
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

-- Enum type for miles transaction type
CREATE TYPE miles_transaction_type AS ENUM ('EARN', 'BURN', 'EXPIRE', 'TRANSFER', 'BONUS');

-- =====================================================
-- FLIGHTS TABLE
-- =====================================================
CREATE TABLE flights (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL,
    from_airport VARCHAR(3) NOT NULL,
    to_airport VARCHAR(3) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    capacity INTEGER NOT NULL,
    booked_seats INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_flights_code UNIQUE (code),
    CONSTRAINT chk_flights_capacity CHECK (capacity > 0),
    CONSTRAINT chk_flights_booked_seats CHECK (booked_seats >= 0 AND booked_seats <= capacity),
    CONSTRAINT chk_flights_time CHECK (arrival_time > departure_time)
);

-- Index for frequent searches
CREATE INDEX idx_flights_from_to ON flights (from_airport, to_airport);
CREATE INDEX idx_flights_departure ON flights (departure_time);
CREATE INDEX idx_flights_status ON flights (status);

-- =====================================================
-- BOOKINGS TABLE
-- =====================================================
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    ref VARCHAR(10) NOT NULL,
    flight_id BIGINT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    user_email VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    price_paid DECIMAL(10, 2) NOT NULL,
    passenger_count INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_bookings_ref UNIQUE (ref),
    CONSTRAINT fk_bookings_flight FOREIGN KEY (flight_id) REFERENCES flights(id),
    CONSTRAINT chk_bookings_price CHECK (price_paid >= 0),
    CONSTRAINT chk_bookings_passenger_count CHECK (passenger_count > 0)
);

-- Index for user lookups
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_bookings_flight ON bookings (flight_id);
CREATE INDEX idx_bookings_status ON bookings (status);

-- =====================================================
-- PASSENGERS TABLE
-- =====================================================
CREATE TABLE passengers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    passport_no VARCHAR(20) NOT NULL,
    date_of_birth DATE,
    nationality VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_passengers_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Index for passenger lookups
CREATE INDEX idx_passengers_booking ON passengers (booking_id);
CREATE INDEX idx_passengers_passport ON passengers (passport_no);

-- =====================================================
-- MILES ACCOUNTS TABLE
-- =====================================================
CREATE TABLE miles_accounts (
    id BIGSERIAL PRIMARY KEY,
    azure_user_id VARCHAR(100) NOT NULL,
    member_number VARCHAR(20) NOT NULL,
    balance INTEGER NOT NULL DEFAULT 0,
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_miles_accounts_user UNIQUE (azure_user_id),
    CONSTRAINT uk_miles_accounts_member UNIQUE (member_number),
    CONSTRAINT chk_miles_balance CHECK (balance >= 0)
);

-- Index for lookups
CREATE INDEX idx_miles_accounts_member ON miles_accounts (member_number);

-- =====================================================
-- MILES TRANSACTIONS TABLE
-- =====================================================
CREATE TABLE miles_transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    amount INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    reference_id VARCHAR(100),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_miles_transactions_account FOREIGN KEY (account_id) REFERENCES miles_accounts(id)
);

-- Index for account lookups
CREATE INDEX idx_miles_transactions_account ON miles_transactions (account_id);
CREATE INDEX idx_miles_transactions_date ON miles_transactions (transaction_date);

-- =====================================================
-- PARTNER AIRLINES TABLE
-- =====================================================
CREATE TABLE partner_airlines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    api_key VARCHAR(64) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    rate_limit INTEGER NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_partner_airlines_code UNIQUE (code),
    CONSTRAINT uk_partner_airlines_api_key UNIQUE (api_key)
);

-- Index for API key lookups
CREATE INDEX idx_partner_airlines_api_key ON partner_airlines (api_key);
CREATE INDEX idx_partner_airlines_active ON partner_airlines (is_active);
