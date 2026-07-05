import React, { useState, useEffect } from 'react';
import { ChevronLeft, Folder, File, Download, ArrowLeft, Terminal } from 'lucide-react';

export default function FilePreviewer({ files, projectName, onBack, onDownload, isDownloading }) {
  const [selectedFile, setSelectedFile] = useState('');
  const [fileList, setFileList] = useState([]);

  useEffect(() => {
    if (files) {
      const keys = Object.keys(files).sort();
      setFileList(keys);
      if (keys.length > 0) {
        setSelectedFile(keys[0]);
      }
    }
  }, [files]);

  const getFileIconColor = (path) => {
    if (path.endsWith('.xml')) return '#ff5f56';
    if (path.endsWith('.properties')) return '#ffbd2e';
    if (path.endsWith('.java')) return '#27c93f';
    return 'var(--text-secondary)';
  };

  const getFileName = (path) => {
    const parts = path.split('/');
    return parts[parts.length - 1];
  };

  const renderCode = (code) => {
    if (!code) return '';
    // A very basic highlight for preview aesthetics
    return code
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      // keywords
      .replace(/\b(package|import|public|class|interface|extends|implements|private|final|return|new|void|throws|static|@Entity|@Table|@Id|@GeneratedValue|@Column|@RestController|@RequestMapping|@GetMapping|@PostMapping|@PutMapping|@DeleteMapping|@PathVariable|@RequestBody|@Valid|@Autowired|@Service|@Repository|@Data|@NoArgsConstructor|@AllArgsConstructor|@Builder|@Bean|@Configuration|@EnableWebSecurity|@ResponseStatus|@RestControllerAdvice|@ExceptionHandler)\b/g, '<span style="color:#ff79c6">$1</span>')
      // annotations
      .replace(/(@[A-Za-z0-9_]+)/g, '<span style="color:#f1fa8c">$1</span>')
      // strings
      .replace(/(".*?")/g, '<span style="color:#f1fa8c">$1</span>')
      // comments
      .replace(/(\/\/.*)/g, '<span style="color:#6272a4">$1</span>')
      // types
      .replace(/\b(String|Long|int|double|boolean|LocalDate|LocalDateTime|ResponseEntity|List|Map|HashMap|ArrayList|Exception|SecurityFilterChain|HttpSecurity|ObjectMapper)\b/g, '<span style="color:#8be9fd">$1</span>');
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <button className="btn btn-secondary" onClick={onBack} style={styles.backBtn}>
          <ArrowLeft size={16} />
          Back to Settings
        </button>

        <div style={styles.projectNameBadge}>
          <Terminal size={15} color="#00f2fe" />
          <span>Project: {projectName}</span>
        </div>

        <button 
          onClick={onDownload} 
          className="btn btn-primary" 
          disabled={isDownloading} 
          style={styles.downloadBtn}
        >
          {isDownloading ? (
            <div className="spinner" style={styles.spinner}></div>
          ) : (
            <Download size={16} />
          )}
          Download ZIP
        </button>
      </header>

      <div style={styles.workspace}>
        {/* File Tree Explorer (Left Panel) */}
        <div className="glass" style={styles.explorer}>
          <div style={styles.explorerTitle}>
            <Folder size={16} color="#4facfe" />
            <span>Files Explorer</span>
          </div>

          <div style={styles.treeList}>
            {fileList.map((path) => (
              <div 
                key={path} 
                onClick={() => setSelectedFile(path)}
                style={{
                  ...styles.treeItem,
                  background: selectedFile === path ? 'rgba(0, 242, 254, 0.08)' : 'transparent',
                  borderLeft: selectedFile === path ? '2px solid var(--accent-cyan)' : '2px solid transparent',
                }}
              >
                <File size={14} color={getFileIconColor(path)} />
                <span 
                  style={{
                    ...styles.treeItemText,
                    color: selectedFile === path ? '#fff' : 'var(--text-secondary)',
                    fontWeight: selectedFile === path ? '600' : '400',
                  }}
                >
                  {getFileName(path)}
                </span>
                <span style={styles.pathToolTip} title={path}>
                  ({path.split('/').slice(0, -1).join('/') || '/'})
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Code Preview Pane (Right Panel) */}
        <div className="glass" style={styles.previewPane}>
          <div style={styles.previewHeader}>
            <span style={styles.activeFilePath}>{selectedFile || 'No file selected'}</span>
          </div>

          <div style={styles.codeContainer}>
            <pre style={styles.pre}>
              <code 
                style={styles.code} 
                dangerouslySetInnerHTML={{ __html: renderCode(files[selectedFile]) }}
              />
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '1200px',
    margin: '30px auto',
    padding: '0 20px',
    height: 'calc(100vh - 120px)',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  backBtn: {
    padding: '8px 16px',
    fontSize: '14px',
  },
  projectNameBadge: {
    background: 'var(--bg-secondary)',
    border: '1px solid var(--glass-border)',
    borderRadius: '20px',
    padding: '6px 16px',
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '14px',
    fontWeight: '600',
  },
  downloadBtn: {
    padding: '8px 20px',
    fontSize: '14px',
  },
  workspace: {
    display: 'flex',
    gap: '20px',
    flex: 1,
    minHeight: '0', // important for nested flex scroll
  },
  explorer: {
    width: '320px',
    borderRadius: '12px',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
  },
  explorerTitle: {
    background: 'rgba(0, 0, 0, 0.2)',
    padding: '14px 18px',
    borderBottom: '1px solid var(--glass-border)',
    fontSize: '14px',
    fontWeight: '600',
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  treeList: {
    flex: 1,
    overflowY: 'auto',
    padding: '12px 0',
  },
  treeItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    padding: '10px 18px',
    cursor: 'pointer',
    transition: 'background var(--transition-fast)',
  },
  treeItemText: {
    fontSize: '14px',
    fontFamily: 'var(--font-sans)',
  },
  pathToolTip: {
    fontSize: '11px',
    color: 'var(--text-muted)',
    marginLeft: 'auto',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    maxWidth: '120px',
  },
  previewPane: {
    flex: 1,
    borderRadius: '12px',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
  },
  previewHeader: {
    background: 'rgba(0, 0, 0, 0.2)',
    padding: '12px 20px',
    borderBottom: '1px solid var(--glass-border)',
  },
  activeFilePath: {
    fontSize: '13px',
    fontFamily: 'var(--font-mono)',
    color: 'var(--accent-cyan)',
  },
  codeContainer: {
    flex: 1,
    overflow: 'auto',
    background: '#12131a',
    padding: '20px',
  },
  pre: {
    margin: 0,
  },
  code: {
    fontFamily: 'var(--font-mono)',
    fontSize: '14px',
    lineHeight: '1.6',
    whiteSpace: 'pre',
    color: '#f8f8f2',
  },
  spinner: {
    width: '14px',
    height: '14px',
    border: '2px solid rgba(255, 255, 255, 0.2)',
    borderTopColor: '#000',
    borderRadius: '50%',
  }
};
