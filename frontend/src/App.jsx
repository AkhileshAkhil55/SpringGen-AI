import React, { useState, useEffect } from 'react';
import PromptInput from './components/PromptInput';
import ProjectCustomizer from './components/ProjectCustomizer';
import FilePreviewer from './components/FilePreviewer';
import HistoryList from './components/HistoryList';

const API_BASE = 'http://localhost:8080/api/projects';

export default function App() {
  const [view, setView] = useState('prompt'); // 'prompt', 'customizer', 'preview'
  const [prompt, setPrompt] = useState('');
  const [metadata, setMetadata] = useState(null);
  const [files, setFiles] = useState({});
  const [history, setHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(false);
  
  // Loading States
  const [isLoading, setIsLoading] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isHistoryLoading, setIsHistoryLoading] = useState(false);

  // Load history on startup
  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    setIsHistoryLoading(true);
    try {
      const response = await fetch(`${API_BASE}/history`);
      if (response.ok) {
        const data = await response.json();
        setHistory(data);
      }
    } catch (e) {
      console.error('Failed to fetch history:', e);
    } finally {
      setIsHistoryLoading(false);
    }
  };

  const handleAnalyze = async (userPrompt) => {
    setIsLoading(true);
    setPrompt(userPrompt);
    try {
      const response = await fetch(`${API_BASE}/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: userPrompt })
      });
      if (response.ok) {
        const data = await response.json();
        setMetadata(data);
        setView('customizer');
      } else {
        alert('Failed to analyze prompt. Please try again.');
      }
    } catch (e) {
      console.error('Error during analysis:', e);
      alert('Could not connect to backend server. Make sure the Spring Boot backend is running on port 8080.');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePreview = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`${API_BASE}/preview`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(metadata)
      });
      if (response.ok) {
        const data = await response.json();
        setFiles(data);
        setView('preview');
      } else {
        alert('Failed to generate preview files.');
      }
    } catch (e) {
      console.error('Error during preview:', e);
      alert('Error connecting to backend.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDownload = async () => {
    setIsDownloading(true);
    try {
      const response = await fetch(`${API_BASE}/download`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          prompt: prompt,
          metadata: metadata
        })
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${metadata.projectName}.zip`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        
        // Reload history
        loadHistory();
      } else {
        alert('Failed to download project ZIP.');
      }
    } catch (e) {
      console.error('Error during download:', e);
      alert('Error connecting to backend.');
    } finally {
      setIsDownloading(false);
    }
  };

  const handleDownloadHistory = async (id, projectName) => {
    try {
      const response = await fetch(`${API_BASE}/history/${id}/download`);
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${projectName}.zip`);
        document.body.appendChild(link);
        link.click();
        link.remove();
      } else {
        alert('Failed to download history project.');
      }
    } catch (e) {
      console.error('Error downloading history item:', e);
    }
  };

  const handleDeleteHistory = async (id) => {
    try {
      const response = await fetch(`${API_BASE}/history/${id}`, {
        method: 'DELETE'
      });
      if (response.ok) {
        loadHistory();
      } else {
        alert('Failed to delete history item.');
      }
    } catch (e) {
      console.error('Error deleting history item:', e);
    }
  };

  return (
    <div style={styles.app}>
      {view === 'prompt' && (
        <PromptInput 
          onAnalyze={handleAnalyze} 
          isLoading={isLoading} 
          onViewHistory={() => setShowHistory(true)} 
        />
      )}

      {view === 'customizer' && (
        <ProjectCustomizer 
          metadata={metadata} 
          onUpdateMetadata={setMetadata} 
          onPreview={handlePreview} 
          onDownload={handleDownload} 
          isDownloading={isDownloading} 
        />
      )}

      {view === 'preview' && (
        <FilePreviewer 
          files={files} 
          projectName={metadata.projectName} 
          onBack={() => setView('customizer')} 
          onDownload={handleDownload} 
          isDownloading={isDownloading} 
        />
      )}

      {showHistory && (
        <HistoryList 
          history={history} 
          onClose={() => setShowHistory(false)} 
          onDownloadHistory={handleDownloadHistory} 
          onDeleteHistory={handleDeleteHistory} 
          isLoading={isHistoryLoading} 
        />
      )}
    </div>
  );
}

const styles = {
  app: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
  }
};
