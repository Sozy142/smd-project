import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api from '../lib/api.js'

const ALL_STATUSES = ['DRAFT', 'PENDING_REVIEW', 'PENDING_APPROVAL', 'APPROVED', 'AA_REJECTED', 'REJECTED', 'PUBLISHED']

const STATUS_COLORS = {
  DRAFT:            { bg: '#e2e8f0', color: '#475569' },
  PENDING_REVIEW:   { bg: '#fef3c7', color: '#92400e' },
  PENDING_APPROVAL: { bg: '#fed7aa', color: '#9a3412' },
  APPROVED:         { bg: '#dcfce7', color: '#166534' },
  AA_REJECTED:      { bg: '#fce7f3', color: '#831843' },
  REJECTED:         { bg: '#fee2e2', color: '#991b1b' },
  PUBLISHED:        { bg: '#dbeafe', color: '#1e40af' },
}

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace(/_/g, ' ')}</span>
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString()
}

function PendingApprovalTab() {
  const navigate = useNavigate()
  const [list, setList] = useState([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => { loadList() }, [])

  async function loadList() {
    try { setList(await api.pendingApproval()) }
    catch (e) { setError(e.message) }
  }

  async function handleApprove(id) {
    const comments = window.prompt('Comments (optional):') ?? ''
    try {
      await api.aaApprove(id, comments)
      setSuccess('Syllabus approved. Lecturer has been notified.')
      setError('')
      loadList()
    } catch (e) { setError(e.message) }
  }

  async function handleReject(id) {
    const reason = window.prompt('Rejection reason (required):')
    if (reason === null) return
    if (!reason.trim()) { setError('Rejection reason is required.'); return }
    try {
      await api.aaReject(id, reason)
      setSuccess('Syllabus rejected. Lecturer has been notified.')
      setError('')
      loadList()
    } catch (e) { setError(e.message) }
  }

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card" style={{ padding: 0 }}>
        {list.length === 0 ? (
          <div className="empty">No syllabi awaiting Level 2 approval.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Code</th><th>Course Name</th><th>Department</th>
                <th>Credits</th><th>Created By</th><th>Updated</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map(s => (
                <tr key={s.id}>
                  <td><strong>{s.courseCode}</strong></td>
                  <td>{s.courseName}</td>
                  <td>{s.department}</td>
                  <td>{s.credits}</td>
                  <td>{s.createdByName}</td>
                  <td>{formatDate(s.updatedAt)}</td>
                  <td>
                    <div className="actions">
                      <button className="btn btn-outline" onClick={() => navigate(`/syllabus/${s.id}`)}>View</button>
                      <button className="btn btn-success" onClick={() => handleApprove(s.id)}>Approve</button>
                      <button className="btn btn-danger" onClick={() => handleReject(s.id)}>Reject</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

function AllSyllabiTab() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const [results, setResults] = useState([])
  const [searched, setSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function handleSearch() {
    setLoading(true)
    setError('')
    try {
      const data = await api.searchAll(keyword, status)
      setResults(data)
      setSearched(true)
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'flex-end' }}>
          <div style={{ flex: 1, minWidth: 200 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: '#555', display: 'block', marginBottom: 4 }}>
              Keyword (course code or name)
            </label>
            <input
              value={keyword}
              onChange={e => setKeyword(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Search all departments…"
              style={{ width: '100%', padding: '7px 10px', border: '1.5px solid #e2e8f0', borderRadius: 6, fontSize: 13 }}
            />
          </div>
          <div style={{ minWidth: 180 }}>
            <label style={{ fontSize: 12, fontWeight: 600, color: '#555', display: 'block', marginBottom: 4 }}>
              Status
            </label>
            <select
              value={status}
              onChange={e => setStatus(e.target.value)}
              style={{ width: '100%', padding: '7px 10px', border: '1.5px solid #e2e8f0', borderRadius: 6, fontSize: 13, background: '#fff' }}
            >
              <option value="">All Statuses</option>
              {ALL_STATUSES.map(s => (
                <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>
              ))}
            </select>
          </div>
          <button className="btn btn-primary" onClick={handleSearch} disabled={loading} style={{ alignSelf: 'flex-end' }}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
      </div>

      {searched && (
        <div className="card" style={{ padding: 0 }}>
          <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0', fontSize: 13, color: '#475569', fontWeight: 600 }}>
            Found {results.length} syllabus{results.length !== 1 ? 'i' : ''}
          </div>
          {results.length === 0 ? (
            <div className="empty">No syllabi found matching your criteria.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Code</th><th>Course Name</th><th>Department</th>
                  <th>Status</th><th>Created By</th><th>Updated</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {results.map(s => (
                  <tr key={s.id}>
                    <td><strong>{s.courseCode}</strong></td>
                    <td>{s.courseName}</td>
                    <td>{s.department}</td>
                    <td><StatusBadge status={s.status} /></td>
                    <td>{s.createdByName}</td>
                    <td>{formatDate(s.updatedAt)}</td>
                    <td>
                      <button className="btn btn-outline" onClick={() => navigate(`/syllabus/${s.id}`)}>View</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </>
  )
}

function DashboardTab() {
  const navigate = useNavigate()
  const [allSyllabi, setAllSyllabi] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.allList()
      .then(setAllSyllabi)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="empty">Loading…</div>
  if (error) return <div className="alert alert-error">{error}</div>

  const counts = {}
  ALL_STATUSES.forEach(s => { counts[s] = 0 })
  allSyllabi.forEach(s => { if (counts[s.status] !== undefined) counts[s.status]++ })

  const recent = [...allSyllabi]
    .sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    .slice(0, 10)

  return (
    <>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: 14, marginBottom: 20 }}>
        {ALL_STATUSES.map(s => {
          const col = STATUS_COLORS[s] || { bg: '#f0f0f0', color: '#333' }
          return (
            <div key={s} style={{
              background: col.bg, color: col.color,
              borderRadius: 10, padding: '16px 18px',
              boxShadow: '0 1px 4px rgba(0,0,0,0.06)'
            }}>
              <div style={{ fontSize: 28, fontWeight: 800, lineHeight: 1 }}>{counts[s]}</div>
              <div style={{ fontSize: 11, fontWeight: 700, marginTop: 6, textTransform: 'uppercase', letterSpacing: 0.5 }}>
                {s.replace(/_/g, ' ')}
              </div>
            </div>
          )
        })}
        <div style={{
          background: '#1a1f36', color: '#fff',
          borderRadius: 10, padding: '16px 18px',
          boxShadow: '0 1px 4px rgba(0,0,0,0.06)'
        }}>
          <div style={{ fontSize: 28, fontWeight: 800, lineHeight: 1 }}>{allSyllabi.length}</div>
          <div style={{ fontSize: 11, fontWeight: 700, marginTop: 6, textTransform: 'uppercase', letterSpacing: 0.5 }}>
            TOTAL
          </div>
        </div>
      </div>

      <div className="card" style={{ padding: 0 }}>
        <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0', fontWeight: 700, fontSize: 14, color: '#1a1f36' }}>
          Recent Activity (last 10)
        </div>
        {recent.length === 0 ? (
          <div className="empty">No syllabi yet.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Code</th><th>Course Name</th><th>Department</th>
                <th>Status</th><th>Created By</th><th>Updated</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {recent.map(s => (
                <tr key={s.id}>
                  <td><strong>{s.courseCode}</strong></td>
                  <td>{s.courseName}</td>
                  <td>{s.department}</td>
                  <td><StatusBadge status={s.status} /></td>
                  <td>{s.createdByName}</td>
                  <td>{formatDate(s.updatedAt)}</td>
                  <td>
                    <button className="btn btn-outline" onClick={() => navigate(`/syllabus/${s.id}`)}>View</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

export default function AcademicAffairsPage() {
  const [tab, setTab] = useState('pending')
  const [pendingCount, setPendingCount] = useState(0)

  useEffect(() => {
    api.pendingApproval()
      .then(data => setPendingCount(data.length))
      .catch(() => {})
  }, [])

  const tabStyle = (t) => ({
    padding: '8px 20px',
    border: 'none',
    borderBottom: tab === t ? '2px solid #4f6ef7' : '2px solid transparent',
    background: 'none',
    color: tab === t ? '#4f6ef7' : '#64748b',
    fontWeight: tab === t ? 700 : 500,
    fontSize: 14,
    cursor: 'pointer',
    position: 'relative'
  })

  return (
    <>
      <Navbar />
      <div className="container">
        <h1 className="page-title">Academic Affairs</h1>

        <div style={{ display: 'flex', borderBottom: '1px solid #e2e8f0', marginBottom: 20 }}>
          <button style={tabStyle('pending')} onClick={() => setTab('pending')}>
            Pending Approval
            {pendingCount > 0 && (
              <span style={{
                marginLeft: 8, background: '#ef4444', color: '#fff',
                borderRadius: 10, padding: '1px 7px', fontSize: 11, fontWeight: 700
              }}>
                {pendingCount}
              </span>
            )}
          </button>
          <button style={tabStyle('all')} onClick={() => setTab('all')}>
            All Syllabi
          </button>
          <button style={tabStyle('dashboard')} onClick={() => setTab('dashboard')}>
            Dashboard
          </button>
        </div>

        {tab === 'pending' && <PendingApprovalTab />}
        {tab === 'all' && <AllSyllabiTab />}
        {tab === 'dashboard' && <DashboardTab />}
      </div>
    </>
  )
}
