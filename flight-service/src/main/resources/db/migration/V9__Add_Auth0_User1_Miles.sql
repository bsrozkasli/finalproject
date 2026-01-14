-- =====================================================
-- Add Miles Account for Auth0 user1@gmail.com
-- V9__Add_Auth0_User1_Miles.sql
-- =====================================================

-- Add miles account for user1@gmail.com (Auth0 user)
-- Note: Auth0 user IDs are typically in format: auth0|xxxxxxxxxx
-- If you know the exact Auth0 user ID (sub claim from JWT), replace 'auth0|user1-gmail-com' with it
-- You can find the user ID in Auth0 Dashboard -> User Management -> Users -> Select user -> User ID field

INSERT INTO miles_accounts (azure_user_id, member_number, balance, tier)
VALUES 
    -- Auth0 user1@gmail.com (replace with actual Auth0 user ID if known)
    -- Common format: auth0|xxxxxxxxxx (where x is the user ID from Auth0)
    -- If user ID is unknown, this will be created automatically on first login via getOrCreateAccount
    ('auth0|user1-gmail-com', 'ML200001', 20000, 'GOLD')
ON CONFLICT (azure_user_id) DO NOTHING;

-- Alternative: If you want to match by a pattern or need to update existing account
-- You can also query Auth0 Management API to get the user ID, or check the JWT token's 'sub' claim
-- when user1@gmail.com logs in.
