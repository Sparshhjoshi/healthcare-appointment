import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';

export default function DoctorDashboard() {
    const navigate = useNavigate();
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedApt, setSelectedApt] = useState(null);
    const [doctorNotes, setDoctorNotes] = useState('');
    const [prescription, setPrescription] = useState('');
    const [medications, setMedications] = useState([]);
    const [newMedName, setNewMedName] = useState('');
    const [newMedFreq, setNewMedFreq] = useState('DAILY');
    const [newMedDur, setNewMedDur] = useState(7);
    const [completing, setCompleting] = useState(false);

    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    const DOCTOR_ID = currentUser ? currentUser.id : 2; 

    useEffect(() => {
        loadAppointments();
    }, []);

    const loadAppointments = async () => {
        try {
            const data = await api.getDoctorAppointments(DOCTOR_ID);
            setAppointments(data);
        } catch (error) {
            console.error("Failed to load appointments", error);
        } finally {
            setLoading(false);
        }
    };

    const handleComplete = async () => {
        if (!doctorNotes.trim()) {
            alert("Doctor notes are required.");
            return;
        }
        
        try {
            setCompleting(true);
            await api.completeAppointment(selectedApt.id, {
                doctorNotes,
                prescription,
                medications
            });
            setSelectedApt(null);
            setDoctorNotes('');
            setPrescription('');
            setMedications([]);
            loadAppointments();
        } catch (error) {
            console.error("Failed to complete appointment", error);
            alert("Failed to complete appointment: " + error.message);
        } finally {
            setCompleting(false);
        }
    };

    return (
        <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem' }}>
                <h1 style={{ fontSize: '2.5rem' }}>Doctor Dashboard</h1>
                <button 
                    className="btn-primary" 
                    style={{ background: 'rgba(255,255,255,0.1)', padding: '0.5rem 1.5rem', boxShadow: 'none' }}
                    onClick={() => navigate('/')}
                >
                    Back Home
                </button>
            </div>

            <h2 style={{ marginBottom: '1.5rem', fontSize: '1.5rem', color: 'var(--text-secondary)' }}>Upcoming Appointments</h2>
            
            {loading ? (
                <p>Loading schedule...</p>
            ) : appointments.length === 0 ? (
                <p>No appointments booked yet.</p>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '2rem' }}>
                    {appointments.map(apt => (
                        <div key={apt.id} className="glass-panel hover-lift" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                                <h3 style={{ fontSize: '1.5rem' }}>
                                    {apt.patient.firstName} {apt.patient.lastName}
                                </h3>
                                <span style={{ background: 'rgba(59, 130, 246, 0.2)', color: '#93c5fd', padding: '0.25rem 0.75rem', borderRadius: '9999px', fontSize: '0.875rem' }}>
                                    {apt.status}
                                </span>
                            </div>
                            
                            <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                                {new Date(apt.appointmentTime).toLocaleString()}
                            </p>
                            
                            {apt.status === 'BOOKED' && (
                                <button 
                                    className="btn-primary" 
                                    style={{ width: '100%', marginTop: 'auto', background: 'rgba(139, 92, 246, 0.2)', boxShadow: 'none', border: '1px solid rgba(139, 92, 246, 0.5)' }}
                                    onClick={() => setSelectedApt(apt)}
                                >
                                    Complete Visit
                                </button>
                            )}
                        </div>
                    ))}
                </div>
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
                    <div className="glass-panel" style={{ padding: '3rem', width: '90%', maxWidth: '800px', maxHeight: '90vh', overflowY: 'auto' }}>
                        <h2 style={{ fontSize: '2rem', marginBottom: '0.5rem', color: '#8b5cf6' }}>Complete Visit</h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                            {selectedApt.patient.firstName} {selectedApt.patient.lastName} - {new Date(selectedApt.appointmentTime).toLocaleString()}
                        </p>
                        
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
                            <div>
                                <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>Original Symptoms</h3>
                                <div style={{ background: 'rgba(255,255,255,0.05)', padding: '1rem', borderRadius: '8px', minHeight: '100px' }}>
                                    {selectedApt.symptoms || "No symptoms provided."}
                                </div>
                            </div>
                            <div>
                                <h3 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: 'var(--text-secondary)' }}>AI Pre-Visit Summary</h3>
                                <div style={{ 
                                    backgroundColor: 'rgba(139, 92, 246, 0.1)', 
                                    borderLeft: '4px solid #8b5cf6',
                                    padding: '1rem',
                                    borderRadius: '0 8px 8px 0',
                                    minHeight: '100px',
                                    whiteSpace: 'pre-wrap'
                                }}>
                                    {selectedApt.preVisitSummary || "No AI summary available."}
                                </div>
                            </div>
                        </div>

                        <div style={{ marginBottom: '1.5rem' }}>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Doctor Notes (Required)</label>
                            <textarea
                                value={doctorNotes}
                                onChange={(e) => setDoctorNotes(e.target.value)}
                                placeholder="Enter clinical notes from the visit..."
                                rows="4"
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white', resize: 'vertical' }}
                            />
                        </div>

                        <div style={{ marginBottom: '2rem' }}>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>General Prescription Notes (Optional)</label>
                            <textarea
                                value={prescription}
                                onChange={(e) => setPrescription(e.target.value)}
                                placeholder="Enter general prescription instructions..."
                                rows="2"
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white', resize: 'vertical' }}
                            />
                        </div>

                        <div style={{ marginBottom: '2rem' }}>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Structured Medications (For Reminders)</label>
                            
                            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
                                <input
                                    type="text"
                                    placeholder="Medication Name"
                                    value={newMedName}
                                    onChange={e => setNewMedName(e.target.value)}
                                    style={{ flex: 2, padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                />
                                <select 
                                    value={newMedFreq}
                                    onChange={e => setNewMedFreq(e.target.value)}
                                    style={{ flex: 1, padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                >
                                    <option value="DAILY">Daily</option>
                                    <option value="TWICE_DAILY">Twice Daily</option>
                                    <option value="WEEKLY">Weekly</option>
                                    <option value="AS_NEEDED">As Needed</option>
                                </select>
                                <input
                                    type="number"
                                    placeholder="Days"
                                    value={newMedDur}
                                    onChange={e => setNewMedDur(Number(e.target.value))}
                                    min="1"
                                    style={{ width: '80px', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                />
                                <button 
                                    onClick={() => {
                                        if (newMedName.trim()) {
                                            setMedications([...medications, { name: newMedName, frequency: newMedFreq, durationDays: newMedDur }]);
                                            setNewMedName('');
                                        }
                                    }}
                                    style={{ background: '#3b82f6', color: 'white', border: 'none', padding: '0.75rem 1.5rem', borderRadius: '8px', cursor: 'pointer' }}
                                >
                                    Add
                                </button>
                            </div>

                            {medications.length > 0 && (
                                <ul style={{ listStyle: 'none', padding: 0, margin: 0, background: 'rgba(0,0,0,0.2)', borderRadius: '8px', overflow: 'hidden' }}>
                                    {medications.map((m, i) => (
                                        <li key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '0.75rem 1rem', borderBottom: i < medications.length - 1 ? '1px solid rgba(255,255,255,0.1)' : 'none' }}>
                                            <span>{m.name} - {m.frequency} ({m.durationDays} days)</span>
                                            <button 
                                                onClick={() => setMedications(medications.filter((_, idx) => idx !== i))}
                                                style={{ background: 'transparent', color: '#ef4444', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}
                                            >
                                                &times;
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </div>

                        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                            <button 
                                style={{ background: 'transparent', border: '1px solid var(--border-color)', color: 'white', padding: '0.75rem 1.5rem', borderRadius: '8px', cursor: 'pointer' }}
                                onClick={() => { setSelectedApt(null); setDoctorNotes(''); setPrescription(''); setMedications([]); setNewMedName(''); }}
                            >
                                Cancel
                            </button>
                            <button 
                                className="btn-primary" 
                                onClick={handleComplete}
                                disabled={completing}
                                style={{ padding: '0.75rem 1.5rem' }}
                            >
                                {completing ? 'Completing...' : 'Submit & Complete'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
