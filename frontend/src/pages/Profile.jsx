import React, { useState } from 'react';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { User, Shield, Key } from 'lucide-react';

const Profile = () => {
  const { user } = useAuth();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordMsg, setPasswordMsg] = useState('');

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setPasswordMsg('');
    if (password !== confirmPassword) {
      setPasswordMsg('Passwords do not match');
      return;
    }
    try {
      await api.post('/auth/reset-password', {
        email: user.email,
        password: password
      });
      setPasswordMsg('Password changed successfully!');
      setPassword('');
      setConfirmPassword('');
    } catch (error) {
      setPasswordMsg('Failed to change password.');
    }
  };

  return (
    <div className="app-container">
      <Sidebar />
      
      <main className="main-content" style={{ padding: '2rem' }}>
        <header style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>User Profile</h1>
          <p style={{ color: 'var(--text-muted)' }}>Manage your account settings and credentials.</p>
        </header>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
          <div className="card">
            <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <User size={20} />
              <span>Account Details</span>
            </h3>
            
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input type="text" className="form-control" value={user?.email || ''} disabled style={{ opacity: 0.7 }} />
            </div>

            <div className="form-group">
              <label className="form-label">Username</label>
              <input type="text" className="form-control" value={user?.username || ''} disabled style={{ opacity: 0.7 }} />
            </div>

            <div className="form-group">
              <label className="form-label">Account Role</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem', fontWeight: '700', color: 'var(--accent-primary)' }}>
                <Shield size={16} />
                <span>{user?.role?.replace('ROLE_', '') || 'USER'}</span>
              </div>
            </div>
          </div>

          <div className="card">
            <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Key size={20} />
              <span>Change Password</span>
            </h3>

            {passwordMsg && (
              <div style={{ 
                padding: '0.75rem 1rem', 
                backgroundColor: passwordMsg.includes('successfully') ? 'rgba(34, 197, 94, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                border: `1px solid ${passwordMsg.includes('successfully') ? 'rgba(34, 197, 94, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`,
                color: passwordMsg.includes('successfully') ? '#22c55e' : '#ef4444',
                borderRadius: 'var(--radius-sm)',
                marginBottom: '1rem',
                fontSize: '0.85rem'
              }}>
                {passwordMsg}
              </div>
            )}

            <form onSubmit={handleChangePassword}>
              <div className="form-group">
                <label className="form-label">New Password</label>
                <input 
                  type="password" 
                  value={password} 
                  onChange={(e) => setPassword(e.target.value)} 
                  className="form-control" 
                  placeholder="••••••••" 
                  required 
                  minLength={6}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Confirm New Password</label>
                <input 
                  type="password" 
                  value={confirmPassword} 
                  onChange={(e) => setConfirmPassword(e.target.value)} 
                  className="form-control" 
                  placeholder="••••••••" 
                  required 
                  minLength={6}
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
                Update Password
              </button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Profile;
