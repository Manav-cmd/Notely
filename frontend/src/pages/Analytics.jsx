import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../services/api';
import { TrendingUp, Award, Clock } from 'lucide-react';

const Analytics = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeWorkspaceId, setActiveWorkspaceId] = useState('');

  useEffect(() => {
    if (activeWorkspaceId) {
      fetchStats();
    }
  }, [activeWorkspaceId]);

  const fetchStats = async () => {
    setLoading(true);
    try {
      const response = await api.get('/dashboard/stats');
      setStats(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-container">
      <Sidebar onWorkspaceChange={(id) => setActiveWorkspaceId(id)} />
      
      <main className="main-content" style={{ padding: '2rem' }}>
        <header style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Workspace Analytics</h1>
          <p style={{ color: 'var(--text-muted)' }}>Visualize note taking trends and tag coverage.</p>
        </header>

        {loading ? (
          <p style={{ color: 'var(--text-muted)' }}>Calculating workspace statistics...</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            
            <div className="card">
              <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <TrendingUp size={20} />
                <span>Note Creation Activity (Last 7 Days)</span>
              </h3>
              
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'end', 
                height: '240px', 
                padding: '1rem 2rem 0',
                borderBottom: '1px solid var(--border-color)',
                marginBottom: '1rem'
              }}>
                {stats?.activityHistory?.map((day, idx) => {
                  const maxCount = Math.max(...stats.activityHistory.map(d => d.notesCount), 1);
                  const heightPercent = Math.max((day.notesCount / maxCount) * 180, 10);
                  
                  return (
                    <div key={idx} style={{ 
                      display: 'flex', 
                      flexDirection: 'column', 
                      alignItems: 'center', 
                      flex: 1, 
                      gap: '0.5rem' 
                    }}>
                      <span style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--accent-primary)' }}>
                        {day.notesCount}
                      </span>
                      <div style={{ 
                        width: '32px', 
                        height: `${heightPercent}px`, 
                        backgroundColor: 'var(--accent-primary)', 
                        borderTopLeftRadius: '4px',
                        borderTopRightRadius: '4px',
                        backgroundImage: 'linear-gradient(to top, var(--accent-primary), #818cf8)',
                        transition: 'height 0.5s ease-out',
                        boxShadow: 'var(--shadow-sm)'
                      }} />
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                        {day.date}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div className="card">
                <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Award size={20} />
                  <span>Tag Coverage</span>
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {stats?.mostUsedTags?.map((tag, idx) => {
                    const maxTagCount = Math.max(...stats.mostUsedTags.map(t => t.count), 1);
                    const widthPercent = (tag.count / maxTagCount) * 100;
                    
                    return (
                      <div key={idx}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', marginBottom: '0.25rem' }}>
                          <span style={{ fontWeight: '600' }}>#{tag.name}</span>
                          <span style={{ color: 'var(--text-muted)' }}>{tag.count} notes</span>
                        </div>
                        <div style={{ height: '8px', width: '100%', backgroundColor: 'var(--bg-primary)', borderRadius: '4px', overflow: 'hidden' }}>
                          <div style={{ height: '100%', width: `${widthPercent}%`, backgroundColor: 'var(--accent-primary)', borderRadius: '4px' }} />
                        </div>
                      </div>
                    );
                  })}
                  {stats?.mostUsedTags?.length === 0 && (
                    <p style={{ color: 'var(--text-muted)' }}>Create notes with tags to see summaries.</p>
                  )}
                </div>
              </div>

              <div className="card" style={{ display: 'flex', flexDirection: 'column', justifycontent: 'space-between' }}>
                <div>
                  <h3 style={{ marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Clock size={20} />
                    <span>Activity Highlights</span>
                  </h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.95rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
                      <span style={{ color: 'var(--text-secondary)' }}>Weekly Target Note Pace</span>
                      <span style={{ fontWeight: '700' }}>{stats?.createdThisWeek || 0} / 5 Created</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
                      <span style={{ color: 'var(--text-secondary)' }}>Workspace Coverage</span>
                      <span style={{ fontWeight: '700' }}>{stats?.totalNotebooks || 0} Notebooks Active</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: 'var(--text-secondary)' }}>System Storage</span>
                      <span style={{ fontWeight: '700' }}>Healthy (Local Disk)</span>
                    </div>
                  </div>
                </div>
                
                <div style={{ 
                  marginTop: '2rem', 
                  padding: '1rem', 
                  backgroundColor: 'var(--accent-light)', 
                  borderRadius: 'var(--radius-sm)', 
                  fontSize: '0.85rem', 
                  color: 'var(--text-primary)' 
                }}>
                  <strong>Recommendation:</strong> Use tags more frequently! Tags allow Notely's AI to suggest references and make searching quick.
                </div>
              </div>
            </div>

          </div>
        )}
      </main>
    </div>
  );
};

export default Analytics;
