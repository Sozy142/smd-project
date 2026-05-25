import React, { useState, useEffect, useRef } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api, { getCurrentUser, logout } from '../lib/api.js'

export default function Navbar() {
  const navigate = useNavigate()
  const user = getCurrentUser()
  const [notifs, setNotifs] = useState([])
  const [open, setOpen] = useState(false)
  const dropRef = useRef(null)

  useEffect(() => {
    if (!user) return
    function load() { api.getNotifications().then(setNotifs).catch(() => {}) }
    load()
    const timer = setInterval(() => { if (getCurrentUser()) load() }, 30000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    if (!open) return
    function handleClick(e) {
      if (dropRef.current && !dropRef.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [open])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  function handleNotifClick(n) {
    setNotifs(prev => prev.map(x => x.id === n.id ? { ...x, isRead: true } : x))
    setOpen(false)
    const link = n.link || (n.syllabusId ? `/syllabus/${n.syllabusId}` : null)
    if (link) navigate(link)
  }

  function handleMarkAll() {
    api.markAllRead().catch(() => {})
    setNotifs(prev => prev.map(x => ({ ...x, isRead: true })))
  }

  function fmt(dt) {
    if (!dt) return ''
    const diff = (Date.now() - new Date(dt)) / 1000
    if (diff < 60) return 'just now'
    if (diff < 3600) return Math.floor(diff / 60) + 'm ago'
    if (diff < 86400) return Math.floor(diff / 3600) + 'h ago'
    return new Date(dt).toLocaleDateString()
  }

  const unread = notifs.filter(n => !n.isRead).length

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-logo">SMD</Link>
      <div className="navbar-right">
        <Link to="/public" style={{ color: '#cbd5e1', textDecoration: 'none', fontSize: 13 }}>
          Public Catalog
        </Link>

        {user?.role === 'HOD' && (
          <Link to="/review" style={{ color: '#cbd5e1', textDecoration: 'none', fontSize: 13 }}>
            Review
          </Link>
        )}
        {user?.role === 'ACADEMIC_AFFAIRS' && (
          <Link to="/aa" style={{ color: '#cbd5e1', textDecoration: 'none', fontSize: 13 }}>
            Academic Affairs
          </Link>
        )}

        {user && (
          <div style={{ position: 'relative' }} ref={dropRef}>
            <button
              onClick={() => setOpen(o => !o)}
              style={{
                background: 'none', border: 'none', cursor: 'pointer',
                color: '#cbd5e1', fontSize: 19, position: 'relative',
                padding: '2px 6px', lineHeight: 1
              }}
            >
              🔔
              {unread > 0 && (
                <span style={{
                  position: 'absolute', top: -3, right: -3,
                  background: '#ef4444', color: '#fff', borderRadius: '50%',
                  fontSize: 9, fontWeight: 700, minWidth: 16, height: 16,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  padding: '0 3px', lineHeight: 1
                }}>
                  {unread > 99 ? '99+' : unread}
                </span>
              )}
            </button>

            {open && (
              <div style={{
                position: 'absolute', right: 0, top: 'calc(100% + 6px)',
                background: '#fff', border: '1px solid #e2e8f0',
                borderRadius: 10, boxShadow: '0 8px 30px rgba(0,0,0,0.15)',
                width: 320, zIndex: 1000, overflow: 'hidden'
              }}>
                <div style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '12px 16px', borderBottom: '1px solid #f0f0f0'
                }}>
                  <span style={{ fontWeight: 700, fontSize: 14, color: '#1a1f36' }}>Notifications</span>
                  {unread > 0 && (
                    <button onClick={handleMarkAll} style={{
                      background: 'none', border: 'none', color: '#4f6ef7',
                      fontSize: 12, cursor: 'pointer', fontWeight: 600
                    }}>
                      Mark all read
                    </button>
                  )}
                </div>
                <div style={{ maxHeight: 360, overflowY: 'auto' }}>
                  {notifs.length === 0 ? (
                    <div style={{ padding: '20px 16px', color: '#94a3b8', fontSize: 13, textAlign: 'center' }}>
                      No notifications
                    </div>
                  ) : notifs.map(n => (
                    <div
                      key={n.id}
                      onClick={() => handleNotifClick(n)}
                      style={{
                        padding: '12px 16px', cursor: 'pointer',
                        background: n.isRead ? '#fff' : '#eef2ff',
                        borderBottom: '1px solid #f8f8f8'
                      }}
                      onMouseEnter={e => e.currentTarget.style.background = '#f1f5f9'}
                      onMouseLeave={e => e.currentTarget.style.background = n.isRead ? '#fff' : '#eef2ff'}
                    >
                      <p style={{
                        margin: '0 0 4px', fontSize: 13, lineHeight: 1.45,
                        fontWeight: n.isRead ? 400 : 600, color: '#1a1f36'
                      }}>
                        {n.message}
                      </p>
                      <span style={{ fontSize: 11, color: '#94a3b8' }}>{fmt(n.createdAt)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}

        {user ? (
          <>
            <span className="navbar-user">{user.fullName}</span>
            <span className="navbar-role">{user.role}</span>
            <button className="btn btn-outline" style={{ color: '#fff', borderColor: '#475569' }}
              onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <Link to="/login"><button className="btn btn-primary">Login</button></Link>
        )}
      </div>
    </nav>
  )
}
