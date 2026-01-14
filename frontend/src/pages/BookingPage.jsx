import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { format } from 'date-fns';

function BookingPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { isAuthenticated, user, getAccessTokenSilently, loginWithRedirect } = useAuth0();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [bookingRef, setBookingRef] = useState(null);
    const [milesBalance, setMilesBalance] = useState(null);
    const [milesLoading, setMilesLoading] = useState(false);

    // Flight bilgisi ve yolcu sayısı location state'den gelir
    const flight = location.state?.flight;
    const passengerCount = location.state?.passengerCount || 1;

    const [passengers, setPassengers] = useState(
        Array(passengerCount).fill().map(() => ({
            title: 'MR',
            firstName: '',
            lastName: '',
            dateOfBirth: null,
            passportNo: '',
            nationality: ''
        }))
    );

    const [bookingData, setBookingData] = useState({
        email: '',
        paymentMethod: 'CREDIT_CARD'
    });

    // Auth0'dan user bilgilerini al
    useEffect(() => {
        if (isAuthenticated && user && passengers.length > 0) {
            setBookingData(prev => ({
                ...prev,
                email: user.email || ''
            }));

            // First passenger is usually the account holder
            const updatedPassengers = [...passengers];
            updatedPassengers[0] = {
                ...updatedPassengers[0],
                firstName: user.given_name || '',
                lastName: user.family_name || ''
            };
            setPassengers(updatedPassengers);
        }
    }, [isAuthenticated, user]);

    // Flight yoksa search sayfasına yönlendir
    useEffect(() => {
        if (!flight) {
            navigate('/');
        }
    }, [flight, navigate]);

    // Fetch miles balance for authenticated users
    useEffect(() => {
        const fetchMilesBalance = async () => {
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
                    const response = await fetch('/api/v1/miles/account', {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                    if (response.ok) {
                        const data = await response.json();
                        setMilesBalance(data.balance || 0);
                    } else {
                        console.warn('Could not fetch miles balance');
                        setMilesBalance(null);
                    }
                } else {
                    setMilesBalance(null);
                }
            } catch (err) {
                console.warn('Miles balance fetch error:', err);
                setMilesBalance(null);
            } finally {
                setMilesLoading(false);
            }
        };

        fetchMilesBalance();
    }, [isAuthenticated, getAccessTokenSilently]);

    const handlePassengerChange = (index, field, value) => {
        const updatedPassengers = [...passengers];
        updatedPassengers[index] = {
            ...updatedPassengers[index],
            [field]: value
        };
        setPassengers(updatedPassengers);
    };

    const totalPrice = (flight?.predictedPrice || flight?.price || 0) * passengerCount;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);



        try {
            // Try to get token if authenticated, but don't fail if consent error
            let token = null;
            if (isAuthenticated) {
                try {
                    token = await getAccessTokenSilently({
                        authorizationParams: {
                            audience: 'https://api.airline.com'
                        }
                    });
                } catch (tokenError) {
                    console.warn('Could not get access token:', tokenError.message);
                    // Continue without token - backend allows unauthenticated requests in dev mode
                }
            }

            const bookingRequest = {
                flightId: flight.id,
                email: bookingData.email,
                paymentMethod: bookingData.paymentMethod,
                passengers: passengers.map(p => ({
                    firstName: p.firstName,
                    lastName: p.lastName,
                    passportNo: p.passportNo,
                    dateOfBirth: p.dateOfBirth ? format(p.dateOfBirth, 'yyyy-MM-dd') : null,
                    nationality: p.nationality || null
                }))
            };

            const headers = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch('/api/v1/bookings', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(bookingRequest)
            });

            if (!response.ok) {
                let errorMessage = 'Booking failed';
                try {
                    const text = await response.text();
                    if (text) {
                        try {
                            const errorData = JSON.parse(text);
                            errorMessage = errorData.message || errorData.error || errorMessage;
                        } catch (e) {
                            // Text var ama JSON değil
                            console.warn('Error response is not JSON:', text);
                        }
                    }
                } catch (parseError) {
                    // Response is empty or not JSON
                    if (response.status === 401 || response.status === 403) {
                        errorMessage = 'Authentication required. Please sign in to complete booking.';
                    }
                }
                throw new Error(errorMessage);
            }

            // Safely parse response - handle empty response
            const responseText = await response.text();
            let bookingResponse = {};
            if (responseText) {
                try {
                    bookingResponse = JSON.parse(responseText);
                } catch (parseError) {
                    console.warn('Response is not valid JSON:', responseText);
                }
            }
            setBookingRef(bookingResponse.ref || bookingResponse.bookingRef || 'CONFIRMED');
            setSuccess(true);



        } catch (err) {
            setError(err.message || 'Failed to create booking');
        } finally {
            setLoading(false);
        }
    };

    if (!flight) {
        return null;
    }

    if (success) {
        return (
            <main>
                <div className="success-container" style={{
                    maxWidth: '600px',
                    margin: '50px auto',
                    padding: '40px',
                    background: 'var(--bg-card)',
                    borderRadius: 'var(--radius)',
                    textAlign: 'center'
                }}>
                    <div style={{ fontSize: '64px', marginBottom: '20px' }}>✅</div>
                    <h2>Booking Confirmed!</h2>
                    <p style={{ margin: '20px 0', fontSize: '1.1rem' }}>
                        Your booking reference: <strong style={{ color: 'var(--primary-light)' }}>{bookingRef}</strong>
                    </p>
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '30px' }}>
                        A confirmation email has been sent to {bookingData.email}
                    </p>
                    <button
                        className="btn btn-primary"
                        onClick={() => navigate('/')}
                    >
                        Search More Flights
                    </button>
                </div>
            </main>
        );
    }

    return (
        <main>
            <div className="booking-container" style={{
                maxWidth: '800px',
                margin: '30px auto',
                padding: '40px'
            }}>
                <div style={{ marginBottom: '30px' }}>
                    <h1>Passenger Information</h1>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '10px' }}>
                        Please fill in the passenger details. Name and surname cannot be changed after booking.
                    </p>
                </div>

                {/* Flight Summary */}
                <div style={{
                    background: 'var(--bg-card)',
                    padding: '20px',
                    borderRadius: 'var(--radius-sm)',
                    marginBottom: '30px',
                    border: '1px solid var(--border)'
                }}>
                    <h3 style={{ marginBottom: '15px' }}>Flight Details</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                        <div>
                            <span style={{ color: 'var(--text-secondary)' }}>From:</span>
                            <strong style={{ marginLeft: '10px' }}>{flight.fromAirport}</strong>
                        </div>
                        <div>
                            <span style={{ color: 'var(--text-secondary)' }}>To:</span>
                            <strong style={{ marginLeft: '10px' }}>{flight.toAirport}</strong>
                        </div>
                        <div>
                            <span style={{ color: 'var(--text-secondary)' }}>Date:</span>
                            <strong style={{ marginLeft: '10px' }}>
                                {new Date(flight.departureTime).toLocaleDateString()}
                            </strong>
                        </div>
                        <div>
                            <span style={{ color: 'var(--text-secondary)' }}>Passengers:</span>
                            <strong style={{ marginLeft: '10px' }}>{passengerCount}</strong>
                        </div>
                        <div>
                            <span style={{ color: 'var(--text-secondary)' }}>Total Price:</span>
                            <strong style={{ marginLeft: '10px', color: 'var(--success)' }}>
                                ${totalPrice.toFixed(2)}
                            </strong>
                        </div>
                    </div>
                </div>

                {/* Sign in to Miles&Smiles */}
                {!isAuthenticated && (
                    <div style={{
                        background: 'rgba(99, 102, 241, 0.1)',
                        padding: '20px',
                        borderRadius: 'var(--radius-sm)',
                        marginBottom: '30px',
                        border: '1px solid rgba(99, 102, 241, 0.3)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <h4 style={{ marginBottom: '5px' }}>Miles&Smiles Member?</h4>
                                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', margin: 0 }}>
                                    Sign in to save passenger information and earn miles
                                </p>
                            </div>
                            <button
                                className="btn"
                                onClick={() => loginWithRedirect({
                                    appState: {
                                        returnTo: location.pathname,
                                        flight: flight
                                    }
                                })}
                                style={{
                                    border: '1px solid var(--primary)',
                                    color: 'var(--primary)',
                                    background: 'transparent'
                                }}
                            >
                                Sign in to Miles&Smiles
                            </button>
                        </div>
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    {passengers.map((p, index) => (
                        <div key={index} style={{
                            marginBottom: '40px',
                            padding: '25px',
                            background: 'rgba(255, 255, 255, 0.03)',
                            borderRadius: 'var(--radius)',
                            border: '1px solid var(--border)'
                        }}>
                            <h3 style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <span style={{
                                    background: 'var(--primary)',
                                    color: 'white',
                                    width: '28px',
                                    height: '28px',
                                    borderRadius: '50%',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '0.9rem'
                                }}>{index + 1}</span>
                                Passenger Details
                            </h3>

                            {/* Title Selection */}
                            <div className="input-group" style={{ marginBottom: '20px' }}>
                                <label>Title</label>
                                <div style={{ display: 'flex', gap: '20px', marginTop: '10px' }}>
                                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                        <input
                                            type="radio"
                                            name={`title-${index}`}
                                            value="MR"
                                            checked={p.title === 'MR'}
                                            onChange={(e) => handlePassengerChange(index, 'title', e.target.value)}
                                            style={{ width: '18px', height: '18px' }}
                                        />
                                        <span>Mr.</span>
                                    </label>
                                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                        <input
                                            type="radio"
                                            name={`title-${index}`}
                                            value="MS"
                                            checked={p.title === 'MS'}
                                            onChange={(e) => handlePassengerChange(index, 'title', e.target.value)}
                                            style={{ width: '18px', height: '18px' }}
                                        />
                                        <span>Ms.</span>
                                    </label>
                                </div>
                            </div>

                            {/* Name Fields */}
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '20px', marginBottom: '20px' }}>
                                <div className="input-group">
                                    <label htmlFor={`firstName-${index}`}>First / Middle name</label>
                                    <input
                                        id={`firstName-${index}`}
                                        type="text"
                                        value={p.firstName}
                                        onChange={(e) => handlePassengerChange(index, 'firstName', e.target.value)}
                                        placeholder="Enter first name"
                                        required
                                    />
                                </div>

                                <div className="input-group">
                                    <label htmlFor={`lastName-${index}`}>Surname</label>
                                    <input
                                        id={`lastName-${index}`}
                                        type="text"
                                        value={p.lastName}
                                        onChange={(e) => handlePassengerChange(index, 'lastName', e.target.value)}
                                        placeholder="Enter surname"
                                        required
                                    />
                                </div>
                            </div>

                            {/* Date of Birth and Passport */}
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '20px' }}>
                                <div className="input-group">
                                    <label htmlFor={`dateOfBirth-${index}`}>Date of Birth</label>
                                    <DatePicker
                                        id={`dateOfBirth-${index}`}
                                        selected={p.dateOfBirth}
                                        onChange={(date) => handlePassengerChange(index, 'dateOfBirth', date)}
                                        dateFormat="dd/MM/yyyy"
                                        maxDate={new Date()}
                                        placeholderText="DD/MM/YYYY"
                                        className="form-control"
                                        showYearDropdown
                                        showMonthDropdown
                                        dropdownMode="select"
                                        required
                                    />
                                </div>

                                <div className="input-group">
                                    <label htmlFor={`passportNo-${index}`}>Passport Number</label>
                                    <input
                                        id={`passportNo-${index}`}
                                        type="text"
                                        value={p.passportNo}
                                        onChange={(e) => handlePassengerChange(index, 'passportNo', e.target.value)}
                                        placeholder="Passport #"
                                        required
                                    />
                                </div>

                                <div className="input-group">
                                    <label htmlFor={`nationality-${index}`}>Nationality</label>
                                    <input
                                        id={`nationality-${index}`}
                                        type="text"
                                        value={p.nationality}
                                        onChange={(e) => handlePassengerChange(index, 'nationality', e.target.value)}
                                        placeholder="e.g., TR"
                                        required
                                    />
                                </div>
                            </div>
                        </div>
                    ))}

                    <h2 style={{ marginBottom: '20px' }}>Contact & Payment</h2>

                    {/* Email */}
                    <div className="input-group" style={{ marginBottom: '20px' }}>
                        <label htmlFor="email">Email Address for Confirmation</label>
                        <input
                            id="email"
                            type="email"
                            value={bookingData.email}
                            onChange={(e) => setBookingData({ ...bookingData, email: e.target.value })}
                            placeholder="your.email@example.com"
                            required
                        />
                    </div>

                    {/* Payment Method */}
                    <div className="input-group" style={{ marginBottom: '20px' }}>
                        <label htmlFor="paymentMethod">Payment Method</label>
                        <select
                            id="paymentMethod"
                            value={bookingData.paymentMethod}
                            onChange={(e) => setBookingData({ ...bookingData, paymentMethod: e.target.value })}
                            style={{ padding: '10px', marginTop: '5px' }}
                        >
                            <option value="CREDIT_CARD">Credit Card</option>
                            {isAuthenticated && (
                                <option value="MILES">Miles&Smiles Points</option>
                            )}
                        </select>
                    </div>

                    {/* Miles Payment Info - Show when Miles is selected */}
                    {isAuthenticated && bookingData.paymentMethod === 'MILES' && (
                        <div style={{
                            background: milesBalance >= totalPrice
                                ? 'rgba(34, 197, 94, 0.1)'
                                : 'rgba(239, 68, 68, 0.1)',
                            padding: '15px',
                            borderRadius: 'var(--radius-sm)',
                            marginBottom: '20px',
                            border: milesBalance >= totalPrice
                                ? '1px solid rgba(34, 197, 94, 0.3)'
                                : '1px solid rgba(239, 68, 68, 0.3)'
                        }} focusable>
                            <p style={{ margin: 0, fontSize: '0.95rem' }}>
                                💰 <strong>Your Miles Balance:</strong> {milesLoading ? 'Loading...' : `${milesBalance?.toLocaleString() || 0} miles`}
                            </p>
                            <p style={{ margin: '8px 0 0 0', fontSize: '0.9rem' }}>
                                🎫 <strong>Total Miles Required:</strong> {Math.round(totalPrice)} miles
                            </p>
                            {milesBalance !== null && milesBalance < totalPrice && (
                                <p style={{ margin: '10px 0 0 0', color: 'var(--danger)', fontSize: '0.85rem', fontWeight: 'bold' }}>
                                    ⚠️ Insufficient miles! You need {Math.round(totalPrice - milesBalance)} more miles.
                                </p>
                            )}
                            {milesBalance !== null && milesBalance >= totalPrice && (
                                <p style={{ margin: '10px 0 0 0', color: 'var(--success)', fontSize: '0.85rem' }}>
                                    ✅ You have enough miles for this booking!
                                </p>
                            )}
                        </div>
                    )}

                    {error && (
                        <div style={{
                            background: 'rgba(239, 68, 68, 0.1)',
                            padding: '15px',
                            borderRadius: 'var(--radius-sm)',
                            marginBottom: '20px',
                            border: '1px solid rgba(239, 68, 68, 0.3)',
                            color: 'var(--danger)'
                        }}>
                            {error}
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: '15px', justifyContent: 'flex-end' }}>
                        <button
                            type="button"
                            className="btn"
                            onClick={() => navigate(-1)}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading}
                        >
                            {loading ? 'Processing...' : 'Complete Booking'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default BookingPage;

