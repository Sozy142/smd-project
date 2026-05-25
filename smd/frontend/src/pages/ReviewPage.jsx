import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api, { getCurrentUser } from '../lib/api.js'

const ALL_STATUSES = ['DRAFT', 'PENDING_REVIEW', 'PENDING_APPROVAL', 'APPROVED', 'AA_REJECTED', 'REJECTED', 'PUBLISHED']

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace(/_/g, ' ')}</span>
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString()
}

function PendingReviewTab() {
  const navigate = useNavigate()
  const [list, setList] = useState([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [commentCounts, setCommentCounts] = useState({})

  useEffect(() => { loadList() }, [])

  async function loadList() {
    try {
      const data = await api.pendingList()
      setList(data)
      const counts = {}
      await Promise.all(data.map(async s => {
        try { counts[s.id] = await api.commentCount(s.id) }
        catch { counts[s.id] = 0 }
      }))
      setCommentCounts(counts)
    } catch (e) { setError(e.message) }
  }

  async function handleApprove(id) {
    const comments = window.prompt('Comments (optional):') ?? ''
    try {
      await api.approve(id, comments)
      setSuccess('Syllabus forwarded to Academic Affairs for Level 2 approval.')
      setError('')
      loadList()
    } catch (e) { setError(e.message) }
  }

  async function handleReject(id) {
    const reason = window.prompt('Rejection reason (required):')
    if (reason === null) return
    if (!reason.trim()) { setError('Rejection reason is required.'); return }
    try {
      await api.reject(id, reason)
      setSuccess('Syllabus rejected.')
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
          <div className="empty">No syllabi pending review.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Code</th><th>Course Name</th><th>Submitted By</th>
                <th>Department</th><th>Credits</th><th>Ver.</th><th>Comments</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map(s => (
                <tr key={s.id}>
                  <td><strong>{s.courseCode}</strong></td>
                  <td>{s.courseName}</td>
                  <td>{s.createdByName}</td>
                  <td>{s.department}</td>
                  <td>{s.credits}</td>
                  <td>v{s.versionNumber}</td>
                  <td style={{ textAlign: 'center' }}>
                    {commentCounts[s.id] > 0 ? (
                      <span style={{ color: '#4f6ef7', fontWeight: 700 }}>{commentCounts[s.id]}</span>
                    ) : <span style={{ color: '#94a3b8' }}>—</span>}
                  </td>
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

function LookupTab() {
  const navigate = useNavigate()
  const user = getCurrentUser()
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
      const data = await api.departmentSearch(keyword, status)
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

      {user?.department && (
        <div className="alert alert-info" style={{ marginBottom: 16 }}>
          Searching in department: <strong>{user.department}</strong>
        </div>
      )}

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
              placeholder="Search…"
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
                  <th>Code</th><th>Course Name</th><th>Credits</th>
                  <th>Status</th><th>Created By</th><th>Updated</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {results.map(s => (
                  <tr key={s.id}>
                    <td><strong>{s.courseCode}</strong></td>
                    <td>{s.courseName}</td>
                    <td>{s.credits}</td>
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

export default function ReviewPage() {
  const [tab, setTab] = useState('pending')

  const tabStyle = (t) => ({
    padding: '8px 20px',
    border: 'none',
    borderBottom: tab === t ? '2px solid #4f6ef7' : '2px solid transparent',
    background: 'none',
    color: tab === t ? '#4f6ef7' : '#64748b',
    fontWeight: tab === t ? 700 : 500,
    fontSize: 14,
    cursor: 'pointer'
  })

  return (
    <>
      <Navbar />
      <div className="container">
        <h1 className="page-title">HoD Review</h1>

        <div style={{ display: 'flex', borderBottom: '1px solid #e2e8f0', marginBottom: 20 }}>
          <button style={tabStyle('pending')} onClick={() => setTab('pending')}>
            Pending Review
          </button>
          <button style={tabStyle('lookup')} onClick={() => setTab('lookup')}>
            Lookup &amp; Analysis
          </button>
        </div>

        {tab === 'pending' && <PendingReviewTab />}
        {tab === 'lookup' && <LookupTab />}
      </div>
    </>
  )
}
