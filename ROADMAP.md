# 🗺 Roadmap

We follow an incremental development process, focusing on the smallest deliverable unit at a time.

## 🚀 Phase 1: The Walking Skeleton
*Goal: E2E connectivity between a basic Backend and Frontend.*

- [ ] **1.1 Workspace Setup**:
  - Initialize git repository structure.
  - Create `backend` (Spring Boot) and `frontend` (Angular) directories.
- [ ] **1.2 Backend Bootstrapping**:
  - Initialize a minimal Spring Boot application.
  - Create a simple "Health Check" endpoint (e.g., `GET /health`).
- [ ] **1.3 Frontend Bootstrapping**:
  - Initialize a minimal Angular application.
  - Create a service to fetch data from the Backend health endpoint.
  - Display the Backend status on the landing page.

## 📝 Phase 2: Domain Foundation - The Quiz
*Goal: An instructor can create a named Quiz.*

- [ ] **2.1 Domain Modeling**: Define the `Quiz` aggregate (initially just an ID and Title).
- [ ] **2.2 Application Layer**: Create a Use Case to `CreateQuiz`.
- [ ] **2.3 Infrastructure (Persistence)**: Implement an in-memory repository for Quizzes.
- [ ] **2.4 Infrastructure (API)**: Expose a `POST /quizzes` endpoint.
- [ ] **2.5 Frontend Implementation**: Create a simple form to input a Quiz title and submit it.

## ❓ Phase 3: The Question
*Goal: Add a question to a Quiz.*

- [ ] **3.1 Domain Modeling**: Add `Question` entity to the `Quiz` aggregate.
- [ ] **3.2 Backend Implementation**: Update API to support adding questions.
- [ ] **3.3 Frontend Implementation**: UI to add questions to a created quiz.

## 🎮 Phase 4: Joining a Session
*Goal: A student can see a Quiz.*

- [ ] **4.1 Read Model**: Create a projection/read-model for "Open Quizzes".
- [ ] **4.2 Frontend**: Student view to list available quizzes.
