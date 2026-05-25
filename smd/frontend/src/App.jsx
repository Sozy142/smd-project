import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { getCurrentUser } from './lib/api.js'
import LoginPage from './pages/LoginPage.jsx'
import LecturerPage from './pages/LecturerPage.jsx'
import ReviewPage from './pages/ReviewPage.jsx'
import AcademicAffairsPage from './pages/AcademicAffairsPage.jsx'
import AdminPage from './pages/AdminPage.jsx'
import PublicPage from './pages/PublicPage.jsx'
import DetailPage from './pages/DetailPage.jsx'

function RoleRedirect() {
  const user = getCurrentUser()
  if (!user) return <Navigate to="/login" replace />
  switch (user.role) {
    case 'LECTURER': return <Navigate to="/lecturer" replace />
    case 'HOD': return <Navigate to="/review" replace />
    case 'ACADEMIC_AFFAIRS': return <Navigate to="/aa" replace />
    case 'ADMIN':
    case 'PRINCIPAL': return <Navigate to="/admin" replace />
    case 'STUDENT': return <Navigate to="/public" replace />
    default: return <Navigate to="/login" replace />
  }
}

function ProtectedRoute({ children, roles }) {
  const user = getCurrentUser()
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RoleRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/public" element={<PublicPage />} />
      <Route path="/syllabus/:id" element={<DetailPage />} />
      <Route path="/lecturer" element={
        <ProtectedRoute roles={['LECTURER']}><LecturerPage /></ProtectedRoute>
      } />
      <Route path="/review" element={
        <ProtectedRoute roles={['HOD']}><ReviewPage /></ProtectedRoute>
      } />
      <Route path="/aa" element={
        <ProtectedRoute roles={['ACADEMIC_AFFAIRS']}><AcademicAffairsPage /></ProtectedRoute>
      } />
      <Route path="/admin" element={
        <ProtectedRoute roles={['ADMIN', 'PRINCIPAL']}><AdminPage /></ProtectedRoute>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
