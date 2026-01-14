-- =====================================================
-- Airline Ticketing System - Database Schema
-- V4__Add_PaymentMethod_To_Bookings.sql
-- =====================================================

ALTER TABLE bookings ADD COLUMN payment_method VARCHAR(20) DEFAULT 'CREDIT_CARD';
