import React, { useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import ReactMarkdown from 'react-markdown';
import Sidebar from '../components/Sidebar';
import api from '../services/api';
import { 
  FileText, Plus, Pin, Star, Archive, Share2, History, MessageSquare, 
  Sparkles, Save, BookOpen, Folder, ChevronRight, X, UserPlus, Check, Trash2, Send
} from 'lucide-react';

const Notes = () => {
  const location = useLocation();
  const [activeWorkspaceId, setActiveWorkspaceId] = useState('');
  
  // Notebook & Folder structure
  const [notebooks, setNotebooks] = useState([]);
  const [selectedNotebookId, setSelectedNotebookId] = useState('');
  const [folders, setFolders] = useState([]);
  const [selectedFolderId, setSelectedFolderId] = useState('');
  
  // Note list
  const [notes, setNotes] = useState([]);
  const [selectedNoteId, setSelectedNoteId] = useState('');
  const [activeNote, setActiveNote] = useState(null);
  
  // Modals state
  const [showNotebookModal, setShowNotebookModal] = useState(false);
  const [newNotebookName, setNewNotebookName] = useState('');
  const [showFolderModal, setShowFolderModal] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [showShareModal, setShowShareModal] = useState(false);
  const [shareEmail, setShareEmail] = useState('');
  const [sharePermission, setSharePermission] = useState('READ');
  const [activeShares, setActiveShares] = useState([]);
  
  // Side drawer panels
  const [showRevisions, setShowRevisions] = useState(false);
  const [revisions, setRevisions] = useState([]);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [newCommentText, setNewCommentText] = useState('');
  const [attachments, setAttachments] = useState([]);
  
  // AI Panel
  const [showAIModal, setShowAIModal] = useState(false);
  const [aiAction, setAiAction] = useState('');
  const [aiLoading, setAiLoading] = useState(false);
  const [aiResult, setAiResult] = useState(null);

  // AI Chat Assistant Panel
  const [showAIChat, setShowAIChat] = useState(false);
  const [chatHistory, setChatHistory] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  
  // Editor status
  const [saveStatus, setSaveStatus] = useState('Saved');
  const [editorTitle, setEditorTitle] = useState('');
  const [editorContent, setEditorContent] = useState('');
  const [editorTags, setEditorTags] = useState('');
  
  // Refs
  const autoSaveTimer = useRef(null);

  useEffect(() => {
    if (activeWorkspaceId) {
      fetchNotebooks(activeWorkspaceId);
      fetchNotes(activeWorkspaceId);
    }
  }, [activeWorkspaceId]);

  useEffect(() => {
    if (selectedNotebookId) {
      fetchFolders(selectedNotebookId);
    } else {
      setFolders([]);
    }
  }, [selectedNotebookId]);

  useEffect(() => {
    if (selectedNoteId) {
      fetchNoteDetails(selectedNoteId);
    } else {
      setActiveNote(null);
    }
  }, [selectedNoteId]);

  // Handle auto-save trigger
  const handleContentChange = (val) => {
    setEditorContent(val);
    setSaveStatus('Saving...');
    
    if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current);
    autoSaveTimer.current = setTimeout(() => {
      saveNoteData(val, editorTitle);
    }, 2000); // 2 second debounce auto-save
  };

  const handleTitleChange = (val) => {
    setEditorTitle(val);
    setSaveStatus('Saving...');
    
    if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current);
    autoSaveTimer.current = setTimeout(() => {
      saveNoteData(editorContent, val);
    }, 2000);
  };

  const saveNoteData = async (content, title) => {
    if (!selectedNoteId) return;
    try {
      const response = await api.put(`/notes/${selectedNoteId}`, {
        title,
        content,
        workspaceId: activeWorkspaceId,
        notebookId: selectedNotebookId || null,
        folderId: selectedFolderId || null,
        tagNames: editorTags.split(',').map(t => t.trim()).filter(Boolean),
        isPinned: activeNote.isPinned,
        isFavorite: activeNote.isFavorite,
        isArchived: activeNote.isArchived
      });
      setSaveStatus('Saved');
      // Refresh list name without replacing active selection details
      setNotes(notes.map(n => n.id === selectedNoteId ? response.data : n));
    } catch (error) {
      setSaveStatus('Error saving');
    }
  };

  const fetchNotebooks = async (workspaceId) => {
    try {
      const response = await api.get(`/workspaces/${workspaceId}/notebooks`);
      setNotebooks(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchFolders = async (notebookId) => {
    try {
      const response = await api.get(`/notebooks/${notebookId}/folders`);
      setFolders(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchNotes = async (workspaceId) => {
    try {
      const response = await api.get(`/workspaces/${workspaceId}/notes?page=0&size=50`);
      setNotes(response.data.content);
      
      // Auto select if redirected from dashboard
      if (location.state?.selectedNoteId) {
        setSelectedNoteId(location.state.selectedNoteId);
      } else if (response.data.content.length > 0 && !selectedNoteId) {
        setSelectedNoteId(response.data.content[0].id);
      }
    } catch (error) {
      console.error(error);
    }
  };

  const fetchNoteDetails = async (id) => {
    try {
      const response = await api.get(`/notes/${id}`);
      setActiveNote(response.data);
      setEditorTitle(response.data.title);
      setEditorContent(response.data.content);
      setEditorTags(response.data.tags.map(t => t.name).join(', '));
      setChatHistory([]); // Reset AI chat history on note change
      
      // Fetch attachments
      const attachRes = await api.get(`/notes/${id}/attachments`);
      setAttachments(attachRes.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleCreateNote = async () => {
    try {
      const response = await api.post('/notes', {
        title: 'Untitled Note',
        content: '',
        workspaceId: activeWorkspaceId,
        notebookId: selectedNotebookId || null,
        folderId: selectedFolderId || null
      });
      setNotes([response.data, ...notes]);
      setSelectedNoteId(response.data.id);
    } catch (error) {
      console.error(error);
    }
  };

  const handleCreateNotebook = async (e) => {
    e.preventDefault();
    if (!newNotebookName.trim()) return;
    try {
      const res = await api.post('/notebooks', {
        name: newNotebookName,
        workspaceId: activeWorkspaceId
      });
      setNotebooks([...notebooks, res.data]);
      setSelectedNotebookId(res.data.id);
      setNewNotebookName('');
      setShowNotebookModal(false);
    } catch (error) {
      console.error(error);
    }
  };

  const handleCreateFolder = async (e) => {
    e.preventDefault();
    if (!newFolderName.trim()) return;
    try {
      const res = await api.post('/folders', {
        name: newFolderName,
        notebookId: selectedNotebookId
      });
      setFolders([...folders, res.data]);
      setSelectedFolderId(res.data.id);
      setNewFolderName('');
      setShowFolderModal(false);
    } catch (error) {
      console.error(error);
    }
  };

  const handleTogglePin = async () => {
    if (!selectedNoteId) return;
    try {
      const response = await api.put(`/notes/${selectedNoteId}/pin`);
      setActiveNote(response.data);
      setNotes(notes.map(n => n.id === selectedNoteId ? response.data : n));
    } catch (error) {
      console.error(error);
    }
  };

  const handleToggleFavorite = async () => {
    if (!selectedNoteId) return;
    try {
      const response = await api.put(`/notes/${selectedNoteId}/favorite`);
      setActiveNote(response.data);
      setNotes(notes.map(n => n.id === selectedNoteId ? response.data : n));
    } catch (error) {
      console.error(error);
    }
  };

  const handleArchiveNote = async () => {
    if (!selectedNoteId) return;
    try {
      await api.put(`/notes/${selectedNoteId}/archive`);
      setNotes(notes.filter(n => n.id !== selectedNoteId));
      setSelectedNoteId('');
    } catch (error) {
      console.error(error);
    }
  };

  // Revision triggers
  const triggerRevisions = async () => {
    setShowRevisions(true);
    setShowComments(false);
    setShowAIChat(false);
    try {
      const response = await api.get(`/notes/${selectedNoteId}/revisions`);
      setRevisions(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleRollback = async (revId) => {
    try {
      const response = await api.post(`/notes/${selectedNoteId}/revisions/${revId}/rollback`);
      setActiveNote(response.data);
      setEditorTitle(response.data.title);
      setEditorContent(response.data.content);
      setShowRevisions(false);
    } catch (error) {
      console.error(error);
    }
  };

  // Comment triggers
  const triggerComments = async () => {
    setShowComments(true);
    setShowRevisions(false);
    setShowAIChat(false);
    try {
      const response = await api.get(`/notes/${selectedNoteId}/comments`);
      setComments(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newCommentText.trim()) return;
    try {
      const response = await api.post(`/notes/${selectedNoteId}/comments`, { content: newCommentText });
      setComments([...comments, response.data]);
      setNewCommentText('');
    } catch (error) {
      console.error(error);
    }
  };

  const handleDeleteComment = async (cId) => {
    try {
      await api.delete(`/notes/${selectedNoteId}/comments/${cId}`);
      setComments(comments.filter(c => c.id !== cId));
    } catch (error) {
      console.error(error);
    }
  };

  // Sharing triggers
  const triggerSharing = async () => {
    setShowShareModal(true);
    try {
      const response = await api.get(`/notes/${selectedNoteId}/shares`);
      setActiveShares(response.data);
    } catch (error) {
      console.error(error);
    }
  };

  const handleShareNote = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post(`/notes/${selectedNoteId}/shares`, {
        email: shareEmail,
        permission: sharePermission
      });
      setActiveShares([...activeShares, response.data]);
      setShareEmail('');
    } catch (error) {
      alert("Failed to share. Make sure user email exists.");
    }
  };

  const handleRevokeShare = async (shareId) => {
    try {
      await api.delete(`/notes/${selectedNoteId}/shares/${shareId}`);
      setActiveShares(activeShares.filter(s => s.id !== shareId));
    } catch (error) {
      console.error(error);
    }
  };

  // Upload attachment trigger
  const handleUploadFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      const res = await api.post(`/notes/${selectedNoteId}/attachments`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setAttachments([...attachments, res.data]);
    } catch (error) {
      alert("Failed to upload file.");
    }
  };

  const handleDeleteAttachment = async (aId) => {
    try {
      await api.delete(`/notes/${selectedNoteId}/attachments/${aId}`);
      setAttachments(attachments.filter(a => a.id !== aId));
    } catch (error) {
      console.error(error);
    }
  };

  // AI layer triggers
  const handleAICall = async (action) => {
    setAiAction(action);
    setAiLoading(true);
    setAiResult(null);
    setShowAIModal(true);

    try {
      const response = await api.post(`/notes/${selectedNoteId}/ai/${action}`);
      setAiResult(response.data);
      
      // Auto-apply layout changes if Format action triggered
      if (action === 'format') {
        const updatedContent = response.data.formattedContent;
        setEditorContent(updatedContent);
        saveNoteData(updatedContent, editorTitle);
      }
    } catch (error) {
      console.error(error);
    } finally {
      setAiLoading(false);
    }
  };

  const handleSendChatMessage = async (msgText) => {
    const textToSend = msgText || chatInput;
    if (!textToSend.trim() || chatLoading) return;

    const newUserMessage = { role: 'user', content: textToSend };
    const updatedHistory = [...chatHistory, newUserMessage];
    setChatHistory(updatedHistory);
    if (!msgText) setChatInput('');
    setChatLoading(true);

    try {
      const response = await api.post(`/notes/${selectedNoteId}/ai/chat`, {
        message: textToSend,
        history: chatHistory
      });
      const assistantMessage = { role: 'assistant', content: response.data.response };
      setChatHistory([...updatedHistory, assistantMessage]);
    } catch (error) {
      console.error("AI Chat failed", error);
      setChatHistory([
        ...updatedHistory,
        { role: 'assistant', content: "⚠️ Sorry, I encountered an error. Make sure the backend server is running and configured correctly." }
      ]);
    } finally {
      setChatLoading(false);
    }
  };

  const handleWorkspaceChange = (id) => {
    setActiveWorkspaceId(id);
    setSelectedNotebookId('');
    setSelectedFolderId('');
    setSelectedNoteId('');
  };

  const filteredNotes = notes.filter(n => {
    if (selectedFolderId) return n.folderId === selectedFolderId;
    if (selectedNotebookId) return n.notebookId === selectedNotebookId;
    return true;
  });

  return (
    <div className="app-container">
      <Sidebar onWorkspaceChange={handleWorkspaceChange} />
      
      {/* Scope lists pane */}
      <div style={{
        width: '300px',
        borderRight: '1px solid var(--border-color)',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'var(--bg-secondary)',
        flexShrink: 0
      }}>
        {/* Notebook header */}
        <div style={{ padding: '1rem', borderBottom: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span className="sidebar-section-title" style={{ margin: 0 }}>Notebooks</span>
            <button onClick={() => setShowNotebookModal(true)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}>
              <Plus size={14} />
            </button>
          </div>
          <select 
            value={selectedNotebookId} 
            onChange={(e) => { setSelectedNotebookId(e.target.value); setSelectedFolderId(''); }}
            className="form-control"
            style={{ width: '100%', padding: '0.4rem' }}
          >
            <option value="">All Notebooks</option>
            {notebooks.map(nb => (
              <option key={nb.id} value={nb.id}>{nb.name}</option>
            ))}
          </select>
        </div>

        {/* Folder list */}
        {selectedNotebookId && (
          <div style={{ padding: '1rem', borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span className="sidebar-section-title" style={{ margin: 0 }}>Folders</span>
              <button onClick={() => setShowFolderModal(true)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}>
                <Plus size={14} />
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
              <button 
                onClick={() => setSelectedFolderId('')}
                className="sidebar-link"
                style={{ 
                  padding: '0.4rem 0.75rem', 
                  backgroundColor: !selectedFolderId ? 'var(--border-color)' : 'transparent',
                  color: 'var(--text-primary)',
                  justifyContent: 'flex-start'
                }}
              >
                <BookOpen size={16} />
                <span>Notebook root</span>
              </button>
              {folders.map(fd => (
                <button 
                  key={fd.id}
                  onClick={() => setSelectedFolderId(fd.id)}
                  className="sidebar-link"
                  style={{ 
                    padding: '0.4rem 0.75rem', 
                    backgroundColor: selectedFolderId === fd.id ? 'var(--border-color)' : 'transparent',
                    color: 'var(--text-primary)',
                    justifyContent: 'flex-start'
                  }}
                >
                  <Folder size={16} />
                  <span>{fd.name}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Note Cards List */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <span style={{ fontWeight: '700', fontSize: '1rem' }}>Notes ({filteredNotes.length})</span>
            <button onClick={handleCreateNote} className="btn btn-primary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }}>
              <Plus size={16} />
              <span>Note</span>
            </button>
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {filteredNotes.map(n => (
              <div 
                key={n.id}
                onClick={() => setSelectedNoteId(n.id)}
                className={`card ${selectedNoteId === n.id ? 'active' : ''}`}
                style={{ 
                  padding: '1rem', 
                  cursor: 'pointer',
                  borderColor: selectedNoteId === n.id ? 'var(--accent-primary)' : 'var(--border-color)',
                  backgroundColor: selectedNoteId === n.id ? 'var(--accent-light)' : 'var(--bg-secondary)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.25rem' }}>
                  <h4 style={{ fontSize: '0.95rem', margin: 0, textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', width: '80%' }}>
                    {n.title || 'Untitled Note'}
                  </h4>
                  <div style={{ display: 'flex', gap: '0.25rem' }}>
                    {n.pinned && <Pin size={12} style={{ color: 'var(--status-pinned)', fill: 'var(--status-pinned)' }} />}
                    {n.favorite && <Star size={12} style={{ color: 'var(--status-favorite)', fill: 'var(--status-favorite)' }} />}
                  </div>
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                  {n.content || 'Empty note content...'}
                </p>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.25rem', marginTop: '0.5rem' }}>
                  {n.tags.map(t => (
                    <span key={t.id} className="tag" style={{ backgroundColor: t.color, fontSize: '0.65rem', padding: '0.1rem 0.4rem' }}>{t.name}</span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Editor Content Area */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
        {activeNote ? (
          <>
            {/* Editor Header Toolbar */}
            <header style={{ 
              height: '70px', 
              borderBottom: '1px solid var(--border-color)', 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center', 
              padding: '0 1.5rem',
              backgroundColor: 'var(--bg-secondary)',
              flexShrink: 0
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', flex: 1 }}>
                <input 
                  type="text" 
                  value={editorTitle}
                  onChange={(e) => handleTitleChange(e.target.value)}
                  style={{ 
                    fontSize: '1.25rem', 
                    fontWeight: '700', 
                    border: 'none', 
                    background: 'transparent', 
                    outline: 'none',
                    color: 'var(--text-primary)',
                    width: '60%'
                  }}
                  placeholder="Note Title"
                />
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{saveStatus}</span>
              </div>
              
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                {/* Save status checks */}
                <button onClick={() => saveNoteData(editorContent, editorTitle)} className="btn btn-icon">
                  <Save size={18} />
                </button>
                <button onClick={handleTogglePin} className="btn btn-icon" style={{ color: activeNote.pinned ? 'var(--status-pinned)' : 'inherit' }}>
                  <Pin size={18} style={{ fill: activeNote.pinned ? 'var(--status-pinned)' : 'none' }} />
                </button>
                <button onClick={handleToggleFavorite} className="btn btn-icon" style={{ color: activeNote.favorite ? 'var(--status-favorite)' : 'inherit' }}>
                  <Star size={18} style={{ fill: activeNote.favorite ? 'var(--status-favorite)' : 'none' }} />
                </button>
                <button onClick={handleArchiveNote} className="btn btn-icon">
                  <Archive size={18} />
                </button>
                <button onClick={triggerSharing} className="btn btn-icon">
                  <Share2 size={18} />
                </button>
                <button onClick={triggerRevisions} className="btn btn-icon">
                  <History size={18} />
                </button>
                <button onClick={triggerComments} className="btn btn-icon" title="View Comments">
                  <MessageSquare size={18} />
                </button>
                <button 
                  onClick={() => { setShowAIChat(!showAIChat); setShowRevisions(false); setShowComments(false); }} 
                  className={`btn btn-icon ${showAIChat ? 'btn-ai-glow' : ''}`} 
                  style={{ color: 'var(--accent-primary)', border: '1px solid var(--accent-primary)', boxShadow: '0 0 10px rgba(99,102,241,0.1)' }}
                  title="Open AI Copilot Chat"
                >
                  <Sparkles size={18} />
                </button>
                
                {/* AI Dropdown options list */}
                <div style={{ position: 'relative' }}>
                  <button onClick={() => handleAICall('summary')} className="btn btn-primary" style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}>
                    <Sparkles size={16} />
                    <span>AI Tools</span>
                  </button>
                  <div style={{ 
                    position: 'absolute', 
                    right: 0, 
                    top: '42px', 
                    backgroundColor: 'var(--bg-secondary)', 
                    border: '1px solid var(--border-color)', 
                    borderRadius: 'var(--radius-sm)',
                    boxShadow: 'var(--shadow-lg)',
                    width: '180px',
                    display: 'none', // Managed in simple clicks or just call actions
                    zIndex: 20
                  }}>
                    {/* Handled directly in click buttons for mock simplicity */}
                  </div>
                </div>
              </div>
            </header>

            {/* Quick Tag bar */}
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', padding: '0.5rem 1.5rem', borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Tags:</span>
              <input 
                type="text" 
                value={editorTags} 
                onChange={(e) => { setEditorTags(e.target.value); setSaveStatus('Saving...'); if (autoSaveTimer.current) clearTimeout(autoSaveTimer.current); autoSaveTimer.current = setTimeout(() => saveNoteData(editorContent, editorTitle), 2000); }} 
                className="form-control" 
                style={{ border: 'none', padding: '0.2rem 0.5rem', width: '300px', fontSize: '0.85rem' }} 
                placeholder="tag1, tag2 (comma separated)" 
              />
            </div>

            {/* AI Tools quickbar */}
            <div style={{ display: 'flex', gap: '0.5rem', padding: '0.5rem 1.5rem', borderBottom: '1px solid var(--border-color)', backgroundColor: 'var(--bg-primary)' }}>
              <button onClick={() => handleAICall('summary')} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Summarize</button>
              <button onClick={() => handleAICall('format')} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Auto-format</button>
              <button onClick={() => handleAICall('suggest-tags')} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Suggest Tags</button>
              <button onClick={() => handleAICall('flashcards')} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Flashcards</button>
              <button onClick={() => handleAICall('quiz')} className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}>Quiz</button>
            </div>

            {/* Split Editor Pane Layout */}
            <div className="editor-layout" style={{ flex: 1 }}>
              {/* Write Side */}
              <div className="editor-pane">
                <textarea 
                  value={editorContent}
                  onChange={(e) => handleContentChange(e.target.value)}
                  className="editor-textarea"
                  placeholder="Write your note content in markdown format..."
                />
                
                {/* File Upload attachments dock */}
                <div style={{ padding: '1rem', borderTop: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: '700' }}>Attachments ({attachments.length})</span>
                    <label className="btn btn-secondary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', cursor: 'pointer', margin: 0 }}>
                      Add File
                      <input type="file" onChange={handleUploadFile} style={{ display: 'none' }} />
                    </label>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                    {attachments.map(att => (
                      <div key={att.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem', padding: '0.4rem', border: '1px dashed var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                        <a href={`/api/notes/${selectedNoteId}/attachments/${att.id}/download`} target="_blank" rel="noreferrer" style={{ color: 'var(--accent-primary)', textDecoration: 'none' }}>
                          {att.fileName} ({Math.round(att.fileSize/1024)} KB)
                        </a>
                        <button onClick={() => handleDeleteAttachment(att.id)} style={{ background: 'none', border: 'none', color: '#f43f5e', cursor: 'pointer' }}>
                          <Trash2 size={14} />
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Live Preview Side */}
              <div className="preview-pane">
                <div className="preview-content">
                  <ReactMarkdown>{editorContent || '# ' + editorTitle}</ReactMarkdown>
                </div>
              </div>
            </div>
          </>
        ) : (
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
            Select a note or create a new one to start writing.
          </div>
        )}
      </div>

      {/* Revisions Side Drawer */}
      {showRevisions && (
        <div style={{ width: '320px', borderLeft: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)', display: 'flex', flexDirection: 'column', padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h3>Revision History</h3>
            <button onClick={() => setShowRevisions(false)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}><X size={14} /></button>
          </div>
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {revisions.map(rev => (
              <div key={rev.id} style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                  <span>Version {rev.versionNumber}</span>
                  <span>by {rev.updatedByUsername}</span>
                </div>
                <h4 style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>{rev.title}</h4>
                <button onClick={() => handleRollback(rev.id)} className="btn btn-secondary" style={{ padding: '0.35rem 0.7rem', fontSize: '0.75rem', width: '100%' }}>
                  Rollback to here
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Comments Side Drawer */}
      {showComments && (
        <div style={{ width: '320px', borderLeft: '1px solid var(--border-color)', backgroundColor: 'var(--bg-secondary)', display: 'flex', flexDirection: 'column', padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h3>Comments</h3>
            <button onClick={() => setShowComments(false)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}><X size={14} /></button>
          </div>
          
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1rem' }}>
            {comments.map(c => (
              <div key={c.id} style={{ padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>
                  <span style={{ fontWeight: '700' }}>{c.username}</span>
                  <button onClick={() => handleDeleteComment(c.id)} style={{ background: 'none', border: 'none', color: '#f43f5e', cursor: 'pointer' }}>
                    <Trash2 size={12} />
                  </button>
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)' }}>{c.content}</p>
              </div>
            ))}
          </div>

          <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '0.5rem' }}>
            <input 
              type="text" 
              value={newCommentText} 
              onChange={(e) => setNewCommentText(e.target.value)} 
              className="form-control" 
              placeholder="Leave a comment..." 
              required 
            />
            <button type="submit" className="btn btn-icon btn-primary" style={{ width: '40px', height: '40px' }} title="Send Comment">
              <Send size={14} />
            </button>
          </form>
        </div>
      )}

      {/* AI Chat Assistant Side Drawer */}
      {showAIChat && (
        <div style={{ 
          width: '380px', 
          borderLeft: '1px solid var(--border-color)', 
          backgroundColor: 'var(--bg-secondary)', 
          display: 'flex', 
          flexDirection: 'column', 
          padding: '1.5rem',
          backdropFilter: 'var(--glass-blur)',
          WebkitBackdropFilter: 'var(--glass-blur)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
              <Sparkles size={20} style={{ color: 'var(--accent-primary)' }} />
              <span>AI Copilot</span>
            </h3>
            <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
              <button 
                onClick={() => setChatHistory([])} 
                className="btn btn-secondary" 
                style={{ padding: '0.2rem 0.5rem', fontSize: '0.7rem' }}
                title="Clear Chat History"
              >
                Clear
              </button>
              <button 
                onClick={() => setShowAIChat(false)} 
                className="btn btn-icon" 
                style={{ width: '28px', height: '28px' }}
              >
                <X size={14} />
              </button>
            </div>
          </div>
          
          {/* Scrollable messages panel */}
          <div style={{ 
            flex: 1, 
            overflowY: 'auto', 
            display: 'flex', 
            flexDirection: 'column', 
            gap: '1rem', 
            marginBottom: '1rem',
            paddingRight: '0.25rem' 
          }}>
            {chatHistory.length === 0 ? (
              <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', marginTop: '2rem' }}>
                <div style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>💡</div>
                <p>Welcome to your AI Copilot! I have full context of this note.</p>
                <p style={{ marginTop: '0.5rem', fontSize: '0.8rem' }}>Click a quick prompt chip below or type your query.</p>
              </div>
            ) : (
              chatHistory.map((msg, i) => (
                <div 
                  key={i} 
                  style={{ 
                    alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
                    maxWidth: '85%',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.25rem'
                  }}
                >
                  <div 
                    className="chat-msg-content"
                    style={{ 
                      padding: '0.75rem 1rem', 
                      borderRadius: 'var(--radius-sm)', 
                      backgroundColor: msg.role === 'user' ? 'var(--accent-primary)' : 'var(--bg-tertiary)',
                      color: msg.role === 'user' ? '#ffffff' : 'var(--text-primary)',
                      border: msg.role === 'user' ? 'none' : '1px solid var(--border-color)',
                      boxShadow: 'var(--shadow-sm)',
                      fontSize: '0.9rem',
                      lineHeight: '1.5'
                    }}
                  >
                    <ReactMarkdown>{msg.content}</ReactMarkdown>
                  </div>
                  
                  {msg.role === 'assistant' && msg.content && !msg.content.startsWith("⚠️") && (
                    <div style={{ display: 'flex', gap: '0.5rem', alignSelf: 'flex-start', marginTop: '0.2rem' }}>
                      <button 
                        onClick={() => {
                          const textarea = document.querySelector('.editor-textarea');
                          if (textarea) {
                            const start = textarea.selectionStart;
                            const end = textarea.selectionEnd;
                            const text = textarea.value;
                            const newContent = text.substring(0, start) + "\n\n" + msg.content + "\n\n" + text.substring(end);
                            handleContentChange(newContent);
                          } else {
                            const newContent = editorContent + "\n\n" + msg.content;
                            handleContentChange(newContent);
                          }
                        }}
                        style={{ background: 'none', border: 'none', color: 'var(--accent-primary)', fontSize: '0.75rem', cursor: 'pointer', padding: 0 }}
                      >
                        Insert at cursor
                      </button>
                      <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>|</span>
                      <button 
                        onClick={() => {
                          if (window.confirm("Are you sure you want to replace the entire note content with the AI response?")) {
                            handleContentChange(msg.content);
                          }
                        }}
                        style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '0.75rem', cursor: 'pointer', padding: 0 }}
                      >
                        Replace content
                      </button>
                    </div>
                  )}
                </div>
              ))
            )}
            
            {chatLoading && (
              <div style={{ alignSelf: 'flex-start', maxWidth: '80%', padding: '0.75rem 1rem', border: '1px dashed var(--accent-primary)', borderRadius: 'var(--radius-sm)', backgroundColor: 'var(--bg-tertiary)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <div style={{ width: '12px', height: '12px', border: '2px solid var(--border-color)', borderTopColor: 'var(--accent-primary)', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Thinking...</span>
              </div>
            )}
          </div>

          {/* Quick chips container */}
          {chatHistory.length === 0 && (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginBottom: '1rem' }}>
              {[
                "Summarize note",
                "Extract action items",
                "Explain main concepts",
                "Improve grammar",
                "Translate to Spanish"
              ].map((chip, idx) => (
                <button 
                  key={idx}
                  onClick={() => handleSendChatMessage(chip)}
                  className="btn btn-secondary"
                  style={{ padding: '0.35rem 0.7rem', borderRadius: '9999px', fontSize: '0.75rem', fontWeight: '500' }}
                >
                  ⚡ {chip}
                </button>
              ))}
            </div>
          )}

          {/* Input text form */}
          <form 
            onSubmit={(e) => { e.preventDefault(); handleSendChatMessage(); }} 
            style={{ display: 'flex', gap: '0.5rem', marginTop: 'auto' }}
          >
            <input 
              type="text" 
              value={chatInput} 
              onChange={(e) => setChatInput(e.target.value)} 
              className="form-control" 
              placeholder="Ask AI anything about this note..." 
              style={{ flex: 1, padding: '0.6rem 0.9rem', fontSize: '0.9rem' }}
              disabled={chatLoading}
              required 
            />
            <button 
              type="submit" 
              className="btn btn-icon btn-primary" 
              style={{ width: '40px', height: '40px' }}
              disabled={chatLoading}
              title="Send to AI"
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}

      {/* Notebook Creation Modal */}
      {showNotebookModal && (
        <div className="modal-overlay">
          <form onSubmit={handleCreateNotebook} className="modal-content">
            <h3 style={{ marginBottom: '1rem' }}>Create Notebook</h3>
            <div className="form-group">
              <label className="form-label">Notebook Name</label>
              <input type="text" value={newNotebookName} onChange={(e) => setNewNotebookName(e.target.value)} className="form-control" required />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
              <button type="button" onClick={() => setShowNotebookModal(false)} className="btn btn-secondary">Cancel</button>
              <button type="submit" className="btn btn-primary">Create</button>
            </div>
          </form>
        </div>
      )}

      {/* Folder Creation Modal */}
      {showFolderModal && (
        <div className="modal-overlay">
          <form onSubmit={handleCreateFolder} className="modal-content">
            <h3 style={{ marginBottom: '1rem' }}>Create Folder</h3>
            <div className="form-group">
              <label className="form-label">Folder Name</label>
              <input type="text" value={newFolderName} onChange={(e) => setNewFolderName(e.target.value)} className="form-control" required />
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
              <button type="button" onClick={() => setShowFolderModal(false)} className="btn btn-secondary">Cancel</button>
              <button type="submit" className="btn btn-primary">Create</button>
            </div>
          </form>
        </div>
      )}

      {/* Sharing Configuration Modal */}
      {showShareModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '600px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ margin: 0 }}>Share Note</h3>
              <button onClick={() => setShowShareModal(false)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}><X size={14} /></button>
            </div>

            <form onSubmit={handleShareNote} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem' }}>
              <div style={{ flex: 1 }}>
                <input 
                  type="email" 
                  value={shareEmail} 
                  onChange={(e) => setShareEmail(e.target.value)} 
                  className="form-control" 
                  placeholder="collaborator@email.com" 
                  required 
                />
              </div>
              <select value={sharePermission} onChange={(e) => setSharePermission(e.target.value)} className="form-control" style={{ width: '120px' }}>
                <option value="READ">Can View</option>
                <option value="EDIT">Can Edit</option>
              </select>
              <button type="submit" className="btn btn-primary">Share</button>
            </form>

            <h4 style={{ marginBottom: '0.75rem', fontSize: '0.95rem' }}>Active Collaborators</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {activeShares.map(sh => (
                <div key={sh.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.5rem 1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)' }}>
                  <div>
                    <div style={{ fontSize: '0.85rem', fontWeight: '700' }}>{sh.sharedWithUsername}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{sh.sharedWithEmail}</div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span className="tag" style={{ backgroundColor: 'var(--accent-primary)', fontSize: '0.65rem' }}>{sh.permission}</span>
                    <button onClick={() => handleRevokeShare(sh.id)} style={{ background: 'none', border: 'none', color: '#f43f5e', cursor: 'pointer' }}>
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              ))}
              {activeShares.length === 0 && <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>This note is private to you.</p>}
            </div>
          </div>
        </div>
      )}

      {/* AI Panel Modal */}
      {showAIModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '600px', width: '90%' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                <Sparkles size={20} style={{ color: 'var(--accent-primary)' }} />
                <span>AI Tools: {aiAction.toUpperCase()}</span>
              </h3>
              <button onClick={() => setShowAIModal(false)} className="btn btn-icon" style={{ width: '28px', height: '28px' }}><X size={14} /></button>
            </div>

            {aiLoading ? (
              <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                <div style={{ width: '40px', height: '40px', border: '4px solid var(--border-color)', borderTopColor: 'var(--accent-primary)', borderRadius: '50%', animation: 'spin 1s linear infinite', margin: '0 auto 1rem' }} />
                Analyzing note content and generating response...
              </div>
            ) : (
              <div style={{ maxHeight: '400px', overflowY: 'auto', padding: '0.5rem 0' }}>
                
                {/* Result summary format */}
                {aiAction === 'summary' && aiResult && (
                  <div className="preview-content">
                    <ReactMarkdown>{aiResult.summary}</ReactMarkdown>
                  </div>
                )}

                {/* Tag suggestions */}
                {aiAction === 'suggest-tags' && aiResult && (
                  <div>
                    <p style={{ marginBottom: '1rem', fontSize: '0.9rem' }}>Click a suggestion to append it to your tags:</p>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      {aiResult.map((tag, i) => (
                        <button 
                          key={i} 
                          onClick={() => {
                            const newTags = editorTags ? `${editorTags}, ${tag}` : tag;
                            setEditorTags(newTags);
                            saveNoteData(editorContent, editorTitle);
                          }}
                          className="btn btn-secondary" 
                          style={{ padding: '0.4rem 0.8rem', borderRadius: '9999px', fontSize: '0.85rem' }}
                        >
                          + {tag}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Auto format confirmation */}
                {aiAction === 'format' && aiResult && (
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#22c55e', marginBottom: '1rem', fontWeight: '700' }}>
                      <Check size={20} />
                      <span>Note formatting has been automatically polished and updated!</span>
                    </div>
                    <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>We cleaned up paragraph spacings, standardized list styles, and organized markdown outlines.</p>
                  </div>
                )}

                {/* Educational Flashcards view */}
                {aiAction === 'flashcards' && aiResult && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                    {aiResult.map((card, i) => (
                      <Flashcard key={i} card={card} />
                    ))}
                  </div>
                )}

                {/* Educational Quiz view */}
                {aiAction === 'quiz' && aiResult && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    {aiResult.map((q, idx) => (
                      <QuizQuestion key={idx} questionData={q} index={idx} />
                    ))}
                  </div>
                )}

              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

// Help helper child component for interactive flashcards
const Flashcard = ({ card }) => {
  const [flipped, setFlipped] = useState(false);
  return (
    <div 
      onClick={() => setFlipped(!flipped)}
      style={{
        perspective: '1000px',
        cursor: 'pointer',
        height: '140px'
      }}
    >
      <div style={{
        position: 'relative',
        width: '100%',
        height: '100%',
        textAlign: 'center',
        transition: 'transform 0.6s',
        transformStyle: 'preserve-3d',
        transform: flipped ? 'rotateY(180deg)' : 'none',
        borderRadius: 'var(--radius-md)',
        border: '1px solid var(--border-color)',
        backgroundColor: 'var(--bg-secondary)',
        boxShadow: 'var(--shadow-sm)'
      }}>
        {/* Front side */}
        <div style={{
          position: 'absolute',
          width: '100%',
          height: '100%',
          backfaceVisibility: 'hidden',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '1rem',
          fontWeight: '700',
          color: 'var(--text-primary)'
        }}>
          {card.front}
        </div>
        {/* Back side */}
        <div style={{
          position: 'absolute',
          width: '100%',
          height: '100%',
          backfaceVisibility: 'hidden',
          transform: 'rotateY(180deg)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: '1rem',
          backgroundColor: 'var(--accent-light)',
          color: 'var(--accent-primary)',
          fontWeight: '500',
          borderRadius: 'var(--radius-md)'
        }}>
          {card.back}
        </div>
      </div>
    </div>
  );
};

// Helper child component for solving AI Quizzes
const QuizQuestion = ({ questionData, index }) => {
  const [selectedOption, setSelectedOption] = useState('');
  const [submitted, setSubmitted] = useState(false);

  return (
    <div style={{ padding: '1rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
      <h4 style={{ marginBottom: '0.75rem', lineHeight: 1.4 }}>Q{index+1}: {questionData.question}</h4>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
        {questionData.options.map((opt, i) => {
          let bg = 'transparent';
          let border = 'var(--border-color)';
          
          if (submitted) {
            if (opt === questionData.correctAnswer) {
              bg = 'rgba(34, 197, 94, 0.15)';
              border = '#22c55e';
            } else if (opt === selectedOption) {
              bg = 'rgba(239, 68, 68, 0.15)';
              border = '#ef4444';
            }
          } else if (opt === selectedOption) {
            bg = 'var(--accent-light)';
            border = 'var(--accent-primary)';
          }

          return (
            <button 
              key={i}
              onClick={() => !submitted && setSelectedOption(opt)}
              style={{
                textAlign: 'left',
                padding: '0.6rem 1rem',
                border: `1px solid ${border}`,
                backgroundColor: bg,
                color: 'var(--text-primary)',
                borderRadius: 'var(--radius-sm)',
                cursor: submitted ? 'default' : 'pointer',
                fontFamily: 'var(--font-sans)',
                fontSize: '0.9rem',
                transition: 'all var(--transition-fast)'
              }}
            >
              {opt}
            </button>
          );
        })}
      </div>
      {!submitted && selectedOption && (
        <button 
          onClick={() => setSubmitted(true)}
          className="btn btn-primary" 
          style={{ marginTop: '1rem', padding: '0.4rem 1rem', fontSize: '0.8rem' }}
        >
          Submit Answer
        </button>
      )}
    </div>
  );
};

export default Notes;
