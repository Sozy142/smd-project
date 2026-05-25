import React, { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api, { getCurrentUser } from '../lib/api.js'

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace('_', ' ')}</span>
}

function Field({ label, value }) {
  if (!value) return null
  return (
    <div className="detail-field">
      <label>{label}</label>
      <p>{value}</p>
    </div>
  )
}

function CloPloMatrixReadOnly({ learningOutcomes, ploOutcomes, cloMappings }) {
  const clos = (learningOutcomes || '').split('\n').map(l => l.trim()).filter(Boolean)
  const plos = (ploOutcomes || '').split('\n').map(l => l.trim()).filter(Boolean)
  let mappings = {}
  try { mappings = JSON.parse(cloMappings || '{}') } catch { mappings = {} }

  if (clos.length === 0 && plos.length === 0) {
    return <div className="empty" style={{ padding: '12px 0', textAlign: 'left', color: '#94a3b8', fontSize: 13 }}>No CLO or PLO data defined for this syllabus.</div>
  }
  if (plos.length === 0) {
    return <div className="empty" style={{ padding: '12px 0', textAlign: 'left', color: '#94a3b8', fontSize: 13 }}>No PLO outcomes defined. Edit this syllabus to add PLO outcomes and create the mapping matrix.</div>
  }
  if (clos.length === 0) {
    return <div className="empty" style={{ padding: '12px 0', textAlign: 'left', color: '#94a3b8', fontSize: 13 }}>No CLO (Learning Outcomes) defined. Edit this syllabus to add learning outcomes.</div>
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: 'auto', minWidth: '100%' }}>
        <thead>
          <tr>
            <th style={{ minWidth: 200 }}>CLO \ PLO</th>
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
                <td key={pi} style={{ textAlign: 'center', color: '#22c55e', fontWeight: 700, fontSize: 16 }}>
                  {mappings[clo] && mappings[clo].includes(plo) ? '✓' : ''}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function SubjectRoadmap({ syllabusId }) {
  const navigate = useNavigate()
  const [tree, setTree] = useState(null)
  const [isMobile, setIsMobile] = useState(window.innerWidth < 640)

  useEffect(() => {
    api.subjectTree(syllabusId).then(setTree).catch(() => {})
  }, [syllabusId])

  useEffect(() => {
    const handler = () => setIsMobile(window.innerWidth < 640)
    window.addEventListener('resize', handler)
    return () => window.removeEventListener('resize', handler)
  }, [])

  if (!tree) return null

  const CFG = {
    prereq:  { bg: '#f1f5f9', border: '1.5px solid #cbd5e1', text: '#334155', creditBg: '#e2e8f0', creditText: '#475569', shadow: '0 1px 3px rgba(0,0,0,0.08)' },
    current: { bg: '#4f6ef7', border: '2px solid #3b5bdb',   text: '#fff',    creditBg: 'rgba(255,255,255,0.22)', creditText: '#fff', shadow: '0 4px 14px rgba(79,110,247,0.38)' },
    next:    { bg: '#f0fdf4', border: '1.5px solid #86efac', text: '#166534', creditBg: '#dcfce7', creditText: '#166534', shadow: '0 1px 3px rgba(0,0,0,0.08)' },
  }

  function card(course, variant) {
    const c = CFG[variant]
    const isCur = variant === 'current'
    return (
      <div
        key={course.id}
        onClick={() => navigate(`/syllabus/${course.id}`)}
        style={{
          background: c.bg, border: c.border, color: c.text, borderRadius: 10,
          padding: isCur ? '13px 18px' : '10px 14px',
          width: isCur ? 175 : 152,
          boxShadow: c.shadow, cursor: 'pointer', textAlign: 'center',
          transition: 'transform 0.14s', userSelect: 'none',
        }}
        onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-2px)' }}
        onMouseLeave={e => { e.currentTarget.style.transform = '' }}
      >
        <div style={{ fontSize: isCur ? 15 : 13, fontWeight: 800, marginBottom: 3 }}>{course.courseCode}</div>
        <div style={{ fontSize: 11, opacity: 0.82, lineHeight: 1.35, marginBottom: course.credits != null ? 7 : 0 }}>{course.courseName}</div>
        {course.credits != null && (
          <span style={{ background: c.creditBg, color: c.creditText, borderRadius: 20, padding: '1px 8px', fontSize: 10, fontWeight: 700 }}>
            {course.credits} cr
          </span>
        )}
      </div>
    )
  }

  function emptyNote(text) {
    return (
      <div style={{ width: 152, padding: '10px 12px', background: '#f8fafc', border: '1.5px dashed #e2e8f0', borderRadius: 8, fontSize: 11, color: '#94a3b8', textAlign: 'center', lineHeight: 1.4 }}>
        {text}
      </div>
    )
  }

  function colLabel(text, color) {
    return <div style={{ fontSize: 9, fontWeight: 800, color, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 7, textAlign: 'center' }}>{text}</div>
  }

  const arrow = isMobile
    ? <div style={{ fontSize: 18, color: '#cbd5e1', textAlign: 'center', margin: '4px 0' }}>↓</div>
    : <div style={{ fontSize: 20, color: '#cbd5e1', padding: '0 10px', alignSelf: 'center', flexShrink: 0, marginTop: 18 }}>→</div>

  return (
    <div className="card" style={{ paddingBottom: 20 }}>
      <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>🗺️ Subject Roadmap</h2>
      <div style={{ display: 'flex', flexDirection: isMobile ? 'column' : 'row', alignItems: isMobile ? 'center' : 'flex-start', justifyContent: 'center', gap: 0 }}>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {colLabel('Prerequisites', '#94a3b8')}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {tree.prerequisites.length > 0
              ? tree.prerequisites.map(p => card(p, 'prereq'))
              : emptyNote('Foundational course')}
          </div>
        </div>

        {arrow}

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {colLabel('Current', '#4f6ef7')}
          {card(tree.current, 'current')}
        </div>

        {arrow}

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          {colLabel('Next Courses', '#16a34a')}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {tree.nextCourses.length > 0
              ? tree.nextCourses.map(n => card(n, 'next'))
              : emptyNote('Advanced course')}
          </div>
        </div>

      </div>
    </div>
  )
}

