import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar.jsx'
import api from '../lib/api.js'

function StatusBadge({ status }) {
  return <span className={`status-badge status-${status}`}>{status.replace('_', ' ')}</span>
}

export default function PublicPage() {
  const navigate = useNavigate()
  const [list, setList] = useState([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => { doSearch('') }, [])

  async function doSearch(q) {
    setLoading(true)
    setError('')
    try {
      const data = await api.publicList(q)
      setList(data)
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  function handleSearch(e) {
    e.preventDefault()
    doSearch(query)
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1 className="page-title">Public Syllabus Catalog</h1>

        {error && <div className="alert alert-error">{error}</div>}

        <form className="search-bar" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="Search by course code, name, or department…"
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </form>

        <div className="card" style={{ padding: 0 }}>
          {list.length === 0 ? (
            <div className="empty">No approved syllabi found.</div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Code</th><th>Course Name</th><th>Department</th>
                  <th>Credits</th><th>Semester</th><th>Status</th><th></th>
                </tr>
              </thead>
              <tbody>
                {list.map(s => (
                  <tr key={s.id}>
                    <td><strong>{s.courseCode}</strong></td>
                    <td>{s.courseName}</td>
                    <td>{s.department}</td>
                    <td>{s.credits}</td>
                    <td>{s.semester}</td>
                    <td><StatusBadge status={s.status} /></td>
                    <td>
                      <button className="btn btn-primary" onClick={() => navigate(`/syllabus/${s.id}`)}>
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  )
}
