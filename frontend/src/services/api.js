// Use VITE_API_URL from environment variables for production (Vercel), fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Helper to get JWT token
const getToken = () => localStorage.getItem('token');

// Custom fetch wrapper to automatically attach JWT token and default headers
const fetchWithAuth = async (endpoint, options = {}) => {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    return response;
};

export const api = {
    // Auth
    login: async (credentials) => {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Login failed');
        }
        const data = await response.json();
        // Save token on successful login
        if (data.token) localStorage.setItem('token', data.token);
        return data;
    },

    register: async (userData) => {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Registration failed');
        }
        const data = await response.json();
        if (data.token) localStorage.setItem('token', data.token);
        return data;
    },

    logout: () => {
        localStorage.removeItem('token');
    },

    // Patient Dashboard
    getDoctors: async (specialization = '') => {
        const endpoint = specialization ? `/doctors?specialization=${encodeURIComponent(specialization)}` : `/doctors`;
        const response = await fetchWithAuth(endpoint);
        if (!response.ok) throw new Error('Failed to fetch doctors');
        return response.json();
    },

    getDoctorSlots: async (doctorId, dateString) => {
        const response = await fetchWithAuth(`/doctors/${doctorId}/slots?date=${dateString}`);
        if (!response.ok) throw new Error('Failed to fetch available slots');
        return response.json();
    },

    // Appointments
    bookAppointment: async (bookingData) => {
        const response = await fetchWithAuth(`/appointments/book`, {
            method: 'POST',
            body: JSON.stringify(bookingData)
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to book appointment');
        }
        return response.json();
    },

    completeAppointment: async (appointmentId, data) => {
        const response = await fetchWithAuth(`/appointments/${appointmentId}/complete`, {
            method: 'POST',
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Failed to complete appointment');
        }
        return response.json();
    },

    cancelAppointment: async (id) => {
        const response = await fetchWithAuth(`/appointments/${id}/cancel`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Failed to cancel appointment');
        return response.json();
    },

    // Symptoms (AI)
    submitSymptoms: async (symptomData) => {
        const response = await fetchWithAuth(`/symptoms/submit`, {
            method: 'POST',
            body: JSON.stringify(symptomData)
        });
        if (!response.ok) throw new Error('Failed to analyze symptoms');
        return response.json();
    },

    // Doctor Dashboard
    getDoctorAppointments: async (doctorId) => {
        const response = await fetchWithAuth(`/appointments/doctor/${doctorId}`);
        if (!response.ok) throw new Error('Failed to fetch appointments');
        return response.json();
    },

    getPatientAppointments: async (patientId) => {
        const response = await fetchWithAuth(`/appointments/patient/${patientId}`);
        if (!response.ok) throw new Error('Failed to fetch appointments');
        return response.json();
    },

    getSymptomForm: async (appointmentId) => {
        const response = await fetchWithAuth(`/symptoms/appointment/${appointmentId}`);
        if (response.status === 404) return null; // No form exists yet
        if (!response.ok) throw new Error('Failed to fetch symptom form');
        return response.json();
    },

    // Admin Dashboard
    getLeaves: async () => {
        const response = await fetchWithAuth(`/admin/leaves`);
        if (!response.ok) throw new Error('Failed to fetch leaves');
        return response.json();
    },

    addLeave: async (leaveData) => {
        const response = await fetchWithAuth(`/admin/leaves`, {
            method: 'POST',
            body: JSON.stringify(leaveData)
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to add leave');
        }
        return response.json();
    },

    deleteLeave: async (id) => {
        const response = await fetchWithAuth(`/admin/leaves/${id}`, {
            method: 'DELETE'
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to delete leave');
        }
    },

    createDoctor: async (doctorData) => {
        const response = await fetchWithAuth(`/admin/doctors`, {
            method: 'POST',
            body: JSON.stringify(doctorData)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Failed to create doctor');
        }
        return response.json();
    },

    updateDoctorProfile: async (doctorId, profileData) => {
        const response = await fetchWithAuth(`/admin/doctors/${doctorId}`, {
            method: 'PUT',
            body: JSON.stringify(profileData)
        });
        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || 'Failed to update doctor profile');
        }
        return response.json();
    }
};
