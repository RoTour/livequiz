[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing student lecture listing API.
 - Marked `Iteration 11 - Student lecture listing API` as complete.

[ListStudentLecturesUseCase](backend/src/main/java/com/livequiz/backend/application/ListStudentLecturesUseCase.java)
Added a student-focused lecture listing read use case for re-entry flow.
 - Introduced non-trivial method `execute(studentId)` that composes enrollments, lectures, and student submissions.
 - Returns list rows with `lectureId`, `title`, `enrolledAt`, `questionCount`, and `answeredCount`.
 - Uses student enrollment ordering and skips stale enrollments that no longer resolve to a lecture.

[StudentLectureController](backend/src/main/java/com/livequiz/backend/infrastructure/web/StudentLectureController.java)
Added new student list endpoint for the authenticated user.
 - Added `GET /api/lectures/students/me`.
 - Added typed DTO mapping via `StudentLectureSummaryResponse`.

[LectureEnrollment repository extensions](backend/src/main/java/com/livequiz/backend/domain/lecture/LectureEnrollmentRepository.java)
Extended enrollment repository contract for student listing query composition.
 - Added `findByStudentId(...)` to support enrollment-driven list assembly.
 - Implemented in in-memory and JPA adapters (`InMemoryLectureEnrollmentRepository`, `JpaPostgresLectureEnrollmentRepository`, `JpaLectureEnrollmentRepository`).

[SecurityConfig](backend/src/main/java/com/livequiz/backend/infrastructure/web/SecurityConfig.java)
Updated security matcher configuration for the new student listing endpoint.
 - Added `/api/lectures/students/me` to student/instructor-accessible matcher set.

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
Expanded integration coverage for student lecture listing behavior.
 - Added `should_list_only_joined_lectures_for_current_student`.
 - Verifies joined lecture filtering and progress hint fields (`questionCount`, `answeredCount`).
