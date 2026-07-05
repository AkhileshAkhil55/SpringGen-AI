import React from 'react';
import { X, Calendar, Download, Trash2, FolderGit2, AlertCircle } from 'lucide-react';

export default function HistoryList({ history, onClose, onDownloadHistory, onDeleteHistory, isLoading }) {
  const formatDate = (dateStr) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateStr;
    }
  };

  return (
    <div style={styles.overlay}>
      <div className="glass" style={styles.drawer}>
        <header style={styles.header}>
          <div style={styles.titleGroup}>
            <FolderGit2 size={20} color="#00f2fe" />
            <h2 style={styles.title}>Generation History</h2>
          </div>
          <button className="btn btn-secondary btn-icon" onClick={onClose} style={styles.closeBtn}>
            <X size={18} />
          </button>
        </header>

        <div style={styles.body}>
          {isLoading ? (
            <div style={styles.loadingContainer}>
              <div className="spinner" style={styles.spinner}></div>
              <span>Fetching history from database...</span>
            </div>
          ) : history.length === 0 ? (
            <div style={styles.emptyContainer}>
              <AlertCircle size={32} color="var(--text-muted)" />
              <h3 style={styles.emptyTitle}>No projects generated yet</h3>
              <p style={styles.emptyText}>Go back to the generator, write a prompt, and download your first application.</p>
            </div>
          ) : (
            <div style={styles.list}>
              {history.map((item) => (
                <div key={item.id} className="glass-card" style={styles.card}>
                  <div style={styles.cardHeader}>
                    <h3 style={styles.projectTitle}>{item.projectName}</h3>
                    <span style={styles.dateLabel}>
                      <Calendar size={12} />
                      {formatDate(item.createdAt)}
                    </span>
                  </div>

                  <p style={styles.promptText}>
                    <strong>Prompt:</strong> "{item.prompt}"
                  </p>

                  {item.structuredJson && (
                    <div style={styles.metaRow}>
                      <span style={styles.metaBadge}>
                        DB: {JSON.parse(item.structuredJson).database || 'mysql'}
                      </span>
                      <span style={styles.metaBadge}>
                        Java: {JSON.parse(item.structuredJson).javaVersion || '21'}
                      </span>
                      <span style={styles.metaBadge}>
                        Entity: {JSON.parse(item.structuredJson).entity || 'Item'}
                      </span>
                    </div>
                  )}

                  <div style={styles.cardActions}>
                    <button 
                      className="btn btn-primary" 
                      onClick={() => onDownloadHistory(item.id, item.projectName)}
                      style={styles.actionBtn}
                    >
                      <Download size={14} />
                      Download ZIP
                    </button>
                    <button 
                      className="btn btn-danger" 
                      onClick={() => {
                        if (confirm(`Are you sure you want to delete ${item.projectName} from history?`)) {
                          onDeleteHistory(item.id);
                        }
                      }}
                      style={styles.deleteBtn}
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    background: 'rgba(0, 0, 0, 0.65)',
    backdropFilter: 'blur(4px)',
    zIndex: 1000,
    display: 'flex',
    justifyContent: 'flex-end',
  },
  drawer: {
    width: '460px',
    height: '100%',
    background: 'var(--bg-secondary)',
    borderLeft: '1px solid var(--glass-border)',
    display: 'flex',
    flexDirection: 'column',
    animation: 'slideIn 0.3s ease',
  },
  header: {
    padding: '20px 24px',
    borderBottom: '1px solid var(--glass-border)',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  titleGroup: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  title: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#fff',
  },
  closeBtn: {
    width: '36px',
    height: '36px',
  },
  body: {
    flex: 1,
    overflowY: 'auto',
    padding: '24px',
  },
  loadingContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '200px',
    gap: '16px',
    color: 'var(--text-secondary)',
  },
  spinner: {
    width: '24px',
    height: '24px',
    border: '2px solid rgba(255, 255, 255, 0.2)',
    borderTopColor: 'var(--accent-cyan)',
    borderRadius: '50%',
  },
  emptyContainer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '300px',
    textAlign: 'center',
    padding: '20px',
  },
  emptyTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#fff',
    marginTop: '16px',
    marginBottom: '8px',
  },
  emptyText: {
    fontSize: '13px',
    color: 'var(--text-secondary)',
    lineHeight: '1.5',
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  card: {
    padding: '20px',
    background: 'rgba(255, 255, 255, 0.02)',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '10px',
  },
  projectTitle: {
    fontSize: '16px',
    fontWeight: '700',
    color: '#fff',
  },
  dateLabel: {
    fontSize: '11px',
    color: 'var(--text-secondary)',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    background: 'var(--bg-tertiary)',
    padding: '4px 8px',
    borderRadius: '12px',
  },
  promptText: {
    fontSize: '13px',
    color: 'var(--text-secondary)',
    lineHeight: '1.4',
    marginBottom: '12px',
    fontStyle: 'italic',
  },
  metaRow: {
    display: 'flex',
    gap: '8px',
    marginBottom: '16px',
  },
  metaBadge: {
    fontSize: '11px',
    background: 'rgba(0, 242, 254, 0.06)',
    border: '1px solid rgba(0, 242, 254, 0.15)',
    color: 'var(--accent-cyan)',
    padding: '2px 8px',
    borderRadius: '4px',
    textTransform: 'uppercase',
  },
  cardActions: {
    display: 'flex',
    gap: '10px',
  },
  actionBtn: {
    flex: 1,
    padding: '8px 16px',
    fontSize: '13px',
  },
  deleteBtn: {
    padding: '8px 12px',
  }
};
