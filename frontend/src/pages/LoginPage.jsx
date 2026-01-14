import { useAuth0 } from "@auth0/auth0-react";
import React from 'react';

function LoginPage() {
    const { loginWithRedirect } = useAuth0();

    const handleLogin = async () => {
        await loginWithRedirect({
            appState: {
                returnTo: "/"
            }
        });
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'center' }}>
                    <div style={{
                        background: 'rgba(99, 102, 241, 0.2)',
                        padding: '16px',
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 0 20px rgba(99, 102, 241, 0.3)'
                    }}>
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="currentColor"
                            style={{ width: '48px', height: '48px', color: '#818cf8' }}
                        >
                            <path d="M3.478 2.404a.75.75 0 0 0-.926.941l2.432 7.905H13.5a.75.75 0 0 1 0 1.5H4.984l-2.432 7.905a.75.75 0 0 0 .926.94 60.519 60.519 0 0 0 18.445-8.986.75.75 0 0 0 0-1.218A60.517 60.517 0 0 0 3.478 2.404Z" />
                        </svg>
                    </div>
                </div>

                <h2>Welcome to SkyWings</h2>
                <p>Your journey begins here. Sign in to access your bookings, check miles, and unlock exclusive travel deals.</p>

                <button
                    className="btn btn-primary"
                    onClick={handleLogin}
                    style={{ fontSize: '1.1rem', padding: '16px', fontWeight: '600', letterSpacing: '0.5px' }}
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                        style={{ width: '24px', height: '24px' }}
                    >
                        <path fillRule="evenodd" d="M18.685 19.097A9.723 9.723 0 0 0 21.75 12c0-5.385-4.365-9.75-9.75-9.75S2.25 6.615 2.25 12a9.723 9.723 0 0 0 3.065 7.097A9.716 9.716 0 0 0 12 21.75a9.716 9.716 0 0 0 6.685-2.653Zm-12.54-1.285A7.486 7.486 0 0 1 12 15a7.486 7.486 0 0 1 5.855 2.812A8.224 8.224 0 0 1 12 20.25a8.224 8.224 0 0 1-5.855-2.438ZM15.75 9a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0Z" clipRule="evenodd" />
                    </svg>
                    Sign In with Auth0
                </button>

                <div className="login-divider">
                    <span>Secure Authentication</span>
                </div>

                <div style={{ marginTop: '30px', padding: '24px', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '16px', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '8px' }}>
                        <span style={{ fontSize: '20px' }}>🎁</span>
                        <h4 style={{ margin: 0, color: 'var(--success)' }}>New Member Bonus</h4>
                    </div>
                    <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: 0, lineHeight: '1.5' }}>
                        Join today and earn <strong style={{ color: 'var(--success)' }}>5,000 miles</strong> instantly on your first international flight booking!
                    </p>
                </div>
            </div>
        </div>
    );
}

export default LoginPage;
