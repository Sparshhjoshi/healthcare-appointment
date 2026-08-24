import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import './index.css'
import PatientDashboard from './pages/PatientDashboard';
import DoctorDashboard from './pages/DoctorDashboard';
import AdminDashboard from './pages/AdminDashboard';
import Auth from './pages/Auth';

function LandingPage() {
  const navigate = useNavigate();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', padding: '2rem' }}>
      <div className="glass-panel" style={{ padding: '4rem', maxWidth: '650px', textAlign: 'center' }}>
        <h1 style={{ fontSize: '3.5rem', marginBottom: '1.5rem', lineHeight: '1.1' }}>
          Healthcare Manager
        </h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.25rem', marginBottom: '3rem', lineHeight: '1.6' }}>
          An AI-powered, enterprise-grade appointment platform. Welcome to the future of healthcare.
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
          <button className="btn-primary" onClick={() => navigate('/auth')}>
            Patient Portal
          </button>
          <button className="btn-primary" onClick={() => navigate('/auth')} style={{ 
            background: 'rgba(255, 255, 255, 0.05)', 
            border: '1px solid var(--border-color)', 
            boxShadow: 'none' 
          }}>
            Doctor Portal
          </button>
        </div>
        <button 
            onClick={() => navigate('/auth')}
            style={{ 
                background: 'transparent', border: 'none', color: 'var(--text-secondary)', 
                textDecoration: 'underline', marginTop: '2rem', cursor: 'pointer'
            }}
        >
            Admin / Staff Access
        </button>
      </div>
    </div>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/auth" element={<Auth />} />
        <Route path="/patient" element={<PatientDashboard />} />
        <Route path="/doctor" element={<DoctorDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
      </Routes>
    </Router>
  )
}

export default App
