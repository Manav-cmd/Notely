import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import api from '../services/api';
import { Search as SearchIcon, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Search = () => {
  const [activeWorkspaceId, setActiveWorkspaceId] = useState('');
  const [query, setQuery] = useState('');
  const [tags, setTags] = useState([]);
  const [selectedTagId, setSelectedTagId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    if (activeWorkspaceId) {
      fetchTags();
      handleSearch();
    }
  }, [activeWorkspaceId]);

  const fetchTags = async () => {
    try {
      const res = await api.get(`/workspaces/${activeWorkspaceId}/notes?page=0&size=100`);
      const allTags = [];
      res.data.content.forEach(n => {
        n.tags.forEach(t => {
          if (!allTags.some(existing => existing.id === t.id)) {
            allTags.push(t);
          }
        });
      });
      setTags(allTags);
    } catch (error) {
      console.error(error);
    }
  };

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    if (!activeWorkspaceId) return;
    setLoading(true);
    try {
      let url = `/search?workspaceId=${activeWorkspaceId}`;
      if (query.trim()) url += `&query=${encodeURIComponent(query.trim())}`;
      if (selectedTagId) url += `&tagId=${selectedTagId}`;
      
      if (startDate) {
        url += `&startDate=${encodeURIComponent(startDate + 'T00:00:00')}`;
      }
      if (endDate) {
        url += `&endDate=${encodeURIComponent(endDate + 'T23:59:59')}`;
      }

      const response = await api.get(url);
      setResults(response.data);
    } catch (error) {
      console.error("Search failed", error);
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setQuery('');
    setSelectedTagId('');
    setStartDate('');
    setEndDate('');
    setResults([]);
  };

  return (
    <div className="app-container">
      <Sidebar onWorkspaceChange={(id) => setActiveWorkspaceId(id)} />
      
      <main className="main-content" style={{ padding: '2rem' }}>
        <header style={{ marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Advanced Search</h1>
          <p style={{ color: 'var(--text-muted)' }}>Find notes by title, tag, or creation date range.</p>
        </header>

        <div className="card" style={{ marginBottom: '2rem' }}>
          <form onSubmit={handleSearch} style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: '1rem', alignItems: 'end' }}>
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Keyword Query</label>
              <div style={{ position: 'relative' }}>
                <SearchIcon size={18} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-muted)' }} />
                <input 
                  type="text" 
                  value={query} 
                  onChange={(e) => setQuery(e.target.value)} 
                  className="form-control" 
                  style={{ paddingLeft: '2.5rem', width: '100%' }} 
                  placeholder="Search note titles or content..." 
                />
              </div>
            </div>

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Filter by Tag</label>
              <select 
                value={selectedTagId} 
                onChange={(e) => setSelectedTagId(e.target.value)}
                className="form-control"
                style={{ width: '100%' }}
              >
                <option value="">All Tags</option>
                {tags.map(t => (
                  <option key={t.id} value={t.id}>#{t.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">From Date</label>
              <input 
                type="date" 
                value={startDate} 
                onChange={(e) => setStartDate(e.target.value)} 
                className="form-control" 
                style={{ width: '100%' }} 
              />
            </div>

            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">To Date</label>
              <input 
                type="date" 
                value={endDate} 
                onChange={(e) => setEndDate(e.target.value)} 
                className="form-control" 
                style={{ width: '100%' }} 
              />
            </div>
          </form>
          
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.25rem' }}>
            <button onClick={handleClear} type="button" className="btn btn-secondary">Clear Filters</button>
            <button onClick={() => handleSearch()} type="button" className="btn btn-primary">Search Notes</button>
          </div>
        </div>

        <div>
          <h3 style={{ marginBottom: '1rem' }}>Search Results ({results.length})</h3>
          {loading ? (
            <p style={{ color: 'var(--text-muted)' }}>Searching database...</p>
          ) : results.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)', border: '1px dashed var(--border-color)', borderRadius: 'var(--radius-md)' }}>
              No notes match your filters. Try widening your search queries.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              {results.map(note => (
                <div 
                  key={note.id}
                  onClick={() => navigate('/notes', { state: { selectedNoteId: note.id } })}
                  className="card"
                  style={{ cursor: 'pointer', display: 'flex', flexDirection: 'column' }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                    <h4 style={{ color: 'var(--text-primary)', fontSize: '1.1rem' }}>{note.title}</h4>
                    <ArrowRight size={16} style={{ color: 'var(--text-muted)' }} />
                  </div>
                  <p style={{ 
                    fontSize: '0.9rem', 
                    color: 'var(--text-secondary)', 
                    flex: 1, 
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    display: '-webkit-box',
                    WebkitLineClamp: 3,
                    WebkitBoxOrient: 'vertical',
                    marginBottom: '1rem'
                  }}>
                    {note.content || 'Empty note content...'}
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: 'auto' }}>
                    {note.tags.map(t => (
                      <span key={t.id} className="tag" style={{ backgroundColor: t.color }}>{t.name}</span>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default Search;
