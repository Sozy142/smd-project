Build a complete, runnable full-stack web application called SMD 
(Syllabus Management and Digitalization System) for a university. 
The app must run with: Spring Boot 3 backend + MySQL + React (Vite) frontend.

═══════════════════════════════════════
PROJECT STRUCTURE
═══════════════════════════════════════
smd/
├── backend/   (Spring Boot Maven project)
└── frontend/  (React + Vite)

═══════════════════════════════════════
BACKEND — Spring Boot 3 + MySQL
═══════════════════════════════════════

Dependencies (pom.xml):
- spring-boot-starter-web
- spring-boot-starter-security
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- mysql-connector-j
- jjwt-api + jjwt-impl + jjwt-jackson (version 0.12.6)
- lombok

application.properties:
  server.port=8080
  spring.datasource.url=jdbc:mysql://localhost:3306/smd_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
  spring.datasource.username=root
  spring.datasource.password=root
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
  app.jwt.secret=smd-super-secret-key-min-256-bits-for-hs256-algorithm-okay
  app.jwt.expiration-ms=86400000

────────────────────────────────────────
ENTITIES (JPA)
────────────────────────────────────────

1. Role (enum): ADMIN, LECTURER, HOD, ACADEMIC_AFFAIRS, PRINCIPAL, STUDENT

2. SyllabusStatus (enum): DRAFT, PENDING_REVIEW, APPROVED, REJECTED, PUBLISHED

3. User entity → table "users":
   - id (PK auto)
   - email (unique, not null)
   - passwordHash
   - firstName, lastName
   - role (enum)
   - department
   - isActive (default true)
   - createdAt, lastLogin

4. Syllabus entity → table "syllabi":
   - id (PK auto)
   - courseCode, courseName
   - department, credits, academicYear, semester
   - description (TEXT)
   - learningOutcomes (TEXT) — CLOs
   - assessmentMethods (TEXT)
   - prerequisites, materials (TEXT)
   - status (enum, default DRAFT)
   - versionNumber (default 1)
   - rejectionReason
   - createdBy → ManyToOne User
   - reviewedBy → ManyToOne User (nullable)
   - createdAt, updatedAt (@PrePersist/@PreUpdate)

5. ApprovalHistory entity → table "approval_history":
   - id (PK auto)
   - syllabus → ManyToOne
   - actor → ManyToOne User
   - actorRole (enum)
   - action (String: SUBMITTED/APPROVED/REJECTED/PUBLISHED)
   - comments
   - actionDate (@PrePersist)

────────────────────────────────────────
REPOSITORIES (JpaRepository)
────────────────────────────────────────
- UserRepository: findByEmail, existsByEmail, findByRole
- SyllabusRepository:
    findByCreatedByOrderByUpdatedAtDesc(User)
    findByStatusOrderByUpdatedAtDesc(SyllabusStatus)
    findByStatusInOrderByUpdatedAtDesc(List<SyllabusStatus>)
    @Query search by keyword in courseCode/courseName/department with status filter
- ApprovalHistoryRepository:
    findBySyllabusOrderByActionDateDesc(Syllabus)

────────────────────────────────────────
SECURITY
────────────────────────────────────────
- JwtService: generateToken(User), extractEmail(token), isTokenValid(token, email)
  Token payload: subject=email, claims: uid, role, name
