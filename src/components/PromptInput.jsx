import React, { useState } from 'react';
import { Terminal, Send, History, Sparkles, BookOpen, ShoppingBag, CheckSquare } from 'lucide-react';

const SUGGESTIONS = [
  {
    title: 'Task Management',
    desc: 'Fields for title, description, due date, status, priority. Relies on MySQL.',
    prompt: 'Generate a Task Management system with fields: title (String), description (String), dueDate (LocalDate), status (String), priority (int). Use MySQL and include validations forNotBlank and Size.',
    icon: CheckSquare
  },
  {
    title: 'E-Commerce Backend',
    desc: 'Fields for name, SKU, price, stock, category. Uses MongoDB.',
    prompt: 'Create an E-Commerce backend for Products. Fields: name (String), sku (String), price (double), stock (int), category (String). Use MongoDB, Lombok, Security, and Swagger.',
    icon: ShoppingBag
  },
  {
    title: 'Library System',
    desc: 'Fields for title, author, ISBN, publish date. PostgreSQL.',
    prompt: 'Create a Library Catalog application for Books. Fields: title (String), author (String), isbn (String), publishedDate (LocalDate), available (boolean). Use PostgreSQL database and enable Swagger.',
    icon: BookOpen
  }
];

export default function PromptInput({ onAnalyze, isLoading, onViewHistory }) {
  const [prompt, setPrompt] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (prompt.trim()) {
      onAnalyze(prompt);
    }
  };

  const selectSuggestion = (sPrompt) => {
    setPrompt(sPrompt);
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div style={styles.logoArea}>
          <div style={styles.logoIcon}>
            <Sparkles size={24} color="#00f2fe" />
          </div>
          <div>
            <h1 style={styles.title}>SpringGen <span className="gradient-text">AI</span></h1>
            <p style={styles.subtitle}>Transform natural language into fully-featured Spring Boot microservices instantly.</p>
          </div>
        </div>

        <button className="btn btn-secondary" onClick={onViewHistory} style={styles.historyBtn}>
          <History size={18} />
          View History
        </button>
      </header>

      <div className="glass-card" style={styles.card}>
        <div style={styles.terminalHeader}>
          <Terminal size={16} color="#00f2fe" />
          <span style={styles.terminalTitle}>springgen-analyzer.sh</span>
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          <textarea
            className="form-input"
            style={styles.textarea}
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="Describe the Spring Boot application you want to generate... e.g., 'Generate a Task Management system with fields: title, dueDate, status. Use MySQL.'"
            disabled={isLoading}
          />

          <div style={styles.actionRow}>
            <span style={styles.tipText}>
              💡 Tip: Specify the database type (MySQL, PostgreSQL, MongoDB) and Lombok/Security needs.
            </span>
            <button 
              type="submit" 
              className="btn btn-primary" 
              disabled={isLoading || !prompt.trim()}
              style={styles.submitBtn}
            >
              {isLoading ? (
                <>
                  <div style={styles.spinner} className="spinner"></div>
                  Analyzing prompt...
                </>
              ) : (
                <>
                  Generate Core
                  <Send size={16} />
                </>
              )}
            </button>
          </div>
        </form>
      </div>

      <div style={styles.suggestionsContainer}>
        <h3 style={styles.suggestionHeader}>Or start from a preset template:</h3>
        <div style={styles.suggestionsGrid}>
          {SUGGESTIONS.map((s, idx) => {
            const Icon = s.icon;
            return (
              <div 
                key={idx} 
                className="glass-card" 
                style={styles.suggestionCard}
                onClick={() => selectSuggestion(s.prompt)}
              >
                <div style={styles.sCardHeader}>
                  <div style={styles.sIconWrapper}>
                    <Icon size={20} color="#00f2fe" />
                  </div>
                  <h4 style={styles.sTitle}>{s.title}</h4>
                </div>
                <p style={styles.sDesc}>{s.desc}</p>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '960px',
    margin: '40px auto',
    padding: '0 20px',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '32px',
  },
  logoArea: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  logoIcon: {
    background: 'rgba(0, 242, 254, 0.1)',
    border: '1px solid rgba(0, 242, 254, 0.2)',
    padding: '12px',
    borderRadius: '16px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: '28px',
    fontWeight: '800',
    color: '#fff',
    lineHeight: 1.2,
  },
  subtitle: {
    fontSize: '14px',
    color: 'var(--text-secondary)',
    marginTop: '4px',
  },
  historyBtn: {
    padding: '10px 18px',
  },
  card: {
    padding: '0',
    overflow: 'hidden',
    border: '1px solid rgba(255, 255, 255, 0.08)',
  },
  terminalHeader: {
    background: 'rgba(0, 0, 0, 0.3)',
    borderBottom: '1px solid var(--glass-border)',
    padding: '12px 20px',
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  terminalTitle: {
    fontFamily: 'var(--font-mono)',
    fontSize: '13px',
    color: 'var(--text-secondary)',
  },
  form: {
    padding: '24px',
  },
  textarea: {
    height: '140px',
    resize: 'none',
    fontSize: '16px',
    lineHeight: '1.5',
    fontFamily: 'var(--font-sans)',
    border: 'none',
    background: 'transparent',
    padding: '0',
    marginBottom: '20px',
  },
  actionRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTop: '1px solid var(--glass-border)',
    paddingTop: '20px',
    flexWrap: 'wrap',
    gap: '16px',
  },
  tipText: {
    fontSize: '13px',
    color: 'var(--text-secondary)',
  },
  submitBtn: {
    minWidth: '160px',
  },
  suggestionsContainer: {
    marginTop: '48px',
  },
  suggestionHeader: {
    fontSize: '16px',
    color: 'var(--text-secondary)',
    marginBottom: '16px',
    fontWeight: '600',
  },
  suggestionsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: '18px',
  },
  suggestionCard: {
    padding: '20px',
    cursor: 'pointer',
    background: 'rgba(255, 255, 255, 0.02)',
  },
  sCardHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginBottom: '10px',
  },
  sIconWrapper: {
    background: 'rgba(0, 242, 254, 0.05)',
    padding: '8px',
    borderRadius: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#fff',
  },
  sDesc: {
    fontSize: '13px',
    color: 'var(--text-secondary)',
    lineHeight: '1.4',
  },
  spinner: {
    width: '18px',
    height: '18px',
    border: '2px solid rgba(255, 255, 255, 0.2)',
    borderTopColor: '#000',
    borderRadius: '50%',
  }
};
