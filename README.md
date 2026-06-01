# Notely - Smart Notes & Knowledge Management System

Notely is a production-ready, full-stack Knowledge Management System designed as a high-fidelity showcase portfolio. It supports user profiles, workspaces, notes creation in rich Markdown, auto-saving, version history tracking, tags, file uploads, full-text searches, and local-rule based AI generators (providing summaries, flashcards, quizzes, formatting, and auto-tag suggestions).

## Tech Stack
* **Backend**: Java 21, Spring Boot 3, Spring Security, JPA/Hibernate
* **Frontend**: React + Vite (Vanilla CSS design system, Lucide icons, Markdown renderer)
* **Database**: PostgreSQL 16
* **Security**: JWT stateless authentication, BCrypt password hashing
* **Deployment**: Docker & Docker Compose

---

## Project Structure
```text
notely/
├── docker-compose.yml
├── README.md
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/notely/
│       ├── NotelyApplication.java
│       ├── config/             # Spring Security, CORS filters
│       ├── controller/         # REST API Controllers
│       ├── dto/                # Request & Response payload schemas
│       ├── entity/             # JPA Entities (User, Note, Workspace, etc.)
│       ├── exception/          # Global Exception Handler
│       ├── repository/         # DB Access layers
│       ├── security/           # JWT authentications
│       └── service/            # Business implementations (AI, Notes, Sharing)
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── main.jsx
        ├── index.css           # Styling system & theme parameters
        ├── App.jsx             # Routing setup
        ├── context/            # Auth & Theme configurations
        ├── services/           # Axios configurations
        └── pages/              # SPA Pages (Dashboard, Editor, Search, Analytics, Profile)
```

---

## Getting Started

### Prerequisites
* **For Docker**: [Docker](https://www.docker.com/products/docker-desktop)
* **For Local Host**: Java 21 JDK, Maven, Node.js

---

### Option A: Run Locally on Host (Fastest Local Run)
By default, the backend fallback uses an in-memory **H2 Database**, so no database setup is required.

1. **Start the Backend (Spring Boot)**:
   Navigate to the `backend/` folder and run:
   ```bash
   mvn spring-boot:run
   ```
   * *H2 DB Web Console*: available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:notelydb`, user: `sa`, empty password).

2. **Start the Frontend (React)**:
   Navigate to the `frontend/` folder and run:
   ```bash
   npm install
   npm run dev
   ```
   * *Frontend URL*: `http://localhost:5173`

---

### Option B: Run via Docker Compose (Full Stack with Postgres)
From the root directory containing `docker-compose.yml`, run:
```bash
docker-compose up --build
```
This starts PostgreSQL, Spring Boot, and Nginx.
* **Frontend client**: `http://localhost`
* **API Swagger Docs**: `http://localhost:8080/swagger-ui/index.html`

---

## API Documentation Reference

All API calls require a Bearer token inside the `Authorization` header after logging in, except `/api/auth/**` routes.

### 1. Authentication
* `POST /api/auth/register` - Create a new user account.
* `POST /api/auth/login` - Authenticate credentials and get JWT token.
* `POST /api/auth/reset-password` - Reset a user's password.

### 2. Workspaces
* `POST /api/workspaces` - Create a new workspace.
* `GET /api/workspaces` - Retrieve the current user's workspaces.
* `GET /api/workspaces/{id}` - Get workspace details.
* `PUT /api/workspaces/{id}` - Update workspace name/description.
* `DELETE /api/workspaces/{id}` - Delete workspace and nested elements.

### 3. Notes
* `POST /api/notes` - Create a new note.
* `GET /api/notes/{id}` - Retrieve details of a note.
* `GET /api/workspaces/{workspaceId}/notes` - Get paginated notes in workspace.
* `PUT /api/notes/{id}` - Save note title, tags, and content.
* `DELETE /api/notes/{id}` - Delete a note.
* `PUT /api/notes/{id}/archive` - Move note to archives.
* `PUT /api/notes/{id}/restore` - Un-archive note.
* `PUT /api/notes/{id}/pin` - Toggle pin priority.
* `PUT /api/notes/{id}/favorite` - Toggle favorite bookmark status.

### 4. Revision History
* `GET /api/notes/{noteId}/revisions` - Retrieve note revision logs.
* `POST /api/notes/{noteId}/revisions/{revisionId}/rollback` - Reset note to target revision version.

### 5. Collaboration & Comments
* `POST /api/notes/{noteId}/shares` - Share note with recipient user email.
* `DELETE /api/notes/{noteId}/shares/{shareId}` - Revoke share.
* `GET /api/notes/{noteId}/shares` - Retrieve shared permissions logs.
* `POST /api/notes/{noteId}/comments` - Leave a comment.
* `GET /api/notes/{noteId}/comments` - Retrieve chronological comments thread.
* `DELETE /api/notes/{noteId}/comments/{commentId}` - Delete comment.

### 6. Attachments
* `POST /api/notes/{noteId}/attachments` - Upload attachments (PDFs, Images, Docs).
* `GET /api/notes/{noteId}/attachments` - List note attachments.
* `GET /api/notes/{noteId}/attachments/{attachmentId}/download` - Stream download file.
* `DELETE /api/notes/{noteId}/attachments/{attachmentId}` - Delete file.

### 7. Smart AI Options
* `POST /api/notes/{noteId}/ai/summary` - Generate summary bullet points.
* `POST /api/notes/{noteId}/ai/format` - Polish layout alignments.
* `POST /api/notes/{noteId}/ai/suggest-tags` - Extract tag names.
* `POST /api/notes/{noteId}/ai/flashcards` - Create study cards.
* `POST /api/notes/{noteId}/ai/quiz` - Compile multiple-choice learning quiz.
