-- =====================================================
-- Add Dev User Miles Account
-- V7__Add_Dev_User_Miles.sql
-- =====================================================

-- Add miles account for dev-user-001 (used in development mode)
INSERT INTO miles_accounts (azure_user_id, member_number, balance, tier)
VALUES 
    ('dev-user-001', 'ML999001', 10000, 'GOLD'),
    ('dev-user-002', 'ML999002', 5000, 'SILVER'),
    ('dev-user-003', 'ML999003', 25000, 'PLATINUM')
ON CONFLICT (azure_user_id) DO NOTHING;
