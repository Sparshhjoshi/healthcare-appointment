import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';

export default function AdminDashboard() {
    const navigate = useNavigate();
    const [doctors, setDoctors] = useState([]);
    const [leaves, setLeaves] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    // Form state
    const [selectedDoctor, setSelectedDoctor] = useState('');
    const [leaveDate, setLeaveDate] = useState('');
    const [reason, setReason] = useState('');
    const [doctorFormData, setDoctorFormData] = useState({
        firstName: '', lastName: '', email: '', password: '', specialization: '', workingHours: '09:00-17:00', slotDuration: 30
    });

    // Edit Doctor State
    const [editingDoctorId, setEditingDoctorId] = useState(null);
    const [editDoctorData, setEditDoctorData] = useState({
        specialization: '', workingHours: '', slotDuration: 30
    });

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [docs, lvs] = await Promise.all([
                api.getDoctors(),
                api.getLeaves()
            ]);
            setDoctors(docs);
            setLeaves(lvs);
        } catch (err) {
            console.error("Failed to load admin data", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddLeave = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        try {
            await api.addLeave({
                doctorId: selectedDoctor,
                leaveDate: leaveDate,
                reason: reason
            });
            setSuccess("Leave successfully scheduled.");
            setSelectedDoctor('');
            setLeaveDate('');
            setReason('');
            loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleCreateDoctor = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        try {
            await api.createDoctor(doctorFormData);
            setSuccess("Doctor created successfully!");
            setDoctorFormData({ firstName: '', lastName: '', email: '', password: '', specialization: '', workingHours: '09:00-17:00', slotDuration: 30 });
            loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleUpdateDoctor = async (id) => {
        setError(null);
        setSuccess(null);
        try {
            await api.updateDoctorProfile(id, editDoctorData);
            setSuccess("Doctor profile updated successfully!");
            setEditingDoctorId(null);
            loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDeleteLeave = async (id) => {
        if (!window.confirm("Are you sure you want to revoke this leave?")) return;
        try {
            await api.deleteLeave(id);
            setSuccess("Leave revoked successfully.");
            loadData();
        } catch (err) {
            setError(err.message);
        }
    };

    if (loading) return <div style={{ padding: '2rem' }}>Loading admin dashboard...</div>;

    return (
        <div style={{ padding: '3rem', maxWidth: '1200px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem' }}>
                <h1 style={{ fontSize: '2.5rem' }}>Admin Dashboard</h1>
                <button 
                    className="btn-primary" 
                    style={{ background: 'rgba(255,255,255,0.1)', padding: '0.5rem 1.5rem', boxShadow: 'none' }}
                    onClick={() => navigate('/')}
                >
                    Back Home
                </button>
            </div>

            {error && (
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5', padding: '1rem', borderRadius: '12px', marginBottom: '2rem', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                    {error}
                </div>
            )}
            
            {success && (
                <div style={{ background: 'rgba(34, 197, 94, 0.1)', color: '#86efac', padding: '1rem', borderRadius: '12px', marginBottom: '2rem', border: '1px solid rgba(34, 197, 94, 0.2)' }}>
                    {success}
                </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                <div className="glass-panel" style={{ padding: '2rem' }}>
                    <h2 style={{ fontSize: '1.25rem', marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>
                        Register New Doctor
                    </h2>
                    <form onSubmit={handleCreateDoctor} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <div style={{ display: 'flex', gap: '1rem' }}>
                            <input
                                type="text"
                                placeholder="First Name"
                                value={doctorFormData.firstName}
                                onChange={e => setDoctorFormData({...doctorFormData, firstName: e.target.value})}
                                required
                                style={{ flex: 1, padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />
                            <input
                                type="text"
                                placeholder="Last Name"
                                value={doctorFormData.lastName}
                                onChange={e => setDoctorFormData({...doctorFormData, lastName: e.target.value})}
                                required
                                style={{ flex: 1, padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />
                        </div>
                        <input
                            type="email"
                            placeholder="Email Address"
                            value={doctorFormData.email}
                            onChange={e => setDoctorFormData({...doctorFormData, email: e.target.value})}
                            required
                            style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                        />
                        <input
                            type="password"
                            placeholder="Initial Password"
                            value={doctorFormData.password}
                            onChange={e => setDoctorFormData({...doctorFormData, password: e.target.value})}
                            required
                            style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                        />
                        <input
                            type="text"
                            placeholder="Specialization (e.g. Cardiology)"
                            value={doctorFormData.specialization}
                            onChange={e => setDoctorFormData({...doctorFormData, specialization: e.target.value})}
                            required
                            style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                        />
                        <button type="submit" className="btn-primary" style={{ padding: '0.75rem' }}>
                            Create Doctor Account
                        </button>
                    </form>
                </div>

                <div className="glass-panel" style={{ padding: '2rem' }}>
                    <h2 style={{ fontSize: '1.25rem', marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>
                        Add Doctor Leave
                    </h2>
                    <form onSubmit={handleAddLeave} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Doctor</label>
                            <select 
                                value={selectedDoctor}
                                onChange={(e) => setSelectedDoctor(e.target.value)}
                                required
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            >
                                <option value="" style={{ color: 'black' }}>Select a Doctor</option>
                                {doctors.map(doc => (
                                    <option key={doc.user?.id} value={doc.user?.id} style={{ color: 'black' }}>
                                        Dr. {doc.user?.firstName} {doc.user?.lastName}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Date</label>
                            <input 
                                type="date"
                                value={leaveDate}
                                onChange={(e) => setLeaveDate(e.target.value)}
                                required
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', marginBottom: '0.5rem', color: 'var(--text-secondary)' }}>Reason (Optional)</label>
                            <input 
                                type="text"
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="E.g., Vacation, Sick Leave"
                                style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />
                        </div>

                        <button type="submit" className="btn-primary" style={{ width: '100%', padding: '0.75rem' }}>
                            Add Leave
                        </button>
                    </form>
                </div>
            </div>

            <div className="glass-panel" style={{ padding: '2rem', marginTop: '2rem' }}>
                <h2 style={{ marginBottom: '1.5rem', fontSize: '1.25rem' }}>Scheduled Leaves</h2>
                {leaves.length === 0 ? (
                    <p style={{ color: 'var(--text-secondary)' }}>No leaves scheduled.</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {leaves.map(leave => (
                            <div key={leave.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)' }}>
                                <div>
                                    <p style={{ fontWeight: '500', marginBottom: '0.25rem' }}>
                                        Dr. {leave.doctor?.firstName} {leave.doctor?.lastName}
                                    </p>
                                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                                        {leave.leaveDate} {leave.reason ? `- ${leave.reason}` : ''}
                                    </p>
                                </div>
                                <button 
                                    onClick={() => handleDeleteLeave(leave.id)}
                                    style={{ background: 'transparent', border: 'none', color: '#fca5a5', cursor: 'pointer', padding: '0.5rem' }}
                                >
                                    Revoke
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="glass-panel" style={{ padding: '2rem', marginTop: '2rem' }}>
                <h2 style={{ marginBottom: '1.5rem', fontSize: '1.25rem' }}>Manage Doctors</h2>
                {doctors.length === 0 ? (
                    <p style={{ color: 'var(--text-secondary)' }}>No doctors registered.</p>
                ) : (
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
                        {doctors.map(doc => (
                            <div key={doc.user?.id} style={{ background: 'rgba(255,255,255,0.02)', padding: '1.5rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)' }}>
                                <h3 style={{ fontSize: '1.1rem', marginBottom: '0.25rem' }}>Dr. {doc.user?.firstName} {doc.user?.lastName}</h3>
                                
                                {editingDoctorId === doc.user?.id ? (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '1rem' }}>
                                        <input
                                            type="text"
                                            value={editDoctorData.specialization}
                                            onChange={e => setEditDoctorData({...editDoctorData, specialization: e.target.value})}
                                            placeholder="Specialization"
                                            style={{ padding: '0.5rem', borderRadius: '6px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                        />
                                        <input
                                            type="text"
                                            value={editDoctorData.workingHours}
                                            onChange={e => setEditDoctorData({...editDoctorData, workingHours: e.target.value})}
                                            placeholder="Hours (e.g. 09:00-17:00)"
                                            style={{ padding: '0.5rem', borderRadius: '6px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                        />
                                        <input
                                            type="number"
                                            value={editDoctorData.slotDuration}
                                            onChange={e => setEditDoctorData({...editDoctorData, slotDuration: parseInt(e.target.value)})}
                                            placeholder="Slot Duration (mins)"
                                            style={{ padding: '0.5rem', borderRadius: '6px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                                        />
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button onClick={() => handleUpdateDoctor(doc.user?.id)} className="btn-primary" style={{ padding: '0.5rem', flex: 1, fontSize: '0.9rem' }}>Save</button>
                                            <button onClick={() => setEditingDoctorId(null)} style={{ padding: '0.5rem', flex: 1, background: 'rgba(255,255,255,0.1)', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer' }}>Cancel</button>
                                        </div>
                                    </div>
                                ) : (
                                    <div style={{ marginTop: '0.5rem' }}>
                                        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '0.25rem' }}><strong>Spec:</strong> {doc.specialization}</p>
                                        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '0.25rem' }}><strong>Hours:</strong> {doc.workingHours}</p>
                                        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1rem' }}><strong>Slot:</strong> {doc.slotDuration} mins</p>
                                        
                                        <button 
                                            className="btn-primary" 
                                            style={{ background: 'rgba(255,255,255,0.1)', padding: '0.5rem 1rem', boxShadow: 'none', fontSize: '0.9rem' }}
                                            onClick={() => {
                                                setEditingDoctorId(doc.user?.id);
                                                setEditDoctorData({
                                                    userId: doc.user?.id,
                                                    specialization: doc.specialization,
                                                    workingHours: doc.workingHours,
                                                    slotDuration: doc.slotDuration
                                                });
                                            }}
                                        >
                                            Edit Profile
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}
