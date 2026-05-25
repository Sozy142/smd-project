import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api, { setSession, getCurrentUser } from '../lib/api.js'

const QUICK_LOGINS = [
  { label: 'Admin', email: 'admin@smd.edu', password: 'admin123', cls: 'btn-secondary' },
  { label: 'Lecturer', email: 'lecturer@smd.edu', password: 'lecturer123', cls: 'btn-primary' },
  { label: 'HoD', email: 'hod@smd.edu', password: 'hod123', cls: 'btn-success' },
  { label: 'Acad. Affairs', email: 'aa@smd.edu', password: 'aa123', cls: 'btn-purple' },
  { label: 'Principal', email: 'principal@smd.edu', password: 'principal123', cls: 'btn-secondary' },
  { label: 'Student', email: 'student@smd.edu', password: 'student123', cls: 'btn-outline' },
]

function getRoleRoute(role) {
  switch (role) {
    case 'LECTURER': return '/lecturer'
    case 'HOD': case 'ACADEMIC_AFFAIRS': return '/review'
    case 'ADMIN': case 'PRINCIPAL': return '/admin'
    case 'STUDENT': return '/public'
    default: return '/'
  }
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function doLogin(e, pw) {
    e && e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await api.login(e ? email : e === null ? email : email, pw || password)
      setSession(res)
      navigate(getRoleRoute(res.role))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function quickLogin(q) {
    setError('')
    setLoading(true)
    try {
      const res = await api.login(q.email, q.password)
      setSession(res)
      navigate(getRoleRoute(res.role))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-title">SMD</div>
        <div className="login-subtitle">Syllabus Management & Digitalization</div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={(e) => { e.preventDefault(); doLogin(null, password) }}>
          <div className="form-group">
            <label>Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)}
              placeholder="you@smd.edu" required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)}
              placeholder="••••••••" required />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '10px' }}
            disabled={loading}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <div className="quick-login">
          <h4>Quick Login (Demo)</h4>
          <div className="quick-login-grid">
            {QUICK_LOGINS.map(q => (
              <button key={q.label} className={`btn ${q.cls}`}
                onClick={() => quickLogin(q)} disabled={loading}>
                {q.label}
              </button>
            ))}
          </div>
        </div>

        <div style={{ marginTop: 20, textAlign: 'center', fontSize: 13, color: '#94a3b8' }}>
          <Link to="/public" style={{ color: '#4f6ef7' }}>Browse public syllabus catalog →</Link>
        </div>
      </div>
    </div>
  )
}
