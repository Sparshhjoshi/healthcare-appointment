import { useState } from 'react';
import { api } from '../services/api';

export default function SymptomModal({ appointment, onClose }) {
    const [symptoms, setSymptoms] = useState('');
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);
        
        try {
            const data = await api.submitSymptoms({
                appointmentId: appointment.id,
                symptoms: symptoms
            });
            setResult(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
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
                
                {!result ? (
                    <>
                        <h2 style={{ fontSize: '2rem', marginBottom: '1rem' }}>Pre-Visit Consultation</h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                            Please describe your symptoms in detail. Our AI will analyze them and prepare a brief for Dr. {appointment?.doctor?.lastName}.
                        </p>

                        <form onSubmit={handleSubmit}>
                            <textarea 
                                value={symptoms}
                                onChange={(e) => setSymptoms(e.target.value)}
                                placeholder="E.g., I've had a really bad headache for the past 3 days..."
                                style={{
                                    width: '100%',
                                    minHeight: '150px',
                                    padding: '1.5rem',
                                    borderRadius: '12px',
                                    backgroundColor: 'rgba(255, 255, 255, 0.05)',
                                    border: '1px solid var(--border-color)',
                                    color: 'white',
                                    fontSize: '1rem',
                                    fontFamily: 'inherit',
                                    marginBottom: '2rem',
                                    resize: 'vertical'
                                }}
                                required
                            />
                            
                            {error && (
                                <div style={{ color: '#fca5a5', marginBottom: '1rem' }}>{error}</div>
                            )}

                            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                                <button type="button" className="btn-primary" style={{ background: 'transparent', boxShadow: 'none' }} onClick={onClose}>
                                    Skip
                                </button>
                                <button type="submit" className="btn-primary" disabled={loading}>
                                    {loading ? 'Analyzing with AI...' : 'Submit Symptoms'}
                                </button>
                            </div>
                        </form>
                    </>
                ) : (
                    <>
                        <h2 style={{ fontSize: '2rem', marginBottom: '1.5rem', color: '#8b5cf6' }}>AI Analysis Complete</h2>
                        
                        <div style={{ marginBottom: '2rem' }}>
                            <h3 style={{ color: 'var(--text-secondary)', fontSize: '1rem', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '1px' }}>
                                Generated Chief Complaint
                            </h3>
                            <div style={{ 
                                backgroundColor: 'rgba(139, 92, 246, 0.1)', 
                                borderLeft: '4px solid #8b5cf6',
                                padding: '1.5rem',
                                borderRadius: '0 8px 8px 0',
                                lineHeight: '1.6',
                                whiteSpace: 'pre-wrap'
                            }}>
                                {result.chiefComplaint}
                            </div>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                            <button className="btn-primary" onClick={onClose}>
                                Return to Dashboard
                            </button>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}