function CommentsSection({ syllabusId, currentUser, comments, commentsErr, onRefresh }) {
  const [text, setText] = useState('')
  const [posting, setPosting] = useState(false)
  const [err, setErr] = useState('')

  const canComment = currentUser && ['LECTURER', 'HOD', 'ACADEMIC_AFFAIRS', 'PRINCIPAL', 'ADMIN'].includes(currentUser.role)

  async function handlePost() {
    if (!text.trim()) return
    setPosting(true)
    setErr('')
    try {
      await api.addComment(syllabusId, text.trim())
      setText('')
      onRefresh()
    } catch (e) { setErr(e.message) }
    finally { setPosting(false) }
  }

  function initials(name) {
    return name.split(' ').map(n => n[0] || '').join('').toUpperCase().slice(0, 2)
  }

  function fmtDate(dt) {
    if (!dt) return ''
    return new Date(dt).toLocaleString()
  }

  const displayErr = err || commentsErr

  return (
    <div className="card">
      <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>💬 Collaborative Review</h2>

      {displayErr && <div className="alert alert-error" style={{ marginBottom: 12 }}>{displayErr}</div>}

      {comments.length === 0 ? (
        <div className="empty" style={{ padding: '12px 0', marginBottom: 16 }}>No comments yet.</div>
      ) : (
        <div style={{ marginBottom: canComment ? 20 : 0 }}>
          {comments.map(c => (
            <div key={c.id} style={{
              display: 'flex', gap: 12, padding: '12px 14px',
              background: c.owner ? '#eef2ff' : '#fafafa',
              borderRadius: 8, marginBottom: 8
            }}>
              <div style={{
                width: 38, height: 38, borderRadius: '50%', background: '#4f6ef7',
                color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 13, fontWeight: 700, flexShrink: 0, letterSpacing: 0.5
              }}>
                {initials(c.authorName)}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6, flexWrap: 'wrap' }}>
                  <strong style={{ fontSize: 13, color: '#1a1f36' }}>{c.authorName}</strong>
                  <span className={`role-badge role-${c.authorRole}`}>{c.authorRole.replace(/_/g, ' ')}</span>
                  {c.owner && <span style={{ fontSize: 11, color: '#4f6ef7', fontWeight: 600 }}>You</span>}
                  <span style={{ color: '#94a3b8', fontSize: 11 }}>{fmtDate(c.createdAt)}</span>
                </div>
                <p style={{ fontSize: 13, color: '#334155', lineHeight: 1.65, whiteSpace: 'pre-wrap', margin: 0 }}>
                  {c.content}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}

      {canComment && (
        <div>
          <textarea
            value={text}
            onChange={e => setText(e.target.value)}
            rows={3}
            placeholder="Write a comment…"
            style={{
              width: '100%', padding: '9px 12px', border: '1.5px solid #e2e8f0',
              borderRadius: 6, fontSize: 13, fontFamily: 'inherit', resize: 'vertical',
              marginBottom: 10, boxSizing: 'border-box'
            }}
          />
          <button
            className="btn btn-primary"
            onClick={handlePost}
            disabled={posting || !text.trim()}
          >{posting ? 'Posting…' : 'Post Comment'}</button>
        </div>
      )}
    </div>
  )
}

function FeedbackSection({ syllabusId, currentUser, feedbackList, onRefresh }) {
  const [text, setText] = useState('')
  const [posting, setPosting] = useState(false)

  const canPost = currentUser?.role === 'STUDENT'

  async function handleSubmit() {
    if (!text.trim()) return
    setPosting(true)
    try {
      await api.submitFeedback(syllabusId, text.trim())
      setText('')
      onRefresh()
    } catch (e) { console.log('Submit feedback error:', e) }
    finally { setPosting(false) }
  }

  function initials(name) {
    return name.split(' ').map(n => n[0] || '').join('').toUpperCase().slice(0, 2)
  }

  function fmtDate(dt) {
    if (!dt) return ''
    return new Date(dt).toLocaleString()
  }

  return (
    <div className="card">
      <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>💬 Student Feedback</h2>

      {feedbackList.length === 0 ? (
        <div className="empty" style={{ padding: '12px 0', marginBottom: canPost ? 16 : 0 }}>
          {canPost ? 'Be the first to leave feedback.' : 'No student feedback yet.'}
        </div>
      ) : (
        <div style={{ marginBottom: canPost ? 20 : 0 }}>
          {feedbackList.map(f => {
            const isOwn = currentUser?.userId === f.studentId
            return (
              <div key={f.id} style={{
                display: 'flex', gap: 12, padding: '12px 14px',
                background: isOwn ? '#f0fdf4' : '#fafafa',
                borderRadius: 8, marginBottom: 8
              }}>
                <div style={{
                  width: 38, height: 38, borderRadius: '50%', background: '#22c55e',
                  color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 13, fontWeight: 700, flexShrink: 0
                }}>
                  {initials(f.studentName)}
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6, flexWrap: 'wrap' }}>
                    <strong style={{ fontSize: 13, color: '#1a1f36' }}>{f.studentName}</strong>
                    {isOwn && <span style={{ fontSize: 11, color: '#16a34a', fontWeight: 600 }}>You</span>}
                    <span style={{ color: '#94a3b8', fontSize: 11 }}>{fmtDate(f.createdAt)}</span>
                  </div>
                  <p style={{ fontSize: 13, color: '#334155', lineHeight: 1.65, whiteSpace: 'pre-wrap', margin: 0 }}>
                    {f.content}
                  </p>
                </div>
              </div>
            )
          })}
        </div>
      )}

      {canPost && (
        <div>
          <textarea
            value={text}
            onChange={e => setText(e.target.value)}
            rows={3}
            placeholder="Share your thoughts about this syllabus…"
            style={{
              width: '100%', padding: '9px 12px', border: '1.5px solid #e2e8f0',
              borderRadius: 6, fontSize: 13, fontFamily: 'inherit', resize: 'vertical',
              marginBottom: 10, boxSizing: 'border-box'
            }}
          />
          <button
            className="btn btn-primary"
            onClick={handleSubmit}
            disabled={posting || !text.trim()}
          >{posting ? 'Submitting…' : 'Submit'}</button>
        </div>
      )}
    </div>
  )
}

