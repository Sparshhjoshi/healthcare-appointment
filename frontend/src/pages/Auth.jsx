import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';

export default function Auth() {
    const navigate = useNavigate();
    const [isLogin, setIsLogin] = useState(true);
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', email: '', password: ''
    });
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            let user;
            if (isLogin) {
                user = await api.login({ email: formData.email, password: formData.password });
            } else {
                user = await api.register(formData);
            }
            
            // Store user in localStorage
            localStorage.setItem('currentUser', JSON.stringify(user));
            
            // Redirect based on role
            const userRole = user.user?.role || user.role; // Handle both nested and flat structures just in case
            if (userRole === 'DOCTOR') navigate('/doctor');
            else if (userRole === 'ADMIN') navigate('/admin');
            else navigate('/patient');
            
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div className="glass-panel" style={{ padding: '3rem', width: '100%', maxWidth: '400px' }}>
                <h2 style={{ fontSize: '2rem', marginBottom: '2rem', textAlign: 'center' }}>
                    {isLogin ? 'Welcome Back' : 'Create Account'}
                </h2>

                {error && (
                    <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#fca5a5', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {!isLogin && (
                        <>
                            <input
                                type="text"
                                placeholder="First Name"
                                value={formData.firstName}
                                onChange={e => setFormData({...formData, firstName: e.target.value})}
                                required
                                style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />
                            <input
                                type="text"
                                placeholder="Last Name"
                                value={formData.lastName}
                                onChange={e => setFormData({...formData, lastName: e.target.value})}
                                required
                                style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                            />

                        </>
                    )}
                    
                    <input
                        type="email"
                        placeholder="Email Address"
                        value={formData.email}
                        onChange={e => setFormData({...formData, email: e.target.value})}
                        required
                        style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                    />
                    
                    <input
                        type="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={e => setFormData({...formData, password: e.target.value})}
                        required
                        style={{ padding: '0.75rem', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)', color: 'white' }}
                    />

                    <button type="submit" className="btn-primary" disabled={loading} style={{ marginTop: '1rem', padding: '0.75rem' }}>
                        {loading ? 'Processing...' : (isLogin ? 'Login' : 'Register')}
                    </button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '1.5rem', color: 'var(--text-secondary)' }}>
                    {isLogin ? "Don't have an account? " : "Already have an account? "}
                    <span 
                        onClick={() => setIsLogin(!isLogin)} 
                        style={{ color: '#8b5cf6', cursor: 'pointer', textDecoration: 'underline' }}
                    >
                        {isLogin ? 'Register' : 'Login'}
                    </span>
                </p>
                <p style={{ textAlign: 'center', marginTop: '1rem' }}>
                     <span onClick={() => navigate('/')} style={{ color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '0.875rem' }}>← Back to Home</span>
                </p>
            </div>
        </div>
    );
}
