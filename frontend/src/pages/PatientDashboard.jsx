import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import BookingModal from '../components/BookingModal';

export default function PatientDashboard() {
    const navigate = useNavigate();
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [bookingError, setBookingError] = useState(null);
    const [bookingSuccess, setBookingSuccess] = useState(null);
    const [searchSpec, setSearchSpec] = useState('');
    
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    const PATIENT_ID = currentUser?.user?.id || currentUser?.id || 1;
    
    const [myAppointments, setMyAppointments] = useState([]);
    const [selectedApt, setSelectedApt] = useState(null); // for viewing summary
    
    // Booking state
    const [bookingDoctor, setBookingDoctor] = useState(null);

    useEffect(() => {
        fetchDoctors('');
        fetchMyAppointments();
    }, []);

    const fetchMyAppointments = async () => {
        try {
            const data = await api.getPatientAppointments(PATIENT_ID);
            setMyAppointments(data);
        } catch (error) {
            console.error("Failed to load my appointments", error);
        }
    };

    const fetchDoctors = async (spec) => {
        try {
            setLoading(true);
            const data = await api.getDoctors(spec);
            setDoctors(data);
        } catch (error) {
            console.error("Failed to load doctors", error);
        } finally {
            setLoading(false);
        }
    };

    const handleBookingSuccess = (appointment) => {
        setBookingDoctor(null);
        setBookingSuccess("Appointment booked successfully!");
        fetchMyAppointments();
    };

    const handleBookingError = (errorMsg) => {
        setBookingError(errorMsg);
    };

    const handleCancelAppointment = async (aptId) => {
        if (!window.confirm("Are you sure you want to cancel this appointment?")) return;
        
        try {
            await api.cancelAppointment(aptId);
            fetchMyAppointments();
        } catch (error) {
            console.error("Failed to cancel appointment", error);
            alert("Failed to cancel appointment.");
        }
    };

    return (
        <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem' }}>
                <h1 style={{ fontSize: '2.5rem' }}>Patient Dashboard</h1>
                <button 
                    className="btn-primary" 
                    style={{ background: 'rgba(255,255,255,0.1)', padding: '0.5rem 1.5rem', boxShadow: 'none' }}
                    onClick={() => navigate('/')}
                >
                    Back Home
                </button>
            </div>

            {bookingError && (
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5', padding: '1rem', borderRadius: '12px', marginBottom: '2rem', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                    {bookingError}
                </div>
            )}
            
            {bookingSuccess && (
                <div style={{ background: 'rgba(34, 197, 94, 0.1)', color: '#86efac', padding: '1rem', borderRadius: '12px', marginBottom: '2rem', border: '1px solid rgba(34, 197, 94, 0.2)' }}>
                    {bookingSuccess}
                </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h2 style={{ fontSize: '1.5rem', color: 'var(--text-secondary)' }}>Available Doctors</h2>
                <input
                    type="text"
                    placeholder="Search specialization..."
                    value={searchSpec}
                    onChange={(e) => {
                        setSearchSpec(e.target.value);
                        fetchDoctors(e.target.value);
                    }}
                    style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white', minWidth: '250px' }}
                />
            </div>
            
            {loading ? (
                <p>Loading doctors...</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '2rem' }}>
                    {doctors.map(profile => (
                        <div key={profile.id} className="glass-panel hover-lift" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
                            <h3 style={{ fontSize: '1.5rem', marginBottom: '0.5rem' }}>
                                {profile.user?.firstName} {profile.user?.lastName}
                            </h3>
                            <p style={{ color: 'var(--accent-blue)', marginBottom: '2rem', fontWeight: '500' }}>
                                {profile.specialization}
                            </p>
                            
                            <button 
                                className="btn-primary" 
                                style={{ width: '100%', marginTop: 'auto' }}
                                onClick={() => setBookingDoctor(profile)}
                            >
                                Book Appointment
                            </button>
                        </div>
                    ))}
                </div>
            )}

            <div style={{ marginTop: '4rem', marginBottom: '1.5rem' }}>
                <h2 style={{ fontSize: '1.5rem', color: 'var(--text-secondary)' }}>My Appointments</h2>
            </div>

            {myAppointments.length === 0 ? (
                <p style={{ color: 'var(--text-secondary)' }}>You have no appointments booked yet.</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '2rem' }}>
                    {myAppointments.map(apt => (
                        <div key={apt.id} className="glass-panel" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                                <h3 style={{ fontSize: '1.5rem', margin: 0 }}>
                                    Dr. {apt.doctor.lastName}
                                </h3>
                                <span style={{ background: apt.status === 'COMPLETED' ? 'rgba(34, 197, 94, 0.2)' : 'rgba(59, 130, 246, 0.2)', color: apt.status === 'COMPLETED' ? '#86efac' : '#93c5fd', padding: '0.25rem 0.75rem', borderRadius: '9999px', fontSize: '0.875rem' }}>
                                    {apt.status}
                                </span>
                            </div>
                            <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                                {new Date(apt.appointmentTime).toLocaleString()}
                            </p>
                            
                            {apt.status === 'BOOKED' && (
                                <button 
                                    style={{ width: '100%', marginTop: 'auto', background: 'transparent', color: '#ef4444', border: '1px solid #ef4444', padding: '0.75rem', borderRadius: '8px', cursor: 'pointer' }}
                                    onClick={() => handleCancelAppointment(apt.id)}
                                >
                                    Cancel Appointment
                                </button>
                            )}
                            
                            {apt.status === 'COMPLETED' && apt.postVisitSummary && (
                                <button 
                                    className="btn-primary" 
                                    style={{ width: '100%', marginTop: 'auto', background: 'rgba(34, 197, 94, 0.2)', boxShadow: 'none', border: '1px solid rgba(34, 197, 94, 0.5)' }}
                                    onClick={() => setSelectedApt(apt)}
                                >
                                    View Post-Visit Summary
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {bookingDoctor && (
                <BookingModal
                    doctor={bookingDoctor}
                    patientId={PATIENT_ID}
                    onClose={() => setBookingDoctor(null)}
                    onSuccess={handleBookingSuccess}
                    onError={handleBookingError}
                />
            )}

            {selectedApt && (
                <div style={{
                    position: 'fixed',
                    top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: 'rgba(15, 23, 42, 0.8)',
                    backdropFilter: 'blur(4px)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000
                }}>
                    <div className="glass-panel" style={{ padding: '3rem', width: '90%', maxWidth: '700px', maxHeight: '90vh', overflowY: 'auto' }}>
                        <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem', color: '#22c55e' }}>Post-Visit Summary</h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                            Dr. {selectedApt.doctor.lastName} - {new Date(selectedApt.appointmentTime).toLocaleString()}
                        </p>
                        
                        <div style={{ 
                            backgroundColor: 'rgba(34, 197, 94, 0.1)', 
                            borderLeft: '4px solid #22c55e',
                            padding: '1.5rem',
                            borderRadius: '0 8px 8px 0',
                            lineHeight: '1.6',
                            whiteSpace: 'pre-wrap',
                            marginBottom: '2rem'
                        }}>
                            {selectedApt.postVisitSummary}
                        </div>

                        {selectedApt.prescription && (
                            <>
                                <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>Prescription</h3>
                                <div style={{ background: 'rgba(255,255,255,0.05)', padding: '1.5rem', borderRadius: '8px', whiteSpace: 'pre-wrap', marginBottom: '2rem' }}>
                                    {selectedApt.prescription}
                                </div>
                            </>
                        )}

                        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <button className="btn-primary" onClick={() => setSelectedApt(null)}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
