[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing instructor per-question student answer history API.
 - Marked `Iteration 09 - Instructor per-question student answer history API` as complete.

[GetQuestionStudentAnswerHistoryUseCase](backend/src/main/java/com/livequiz/backend/application/GetQuestionStudentAnswerHistoryUseCase.java)
Added a dedicated instructor read-model use case for per-question student answer history.
 - Introduced non-trivial method `execute(lectureId, questionId)` to return per-student history rows with `studentId`, `latestAnswerAt`, `attemptCount`, and `latestAnswerText`.
 - Enforces lecture ownership with `InstructorLectureAccessService` and validates question presence with explicit `QUESTION_NOT_FOUND` handling.
 - Includes enrolled students with zero attempts so non-responding students are visible in the response.

[LectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/LectureController.java)
Added new additive endpoint for question-level answer-history drilldown.
 - New `GET /api/lectures/{lectureId}/questions/{questionId}/answers/history` endpoint.
 - Added `StudentAnswerHistoryResponse` mapping with stable serialized timestamp behavior.

[Repository query extensions](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureEnrollmentRepository.java)
Extended repository contracts to support student-level history assembly.
 - `LectureEnrollmentRepository` now exposes enrolled student ids for a lecture.
 - `SubmissionRepository` now exposes per-student submission count by lecture/question.
 - Implemented adapter support across in-memory and JPA persistence (`InMemoryLectureEnrollmentRepository`, `InMemorySubmissionRepository`, `JpaPostgresLectureEnrollmentRepository`, `JpaPostgresSubmissionRepository`, `JpaLectureEnrollmentRepository`, `JpaSubmissionRepository`).

[SecurityConfig and integration tests](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Expanded test identity coverage and contract verification for history endpoint behavior.
 - Added `student2` in-memory user for multi-student history scenarios.
 - `QuizControllerIT` now includes:
   - `should_return_question_student_answer_history_for_owned_lecture`
   - ownership enforcement coverage for the new history endpoint.
