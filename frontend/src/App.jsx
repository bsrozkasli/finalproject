import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import SearchPage from './pages/SearchPage';
import LoginPage from './pages/LoginPage';
import BookingPage from './pages/BookingPage';
import AdminPage from './pages/AdminPage';
import { milesApi } from './api/flightApi';
import { isAdmin as checkIsAdmin } from './utils/jwtUtils';

function Navbar() {
    const { isAuthenticated, user, logout, getAccessTokenSilently, getIdTokenClaims } = useAuth0();
    const navigate = useNavigate();
    const [milesBalance, setMilesBalance] = useState(null);
    const [milesLoading, setMilesLoading] = useState(false);
    const [isUserAdmin, setIsUserAdmin] = useState(false);

    // DEBUG: Check deployment version
    useEffect(() => {
        console.log('Frontend Version: v1.1 - Nginx Proxy Fix - ' + new Date().toISOString());
    }, []);

    useEffect(() => {
        const fetchMilesAndCheckAdmin = async () => {
            if (!isAuthenticated) {
                setMilesBalance(null);
                setIsUserAdmin(false);
                return;
            }

            setMilesLoading(true);
            try {
                // Get Auth0 domain from environment (for role namespace lookup)
                const auth0Domain = import.meta.env.VITE_AUTH0_DOMAIN?.replace('https://', '').replace('http://', '') || null;

                // Get access token (scopes are usually in access token)
                const accessToken = await getAccessTokenSilently({
                    authorizationParams: {
                        audience: 'https://api.airline.com'
                    }
                }).catch(() => null);

                // Get ID token (roles are usually in ID token in Auth0)
                const idTokenClaims = await getIdTokenClaims().catch(() => null);

                let adminCheck = false;

                // Admin email list - users with these emails get admin access
                const adminEmails = ['admin@gmail.com'];
                const userEmail = idTokenClaims?.email || '';

                // Check if user email is in admin list
                if (adminEmails.includes(userEmail.toLowerCase())) {
                    adminCheck = true;
                }

                if (!adminCheck && accessToken) {
                    // Check admin from access token (scopes and roles)
                    adminCheck = checkIsAdmin(accessToken, auth0Domain);
                }

                if (accessToken) {
                    // Fetch miles balance
                    try {
                        const account = await milesApi.getAccount(accessToken);
                        setMilesBalance(account.balance || 0);
                    } catch (e) {
                        console.warn('Could not fetch miles:', e);
                    }
                }

                // Also check ID token for roles (Auth0 often puts roles in ID token)
                if (!adminCheck && idTokenClaims) {
                    // Extract roles from ID token claims
                    const roles = idTokenClaims[`https://${auth0Domain}/roles`]
                        || idTokenClaims['https://api.airline.com/roles']
                        || idTokenClaims.roles
                        || [];

                    const roleArray = Array.isArray(roles) ? roles : (typeof roles === 'string' ? [roles] : []);
                    adminCheck = roleArray.some(r => {
                        const roleLower = String(r).toLowerCase();
                        return roleLower === 'admin' || roleLower.includes('admin');
                    });
                }

                setIsUserAdmin(adminCheck);
            } catch (err) {
                console.warn('Could not fetch miles balance or check admin status:', err);
                setMilesBalance(null);
                setIsUserAdmin(false);
            } finally {
                setMilesLoading(false);
            }
        };

        fetchMilesAndCheckAdmin();
    }, [isAuthenticated, getAccessTokenSilently, getIdTokenClaims]);

    const handleLogout = () => {
        logout({ returnTo: window.location.origin });
    };

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M3.478 2.404a.75.75 0 0 0-.926.941l2.432 7.905H13.5a.75.75 0 0 1 0 1.5H4.984l-2.432 7.905a.75.75 0 0 0 .926.94 60.519 60.519 0 0 0 18.445-8.986.75.75 0 0 0 0-1.218A60.517 60.517 0 0 0 3.478 2.404Z" />
                </svg>
                SkyWings
            </Link>
            <div className="navbar-links">
                <Link to="/">Search Flights</Link>
                {isAuthenticated && (
                    <>
                        {isUserAdmin && <Link to="/admin">Admin</Link>}
                        {milesBalance !== null && (
                            <span style={{
                                color: 'var(--primary)',
                                fontWeight: '600',
                                padding: '4px 12px',
                                background: 'rgba(99, 102, 241, 0.1)',
                                borderRadius: '4px'
                            }}>
                                ✈️ {milesLoading ? '...' : `${milesBalance.toLocaleString()} miles`}
                            </span>
                        )}
                        <span style={{ color: 'var(--text-secondary)' }}>{user?.email}</span>
                        <button
                            onClick={handleLogout}
                            className="btn"
                            style={{ marginLeft: '10px' }}
                        >
                            Logout
                        </button>
                    </>
                )}
                {!isAuthenticated && (
                    <Link to="/login" className="btn btn-primary">
                        Sign In
                    </Link>
                )}
            </div>
        </nav>
    );
}

function AuthErrorHandler() {
    const { error, isLoading } = useAuth0();
    const [urlError, setUrlError] = useState(null);

    useEffect(() => {
        // Check for error in URL (Auth0 callback errors)
        const params = new URLSearchParams(window.location.search);
        const errorParam = params.get('error');
        const errorDescription = params.get('error_description');

        if (errorParam) {
            setUrlError({
                error: errorParam,
                description: errorDescription || 'Authentication failed'
            });
            // Clear the error from URL without refreshing
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }, []);

    if (isLoading) {
        return (
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: '200px',
                color: 'var(--text-secondary)'
            }}>
                <div style={{ textAlign: 'center' }}>
                    <div className="loading-spinner" style={{ margin: '0 auto 16px' }}></div>
                    <p>Loading...</p>
                </div>
            </div>
        );
    }

    if (error || urlError) {
        const displayError = error || urlError;
        return (
            <div style={{
                padding: '20px',
                margin: '20px',
                background: 'rgba(239, 68, 68, 0.1)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                borderRadius: '12px',
                color: '#ef4444'
            }}>
                <h3 style={{ margin: '0 0 8px 0' }}>⚠️ Authentication Error</h3>
                <p style={{ margin: 0, color: 'var(--text-secondary)' }}>
                    {displayError.description || displayError.message || 'An error occurred during authentication.'}
                </p>
                <button
                    onClick={() => {
                        setUrlError(null);
                        window.location.href = '/';
                    }}
                    className="btn btn-primary"
                    style={{ marginTop: '16px' }}
                >
                    Try Again
                </button>
            </div>
        );
    }

    return null;
}

function App() {
    return (
        <Router>
            <div className="container">
                <Navbar />
                <AuthErrorHandler />
                <Routes>
                    <Route path="/" element={<SearchPage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/booking" element={<BookingPage />} />
                    <Route path="/admin" element={<AdminPage />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;
