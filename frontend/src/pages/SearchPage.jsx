import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AirportAutocomplete from '../components/AirportAutocomplete';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { format } from 'date-fns';

function SearchPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useState({
        from: '',
        to: '',
        date: new Date(),
        passengers: 1,
        flexible: false
    });
    const [flights, setFlights] = useState([]);
    const [pageInfo, setPageInfo] = useState({ page: 0, size: 10, totalPages: 0, totalElements: 0 });
    const [loading, setLoading] = useState(false);
    const [searched, setSearched] = useState(false);
    const [availableDates, setAvailableDates] = useState([]);

    // Fetch available dates when From/To changes
    useEffect(() => {
        const fetchAvailableDates = async () => {
            if (searchParams.from && searchParams.to) {
                try {
                    const response = await fetch(
                        `/api/v1/flights/dates?from=${searchParams.from}&to=${searchParams.to}`
                    );
                    if (response.ok) {
                        const dates = await response.json();
                        // Parse "YYYY-MM-DD" strictly as local dates
                        const parsedDates = dates.map(d => {
                            const [year, month, day] = d.split('-').map(Number);
                            return new Date(year, month - 1, day);
                        });
                        setAvailableDates(parsedDates);
                    }
                } catch (err) {
                    console.error("Failed to fetch dates", err);
                }
            } else {
                setAvailableDates([]);
            }
        };
        fetchAvailableDates();
    }, [searchParams.from, searchParams.to]);

    // Clear 'to' and reset date when 'from' changes to a different value
    useEffect(() => {
        // This effect ensures 'to' is cleared when 'from' becomes invalid or changes
        // The onChange handler also handles this, but this is a safety net
    }, [searchParams.from]);

    const performSearch = async (page = 0) => {
        setLoading(true);
        setSearched(true);

        try {
            const params = new URLSearchParams({
                from: searchParams.from.toUpperCase(),
                to: searchParams.to.toUpperCase(),
                date: format(searchParams.date, 'yyyy-MM-dd'),
                passengers: searchParams.passengers,
                flexible: searchParams.flexible,
                page: page,
                size: 10
            });

            const response = await fetch(`/api/v1/flights/search?${params}`);

            if (response.ok) {
                const data = await response.json();
                setFlights(data.content); // Page response has content
                setPageInfo({
                    page: data.number,
                    size: data.size,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements
                });
            } else {
                console.error('Failed to fetch flights');
                setFlights([]);
            }
        } catch (error) {
            console.error('Error searching flights:', error);
            setFlights([]);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        performSearch(0);
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < pageInfo.totalPages) {
            performSearch(newPage);
        }
    };

    const formatTime = (dateTime) => {
        if (!dateTime) return '';
        const date = new Date(dateTime);
        return date.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    };

    const formatDate = (dateTime) => {
        if (!dateTime) return '';
        const date = new Date(dateTime);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    };

    const formatDuration = (minutes) => {
        if (!minutes) return '';
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hours}h ${mins}m`;
    };

    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(price);
    };

    return (
        <main>
            <section className="hero">
                <h1>Find Your Next Adventure</h1>
                <p>Discover the world's most amazing destinations at the best prices.</p>
            </section>

            <form className="search-form" onSubmit={handleSearch}>
                <div className="search-form-grid">

                    <div className="input-group">
                        <label htmlFor="from">From</label>
                        <AirportAutocomplete
                            value={searchParams.from}
                            onChange={(val) => {
                                const prevFrom = searchParams.from;
                                setSearchParams(prev => {
                                    // If from changed to a different valid airport, clear to and reset date
                                    if (val && val.length === 3 && val !== prevFrom) {
                                        return {
                                            ...prev,
                                            from: val,
                                            to: '',
                                            date: new Date()
                                        };
                                    }
                                    // If from is cleared or invalid, clear to
                                    if (!val || val.length !== 3) {
                                        return {
                                            ...prev,
                                            from: val,
                                            to: ''
                                        };
                                    }
                                    // Otherwise just update from
                                    return { ...prev, from: val };
                                });
                            }}
                            placeholder="Origin (e.g. L, LAX, London)"
                        />
                        <div className="helper-text-container" style={{ minHeight: '20px', marginTop: '4px' }}></div>
                    </div>

                    <div className="input-group">
                        <label htmlFor="to">To</label>
                        <AirportAutocomplete
                            value={searchParams.to}
                            onChange={(val) => setSearchParams({ ...searchParams, to: val })}
                            placeholder={searchParams.from ? "Destination (e.g. I, IST, Istanbul)" : "Select origin first"}
                            dependentSource={searchParams.from && searchParams.from.length === 3 ? searchParams.from.toUpperCase() : null}
                            disabled={!searchParams.from || searchParams.from.length !== 3}
                        />
                        <div className="helper-text-container" style={{ minHeight: '20px', marginTop: '4px' }}>
                            {!searchParams.from && <small style={{ color: 'var(--text-muted)' }}>Select origin first</small>}
                        </div>
                    </div>

                    <div className="input-group">
                        <label htmlFor="date">Departure Date</label>
                        <DatePicker
                            selected={searchParams.date}
                            onChange={(date) => setSearchParams({ ...searchParams, date })}
                            dateFormat="dd/MM/yyyy"
                            minDate={new Date()}
                            highlightDates={availableDates}
                            includeDates={availableDates.length > 0 ? availableDates : undefined}
                            filterDate={(date) => {
                                // Only allow dates that have flights
                                if (availableDates.length === 0) {
                                    return false; // No flights available, disable all dates
                                }
                                // Check if the date is in availableDates
                                const dateStr = format(date, 'yyyy-MM-dd');
                                return availableDates.some(availableDate =>
                                    format(availableDate, 'yyyy-MM-dd') === dateStr
                                );
                            }}
                            placeholderText={availableDates.length > 0 ? "Select a valid date" : "Select route first"}
                            className="form-control"
                            disabled={!searchParams.from || !searchParams.to || availableDates.length === 0}
                            onFocus={(e) => e.target.readOnly = true}
                        />
                        <div className="helper-text-container" style={{ minHeight: '20px', marginTop: '4px', fontSize: '0.8rem', color: '#94a3b8' }}>
                            {availableDates.length > 0 ? `${availableDates.length} flights available` :
                                (searchParams.from && searchParams.to ? 'No flights available for this route' : 'Select route first')}
                        </div>
                    </div>

                    <div className="input-group">
                        <label htmlFor="passengers">Passengers</label>
                        <select
                            id="passengers"
                            value={searchParams.passengers}
                            onChange={(e) => setSearchParams({ ...searchParams, passengers: parseInt(e.target.value) })}
                        >
                            {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(num => (
                                <option key={num} value={num}>
                                    {num} {num === 1 ? 'Passenger' : 'Passengers'}
                                </option>
                            ))}
                        </select>
                        <div className="helper-text-container" style={{ minHeight: '20px', marginTop: '4px' }}></div>
                    </div>

                </div>

                <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? 'Searching...' : 'Search Flights'}
                </button>

            </form>

            {/* Results */}
            {loading && <div className="spinner"></div>}

            {
                !loading && searched && flights.length === 0 && (
                    <div className="empty-state">
                        <h3>No flights found</h3>
                        <p>Try adjusting your search criteria or dates</p>
                    </div>
                )
            }

            {
                !loading && flights.length > 0 && (
                    <>
                        <div className="flights-grid">
                            {flights.map((flight) => (
                                <div key={flight.id} className="flight-card">
                                    <div className="flight-route">
                                        <div className="flight-airport">
                                            <div className="code">{flight.fromAirport}</div>
                                            <div className="name">{formatTime(flight.departureTime)}</div>
                                        </div>

                                        <div className="flight-duration">
                                            <div>{formatDuration(flight.durationMinutes)}</div>
                                            <div className="line"></div>
                                            <div>Direct</div>
                                        </div>

                                        <div className="flight-airport">
                                            <div className="code">{flight.toAirport}</div>
                                            <div className="name">{formatTime(flight.arrivalTime)}</div>
                                        </div>
                                    </div>

                                    <div className="flight-details">
                                        <div className="flight-code">{flight.code}</div>
                                        <div className="flight-time">{formatDate(flight.departureTime)}</div>
                                        <span className="badge badge-success">{flight.availableSeats} seats left</span>
                                    </div>

                                    <div className="flight-price">
                                        <div className="amount">{formatPrice(flight.predictedPrice || flight.price)}</div>
                                        <div className="per-person">per person</div>
                                    </div>

                                    <button
                                        className="btn btn-primary"
                                        onClick={() => navigate('/booking', { state: { flight, passengerCount: searchParams.passengers } })}
                                    >
                                        Book Now
                                    </button>
                                </div>
                            ))}
                        </div>

                        {/* Pagination Controls */}
                        <div className="pagination" style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginTop: '20px' }}>
                            <button
                                className="btn"
                                disabled={pageInfo.page === 0}
                                onClick={() => handlePageChange(pageInfo.page - 1)}
                            >
                                Previous
                            </button>
                            <span style={{ alignSelf: 'center' }}>
                                Page {pageInfo.page + 1} of {pageInfo.totalPages}
                            </span>
                            <button
                                className="btn"
                                disabled={pageInfo.page >= pageInfo.totalPages - 1}
                                onClick={() => handlePageChange(pageInfo.page + 1)}
                            >
                                Next
                            </button>
                        </div>
                    </>
                )
            }
        </main >
    );
}

export default SearchPage;
