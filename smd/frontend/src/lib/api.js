const BASE_URL = 'http://localhost:8080'

export function setSession(authResponse) {
  localStorage.setItem('token', authResponse.token)
  localStorage.setItem('user', JSON.stringify(authResponse))
}

export function getCurrentUser() {
  const raw = localStorage.getItem('user')
  return raw ? JSON.parse(raw) : null
}

export function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

async function request(method, path, body) {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  })

  if (!res.ok) {
    let msg = `HTTP ${res.status}`
    try {
      const err = await res.json()
      msg = err.message || msg
    } catch (_) {}
    throw new Error(msg)
  }

  const text = await res.text()
  return text ? JSON.parse(text) : null
}

const api = {
  login: (email, password) => request('POST', '/api/auth/login', { email, password }),
  me: () => request('GET', '/api/auth/me'),

  createSyllabus: (data) => request('POST', '/api/syllabi', data),
  updateSyllabus: (id, data) => request('PUT', `/api/syllabi/${id}`, data),
  submitSyllabus: (id) => request('POST', `/api/syllabi/${id}/submit`),
  myList: () => request('GET', '/api/syllabi/mine'),
  pendingList: () => request('GET', '/api/syllabi/pending'),
  approve: (id, comments) => request('POST', `/api/syllabi/${id}/approve`, { comments }),
  reject: (id, comments) => request('POST', `/api/syllabi/${id}/reject`, { comments }),
  publish: (id) => request('POST', `/api/syllabi/${id}/publish`),
  getOne: (id) => request('GET', `/api/syllabi/${id}`),
  aiSummary: (id) => request('GET', `/api/syllabi/${id}/ai-summary`),
  getVersions: (id) => request('GET', `/api/syllabi/${id}/versions`),
  aiCompare: (id, versionId1, versionId2) => request('GET', `/api/syllabi/${id}/ai-compare?versionId1=${versionId1}&versionId2=${versionId2}`),
  allList: () => request('GET', '/api/syllabi/all'),
  newVersion: (id) => request('POST', `/api/syllabi/${id}/new-version`),

  getComments: (id) => request('GET', `/api/syllabi/${id}/comments`),
  addComment: (id, content) => request('POST', `/api/syllabi/${id}/comments`, { content }),
  deleteComment: (id, commentId) => request('DELETE', `/api/syllabi/${id}/comments/${commentId}`),
  commentCount: (id) => request('GET', `/api/syllabi/${id}/comments/count`),

  aaApprove: (id, comments) => request('POST', `/api/syllabi/${id}/aa-approve`, { comments }),
  aaReject: (id, comments) => request('POST', `/api/syllabi/${id}/aa-reject`, { comments }),
  pendingApproval: () => request('GET', '/api/syllabi/pending-approval'),
  departmentSearch: (keyword, status) => request('GET', `/api/syllabi/department-search?keyword=${encodeURIComponent(keyword || '')}&status=${encodeURIComponent(status || '')}`),
  searchAll: (keyword, status) => request('GET', `/api/syllabi/search?keyword=${encodeURIComponent(keyword || '')}&status=${encodeURIComponent(status || '')}`),

  followStatus: (id) => request('GET', `/api/syllabi/${id}/follow/status`),
  follow: (id) => request('POST', `/api/syllabi/${id}/follow`),
  unfollow: (id) => request('DELETE', `/api/syllabi/${id}/follow`),

  getFeedback: (id) => request('GET', `/api/syllabi/${id}/feedback`),
  submitFeedback: (id, content) => request('POST', `/api/syllabi/${id}/feedback`, { content }),

  getNotifications: () => request('GET', '/api/notifications'),
  markAllRead: () => request('PATCH', '/api/notifications/read-all'),
  markReadBySyllabus: (syllabusId) => request('PATCH', `/api/notifications/read-by-syllabus/${syllabusId}`),

  publicList: (q) => request('GET', `/api/public/syllabi?q=${encodeURIComponent(q || '')}`),
  publicOne: (id) => request('GET', `/api/public/syllabi/${id}`),
  publicAi: (id) => request('GET', `/api/public/syllabi/${id}/ai-summary`),
  subjectTree: (id) => request('GET', `/api/public/syllabi/${id}/subject-tree`),

  adminUsers: () => request('GET', '/api/admin/users'),
  adminCreateUser: (data) => request('POST', '/api/admin/users', data),
  adminUpdateUser: (id, data) => request('PUT', `/api/admin/users/${id}`, data),
  adminToggleStatus: (id) => request('PATCH', `/api/admin/users/${id}/toggle-status`)
}

export default api
