import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { FileText, Calendar, Plus, FolderKanban, Tag, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState('');
  const [recentNotes, setRecentNotes] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (activeWorkspaceId) {
      fetchDashboardData();
    }
  }, [activeWorkspaceId]);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const statsRes = await api.get('/dashboard/stats');
      setStats(statsRes.data);
      
      const notesRes = await api.get(`/workspaces/${activeWorkspaceId}/notes?page=0&size=3&sortBy=updatedAt&direction=desc`);
      setRecentNotes(notesRes.data.content);
    } catch (error) {
      console.error("Error loading dashboard metrics", error);
    } finally {
      setLoading(false);
    }
  };

  const handleWorkspaceChange = (id) => {
    setActiveWorkspaceId(id);
  };

  const handleCreateQuickNote = async () => {
    try {
      const res = await api.post('/notes', {
        title: 'Untitled Quick Note',
        content: '',
        workspaceId: activeWorkspaceId
      });
      navigate('/notes', { state: { selectedNoteId: res.data.id } });
    } catch (error) {
      console.error("Failed to create quick note", error);
    }
  };

  return (
    <div className="app-container">
      <Sidebar onWorkspaceChange={handleWorkspaceChange} />
      
      <main className="main-content" style={{ padding: '2rem' }}>
        <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Hello, {user?.username || 'User'}!</h1>
            <p style={{ color: 'var(--text-muted)' }}>Here is what's happening with your knowledge system today.</p>
          </div>
          <button onClick={handleCreateQuickNote} className="btn btn-primary">
            <Plus size={18} />
            <span>Quick Note</span>
          </button>
        </header>

        {loading ? (
          <div style={{ color: 'var(--text-muted)' }}>Loading stats...</div>
        ) : (
          <>
            <div className="dashboard-grid">
              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Total Notes</span>
                  <FileText size={20} style={{ color: 'var(--accent-primary)' }} />
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '700' }}>{stats?.totalNotes || 0}</div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Created This Week</span>
                  <Calendar size={20} style={{ color: '#10b981' }} />
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '700' }}>{stats?.createdThisWeek || 0}</div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Notebooks</span>
                  <FolderKanban size={20} style={{ color: '#ef4444' }} />
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '700' }}>{stats?.totalNotebooks || 0}</div>
              </div>

              <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <span style={{ color: 'var(--text-muted)', fontWeight: '600' }}>Workspaces</span>
                  <FolderKanban size={20} style={{ color: '#8b5cf6' }} />
                </div>
                <div style={{ fontSize: '2rem', fontWeight: '700' }}>{stats?.totalWorkspaces || 0}</div>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
              <div className="card" style={{ padding: '1.5rem' }}>
                <h3 style={{ marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <FileText size={20} />
                  <span>Recent Notes</span>
                </h3>
                {recentNotes.length === 0 ? (
                  <div style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem' }}>
                    No notes in this workspace. Click "Quick Note" above to write your first note!
                  </div>
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {recentNotes.map(note => (
                      <div 
                        key={note.id} 
                        onClick={() => navigate('/notes', { state: { selectedNoteId: note.id } })}
                        style={{ 
                          padding: '1rem', 
                          border: '1px solid var(--border-color)', 
                          borderRadius: 'var(--radius-sm)', 
                          cursor: 'pointer',
                          transition: 'background var(--transition-fast)'
                        }}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'var(--accent-light)'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                      >
                        <h4 style={{ marginBottom: '0.25rem', color: 'var(--text-primary)' }}>{note.title}</h4>
                        <p style={{ 
                          fontSize: '0.85rem', 
                          color: 'var(--text-muted)', 
                          overflow: 'hidden', 
                          textOverflow: 'ellipsis', 
                          whiteSpace: 'nowrap' 
                        }}>
                          {note.content || 'Empty note content...'}
                        </p>
                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                          {note.tags.map(t => (
                            <span key={t.id} className="tag" style={{ backgroundColor: t.color }}>{t.name}</span>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                <div className="card" style={{ borderLeft: '4px solid var(--accent-primary)', background: 'var(--accent-light)' }}>
                  <h3 style={{ marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--accent-primary)' }}>
                    <Sparkles size={18} />
                    <span>AI Productivity Insight</span>
                  </h3>
                  <p style={{ fontSize: '0.9rem', color: 'var(--text-primary)', lineHeight: 1.6 }}>
                    {stats?.productivityInsight || 'Generating active tips for your notes system...'}
                  </p>
                </div>

                <div className="card">
                  <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Tag size={20} />
                    <span>Most Used Tags</span>
                  </h3>
                  {stats?.mostUsedTags?.length === 0 ? (
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>Tags will appear here once you tag notes.</p>
                  ) : (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                      {stats?.mostUsedTags?.map((t, idx) => (
                        <div 
                          key={idx} 
                          style={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '0.5rem', 
                            padding: '0.4rem 0.8rem', 
                            backgroundColor: 'var(--bg-primary)', 
                            border: '1px solid var(--border-color)', 
                            borderRadius: '9999px',
                            fontSize: '0.85rem' 
                          }}
                        >
                          <span style={{ fontWeight: '700', color: 'var(--accent-primary)' }}>#{t.name}</span>
                          <span style={{ color: 'var(--text-muted)' }}>({t.count})</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
};

export default Dashboard;
