import React, { useState } from 'react';
import { Plus, Trash2, Settings, Shield, FileCode, CheckSquare, Layers, Download } from 'lucide-react';

const FIELD_TYPES = ['Long', 'String', 'double', 'int', 'boolean', 'LocalDate', 'LocalDateTime'];

export default function ProjectCustomizer({ metadata, onUpdateMetadata, onPreview, onDownload, isDownloading }) {
  const [newFieldName, setNewFieldName] = useState('');
  const [newFieldType, setNewFieldType] = useState('String');

  const updateField = (key, value) => {
    onUpdateMetadata({
      ...metadata,
      [key]: value
    });
  };

  const addField = () => {
    if (!newFieldName.trim()) return;
    const name = newFieldName.trim();
    
    // Check if field already exists
    if (metadata.fields.some(f => f.name.toLowerCase() === name.toLowerCase())) {
      alert('Field already exists.');
      return;
    }

    const newField = {
      name,
      type: newFieldType,
      validations: []
    };

    // Default validations based on name / type
    if (name.toLowerCase() === 'id') {
      newField.validations = ['@Id', '@GeneratedValue(strategy = GenerationType.IDENTITY)'];
      newField.type = 'Long';
    } else {
      if (newFieldType === 'String') {
        newField.validations = ['@NotBlank', '@Size(min = 2, max = 100)'];
      } else {
        newField.validations = ['@NotNull'];
      }
    }

    onUpdateMetadata({
      ...metadata,
      fields: [...metadata.fields, newField]
    });
    setNewFieldName('');
  };

  const removeField = (index) => {
    const updatedFields = metadata.fields.filter((_, i) => i !== index);
    onUpdateMetadata({
      ...metadata,
      fields: updatedFields
    });
  };

  const toggleValidation = (fieldIndex, valAnnotation) => {
    const updatedFields = metadata.fields.map((field, i) => {
      if (i === fieldIndex) {
        const hasVal = field.validations.includes(valAnnotation);
        const newVals = hasVal
          ? field.validations.filter(v => v !== valAnnotation)
          : [...field.validations, valAnnotation];
        return { ...field, validations: newVals };
      }
      return field;
    });

    onUpdateMetadata({
      ...metadata,
      fields: updatedFields
    });
  };

  const handleFieldTypeChange = (fieldIndex, newType) => {
    const updatedFields = metadata.fields.map((field, i) => {
      if (i === fieldIndex) {
        return { ...field, type: newType };
      }
      return field;
    });
    onUpdateMetadata({
      ...metadata,
      fields: updatedFields
    });
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Customize <span className="gradient-text">Project Configuration</span></h2>
      <p style={styles.subtitle}>Fine-tune your generated files and database bindings before generating the ZIP package.</p>

      <div style={styles.grid}>
        {/* Left Column: Metadata settings */}
        <div className="glass-card" style={styles.sectionCard}>
          <div style={styles.sectionHeader}>
            <Settings size={18} color="#00f2fe" />
            <h3 style={styles.sectionTitle}>Project Info & Dependencies</h3>
          </div>

          <div className="form-group">
            <label className="form-label">Project Name (PascalCase)</label>
            <input 
              type="text" 
              className="form-input" 
              value={metadata.projectName || ''} 
              onChange={(e) => updateField('projectName', e.target.value.replace(/\s+/g, ''))}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Package Name</label>
            <input 
              type="text" 
              className="form-input" 
              value={metadata.packageName || ''} 
              onChange={(e) => updateField('packageName', e.target.value.replace(/\s+/g, '').toLowerCase())}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Database Management System</label>
            <select 
              className="form-select" 
              value={metadata.database || 'mysql'} 
              onChange={(e) => updateField('database', e.target.value)}
            >
              <option value="mysql">MySQL</option>
              <option value="postgresql">PostgreSQL</option>
              <option value="mongodb">MongoDB</option>
              <option value="sqlserver">Microsoft SQL Server</option>
            </select>
          </div>

          <div className="form-group" style={styles.checkboxGroup}>
            <label className="form-label">Java Version</label>
            <select 
              className="form-select" 
              value={metadata.javaVersion || '21'} 
              onChange={(e) => updateField('javaVersion', e.target.value)}
            >
              <option value="21">Java 21 (Recommended)</option>
              <option value="17">Java 17</option>
            </select>
          </div>

          <div style={styles.divider}></div>

          <h4 style={styles.subSubTitle}>Features & Starters</h4>

          <label style={styles.checkboxLabel}>
            <input 
              type="checkbox" 
              checked={metadata.lombok} 
              onChange={(e) => updateField('lombok', e.target.checked)} 
              style={styles.checkbox}
            />
            <div>
              <strong>Lombok</strong>
              <p style={styles.checkDesc}>Reduce boilerplates using @Data, @Builder annotations</p>
            </div>
          </label>

          <label style={styles.checkboxLabel}>
            <input 
              type="checkbox" 
              checked={metadata.security} 
              onChange={(e) => updateField('security', e.target.checked)} 
              style={styles.checkbox}
            />
            <div>
              <strong>Spring Security</strong>
              <p style={styles.checkDesc}>Integrate CORS, CSRF rules and endpoints filters</p>
            </div>
          </label>

          <label style={styles.checkboxLabel}>
            <input 
              type="checkbox" 
              checked={metadata.swagger} 
              onChange={(e) => updateField('swagger', e.target.checked)} 
              style={styles.checkbox}
            />
            <div>
              <strong>Swagger / OpenAPI UI</strong>
              <p style={styles.checkDesc}>Visual interactive documentation via /swagger-ui.html</p>
            </div>
          </label>
        </div>

        {/* Right Column: Entity fields */}
        <div className="glass-card" style={styles.sectionCard}>
          <div style={styles.sectionHeader}>
            <Layers size={18} color="#9d4edd" />
            <h3 style={styles.sectionTitle}>Entity Builder: {metadata.entity}</h3>
          </div>

          <div className="form-group">
            <label className="form-label">Entity Model Name (PascalCase)</label>
            <input 
              type="text" 
              className="form-input" 
              value={metadata.entity || ''} 
              onChange={(e) => updateField('entity', e.target.value.replace(/\s+/g, ''))}
            />
          </div>

          <div style={styles.fieldsContainer}>
            <h4 style={styles.subSubTitle}>Properties / Schema Fields</h4>

            <div style={styles.fieldsList}>
              {metadata.fields?.map((field, fIdx) => (
                <div key={fIdx} style={styles.fieldItem}>
                  <div style={styles.fieldRow}>
                    <input 
                      type="text" 
                      className="form-input" 
                      value={field.name} 
                      disabled 
                      style={{ ...styles.fieldInput, width: '45%' }}
                    />
                    <select 
                      className="form-select" 
                      value={field.type} 
                      onChange={(e) => handleFieldTypeChange(fIdx, e.target.value)}
                      style={{ ...styles.fieldInput, width: '40%' }}
                    >
                      {FIELD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                    <button 
                      className="btn btn-danger" 
                      onClick={() => removeField(fIdx)} 
                      style={styles.deleteBtn}
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>

                  {/* Validations Sub Row */}
                  <div style={styles.validationRow}>
                    <span style={styles.valLabel}>Validations:</span>
                    <label style={styles.valBadge}>
                      <input 
                        type="checkbox" 
                        checked={field.validations.includes('@Id')} 
                        onChange={() => toggleValidation(fIdx, '@Id')}
                        style={styles.miniCheck}
                      /> @Id
                    </label>
                    <label style={styles.valBadge}>
                      <input 
                        type="checkbox" 
                        checked={field.validations.includes('@NotNull')} 
                        onChange={() => toggleValidation(fIdx, '@NotNull')}
                        style={styles.miniCheck}
                      /> @NotNull
                    </label>
                    {field.type === 'String' && (
                      <>
                        <label style={styles.valBadge}>
                          <input 
                            type="checkbox" 
                            checked={field.validations.includes('@NotBlank')} 
                            onChange={() => toggleValidation(fIdx, '@NotBlank')}
                            style={styles.miniCheck}
                          /> @NotBlank
                        </label>
                        <label style={styles.valBadge}>
                          <input 
                            type="checkbox" 
                            checked={field.validations.includes('@Email')} 
                            onChange={() => toggleValidation(fIdx, '@Email')}
                            style={styles.miniCheck}
                          /> @Email
                        </label>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Add Field Inline Form */}
            <div style={styles.addFieldContainer}>
              <input 
                type="text" 
                placeholder="New property name (e.g. price)" 
                className="form-input" 
                value={newFieldName} 
                onChange={(e) => setNewFieldName(e.target.value.replace(/[^a-zA-Z0-9]/g, ''))}
                style={{ flex: 2 }}
              />
              <select 
                className="form-select" 
                value={newFieldType} 
                onChange={(e) => setNewFieldType(e.target.value)}
                style={{ flex: 1 }}
              >
                {FIELD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
              <button className="btn btn-secondary" onClick={addField} style={styles.addBtn}>
                <Plus size={16} />
                Add Property
              </button>
            </div>
          </div>
        </div>
      </div>

      <div style={styles.footerRow}>
        <button 
          onClick={onPreview} 
          className="btn btn-secondary" 
          style={styles.footerBtn}
        >
          <FileCode size={18} color="#00f2fe" />
          Preview Generated Files
        </button>

        <button 
          onClick={onDownload} 
          className="btn btn-primary" 
          disabled={isDownloading || !metadata.fields || metadata.fields.length === 0}
          style={styles.footerBtn}
        >
          {isDownloading ? (
            <>
              <div style={styles.spinner} className="spinner"></div>
              Downloading...
            </>
          ) : (
            <>
              Download Zip Archive
              <Download size={18} />
            </>
          )}
        </button>
      </div>
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '1080px',
    margin: '40px auto',
    padding: '0 20px',
  },
  title: {
    fontSize: '28px',
    fontWeight: '800',
    color: '#fff',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: '14px',
    color: 'var(--text-secondary)',
    marginTop: '6px',
    textAlign: 'center',
    marginBottom: '40px',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))',
    gap: '30px',
    marginBottom: '40px',
  },
  sectionCard: {
    background: 'var(--glass-bg)',
    border: '1px solid var(--glass-border)',
    padding: '28px',
  },
  sectionHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginBottom: '24px',
    borderBottom: '1px solid var(--glass-border)',
    paddingBottom: '14px',
  },
  sectionTitle: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#fff',
  },
  subSubTitle: {
    fontSize: '14px',
    color: 'var(--text-primary)',
    fontWeight: '700',
    marginBottom: '16px',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
  },
  divider: {
    height: '1px',
    background: 'var(--glass-border)',
    margin: '24px 0',
  },
  checkboxLabel: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '14px',
    cursor: 'pointer',
    marginBottom: '16px',
    padding: '12px',
    borderRadius: '8px',
    background: 'rgba(255, 255, 255, 0.01)',
    transition: 'background var(--transition-fast)',
  },
  checkbox: {
    marginTop: '4px',
    cursor: 'pointer',
  },
  checkDesc: {
    fontSize: '12px',
    color: 'var(--text-secondary)',
    marginTop: '2px',
  },
  fieldsContainer: {
    marginTop: '20px',
  },
  fieldsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
    maxHeight: '340px',
    overflowY: 'auto',
    paddingRight: '8px',
    marginBottom: '20px',
  },
  fieldItem: {
    background: 'rgba(0, 0, 0, 0.2)',
    border: '1px solid var(--glass-border)',
    padding: '12px 14px',
    borderRadius: '10px',
  },
  fieldRow: {
    display: 'flex',
    gap: '10px',
    alignItems: 'center',
  },
  fieldInput: {
    padding: '8px 12px',
    fontSize: '14px',
  },
  deleteBtn: {
    padding: '8px 12px',
  },
  validationRow: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '10px',
    alignItems: 'center',
    marginTop: '10px',
    borderTop: '1px solid rgba(255, 255, 255, 0.03)',
    paddingTop: '8px',
  },
  valLabel: {
    fontSize: '11px',
    color: 'var(--text-muted)',
    fontWeight: '600',
  },
  valBadge: {
    fontSize: '12px',
    color: 'var(--text-secondary)',
    display: 'inline-flex',
    alignItems: 'center',
    gap: '5px',
    cursor: 'pointer',
  },
  miniCheck: {
    cursor: 'pointer',
  },
  addFieldContainer: {
    display: 'flex',
    gap: '10px',
    borderTop: '1px solid var(--glass-border)',
    paddingTop: '20px',
  },
  addBtn: {
    padding: '10px 16px',
    fontSize: '14px',
  },
  footerRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '40px',
    borderTop: '1px solid var(--glass-border)',
    paddingTop: '30px',
    gap: '20px',
  },
  footerBtn: {
    minWidth: '220px',
  },
  spinner: {
    width: '18px',
    height: '18px',
    border: '2px solid rgba(255, 255, 255, 0.2)',
    borderTopColor: '#000',
    borderRadius: '50%',
  }
};