- CustomUserDetailsService: loadUserByUsername(email) → check isActive
- JwtAuthenticationFilter: OncePerRequestFilter, parse Bearer token, set SecurityContext
- SecurityConfig:
    CSRF disabled, STATELESS session
    CORS: allow http://localhost:3000 and http://localhost:5173
    Public routes: /api/auth/**, /api/public/**
    All other routes: authenticated
    @EnableMethodSecurity for @PreAuthorize

────────────────────────────────────────
DTOs
────────────────────────────────────────
- LoginRequest: email, password (validated)
- AuthResponse: token, userId, email, fullName, role, department
- SyllabusRequest: courseCode*, courseName*, credits*, department,
  academicYear, semester, description, learningOutcomes,
  assessmentMethods, prerequisites, materials
- SyllabusResponse: all fields + createdByName + reviewedByName
  (static factory: SyllabusResponse.from(Syllabus))
- ReviewDecisionRequest: comments

────────────────────────────────────────
SERVICES
────────────────────────────────────────

AuthService:
  login(LoginRequest) → AuthResponse
    - authenticate via AuthenticationManager
    - update lastLogin
    - return token + user info

SyllabusService:
  // LECTURER
  create(SyllabusRequest, email) → SyllabusResponse
  update(id, SyllabusRequest, email) → SyllabusResponse
    - only owner can edit
    - only DRAFT or REJECTED status allowed
    - if REJECTED → increment versionNumber, reset to DRAFT
  submit(id, email) → SyllabusResponse
    - only DRAFT allowed → set PENDING_REVIEW
    - log ApprovalHistory action=SUBMITTED

  // HOD / ACADEMIC_AFFAIRS
  approve(id, comments, email) → SyllabusResponse
    - only PENDING_REVIEW → set APPROVED
    - set reviewedBy
    - log action=APPROVED
  reject(id, reason, email) → SyllabusResponse
    - only PENDING_REVIEW → set REJECTED
    - reason required
    - log action=REJECTED

  // ADMIN / PRINCIPAL
  publish(id, email) → SyllabusResponse
    - only APPROVED → set PUBLISHED
    - log action=PUBLISHED

  // READ
  listMine(email) — for Lecturer
  listPendingReview() — for HOD/AA
  listAll() — for Admin/Principal
  listPublic(keyword) — no auth needed
  getById(id, email) — STUDENT only sees APPROVED/PUBLISHED
  aiSummary(id) → String (mock, format nicely with course info)

────────────────────────────────────────
CONTROLLERS
────────────────────────────────────────

AuthController (/api/auth):
  POST /login → login
  GET  /me    → current user info

SyllabusController (/api/syllabi):
  POST   /              @PreAuthorize LECTURER → create
  PUT    /{id}          @PreAuthorize LECTURER → update
  POST   /{id}/submit   @PreAuthorize LECTURER → submit
  GET    /mine          @PreAuthorize LECTURER → listMine
  GET    /pending       @PreAuthorize HOD,ACADEMIC_AFFAIRS,PRINCIPAL → listPendingReview
  POST   /{id}/approve  @PreAuthorize HOD,ACADEMIC_AFFAIRS → approve
  POST   /{id}/reject   @PreAuthorize HOD,ACADEMIC_AFFAIRS → reject
  POST   /{id}/publish  @PreAuthorize ADMIN,PRINCIPAL → publish
  GET    /all           @PreAuthorize ADMIN,PRINCIPAL → listAll
  GET    /{id}          authenticated → getById
  GET    /{id}/ai-summary → aiSummary (mock)

PublicController (/api/public):
  GET /syllabi?q=keyword → listPublic (no auth)
  GET /syllabi/{id}      → public detail (only APPROVED/PUBLISHED)
  GET /syllabi/{id}/ai-summary → public AI mock

GlobalExceptionHandler (@RestControllerAdvice):
  Handle ApiException, BadCredentialsException, AccessDeniedException,
  MethodArgumentNotValidException, Exception
  Always return: { timestamp, status, error, message }

DataSeeder (CommandLineRunner, runs only if users table empty):
  Create 6 users (one per role):
    admin@smd.edu / admin123 / ADMIN / IT
    lecturer@smd.edu / lecturer123 / LECTURER / Computer Science
    hod@smd.edu / hod123 / HOD / Computer Science
    aa@smd.edu / aa123 / ACADEMIC_AFFAIRS / Academic Affairs
    principal@smd.edu / principal123 / PRINCIPAL / Rectorate
    student@smd.edu / student123 / STUDENT / Computer Science
  Create 2 sample syllabi:
    CS101 "Introduction to Programming" → status APPROVED (reviewedBy HoD)
    CS202 "Data Structures and Algorithms" → status DRAFT
  Print demo accounts table to console on startup

═══════════════════════════════════════
FRONTEND — React + Vite
═══════════════════════════════════════

package.json dependencies:
  react, react-dom, react-router-dom
devDependencies: @vitejs/plugin-react, vite
NO axios, NO tailwind, NO UI library — plain CSS only

────────────────────────────────────────
FILE STRUCTURE
────────────────────────────────────────
src/
├── main.jsx          (ReactDOM.createRoot + BrowserRouter)
├── App.jsx           (Routes)
├── styles.css        (global styles)
├── lib/
│   └── api.js        (fetch wrapper + session helpers)
├── components/
│   └── Navbar.jsx
└── pages/
    ├── LoginPage.jsx
    ├── LecturerPage.jsx
    ├── ReviewPage.jsx
    ├── AdminPage.jsx
    ├── PublicPage.jsx
    └── DetailPage.jsx

────────────────────────────────────────
api.js
────────────────────────────────────────
BASE_URL = 'http://localhost:8080'

Session helpers:
  setSession(authResponse) → save token + user to localStorage
  getCurrentUser() → parse from localStorage
  logout() → clear localStorage

request(method, path, body):
  add Authorization: Bearer <token> if exists
  throw Error with message from response if !res.ok

Export api object with methods:
  login(email, password)
  me()
  createSyllabus(data), updateSyllabus(id, data)
  submitSyllabus(id)
  myList()
  pendingList()
  approve(id, comments), reject(id, comments)
  publish(id)
  getOne(id), aiSummary(id)
  allList()
  publicList(q), publicOne(id), publicAi(id)

────────────────────────────────────────
ROUTING (App.jsx)
────────────────────────────────────────
/ → redirect based on role
/login → LoginPage (public)
/public → PublicPage (public, no auth needed)
/syllabus/:id → DetailPage (public accessible)
/lecturer → LecturerPage (role: LECTURER only)
/review → ReviewPage (role: HOD, ACADEMIC_AFFAIRS)
/admin → AdminPage (role: ADMIN, PRINCIPAL)
* → redirect to /

Role → default route mapping:
  LECTURER → /lecturer
  HOD, ACADEMIC_AFFAIRS → /review
  STUDENT → /public
  ADMIN, PRINCIPAL → /admin

────────────────────────────────────────
PAGES
────────────────────────────────────────

LoginPage:
  Form: email + password inputs
  On submit: call api.login → setSession → navigate to role route
  QUICK LOGIN section: 6 buttons (Admin/Lecturer/HoD/AA/Principal/Student)
    each button fills credentials and logs in immediately
  Link to /public at bottom

LecturerPage:
  Table: list my syllabi (GET /mine)
  Columns: courseCode, courseName, credits, versionNumber, status, updatedAt, actions
  Actions per row:
    View button → navigate /syllabus/:id
    Edit button (only if DRAFT or REJECTED) → show inline form
    Submit button (only if DRAFT or REJECTED) → POST /{id}/submit with confirm()
  If REJECTED: show rejectionReason in red under row
  "+ New Syllabus" button → show create form
  Form fields: courseCode*, courseName*, credits*, department,
    academicYear, semester, description, learningOutcomes (textarea),
    assessmentMethods (textarea), prerequisites, materials
  Save → POST or PUT → reload list → hide form

ReviewPage:
  Table: pending syllabi (GET /pending)
  Columns: courseCode, courseName, createdByName, department, credits, versionNumber, actions
  Actions: View, Approve (prompt for comment), Reject (prompt for reason — required)
  Show success/error message after action

AdminPage:
  Stats row: count syllabi per status (DRAFT/PENDING_REVIEW/APPROVED/REJECTED/PUBLISHED)
  Table: all syllabi (GET /all)
  Actions: View, Publish button (only for APPROVED status)

PublicPage:
  Search bar (input + button) → GET /api/public/syllabi?q=...
  Table: courseCode, courseName, department, credits, semester, View button
  Works without login
  Show login button in navbar if not logged in

DetailPage (/syllabus/:id):
  Show all syllabus fields in a clean layout
  Status badge
  "AI Summary" section with "Generate" button
    → calls ai-summary endpoint
    → displays result in highlighted box
    → use public endpoint if not logged in, authenticated endpoint if logged in
  Back button

────────────────────────────────────────
STYLES (styles.css)
────────────────────────────────────────
Clean, minimal design. Include styles for:
- .navbar (sticky top, logo left, user info right)
- .container (max-width 1100px, centered, padding 24px)
- .card (white, border-radius, shadow, padding)
- table with th/td styling and hover
- .status-DRAFT/PENDING_REVIEW/APPROVED/REJECTED/PUBLISHED (colored badges)
- .btn-primary (blue), .btn-success (green), .btn-danger (red),
  .btn-secondary (gray), .btn-purple (violet)
- .alert-error, .alert-success, .alert-info
- .login-page (centered, gradient background)
- .quick-login section with grid buttons
- .detail-grid (2 column grid, .full spans both)
- .ai-box (yellow gradient background for AI result)
- .empty (centered gray text for empty states)
- responsive: single column on mobile

═══════════════════════════════════════
WHAT TO EXCLUDE (do not build these)
═══════════════════════════════════════
- No Redis, no Kafka, no Elasticsearch
- No Python AI microservice (use mock string instead)
- No React Native / mobile app
- No email notifications (UI toast only)
- No file upload / PDF export
- No complex reporting or charts
- No AA and Principal approval screens (mention in comments as roadmap)
- No unit tests

═══════════════════════════════════════
DELIVERABLE
═══════════════════════════════════════
Generate ALL files completely. Do not truncate any file.
Every file must be complete and immediately runnable.

Backend: run with `mvn spring-boot:run` → starts on port 8080
Frontend: run with `npm install && npm run dev` → starts on port 3000

The full demo flow must work:
1. Open http://localhost:3000
2. Quick-login as Lecturer → create syllabus → submit
3. Quick-login as HoD → approve it
4. Quick-login as Student → search and view → click AI Summary
5. Quick-login as Admin → publish it