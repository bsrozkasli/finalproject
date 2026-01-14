import { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import AirportAutocomplete from '../components/AirportAutocomplete';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { format } from 'date-fns';

function AdminPage() {
    const { isAuthenticated, getAccessTokenSilently } = useAuth0();
    const [loading, setLoading] = useState(false);
    const [predicting, setPredicting] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [predictedPrice, setPredictedPrice] = useState(null);

    const [flightData, setFlightData] = useState({
        code: '',
        fromAirport: '',
        toAirport: '',
        departureTime: new Date(),
        arrivalTime: new Date(),
        duration: '',
        price: '',
        capacity: ''
    });

    useEffect(() => {
        if (!isAuthenticated) {
            setError('Please login to access admin panel');
        }
    }, [isAuthenticated]);

    const calculateArrivalTime = (departure, duration) => {
        if (!departure || !duration) return null;
        
        const [hours, minutes] = duration.split(/[h\s]+/).filter(Boolean).map(Number);
        const totalMinutes = (hours || 0) * 60 + (minutes || 0);
        const arrival = new Date(departure);
        arrival.setMinutes(arrival.getMinutes() + totalMinutes);
        return arrival;
    };

    const handleDurationChange = (duration) => {
        setFlightData(prev => ({
            ...prev,
            duration: duration
        }));
        
        if (flightData.departureTime) {
            const arrival = calculateArrivalTime(flightData.departureTime, duration);
            if (arrival) {
                setFlightData(prev => ({
                    ...prev,
                    arrivalTime: arrival
                }));
            }
        }
    };

    const handleDepartureTimeChange = (date) => {
        setFlightData(prev => ({
            ...prev,
            departureTime: date
        }));
        
        if (flightData.duration) {
            const arrival = calculateArrivalTime(date, flightData.duration);
            if (arrival) {
                setFlightData(prev => ({
                    ...prev,
                    arrivalTime: arrival
                }));
            }
        }
    };

    const getTimeCategory = (hour) => {
        if (hour >= 5 && hour < 12) return 'Morning';
        if (hour >= 12 && hour < 17) return 'Afternoon';
        if (hour >= 17 && hour < 21) return 'Evening';
        return 'Night';
    };

    const calculateDurationHours = (durationStr) => {
        const [hours, minutes] = durationStr.split(/[h\s]+/).filter(Boolean).map(Number);
        return (hours || 0) + (minutes || 0) / 60.0;
    };

    const handlePredictPrice = async () => {
        if (!flightData.fromAirport || !flightData.toAirport || !flightData.duration || !flightData.departureTime) {
            setError('Please fill in From, To, Duration, and Flight Date fields first');
            return;
        }

        setPredicting(true);
        setError(null);

        try {
            // ML service'e prediction request gönder
            const mlServiceUrl = import.meta.env.VITE_ML_SERVICE_URL || 'http://localhost:8090';
            
            // Calculate days left until departure
            const today = new Date();
            const departure = new Date(flightData.departureTime);
            const daysLeft = Math.max(1, Math.ceil((departure - today) / (1000 * 60 * 60 * 24)));

            // Get time categories
            const depHour = departure.getHours();
            const arrHour = flightData.arrivalTime ? new Date(flightData.arrivalTime).getHours() : depHour + 2;
            const departureTime = getTimeCategory(depHour);
            const arrivalTime = getTimeCategory(arrHour);

            // Calculate duration in hours
            const durationHours = calculateDurationHours(flightData.duration);

            // Extract airline from flight code (e.g., TK123 -> Turkish Airlines)
            const airlineCode = flightData.code ? flightData.code.substring(0, 2) : 'TK';
            const airlineMap = {
                'TK': 'Turkish Airlines',
                'LH': 'Lufthansa',
                'BA': 'British Airways',
                'AF': 'Air France'
            };
            const airline = airlineMap[airlineCode] || 'Turkish Airlines';

            // Map airport codes to cities (simplified - can be enhanced)
            const airportToCity = (code) => {
                const cityMap = {
                    'IST': 'Istanbul', 'SAW': 'Istanbul',
                    'JFK': 'New York', 'LAX': 'Los Angeles',
                    'LHR': 'London', 'CDG': 'Paris',
                    'FRA': 'Frankfurt', 'AMS': 'Amsterdam'
                };
                return cityMap[code.toUpperCase()] || code;
            };

            const response = await fetch(`${mlServiceUrl}/predict`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    airline: airline,
                    source_city: airportToCity(flightData.fromAirport),
                    destination_city: airportToCity(flightData.toAirport),
                    departure_time: departureTime,
                    arrival_time: arrivalTime,
                    stops: 'zero', // Assuming direct flights
                    class: 'Economy',
                    duration: durationHours,
                    days_left: daysLeft
                })
            });

            if (!response.ok) {
                throw new Error('Price prediction failed');
            }

            const data = await response.json();
            // ML service returns price in INR, convert to USD (approximate)
            const priceInUSD = (data.predicted_price || data.price) * 0.012;
            setPredictedPrice(priceInUSD);
            setFlightData(prev => ({
                ...prev,
                price: priceInUSD.toFixed(2)
            }));

        } catch (err) {
            setError('Failed to predict price. Using default calculation.');
            // Fallback: basit hesaplama
            const [hours, minutes] = flightData.duration.split(/[h\s]+/).filter(Boolean).map(Number);
            const totalMinutes = (hours || 0) * 60 + (minutes || 0);
            const estimatedPrice = Math.round(totalMinutes * 2.5); // $2.5 per minute
            setPredictedPrice(estimatedPrice);
            setFlightData(prev => ({
                ...prev,
                price: estimatedPrice.toString()
            }));
        } finally {
            setPredicting(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const token = await getAccessTokenSilently({
                authorizationParams: {
                    audience: 'https://api.airline.com'
                }
            });

            const [depHours, depMins] = format(flightData.departureTime, 'HH:mm').split(':').map(Number);
            const departureDateTime = new Date(flightData.departureTime);
            departureDateTime.setHours(depHours, depMins, 0, 0);

            const [arrHours, arrMins] = format(flightData.arrivalTime, 'HH:mm').split(':').map(Number);
            const arrivalDateTime = new Date(flightData.arrivalTime);
            arrivalDateTime.setHours(arrHours, arrMins, 0, 0);

            const flightRequest = {
                code: flightData.code,
                fromAirport: flightData.fromAirport.toUpperCase(),
                toAirport: flightData.toAirport.toUpperCase(),
                departureTime: departureDateTime.toISOString(),
                arrivalTime: arrivalDateTime.toISOString(),
                price: parseFloat(flightData.price),
                capacity: parseInt(flightData.capacity)
            };

            const response = await fetch('/api/v1/admin/flights', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(flightRequest)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to add flight');
            }

            setSuccess(true);
            // Reset form
            setFlightData({
                code: '',
                fromAirport: '',
                toAirport: '',
                departureTime: new Date(),
                arrivalTime: new Date(),
                duration: '',
                price: '',
                capacity: ''
            });
            setPredictedPrice(null);

            setTimeout(() => setSuccess(false), 3000);

        } catch (err) {
            setError(err.message || 'Failed to add flight');
        } finally {
            setLoading(false);
        }
    };

    if (!isAuthenticated) {
        return (
            <main>
                <div style={{
                    maxWidth: '600px',
                    margin: '50px auto',
                    padding: '40px',
                    background: 'var(--bg-card)',
                    borderRadius: 'var(--radius)',
                    textAlign: 'center'
                }}>
                    <h2>Access Denied</h2>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '10px' }}>
                        Please login to access the admin panel
                    </p>
                </div>
            </main>
        );
    }

    return (
        <main>
            <div className="admin-container" style={{
                maxWidth: '900px',
                margin: '30px auto',
                padding: '40px'
            }}>
                <div style={{ marginBottom: '30px', display: 'flex', alignItems: 'center', gap: '15px' }}>
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                        style={{ width: '32px', height: '32px', color: 'var(--primary)' }}
                    >
                        <path d="M3.478 2.404a.75.75 0 0 0-.926.941l2.432 7.905H13.5a.75.75 0 0 1 0 1.5H4.984l-2.432 7.905a.75.75 0 0 0 .926.94 60.519 60.519 0 0 0 18.445-8.986.75.75 0 0 0 0-1.218A60.517 60.517 0 0 0 3.478 2.404Z" />
                    </svg>
                    <h1>Flight Entry</h1>
                </div>

                {success && (
                    <div style={{
                        background: 'rgba(16, 185, 129, 0.1)',
                        padding: '15px',
                        borderRadius: 'var(--radius-sm)',
                        marginBottom: '20px',
                        border: '1px solid rgba(16, 185, 129, 0.3)',
                        color: 'var(--success)'
                    }}>
                        Flight added successfully!
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
                        {/* From City */}
                        <div className="input-group">
                            <label htmlFor="fromCity">From City</label>
                            <AirportAutocomplete
                                value={flightData.fromAirport}
                                onChange={(val) => setFlightData({ ...flightData, fromAirport: val })}
                                placeholder="Enter departure city"
                            />
                        </div>

                        {/* To City */}
                        <div className="input-group">
                            <label htmlFor="toCity">To City</label>
                            <AirportAutocomplete
                                value={flightData.toAirport}
                                onChange={(val) => setFlightData({ ...flightData, toAirport: val })}
                                placeholder="Enter destination city"
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
                        {/* Flight Date */}
                        <div className="input-group">
                            <label htmlFor="flightDate">Flight Date</label>
                            <DatePicker
                                selected={flightData.departureTime}
                                onChange={handleDepartureTimeChange}
                                dateFormat="yyyy-MM-dd"
                                minDate={new Date()}
                                className="form-control"
                                showTimeSelect
                                timeFormat="HH:mm"
                                timeIntervals={15}
                            />
                        </div>

                        {/* Flight Code */}
                        <div className="input-group">
                            <label htmlFor="flightCode">Flight Code</label>
                            <input
                                id="flightCode"
                                type="text"
                                value={flightData.code}
                                onChange={(e) => setFlightData({ ...flightData, code: e.target.value.toUpperCase() })}
                                placeholder="e.g., TK123"
                                required
                                style={{ textTransform: 'uppercase' }}
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
                        {/* Duration */}
                        <div className="input-group">
                            <label htmlFor="duration">Duration</label>
                            <input
                                id="duration"
                                type="text"
                                value={flightData.duration}
                                onChange={(e) => handleDurationChange(e.target.value)}
                                placeholder="e.g., 2h 30m"
                                required
                            />
                        </div>

                        {/* Price with Predict Button */}
                        <div className="input-group">
                            <label htmlFor="price" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span>Price</span>
                                <button
                                    type="button"
                                    onClick={handlePredictPrice}
                                    disabled={predicting || !flightData.fromAirport || !flightData.toAirport || !flightData.duration}
                                    className="btn"
                                    style={{
                                        padding: '6px 12px',
                                        fontSize: '0.85rem',
                                        background: 'var(--primary)',
                                        color: 'white',
                                        border: 'none'
                                    }}
                                >
                                    {predicting ? 'Predicting...' : 'Predict'}
                                </button>
                            </label>
                            <input
                                id="price"
                                type="number"
                                value={flightData.price}
                                onChange={(e) => setFlightData({ ...flightData, price: e.target.value })}
                                placeholder="e.g., $299"
                                required
                                min="0"
                                step="0.01"
                            />
                            {predictedPrice && (
                                <small style={{ color: 'var(--success)', marginTop: '5px', display: 'block' }}>
                                    Predicted: ${predictedPrice.toFixed(2)}
                                </small>
                            )}
                        </div>
                    </div>

                    {/* Capacity */}
                    <div className="input-group" style={{ marginBottom: '30px' }}>
                        <label htmlFor="capacity">Capacity</label>
                        <input
                            id="capacity"
                            type="number"
                            value={flightData.capacity}
                            onChange={(e) => setFlightData({ ...flightData, capacity: e.target.value })}
                            placeholder="e.g., 180"
                            required
                            min="1"
                        />
                    </div>

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

                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={loading}
                            style={{ minWidth: '150px' }}
                        >
                            {loading ? 'Saving...' : 'SAVE'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default AdminPage;