export default function DetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const user = getCurrentUser()
  const [syllabus, setSyllabus] = useState(null)
  const [aiText, setAiText] = useState('')
  const [aiLoading, setAiLoading] = useState(false)
  const [error, setError] = useState('')
  const [comments, setComments] = useState([])
  const [commentsErr, setCommentsErr] = useState('')
  const [followStatus, setFollowStatus] = useState({ following: false, followerCount: 0 })
  const [feedbackList, setFeedbackList] = useState([])
  const [versions, setVersions] = useState([])
  const [v1Id, setV1Id] = useState('')
  const [v2Id, setV2Id] = useState('')
  const [compareResult, setCompareResult] = useState(null)
  const [compareLoading, setCompareLoading] = useState(false)
  const [compareError, setCompareError] = useState('')
  const [aiError, setAiError] = useState('')

  const isStudent = user?.role === 'STUDENT'
  const canSeeComments = user && ['LECTURER', 'HOD', 'ACADEMIC_AFFAIRS', 'PRINCIPAL', 'ADMIN'].includes(user.role)
  const canSeeFeedback = isStudent || canSeeComments
  const canCompare = user && ['LECTURER', 'HOD', 'ACADEMIC_AFFAIRS', 'ADMIN', 'PRINCIPAL'].includes(user.role)

  useEffect(() => {
    async function load() {
      try {
        const data = user ? await api.getOne(id) : await api.publicOne(id)
        setSyllabus(data)
      } catch (e) { setError(e.message) }
    }
    load()
  }, [id])

  useEffect(() => {
    if (canSeeComments) loadComments()
  }, [id, canSeeComments])

  useEffect(() => {
    if (canSeeFeedback) loadFeedback()
  }, [id, canSeeFeedback])

  useEffect(() => {
    if (user) api.markReadBySyllabus(id).catch(() => {})
  }, [id])

  useEffect(() => {
    api.followStatus(id)
      .then(data => { console.log('Follow status:', data); setFollowStatus(data) })
      .catch(e => console.log('Follow status error:', e))
  }, [id])

  useEffect(() => {
    if (canCompare) {
      api.getVersions(id)
        .then(data => {
          setVersions(data)
          if (data.length >= 2) {
            setV1Id(String(data[0].id))
            setV2Id(String(data[data.length - 1].id))
          }
        })
        .catch(() => {})
    }
  }, [id, canCompare])

  async function loadComments() {
    try {
      setCommentsErr('')
      setComments(await api.getComments(id))
    } catch (e) { setCommentsErr(e.message) }
  }

  async function loadFeedback() {
    try {
      setFeedbackList(await api.getFeedback(id))
    } catch (e) { console.log('Load feedback error:', e) }
  }

  async function handleAiSummary() {
    setAiLoading(true)
    setAiText('')
    setAiError('')
    try {
      const text = user ? await api.aiSummary(id) : await api.publicAi(id)
      setAiText(text)
    } catch (e) {
      setAiError('AI Summary tạm thời không khả dụng. Vui lòng thử lại sau.')
    }
    finally { setAiLoading(false) }
  }

  async function handleCompare() {
    if (!v1Id || !v2Id || v1Id === v2Id) return
    setCompareLoading(true)
    setCompareResult(null)
    setCompareError('')
    try {
      const result = await api.aiCompare(id, v1Id, v2Id)
      setCompareResult(result)
    } catch (e) {
      setCompareError('So sánh AI tạm thời không khả dụng. Vui lòng thử lại sau.')
    }
    finally { setCompareLoading(false) }
  }

  async function handleFollow() {
    try {
      await api.follow(id)
      setFollowStatus(prev => ({ ...prev, following: true, followerCount: prev.followerCount + 1 }))
    } catch (e) { console.log('Follow error:', e) }
  }

  async function handleUnfollow() {
    if (!window.confirm('Unfollow this course?')) return
    try {
      await api.unfollow(id)
      setFollowStatus(prev => ({ ...prev, following: false, followerCount: Math.max(0, prev.followerCount - 1) }))
    } catch (e) { console.log('Follow error:', e) }
  }

  if (error) return (
    <>
      <Navbar />
      <div className="container">
        <div className="alert alert-error">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>← Back</button>
      </div>
    </>
  )

  if (!syllabus) return (
    <>
      <Navbar />
      <div className="container"><div className="empty">Loading…</div></div>
    </>
  )

  const isPublished = syllabus.status === 'PUBLISHED'

  const aiSummaryCard = (
    <div className="card">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
        <h2 style={{ fontSize: 16, fontWeight: 700 }}>AI Summary</h2>
        <button className="btn btn-purple" onClick={handleAiSummary} disabled={aiLoading}>
          {aiLoading ? (
            <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ width: 12, height: 12, border: '2px solid rgba(255,255,255,0.4)', borderTopColor: '#fff', borderRadius: '50%', display: 'inline-block', animation: 'spin 0.7s linear infinite' }} />
              Generating…
            </span>
          ) : 'Generate'}
        </button>
      </div>
      {aiError && (
        <div className="alert alert-error" style={{ marginBottom: 0 }}>{aiError}</div>
      )}
      {!aiError && aiText ? (
        <>
          <div className="ai-box">{aiText}</div>
          <div style={{ marginTop: 8, fontSize: 11, color: '#8b5cf6', fontWeight: 600, textAlign: 'right' }}>
            Powered by SMD AI ✨
          </div>
        </>
      ) : !aiError && (
        <div className="empty" style={{ padding: 16 }}>
          Click "Generate" to get an AI-powered summary of this syllabus.
        </div>
      )}
    </div>
  )

  const aiCompareCard = canCompare && (
    <div className="card">
      <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>🤖 AI Version Comparison</h2>
      {versions.length < 2 ? (
        <div className="empty" style={{ padding: '12px 0' }}>
          At least 2 submitted versions needed for comparison.
        </div>
      ) : (
        <>
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'flex-end', marginBottom: 14 }}>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: '#555', display: 'block', marginBottom: 4 }}>
                Compare version
              </label>
              <select
                value={v1Id}
                onChange={e => setV1Id(e.target.value)}
                style={{ padding: '7px 10px', border: '1.5px solid #e2e8f0', borderRadius: 6, fontSize: 13, background: '#fff' }}
              >
                {versions.map(v => (
                  <option key={v.id} value={String(v.id)}>
                    v{v.versionNumber} — {new Date(v.snapshotAt).toLocaleDateString()}
                  </option>
                ))}
              </select>
            </div>
            <div style={{ fontSize: 13, color: '#94a3b8', alignSelf: 'flex-end', paddingBottom: 8 }}>vs</div>
            <div>
              <label style={{ fontSize: 12, fontWeight: 600, color: '#555', display: 'block', marginBottom: 4 }}>
                With version
              </label>
              <select
                value={v2Id}
                onChange={e => setV2Id(e.target.value)}
                style={{ padding: '7px 10px', border: '1.5px solid #e2e8f0', borderRadius: 6, fontSize: 13, background: '#fff' }}
              >
                {versions.map(v => (
                  <option key={v.id} value={String(v.id)}>
                    v{v.versionNumber} — {new Date(v.snapshotAt).toLocaleDateString()}
                  </option>
                ))}
              </select>
            </div>
            <button
              className="btn btn-purple"
              onClick={handleCompare}
              disabled={compareLoading || !v1Id || !v2Id || v1Id === v2Id}
              style={{ alignSelf: 'flex-end' }}
            >
              {compareLoading ? (
                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={{ width: 12, height: 12, border: '2px solid rgba(255,255,255,0.4)', borderTopColor: '#fff', borderRadius: '50%', display: 'inline-block', animation: 'spin 0.7s linear infinite' }} />
                  Comparing…
                </span>
              ) : 'Compare with AI'}
            </button>
          </div>

          {compareError && (
            <div className="alert alert-error">{compareError}</div>
          )}
          {compareResult && (
            <>
              <div style={{
                background: '#f5f3ff', border: '1px solid #ddd6fe', borderRadius: 8,
                padding: '14px 16px', fontSize: 13, lineHeight: 1.75,
                whiteSpace: 'pre-wrap', color: '#1e1b4b'
              }}>
                <strong style={{ display: 'block', marginBottom: 8, color: '#5b21b6' }}>
                  v{compareResult.version1Number} → v{compareResult.version2Number}
                </strong>
                {compareResult.comparison}
              </div>
              <div style={{ marginTop: 8, fontSize: 11, color: '#8b5cf6', fontWeight: 600, textAlign: 'right' }}>
                Powered by Gemini AI ✨
              </div>
            </>
          )}
        </>
      )}
    </div>
  )

  return (
    <>
      <Navbar />
      <div className="container">

        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 20, flexWrap: 'wrap' }}>
          <button className="btn btn-secondary" onClick={() => navigate(-1)}>← Back</button>
          <h1 style={{ fontSize: 20, fontWeight: 700, color: '#1a1f36' }}>
            {syllabus.courseCode} — {syllabus.courseName}
          </h1>
          <StatusBadge status={syllabus.status} />

          {isPublished && (
            <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 10 }}>
              {isStudent ? (
                <button
                  className={followStatus.following ? 'btn btn-outline' : 'btn btn-primary'}
                  style={followStatus.following ? { borderColor: '#22c55e', color: '#15803d' } : {}}
                  onClick={followStatus.following ? handleUnfollow : handleFollow}
                >
                  {followStatus.following ? '✓ Following' : '🔔 Follow'}
                </button>
              ) : !user ? (
                <Link to="/login" style={{ fontSize: 13, color: '#4f6ef7', textDecoration: 'none' }}>
                  Login to follow
                </Link>
              ) : null}
              <span style={{ fontSize: 12, color: '#94a3b8' }}>
                {followStatus.followerCount} follower{followStatus.followerCount !== 1 ? 's' : ''}
              </span>
            </div>
          )}
        </div>

        {/* Syllabus info card */}
        <div className="card">
          <div className="detail-grid">
            <Field label="Course Code" value={syllabus.courseCode} />
            <Field label="Course Name" value={syllabus.courseName} />
            <Field label="Department" value={syllabus.department} />
            <Field label="Credits" value={syllabus.credits?.toString()} />
            <Field label="Academic Year" value={syllabus.academicYear} />
            <Field label="Semester" value={syllabus.semester} />
            <div className="detail-field full">
              <label>Description</label>
              <p>{syllabus.description || '—'}</p>
            </div>
            <div className="detail-field full">
              <label>Learning Outcomes (CLOs)</label>
              <p>{syllabus.learningOutcomes || '—'}</p>
            </div>
            {syllabus.ploOutcomes && (
              <div className="detail-field full">
                <label>Program Learning Outcomes (PLOs)</label>
                <p>{syllabus.ploOutcomes}</p>
              </div>
            )}
            <div className="detail-field full">
              <label>Assessment Methods</label>
              <p>{syllabus.assessmentMethods || '—'}</p>
            </div>
            <Field label="Prerequisites" value={syllabus.prerequisites} />
            <Field label="Materials" value={syllabus.materials} />
            <Field label="Created By" value={syllabus.createdByName} />
            <Field label="Reviewed By" value={syllabus.reviewedByName} />
            <Field label="Version" value={`v${syllabus.versionNumber}`} />
            {syllabus.rejectionReason && (
              <div className="detail-field full">
                <label>Rejection Reason</label>
                <p style={{ color: '#991b1b' }}>{syllabus.rejectionReason}</p>
              </div>
            )}
          </div>
        </div>

        {/* Subject Roadmap */}
        <SubjectRoadmap syllabusId={id} />

        {/* CLO-PLO Mapping */}
        <div className="card">
          <h2 style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>CLO-PLO Mapping</h2>
          <CloPloMatrixReadOnly
            learningOutcomes={syllabus.learningOutcomes}
            ploOutcomes={syllabus.ploOutcomes}
            cloMappings={syllabus.cloMappings}
          />
        </div>

        {/* Role-based section order */}
        {isStudent ? (
          <>
            {aiSummaryCard}
            <FeedbackSection
              syllabusId={id}
              currentUser={user}
              feedbackList={feedbackList}
              onRefresh={loadFeedback}
            />
          </>
        ) : canSeeComments ? (
          <>
            {['HOD', 'ACADEMIC_AFFAIRS'].includes(user.role) && (
              <div className="card">
                <p style={{ fontSize: 14, color: '#334155', margin: 0 }}>
                  <span style={{ fontWeight: 700, fontSize: 22, color: '#4f6ef7', marginRight: 8 }}>
                    {comments.length}
                  </span>
                  comment{comments.length !== 1 ? 's' : ''} from Collaborative Review
                </p>
              </div>
            )}
            <CommentsSection
              syllabusId={id}
              currentUser={user}
              comments={comments}
              commentsErr={commentsErr}
              onRefresh={loadComments}
            />
            <FeedbackSection
              syllabusId={id}
              currentUser={user}
              feedbackList={feedbackList}
              onRefresh={loadFeedback}
            />
            {aiSummaryCard}
            {aiCompareCard}
          </>
        ) : (
          aiSummaryCard
        )}

      </div>
    </>
  )
}
