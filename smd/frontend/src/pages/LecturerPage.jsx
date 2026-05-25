import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api from '../lib/api.js'

const EMPTY_FORM = {
  courseCode: '', courseName: '', credits: '', department: '',
  academicYear: '', semester: '', description: '',
  learningOutcomes: '', ploOutcomes: '', assessmentMethods: '', prerequisites: '', materials: '',
  cloMappings: '{}'
}

function parseCloMappings(json) {
  try { return JSON.parse(json || '{}') } catch { return {} }
}

function CloPloMatrix({ learningOutcomes, ploOutcomes, cloMappings, onChange }) {
  const clos = (learningOutcomes || '').split('\n').map(l => l.trim()).filter(Boolean)
  const plos = (ploOutcomes || '').split('\n').map(l => l.trim()).filter(Boolean)
  const mappings = parseCloMappings(cloMappings)

  if (clos.length === 0 || plos.length === 0) {
    return (
      <div className="empty" style={{ padding: '12px 0', textAlign: 'left', color: '#94a3b8', fontSize: 13 }}>
        Enter CLOs (Learning Outcomes) and PLOs above to build the mapping matrix.
      </div>
    )
  }

  function toggle(clo, plo) {
    const next = { ...mappings }
    const current = next[clo] || []
    if (current.includes(plo)) {
      next[clo] = current.filter(p => p !== plo)
    } else {
      next[clo] = [...current, plo]
    }
    onChange(JSON.stringify(next))
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: 'auto', minWidth: '100%' }}>
        <thead>
          <tr>
            <th style={{ minWidth: 180 }}>CLO \ PLO</th>
            {plos.map((plo, i) => (
              <th key={i} style={{ textAlign: 'center', minWidth: 80, fontSize: 11 }}>{plo}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {clos.map((clo, ci) => (
            <tr key={ci}>
              <td style={{ fontSize: 12 }}>{clo}</td>
              {plos.map((plo, pi) => (
                <td key={pi} style={{ textAlign: 'center' }}>
                  <input
                    type="checkbox"
                    checked={!!(mappings[clo] && mappings[clo].includes(plo))}
                    onChange={() => toggle(clo, plo)}
                  />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace('_', ' ')}</span>
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString()
}

export default function LecturerPage() {
  const navigate = useNavigate()
  const [list, setList] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [editId, setEditId] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const [notifications, setNotifications] = useState([])

  useEffect(() => { loadList(); loadNotifications() }, [])

  async function loadList() {
    try {
      const data = await api.myList()
      setList(data)
    } catch (e) { setError(e.message) }
  }

  async function loadNotifications() {
    try {
      setNotifications(await api.getNotifications())
    } catch {}
  }

  async function handleMarkAllRead() {
    try {
      await api.markAllRead()
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
    } catch {}
  }

  function getUnreadCount(syllabusId) {
    return notifications.filter(n => String(n.syllabusId) === String(syllabusId) && !n.isRead).length
  }

  const totalUnread = notifications.filter(n => !n.isRead).length

  function openCreate() {
    setForm(EMPTY_FORM)
    setEditId(null)
    setShowForm(true)
    setError('')
    setSuccess('')
  }

  function openEdit(s) {
    setForm({
      courseCode: s.courseCode || '', courseName: s.courseName || '',
      credits: s.credits || '', department: s.department || '',
      academicYear: s.academicYear || '', semester: s.semester || '',
      description: s.description || '', learningOutcomes: s.learningOutcomes || '',
      ploOutcomes: s.ploOutcomes || '',
      assessmentMethods: s.assessmentMethods || '', prerequisites: s.prerequisites || '',
      materials: s.materials || '', cloMappings: s.cloMappings || '{}'
    })
    setEditId(s.id)
    setShowForm(true)
    setError('')
    setSuccess('')
  }

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  }

  async function handleSubmitForm(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const payload = { ...form, credits: parseInt(form.credits) || 0 }
      if (editId) { await api.updateSyllabus(editId, payload) }
      else { await api.createSyllabus(payload) }
      setSuccess(editId ? 'Syllabus updated!' : 'Syllabus created!')
      setShowForm(false)
      loadList()
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  async function handleSubmit(id) {
    if (!window.confirm('Submit this syllabus for review?')) return
    try {
      await api.submitSyllabus(id)
      setSuccess('Submitted for review.')
      loadList()
    } catch (e) { setError(e.message) }
  }

  const canEdit = (s) => s.status === 'DRAFT' || s.status === 'REJECTED' || s.status === 'AA_REJECTED'
  const canVersion = (s) => s.status === 'APPROVED' || s.status === 'PUBLISHED'

  async function handleNewVersion(s) {
    try {
      const newDraft = await api.newVersion(s.id)
      loadList()
      setForm({
        courseCode: newDraft.courseCode || '', courseName: newDraft.courseName || '',
        credits: newDraft.credits || '', department: newDraft.department || '',
        academicYear: newDraft.academicYear || '', semester: newDraft.semester || '',
        description: newDraft.description || '', learningOutcomes: newDraft.learningOutcomes || '',
        ploOutcomes: newDraft.ploOutcomes || '',
        assessmentMethods: newDraft.assessmentMethods || '', prerequisites: newDraft.prerequisites || '',
        materials: newDraft.materials || '', cloMappings: newDraft.cloMappings || '{}'
      })
      setEditId(newDraft.id)
      setShowForm(true)
      setError('')
      setSuccess('New draft version created. Edit and submit for review.')
    } catch (e) { setError(e.message) }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="header-row">
          <h1 className="page-title">My Syllabi</h1>
          <button className="btn btn-primary" onClick={openCreate}>+ New Syllabus</button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        {showForm && (
          <div className="card">
            <h2 style={{ marginBottom: 16, fontSize: 16 }}>{editId ? 'Edit Syllabus' : 'New Syllabus'}</h2>
            <form onSubmit={handleSubmitForm}>
              <div className="form-row">
                <div className="form-group">
                  <label>Course Code *</label>
                  <input name="courseCode" value={form.courseCode} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Course Name *</label>
                  <input name="courseName" value={form.courseName} onChange={handleChange} required />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Credits *</label>
                  <input name="credits" type="number" min="1" value={form.credits} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Department</label>
                  <input name="department" value={form.department} onChange={handleChange} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Academic Year</label>
                  <input name="academicYear" value={form.academicYear} onChange={handleChange} placeholder="2024-2025" />
                </div>
                <div className="form-group">
                  <label>Semester</label>
                  <input name="semester" value={form.semester} onChange={handleChange} placeholder="1 or 2" />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea name="description" value={form.description} onChange={handleChange} rows={3} />
              </div>
              <div className="form-group">
                <label>Learning Outcomes (CLOs)</label>
                <textarea name="learningOutcomes" value={form.learningOutcomes} onChange={handleChange} rows={4}
                  placeholder="One CLO per line, e.g.&#10;CLO1: Apply software engineering principles&#10;CLO2: Design system architecture" />
              </div>
              <div className="form-group">
                <label>Program Learning Outcomes (PLOs)</label>
                <textarea name="ploOutcomes" value={form.ploOutcomes} onChange={handleChange} rows={3}
                  placeholder="One PLO per line, e.g.&#10;PLO1: Technical knowledge&#10;PLO2: Problem solving" />
              </div>
              <div className="form-group">
                <label>CLO-PLO Mapping Matrix</label>
                <CloPloMatrix
                  learningOutcomes={form.learningOutcomes}
                  ploOutcomes={form.ploOutcomes}
                  cloMappings={form.cloMappings}
                  onChange={val => setForm(f => ({ ...f, cloMappings: val }))}
                />
              </div>
              <div className="form-group">
                <label>Assessment Methods</label>
                <textarea name="assessmentMethods" value={form.assessmentMethods} onChange={handleChange} rows={3} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Prerequisites</label>
                  <input name="prerequisites" value={form.prerequisites} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Materials</label>
                  <input name="materials" value={form.materials} onChange={handleChange} />
                </div>
              </div>
              <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving…' : 'Save'}
                </button>
                <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {totalUnread > 0 && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
            <span style={{ fontSize: 13, color: '#64748b' }}>
              {totalUnread} unread comment notification{totalUnread !== 1 ? 's' : ''}
            </span>
            <button className="btn btn-secondary" style={{ fontSize: 12, padding: '4px 12px' }} onClick={handleMarkAllRead}>
              Mark all read
            </button>
          </div>
        )}

        <div className="card" style={{ padding: 0 }}>
          {list.length === 0 ? (
            <div className="empty">No syllabi yet. Click "+ New Syllabus" to create one.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Code</th><th>Course Name</th><th>Credits</th>
                  <th>Ver.</th><th>Status</th><th>Updated</th><th>New Comments</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {list.map(s => {
                  const unread = getUnreadCount(s.id)
                  return (
                    <React.Fragment key={s.id}>
                      <tr>
                        <td><strong>{s.courseCode}</strong></td>
                        <td>{s.courseName}</td>
                        <td>{s.credits}</td>
                        <td>v{s.versionNumber}</td>
                        <td><StatusBadge status={s.status} /></td>
                        <td>{formatDate(s.updatedAt)}</td>
                        <td style={{ textAlign: 'center' }}>
                          {unread > 0 ? (
                            <button
                              onClick={() => navigate(`/syllabus/${s.id}`)}
                              style={{
                                background: '#ef4444', color: '#fff', border: 'none',
                                borderRadius: 12, padding: '2px 10px', fontSize: 12,
                                fontWeight: 700, cursor: 'pointer', minWidth: 28
                              }}
                            >{unread}</button>
                          ) : <span style={{ color: '#94a3b8' }}>—</span>}
                        </td>
                        <td>
                          <div className="actions">
                            <button className="btn btn-outline" onClick={() => navigate(`/syllabus/${s.id}`)}>View</button>
                            {canEdit(s) && (
                              <button className="btn btn-primary" onClick={() => openEdit(s)}>Edit</button>
                            )}
                            {canEdit(s) && (
                              <button className="btn btn-success" onClick={() => handleSubmit(s.id)}>Submit</button>
                            )}
                            {canVersion(s) && (
                              <button className="btn btn-purple" onClick={() => handleNewVersion(s)}>New Version</button>
                            )}
                          </div>
                        </td>
                      </tr>
                      {(s.status === 'REJECTED' || s.status === 'AA_REJECTED') && s.rejectionReason && (
                        <tr className="rejection-row">
                          <td colSpan={8}>
                            <div className="rejection-text" style={s.status === 'AA_REJECTED' ? { color: '#831843', borderLeftColor: '#831843' } : {}}>
                              {s.status === 'AA_REJECTED' ? '⚠ Rejected by Academic Affairs: ' : '⚠ Rejected: '}{s.rejectionReason}
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  )
}
