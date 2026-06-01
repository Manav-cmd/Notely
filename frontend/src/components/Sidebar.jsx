import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import api from '../services/api';
import { LayoutDashboard, FileText, Search, BarChart3, User, LogOut, Sun, Moon, Plus, Info } from 'lucide-react';

const Sidebar = ({ onWorkspaceChange }) => {
  const { logout } = useAuth();
  const { darkTheme, toggleTheme } = useTheme();
  const [workspaces, setWorkspaces] = useState([]);
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newWorkspaceName, setNewWorkspaceName] = useState('');

  useEffect(() => {
    fetchWorkspaces();
  }, []);

  const fetchWorkspaces = async () => {
    try {
      const response = await api.get('/workspaces');
      setWorkspaces(response.data);
      if (response.data.length > 0) {
        const stored = localStorage.getItem('activeWorkspaceId');
        const isValid = response.data.some(ws => ws.id === stored);
        const defaultId = (stored && isValid) ? stored : response.data[0].id;
        setSelectedWorkspaceId(defaultId);
        localStorage.setItem('activeWorkspaceId', defaultId);
        if (onWorkspaceChange) onWorkspaceChange(defaultId);
      } else {
        const res = await api.post('/workspaces', { name: 'My Workspace', description: 'Default personal workspace' });
        setWorkspaces([res.data]);
        setSelectedWorkspaceId(res.data.id);
        localStorage.setItem('activeWorkspaceId', res.data.id);
        if (onWorkspaceChange) onWorkspaceChange(res.data.id);
      }
    } catch (error) {
      console.error("Failed to fetch workspaces", error);
    }
  };

  const handleWorkspaceSelect = (e) => {
    const id = e.target.value;
    setSelectedWorkspaceId(id);
    localStorage.setItem('activeWorkspaceId', id);
    if (onWorkspaceChange) onWorkspaceChange(id);
  };

  const handleCreateWorkspace = async (e) => {
    e.preventDefault();
    if (!newWorkspaceName.trim()) return;
    try {
      const response = await api.post('/workspaces', { name: newWorkspaceName });
      setWorkspaces([...workspaces, response.data]);
      setSelectedWorkspaceId(response.data.id);
      localStorage.setItem('activeWorkspaceId', response.data.id);
      setNewWorkspaceName('');
      setShowCreateModal(false);
      if (onWorkspaceChange) onWorkspaceChange(response.data.id);
    } catch (error) {
      console.error("Failed to create workspace", error);
    }
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span>📝 Notely</span>
      </div>

      <div style={{ marginBottom: '1.5rem', padding: '0 0.5rem' }}>
        <label className="form-label" style={{ color: 'var(--text-sidebar-muted)', display: 'block', marginBottom: '0.5rem' }}>Workspace</label>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <select 
            value={selectedWorkspaceId} 
            onChange={handleWorkspaceSelect}
            className="form-control"
            style={{ 
              backgroundColor: 'var(--bg-sidebar-hover)', 
              borderColor: 'rgba(255,255,255,0.1)', 
              color: 'var(--text-sidebar)', 
              flex: 1,
              padding: '0.5rem'
            }}
          >
            {workspaces.map(ws => (
              <option key={ws.id} value={ws.id}>{ws.name}</option>
            ))}
          </select>
          <button 
            onClick={() => setShowCreateModal(true)} 
            className="btn btn-icon btn-primary" 
            style={{ width: '36px', height: '36px' }}
            title="Create Workspace"
          >
            <Plus size={18} />
          </button>
        </div>
      </div>

      <nav className="sidebar-menu">
        <NavLink to="/dashboard" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <LayoutDashboard size={18} />
          <span>Dashboard</span>
        </NavLink>
        <NavLink to="/notes" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <FileText size={18} />
          <span>My Notes</span>
        </NavLink>
        <NavLink to="/search" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <Search size={18} />
          <span>Search</span>
        </NavLink>
        <NavLink to="/analytics" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <BarChart3 size={18} />
          <span>Analytics</span>
        </NavLink>
        <NavLink to="/profile" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <User size={18} />
          <span>Profile</span>
        </NavLink>
        <NavLink to="/about" className={({ isActive }) => isActive ? "sidebar-link active" : "sidebar-link"}>
          <Info size={18} />
          <span>About</span>
        </NavLink>
      </nav>

      <div style={{ marginTop: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        <button 
          onClick={toggleTheme} 
          className="sidebar-link" 
          style={{ background: 'none', border: 'none', width: '100%', textAlign: 'left' }}
        >
          {darkTheme ? <Sun size={18} /> : <Moon size={18} />}
          <span>{darkTheme ? 'Light Mode' : 'Dark Mode'}</span>
        </button>

        <button 
          onClick={logout} 
          className="sidebar-link" 
          style={{ background: 'none', border: 'none', width: '100%', textAlign: 'left', color: '#f43f5e' }}
        >
          <LogOut size={18} />
          <span>Log out</span>
        </button>
      </div>

      {showCreateModal && (
        <div className="modal-overlay">
          <form onSubmit={handleCreateWorkspace} className="modal-content">
            <h3 style={{ marginBottom: '1rem', color: 'var(--text-primary)' }}>New Workspace</h3>
            <div className="form-group">
              <label className="form-label">Name</label>
              <input 
                type="text" 
                value={newWorkspaceName} 
                onChange={(e) => setNewWorkspaceName(e.target.value)} 
                className="form-control" 
                placeholder="e.g. Work, College, Side Project" 
                required 
              />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
              <button type="button" onClick={() => setShowCreateModal(false)} className="btn btn-secondary">Cancel</button>
              <button type="submit" className="btn btn-primary">Create</button>
            </div>
          </form>
        </div>
      )}
    </aside>
  );
};

export default Sidebar;
