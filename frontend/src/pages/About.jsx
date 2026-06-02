import React from 'react';
import Sidebar from '../components/Sidebar';
import { Sparkles, FileText, Share2, History, Shield, Database, Github } from 'lucide-react';

const About = () => {
  return (
    <div className="app-container">
      <Sidebar />
      
      <main className="main-content" style={{ padding: '2.5rem' }}>
        {/* Hero Header */}
        <header style={{ textAlign: 'center', marginBottom: '3.5rem', marginTop: '1rem' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '80px', height: '80px', borderRadius: '50%', background: 'var(--accent-light)', color: 'var(--accent-primary)', marginBottom: '1.5rem', boxShadow: '0 0 20px rgba(99,102,241,0.25)', animation: 'pulseGlow 3s infinite' }}>
            <Sparkles size={40} />
          </div>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', background: 'var(--accent-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            About Notely
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto' }}>
            A high-fidelity Smart Notes &amp; Knowledge Management System built with enterprise standards and premium aesthetics.
          </p>
        </header>

        {/* Feature Grid */}
        <section>
          <h2 style={{ fontSize: '1.75rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
            Core Capabilities
          </h2>
          
          <div className="about-features-grid">
            <div className="card feature-glow-card">
              <div style={{ color: 'var(--accent-primary)', marginBottom: '1rem' }}>
                <Sparkles size={28} />
              </div>
              <h3 style={{ marginBottom: '0.75rem' }}>Local AI Copilot</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Generate instant summaries, suggest note tags, format raw layouts, and build flipped flashcard games or quizzes to test your memory.
              </p>
            </div>

            <div className="card feature-glow-card">
              <div style={{ color: 'var(--accent-primary)', marginBottom: '1rem' }}>
                <History size={28} />
              </div>
              <h3 style={{ marginBottom: '0.75rem' }}>Revision Version Control</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Tracks every content modification. Review past versions in a side-by-side history panel and roll back notes to any saved point with one click.
              </p>
            </div>

            <div className="card feature-glow-card">
              <div style={{ color: 'var(--accent-primary)', marginBottom: '1rem' }}>
                <Share2 size={28} />
              </div>
              <h3 style={{ marginBottom: '0.75rem' }}>Collaborative Sharing</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Share notebooks with collaborators. Designate Read-Only or Edit privileges, revoke permissions instantly, and converse in comments threads.
              </p>
            </div>

            <div className="card feature-glow-card">
              <div style={{ color: 'var(--accent-primary)', marginBottom: '1rem' }}>
                <FileText size={28} />
              </div>
              <h3 style={{ marginBottom: '0.75rem' }}>Rich Markdown &amp; Uploads</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', lineHeight: 1.6 }}>
                Format your thoughts in standard Markdown syntax. Add file attachments (PDFs, images, documents) stored securely on the local server.
              </p>
            </div>
          </div>
        </section>

        {/* Stack details */}
        <section style={{ marginTop: '2rem' }}>
          <h2 style={{ fontSize: '1.75rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
            System Architecture
          </h2>
          <div className="card" style={{ padding: '2rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <div>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: 'var(--accent-primary)' }}>
                <Database size={20} />
                <span>Backend Stack</span>
              </h3>
              <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', listStyle: 'none', fontSize: '0.95rem' }}>
                <li>🚀 **Spring Boot 3.2** (REST endpoints &amp; logic controllers)</li>
                <li>🛡️ **Spring Security** &amp; **Stateless JWT** filters</li>
                <li>💾 **Spring Data JPA** &amp; **Hibernate** ORM mapping</li>
                <li>🔌 Default fallback to in-memory **H2 DB** (autodetects PostgreSQL)</li>
              </ul>
            </div>
            <div>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: 'var(--accent-primary)' }}>
                <Shield size={20} />
                <span>Frontend Stack</span>
              </h3>
              <ul style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', listStyle: 'none', fontSize: '0.95rem' }}>
                <li>⚛️ **React 18** (Vite SPA template)</li>
                <li>🎨 **Vanilla CSS Variable Themes** (glassmorphic aesthetic &amp; dark mode)</li>
                <li>📦 **Lucide Icons** &amp; **React Markdown** parsers</li>
                <li>🐳 Containerized compilation served via reverse-proxying **Nginx**</li>
              </ul>
            </div>
          </div>
        </section>

        {/* Footer */}
        <footer style={{ marginTop: '4rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          <p>© 2026 Notely Knowledge System. Portfolio Project showcased on GitHub.</p>
        </footer>
      </main>
    </div>
  );
};

export default About;
