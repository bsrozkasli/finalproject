const API_BASE_URL = '/api/v1';

export const flightApi = {
    searchFlights: async (params) => {
        const queryParams = new URLSearchParams(params);
        const response = await fetch(`${API_BASE_URL}/flights/search?${queryParams}`);
        if (!response.ok) {
            throw new Error('Failed to search flights');
        }
        return response.json();
    },

    getFlightById: async (id) => {
        const response = await fetch(`${API_BASE_URL}/flights/${id}`);
        if (!response.ok) {
            throw new Error('Failed to get flight');
        }
        return response.json();
    },

    getFlightByCode: async (code) => {
        const response = await fetch(`${API_BASE_URL}/flights/code/${code}`);
        if (!response.ok) {
            throw new Error('Failed to get flight');
        }
        return response.json();
    },
};

export const bookingApi = {
    createBooking: async (bookingData, token) => {
        const response = await fetch(`${API_BASE_URL}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
            body: JSON.stringify(bookingData),
        });
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create booking');
        }
        return response.json();
    },

    getBookingByRef: async (ref) => {
        const response = await fetch(`${API_BASE_URL}/bookings/${ref}`);
        if (!response.ok) {
            throw new Error('Failed to get booking');
        }
        return response.json();
    },

    getMyBookings: async (token) => {
        const response = await fetch(`${API_BASE_URL}/bookings/my-bookings`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to get bookings');
        }
        return response.json();
    },

    cancelBooking: async (ref, token) => {
        const response = await fetch(`${API_BASE_URL}/bookings/${ref}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to cancel booking');
        }
        return response.json();
    },
};

export const milesApi = {
    getAccount: async (token) => {
        const response = await fetch(`${API_BASE_URL}/miles/account`, {
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to get miles account');
        }
        return response.json();
    },

    burnMiles: async (amount, description, token) => {
        const params = new URLSearchParams({ amount, description });
        const response = await fetch(`${API_BASE_URL}/miles/burn?${params}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to burn miles');
        }
        return response.json();
    },
};
