import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { useState, useEffect } from 'react';
import SearchPage from './pages/SearchPage';
import LoginPage from './pages/LoginPage';
import BookingPage from './pages/BookingPage';
import AdminPage from './pages/AdminPage';
import { milesApi } from './api/flightApi';

function Navbar() {
    const { isAuthenticated, user, logout, getAccessTokenSilently } = useAuth0();
    const navigate = useNavigate();
    const [milesBalance, setMilesBalance] = useState(null);
    const [milesLoading, setMilesLoading] = useState(false);

    useEffect(() => {
        const fetchMiles = async () => {
            if (!isAuthenticated) {
                setMilesBalance(null);
                return;
            }

            setMilesLoading(true);
            try {
                const token = await getAccessTokenSilently({
                    authorizationParams: {
                        audience: 'https://api.airline.com'
                    }
                }).catch(() => null);
                
                if (token) {
                    const account = await milesApi.getAccount(token);
                    setMilesBalance(account.balance || 0);
                }
            } catch (err) {
                console.warn('Could not fetch miles balance:', err);
                setMilesBalance(null);
            } finally {
                setMilesLoading(false);
            }
        };

        fetchMiles();
    }, [isAuthenticated, getAccessTokenSilently]);

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
                        <Link to="/admin">Admin</Link>
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

function App() {
    return (
        <Router>
            <div className="container">
                <Navbar />
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
