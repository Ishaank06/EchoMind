<div align="center">

# 🧠 EchoMind

**Your Digital Twin for Knowledge and Memory**

[![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

*One question. Every source. Instant answers.*

[Getting Started](#-getting-started) •
[Architecture](#-architecture) •
[Tech Stack](#-tech-stack) •
[Roadmap](#-roadmap)

</div>

---

## 📌 The Problem

Every day we scatter information across dozens of platforms:

| Platform | What lives there |
|----------|-----------------|
| **Notion** | Notes & wikis |
| **Google Drive** | Documents & spreadsheets |
| **Gmail** | Emails & attachments |
| **GitHub** | Code, issues & commits |
| **Calendar** | Meetings & events |
| **Chrome** | Bookmarks & saved pages |
| **WhatsApp** | Conversations & shared files |

Finding something later is nearly impossible — every service has its own search, and **none of them understand context**.

## 💡 The Solution

EchoMind is a centralized platform that continuously indexes your digital footprint and builds a **personal knowledge graph**.

Instead of searching each app separately, ask one question in natural language:

> *"Where did I save the API design for my internship?"*

> *"Show everything related to Project Alpha."*

The system retrieves information from **every connected source** and provides a single, contextual answer.

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│              (TypeScript + Tailwind CSS)                  │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API
┌──────────────────────▼──────────────────────────────────┐
│                Spring Boot Backend                       │
│                                                          │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  │
│  │    Auth       │  │  Sync         │  │   Search     │  │
│  │   Module      │  │  Scheduler    │  │   Service    │  │
│  └──────────────┘  └───────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  │
│  │    File       │  │  Connector    │  │     AI       │  │
│  │   Service     │  │  Manager      │  │   Gateway    │  │
│  └──────────────┘  └───────────────┘  └──────┬───────┘  │
│                                               │          │
└────────┬──────────────────┬───────────────────┼──────────┘
         │                  │                   │
    ┌────▼────┐       ┌─────▼─────┐    ┌───────▼────────┐
    │PostgreSQL│       │   Redis   │    │ Python AI      │
    │   + JPA  │       │  (Cache)  │    │ Microservice   │
    └─────────┘       └───────────┘    │ (LLM + Embeds) │
                                       └────────────────┘
```

---

## 🧩 Core Modules

### 🔐 Authentication
- Secure signup/login with password hashing
- JWT-based stateless authentication
- OAuth 2.0 login (Google & GitHub)
- Role-based access control

### 🔌 Data Connectors
Connect your digital world:
- **Google Drive** — Documents, spreadsheets, presentations
- **Gmail** — Emails and attachments
- **GitHub** — Repositories, commits, issues, READMEs
- **Notion** — Pages, databases, wikis
- **Calendar** — Events, meetings, schedules
- **Local Files** — PDFs, text files, images

The backend periodically syncs data into a unified, searchable database.

### 🔍 Indexing Engine
When new content arrives, EchoMind:
1. **Extracts** text and metadata
2. **Stores** structured data in PostgreSQL
3. **Generates** vector embeddings for semantic search
4. **Builds** relationships between entities (knowledge graph)

### 🌐 Universal Search
Search shouldn't depend on filenames. Ask naturally:

> *"Find the document where I discussed Redis caching."*

Results span across **all** connected sources — PDFs, notes, emails, commits, calendar events — in one unified view.

### 🕸 Knowledge Graph
Visualize relationships between everything you've touched:

```
Internship Project
├── 📄 Resume.pdf
├── 📧 Offer Letter (Gmail)
├── 💻 GitHub Repository
├── 📧 Email Thread
├── 📅 Interview (Calendar)
└── 📝 Preparation Notes (Notion)
```

Explore your information **visually** instead of digging through folders.

### 🤖 AI Assistant
Ask questions about your own data:

> *"What did I work on last month?"*
> *"Summarize everything related to my research paper."*
> *"Which files mention Docker?"*

The assistant searches your indexed knowledge base before answering — grounded in **your** information, not generic web results.

### 📅 Timeline
A chronological activity history — like Git history for your digital life:

```
June 15, 2026
  • 📄 Uploaded Resume.pdf
  • 💻 GitHub commit: "Add authentication module"
  • 📅 Interview scheduled with Company X
  • 📝 Created meeting notes
  • 📧 Received offer letter
```

### 📊 Dashboard
At-a-glance insights:
- Most active projects
- Storage usage across sources
- Frequently accessed documents
- Weekly productivity trends
- Connected services status
- Recent search history

---

## 🛠 Tech Stack

### Backend (Core)
| Technology | Purpose |
|-----------|---------|
| **Java 21** | Primary language |
| **Spring Boot 4.x** | Application framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Database access layer |
| **Hibernate** | ORM for PostgreSQL |
| **PostgreSQL 15** | Primary database |
| **Redis 7** | Caching & session management |
| **Docker Compose** | Local infrastructure |
| **Maven** | Build & dependency management |

### Frontend (Planned)
| Technology | Purpose |
|-----------|---------|
| **React** | UI framework |
| **TypeScript** | Type-safe JavaScript |
| **Tailwind CSS** | Utility-first styling |

### AI Layer (Planned)
| Technology | Purpose |
|-----------|---------|
| **Python + FastAPI** | AI microservice |
| **Ollama / Gemini / OpenAI** | LLM for natural language |
| **pgvector / Qdrant** | Vector database for semantic search |

> The Java backend remains the **core application** while the AI service is isolated — keeping the architecture clean, testable, and independently scalable.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** — [Download](https://adoptium.net/)
- **Docker Desktop** — [Download](https://www.docker.com/products/docker-desktop/)
- **Git**

### Setup

```bash
# Clone the repository
git clone https://github.com/Ishaank06/EchoMind.git
cd EchoMind

# Start PostgreSQL (and Redis) via Docker
docker compose up -d

# Run the Spring Boot application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Verify It's Running

```bash
# Health check (should show PostgreSQL connection status)
curl http://localhost:8080/actuator/health
```

### API Endpoints (Phase 2)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users` | Create a new user |
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `GET` | `/actuator/health` | Health check with DB status |

**Example — Create a user:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Ishaan", "email": "ishaan@example.com"}'
```

**Response:**
```json
{
  "id": "e2e9d17f-ed1e-4883-8830-c21753e74ba4",
  "name": "Ishaan",
  "email": "ishaan@example.com"
}
```

---

## 🗺 Roadmap

- [x] **Phase 1** — Project scaffolding (Docker, PostgreSQL, Redis)
- [x] **Phase 2** — Spring Boot backend with User CRUD, validation, error handling, Actuator
- [ ] **Phase 3** — Authentication (JWT + OAuth 2.0 with Google/GitHub)
- [ ] **Phase 4** — Data Connectors (Google Drive, Gmail, GitHub, Notion)
- [ ] **Phase 5** — Indexing Engine (text extraction, metadata, relationships)
- [ ] **Phase 6** — Universal Search (full-text + semantic search)
- [ ] **Phase 7** — Knowledge Graph (entity relationships, visualization)
- [ ] **Phase 8** — AI Assistant (Python microservice, LLM integration)
- [ ] **Phase 9** — Timeline & Dashboard
- [ ] **Phase 10** — React Frontend

---

## 📂 Project Structure

```
src/main/java/com/echomind/
├── EchoMindApplication.java       # Application entry point
├── controller/                    # REST API endpoints
│   └── UserController.java
├── service/                       # Business logic & transactions
│   └── UserService.java
├── repository/                    # Data access (Spring Data JPA)
│   └── UserRepository.java
├── entity/                        # JPA entities (database tables)
│   └── User.java
├── dto/                           # Request/Response data shapes
│   ├── CreateUserRequest.java
│   └── UserResponse.java
├── exception/                     # Error handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
└── config/                        # Spring configuration (upcoming)
```

---

## 🤝 Contributing

This is a personal learning-by-building project, but suggestions and feedback are welcome! Feel free to open an issue.

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ☕ and curiosity by [@Ishaank06](https://github.com/Ishaank06)**

</div>
