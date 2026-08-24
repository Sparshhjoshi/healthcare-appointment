import { useState, useEffect } from 'react';
import { api } from '../services/api';

export default function BookingModal({ doctor, patientId, onClose, onSuccess, onError }) {
    const [date, setDate] = useState('');
    const [slots, setSlots] = useState([]);
    const [loadingSlots, setLoadingSlots] = useState(false);
    const [selectedTime, setSelectedTime] = useState(null);
    const [symptoms, setSymptoms] = useState('');
    const [booking, setBooking] = useState(false);

    useEffect(() => {
        if (date) {
            setSelectedTime(null);
            setSymptoms('');
            fetchSlots();
        } else {
            setSlots([]);
        }
    }, [date]);

    const fetchSlots = async () => {
        try {
            setLoadingSlots(true);
            const available = await api.getDoctorSlots(doctor.user.id, date);
            setSlots(available);
        } catch (error) {
            console.error("Failed to load slots", error);
            setSlots([]);
        } finally {
            setLoadingSlots(false);
        }
    };

    const handleBook = async () => {
        if (!symptoms.trim()) {
            alert("Please enter your symptoms or chief complaint.");
            return;
        }
        try {
            setBooking(true);
            const appointmentTime = `${date}T${selectedTime}:00`;
            const appointment = await api.bookAppointment({
                patientId: patientId,
                doctorId: doctor.user.id,
                appointmentTime: appointmentTime,
                symptoms: symptoms
            });
            onSuccess(appointment);
        } catch (error) {
            onError(error.message);
            onClose();
        } finally {
            setBooking(false);
        }
    };

    return (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, backdropFilter: 'blur(5px)' }}>
            <div className="glass-panel" style={{ padding: '2.5rem', width: '100%', maxWidth: '500px', maxHeight: '90vh', overflowY: 'auto' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem' }}>
                    <div>
                        <h2 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>Book Appointment</h2>
                        <p style={{ color: 'var(--text-secondary)' }}>
                            Dr. {doctor.user?.firstName} {doctor.user?.lastName} ({doctor.specialization})
                        </p>
                    </div>
                    <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: 'white', fontSize: '1.5rem', cursor: 'pointer', padding: '0.5rem' }}>×</button>
                </div>

                <div style={{ marginBottom: '2rem' }}>
                    <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Select Date</label>
                    <input 
                        type="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        min={new Date().toISOString().split('T')[0]} // Prevent past dates
                        style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                    />
                </div>

                {loadingSlots && <p style={{ color: 'var(--text-secondary)', textAlign: 'center' }}>Checking availability...</p>}

                {!loadingSlots && date && slots.length === 0 && (
                    <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5', padding: '1rem', borderRadius: '12px', border: '1px solid rgba(239, 68, 68, 0.2)', textAlign: 'center' }}>
                        No slots available on this date.
                    </div>
                )}

                {!loadingSlots && slots.length > 0 && !selectedTime && (
                    <div>
                        <label style={{ display: 'block', marginBottom: '1rem', color: 'var(--text-secondary)' }}>Available Times</label>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.5rem' }}>
                            {slots.map(time => (
                                <button
                                    key={time}
                                    onClick={() => setSelectedTime(time)}
                                    disabled={booking}
                                    style={{
                                        padding: '0.5rem',
                                        borderRadius: '8px',
                                        background: 'rgba(59, 130, 246, 0.1)',
                                        border: '1px solid rgba(59, 130, 246, 0.3)',
                                        color: '#60a5fa',
                                        cursor: 'pointer',
                                        transition: 'all 0.2s'
                                    }}
                                    onMouseOver={(e) => { e.target.style.background = 'rgba(59, 130, 246, 0.2)' }}
                                    onMouseOut={(e) => { e.target.style.background = 'rgba(59, 130, 246, 0.1)' }}
                                >
                                    {time}
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {selectedTime && (
                    <div style={{ marginTop: '1rem' }}>
                        <div style={{ padding: '1rem', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.2)', marginBottom: '1.5rem' }}>
                            <p style={{ margin: 0, color: '#60a5fa' }}>Selected Time: <strong>{date} at {selectedTime}</strong></p>
                            <button 
                                onClick={() => setSelectedTime(null)} 
                                style={{ background: 'transparent', border: 'none', color: 'var(--text-secondary)', fontSize: '0.8rem', cursor: 'pointer', marginTop: '0.5rem', padding: 0 }}
                            >
                                Change Time
                            </button>
                        </div>
                        
                        <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Chief Complaint / Symptoms</label>
                        <textarea
                            value={symptoms}
                            onChange={(e) => setSymptoms(e.target.value)}
                            placeholder="Please describe your symptoms briefly for the AI Pre-Visit Summary..."
                            rows="4"
                            required
                            style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white', marginBottom: '1.5rem', resize: 'vertical' }}
                        />

                        <button 
                            onClick={handleBook}
                            disabled={booking}
                            className="btn-primary"
                            style={{ width: '100%', padding: '1rem', fontSize: '1.1rem', opacity: booking ? 0.7 : 1 }}
                        >
                            {booking ? 'Confirming...' : 'Confirm Appointment'}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
