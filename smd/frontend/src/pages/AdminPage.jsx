import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api, { getCurrentUser } from '../lib/api.js'

const STATUSES = ['DRAFT', 'PENDING_REVIEW', 'APPROVED', 'REJECTED', 'PUBLISHED']
const ROLES = ['ADMIN', 'LECTURER', 'HOD', 'ACADEMIC_AFFAIRS', 'PRINCIPAL', 'STUDENT']

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace('_', ' ')}</span>
}

function RoleBadge({ role }) {
  return <span className={`role-badge role-${role}`}>{role.replace('_', ' ')}</span>
}

function UserStatusBadge({ isActive }) {
  return (
    <span className={`role-badge ${isActive ? 'user-active' : 'user-locked'}`}>
      {isActive ? 'Active' : 'Locked'}
    </span>
  )
}

function formatDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleDateString()
}

const EMPTY_USER_FORM = { email: '', password: '', firstName: '', lastName: '', role: 'LECTURER', department: '' }

// ─── DASHBOARD TAB ──────────────────────────────────────────────────────────
function DashboardTab({ syllabi, users }) {
  const recent = [...syllabi].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)).slice(0, 10)
  const countByStatus = (st) => syllabi.filter(s => s.status === st).length
  const navigate = useNavigate()

  return (
    <>
      <div className="summary-grid" style={{ gridTemplateColumns: 'repeat(2,1fr)', marginBottom: 12 }}>
        <div className="summary-card blue">
          <div className="s-num">{users.length}</div>
          <div className="s-label">Total Users</div>
        </div>
        <div className="summary-card green">
          <div className="s-num">{syllabi.length}</div>
          <div className="s-label">Total Syllabi</div>
        </div>
      </div>

      <div className="stats-row" style={{ gridTemplateColumns: 'repeat(5,1fr)', marginBottom: 24 }}>
        {STATUSES.map(st => (
          <div className="stat-card" key={st}>
            <div className="stat-num">{countByStatus(st)}</div>
            <div className="stat-label">{st.replace('_', ' ')}</div>
          </div>
        ))}
      </div>

      <div className="card" style={{ padding: 0 }}>
        <div style={{ padding: '16px 20px 12px', borderBottom: '1px solid #f0f0f0' }}>
          <strong style={{ fontSize: 14 }}>Recent Activity</strong>
          <span style={{ color: '#94a3b8', fontSize: 12, marginLeft: 8 }}>10 most recent syllabi</span>
        </div>
        {recent.length === 0 ? (
          <div className="empty">No syllabi yet.</div>
        ) : (
          <table>
            <thead>
              <tr><th>Code</th><th>Course Name</th><th>Created By</th><th>Status</th><th>Updated</th></tr>
            </thead>
            <tbody>
              {recent.map(s => (
                <tr key={s.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/syllabus/${s.id}`)}>
                  <td><strong>{s.courseCode}</strong></td>
                  <td>{s.courseName}</td>
                  <td>{s.createdByName || '—'}</td>
                  <td><StatusBadge status={s.status} /></td>
                  <td>{formatDate(s.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  )
}

// ─── USER MANAGEMENT TAB ────────────────────────────────────────────────────
function UserManagementTab({ users, onRefresh }) {
  const [search, setSearch] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editUser, setEditUser] = useState(null)
  const [form, setForm] = useState(EMPTY_USER_FORM)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const filtered = users.filter(u => {
    const q = search.toLowerCase()
    return !q || u.email.toLowerCase().includes(q) || u.role.toLowerCase().includes(q) ||
      (u.fullName && u.fullName.toLowerCase().includes(q))
  })

  function openAdd() {
    setForm(EMPTY_USER_FORM)
    setEditUser(null)
    setError('')
    setShowModal(true)
  }

  function openEdit(u) {
    setForm({ email: u.email, password: '', firstName: u.firstName, lastName: u.lastName, role: u.role, department: u.department || '' })
    setEditUser(u)
    setError('')
    setShowModal(true)
  }

  function closeModal() { setShowModal(false); setEditUser(null); setError('') }

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  }

  async function handleSave(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      if (editUser) {
        await api.adminUpdateUser(editUser.id, form)
        setSuccess('User updated.')
      } else {
        await api.adminCreateUser(form)
        setSuccess('User created.')
      }
      closeModal()
      onRefresh()
    } catch (err) { setError(err.message) }
    finally { setLoading(false) }
  }

  async function handleToggle(u) {
    const action = u.isActive ? 'lock' : 'unlock'
    if (!window.confirm(`${action.charAt(0).toUpperCase() + action.slice(1)} ${u.email}?`)) return
    try {
      await api.adminToggleStatus(u.id)
      setSuccess(`User ${action}ed.`)
      onRefresh()
    } catch (err) { setError(err.message) }
  }

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="filter-bar">
        <input
          placeholder="Search by email, name, or role…"
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <button className="btn btn-primary" onClick={openAdd}>+ Add User</button>
      </div>

      <div className="card" style={{ padding: 0 }}>
        {filtered.length === 0 ? (
          <div className="empty">No users found.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Email</th><th>Full Name</th><th>Role</th>
                <th>Department</th><th>Status</th><th>Last Login</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(u => (
                <tr key={u.id}>
                  <td>{u.email}</td>
                  <td>{u.fullName}</td>
                  <td><RoleBadge role={u.role} /></td>
                  <td>{u.department || '—'}</td>
                  <td><UserStatusBadge isActive={u.isActive} /></td>
                  <td>{formatDate(u.lastLogin)}</td>
                  <td>
                    <div className="actions">
                      <button className="btn btn-outline" onClick={() => openEdit(u)}>Edit</button>
                      <button
                        className={`btn ${u.isActive ? 'btn-danger' : 'btn-success'}`}
                        onClick={() => handleToggle(u)}
                      >
                        {u.isActive ? 'Lock' : 'Unlock'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeModal()}>
          <div className="modal">
            <div className="modal-header">
              <h2>{editUser ? 'Edit User' : 'Add New User'}</h2>
              <button className="modal-close" onClick={closeModal}>×</button>
            </div>
            <form onSubmit={handleSave}>
              <div className="modal-body">
                {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}
                <div className="form-group">
                  <label>Email *</label>
                  <input name="email" type="email" value={form.email} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>{editUser ? 'New Password (leave blank to keep)' : 'Password *'}</label>
                  <input name="password" type="password" value={form.password} onChange={handleChange}
                    required={!editUser} placeholder={editUser ? '(unchanged)' : ''} />
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>First Name *</label>
                    <input name="firstName" value={form.firstName} onChange={handleChange} required />
                  </div>
                  <div className="form-group">
                    <label>Last Name *</label>
                    <input name="lastName" value={form.lastName} onChange={handleChange} required />
                  </div>
                </div>
                <div className="form-row">
                  <div className="form-group">
                    <label>Role *</label>
                    <select name="role" value={form.role} onChange={handleChange} required>
                      {ROLES.map(r => <option key={r} value={r}>{r.replace('_', ' ')}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Department</label>
                    <input name="department" value={form.department} onChange={handleChange} />
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={closeModal}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Saving…' : editUser ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  )
}

// ─── SYLLABUS MANAGEMENT TAB ─────────────────────────────────────────────────
function SyllabusManagementTab({ syllabi, onRefresh }) {
  const navigate = useNavigate()
  const [statusFilter, setStatusFilter] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const filtered = statusFilter ? syllabi.filter(s => s.status === statusFilter) : syllabi

  async function handlePublish(id) {
    if (!window.confirm('Publish this syllabus?')) return
    try {
      await api.publish(id)
      setSuccess('Syllabus published.')
      setError('')
      onRefresh()
    } catch (e) { setError(e.message) }
  }

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="filter-bar">
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}
          style={{ minWidth: 160 }}>
          <option value="">All Statuses</option>
          {STATUSES.map(st => <option key={st} value={st}>{st.replace('_', ' ')}</option>)}
        </select>
        <span style={{ color: '#94a3b8', fontSize: 13 }}>
          Showing {filtered.length} of {syllabi.length}
        </span>
      </div>

      <div className="card" style={{ padding: 0 }}>
        {filtered.length === 0 ? (
          <div className="empty">No syllabi found.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Code</th><th>Course Name</th><th>Department</th>
                <th>Credits</th><th>Status</th><th>Updated</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(s => (
                <tr key={s.id}>
                  <td><strong>{s.courseCode}</strong></td>
                  <td>{s.courseName}</td>
                  <td>{s.department}</td>
                  <td>{s.credits}</td>
                  <td><StatusBadge status={s.status} /></td>
                  <td>{formatDate(s.updatedAt)}</td>
                  <td>
                    <div className="actions">
                      <button className="btn btn-outline" onClick={() => navigate(`/syllabus/${s.id}`)}>View</button>
                      {s.status === 'APPROVED' && (
                        <button className="btn btn-purple" onClick={() => handlePublish(s.id)}>Publish</button>
                      )}
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

// ─── MAIN PAGE ───────────────────────────────────────────────────────────────
export default function AdminPage() {
  const currentUser = getCurrentUser()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [tab, setTab] = useState('dashboard')
  const [syllabi, setSyllabi] = useState([])
  const [users, setUsers] = useState([])
  const [pageError, setPageError] = useState('')

  useEffect(() => { loadSyllabi() }, [])
  useEffect(() => { if (isAdmin) loadUsers() }, [isAdmin])

  async function loadSyllabi() {
    try { setSyllabi(await api.allList()) }
    catch (e) { setPageError(e.message) }
  }

  async function loadUsers() {
    try { setUsers(await api.adminUsers()) }
    catch (e) { setPageError(e.message) }
  }

  const TABS = [
    { key: 'dashboard', label: 'Dashboard' },
    ...(isAdmin ? [{ key: 'users', label: 'User Management' }] : []),
    { key: 'syllabi', label: 'Syllabus Management' },
  ]

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="header-row" style={{ marginBottom: 16 }}>
          <h1 className="page-title" style={{ marginBottom: 0 }}>Admin Panel</h1>
          <span style={{ color: '#94a3b8', fontSize: 13 }}>
            Logged in as <strong>{currentUser?.fullName}</strong> ({currentUser?.role})
          </span>
        </div>

        {pageError && <div className="alert alert-error">{pageError}</div>}

        <div className="tab-nav">
          {TABS.map(t => (
            <button
              key={t.key}
              className={`tab-btn${tab === t.key ? ' active' : ''}`}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {tab === 'dashboard' && <DashboardTab syllabi={syllabi} users={users} />}
        {tab === 'users' && isAdmin && (
          <UserManagementTab users={users} onRefresh={loadUsers} />
        )}
        {tab === 'syllabi' && (
          <SyllabusManagementTab syllabi={syllabi} onRefresh={loadSyllabi} />
        )}
      </div>
    </>
  )
}
