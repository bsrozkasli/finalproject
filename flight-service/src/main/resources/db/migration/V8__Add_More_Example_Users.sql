-- =====================================================
-- Add More Example Users with Miles Accounts
-- V8__Add_More_Example_Users.sql
-- =====================================================

-- Add more example users with various miles balances for testing
-- Note: These are example user IDs. In production, these should match actual Auth0 user IDs.
-- To authorize these users, you need to create them in Auth0 with matching user IDs (sub claim)

INSERT INTO miles_accounts (azure_user_id, member_number, balance, tier)
VALUES 
    -- Example user with medium balance
    ('auth0|example-user-001', 'ML800001', 15000, 'SILVER'),
    ('auth0|example-user-002', 'ML800002', 25000, 'GOLD'),
    ('auth0|example-user-003', 'ML800003', 5000, 'BRONZE'),
    ('auth0|example-user-004', 'ML800004', 75000, 'PLATINUM'),
    ('auth0|example-user-005', 'ML800005', 12000, 'BRONZE')
ON CONFLICT (azure_user_id) DO NOTHING;
