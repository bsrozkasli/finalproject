import { useState, useEffect, useRef } from 'react';
import './AirportAutocomplete.css';

function AirportAutocomplete({ label, value, onChange, placeholder, disabled, dependentSource }) {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const [loading, setLoading] = useState(false);
    const [selectedAirport, setSelectedAirport] = useState(null);
    const wrapperRef = useRef(null);

    // Close dropdown when clicking outside
    useEffect(() => {
        function handleClickOutside(event) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [wrapperRef]);

    // Update query when value changes externally (e.g., when parent clears it)
    useEffect(() => {
        if (!value) {
            setQuery('');
            setSelectedAirport(null);
        } else if (value.length === 3 && query.length <= 3) {
            // If value is set externally and it's a code, and query is empty or just a code, update it
            // This prevents overwriting user input
            setQuery(value);
        }
    }, [value]); // Only depend on value to avoid loops

    const handleInputChange = async (e) => {
        const val = e.target.value;
        setQuery(val);
        setSelectedAirport(null); // Clear selection when user types

        if (val.length < 1) {
            if (dependentSource) {
                // For dependent source (To field), allow empty query to show all destinations
                // Continue to fetch
            } else {
                // For From field, clear results when empty
                setResults([]);
                setIsOpen(false);
                onChange(''); // Clear the value
                return;
            }
        }

        setIsOpen(true);
        setLoading(true);
        try {
            let url;
            if (dependentSource) {
                // For dependent search (e.g., "To" field), search within valid destinations
                // Allow empty query to fetch all valid destinations
                url = `/api/v1/airports/destinations/search?from=${dependentSource}&query=${encodeURIComponent(val || '')}`;
            } else {
                // General search (e.g., "From" field) - allow single character search
                // Backend will handle the search for any length >= 1
                url = `/api/v1/airports?query=${encodeURIComponent(val)}`;
            }

            const response = await fetch(url);
            if (response.ok) {
                const data = await response.json();
                setResults(data);
                if (data.length > 0) {
                    setIsOpen(true);
                }
            } else {
                setResults([]);
            }
        } catch (error) {
            console.error("Error fetching airports:", error);
            setResults([]);
        } finally {
            setLoading(false);
        }
    };

    const handleSelect = (airport) => {
        setSelectedAirport(airport);
        setQuery(`${airport.city} (${airport.code})`);
        onChange(airport.code);
        setIsOpen(false);
    };

    const handleFocus = () => {
        if (dependentSource) {
            // dependent source: always fetch/show on focus.
            // If results empty, try fetch (pass empty query manually)
            if (results.length === 0) {
                // Trigger a manual fetch for empty string
                // We can reuse logic or just duplicate fetch call
                setLoading(true);
                fetch(`/api/v1/airports/destinations/search?from=${dependentSource}&query=`)
                    .then(res => res.json())
                    .then(data => {
                        setResults(data);
                        if (data.length > 0) setIsOpen(true);
                    })
                    .catch(err => console.error(err))
                    .finally(() => setLoading(false));
            } else {
                setIsOpen(true);
            }
        } else {
            if (query.length >= 1 && results.length > 0) {
                setIsOpen(true);
            }
        }
    };

    return (
        <div className="input-group autocomplete-wrapper" ref={wrapperRef}>
            {label && <label>{label}</label>}
            <input
                type="text"
                value={query}
                onChange={handleInputChange}
                onFocus={handleFocus}
                placeholder={placeholder}
                disabled={disabled || (dependentSource && !dependentSource)}
            />
            {loading && <div className="autocomplete-spinner">Loading...</div>}
            {isOpen && (
                <ul className="autocomplete-results">
                    {results.length > 0 ? (
                        results.map((airport) => (
                            <li key={airport.code} onClick={() => handleSelect(airport)}>
                                <div className="airport-info">
                                    <div className="airport-main">
                                        <span className="code">{airport.code}</span>
                                        <span className="city">{airport.city}, {airport.country}</span>
                                    </div>
                                    <div className="airport-sub">
                                        <span className="name">{airport.name}</span>
                                    </div>
                                </div>
                                <div className="airport-icon">✈️</div>
                            </li>
                        ))
                    ) : (
                        !loading && query.length >= 1 && (
                            <li className="no-results" style={{ padding: '12px', color: 'var(--text-muted)', textAlign: 'center' }}>
                                {dependentSource ? 'No destinations found' : 'No airports found'}
                            </li>
                        )
                    )}
                </ul>
            )}
        </div>
    );
}

export default AirportAutocomplete;
