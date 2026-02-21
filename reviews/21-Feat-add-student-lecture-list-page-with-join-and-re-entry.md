[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing student lecture list page delivery.
 - Marked `Iteration 13 - Student lecture list page (join + re-entry)` as complete.

[StudentLectureList page](frontend/src/app/student/student-lecture-list.ts)
Added a dedicated student list entry page for join and re-entry workflows.
 - Introduced non-trivial methods `refreshLectures()`, `joinLecture()`, and `openLecture(...)`.
 - Join flow now normalizes invite codes, refreshes list, and navigates into route-driven lecture room path.
 - Handles loading/empty/error states with explicit status messaging.

[StudentLectureList template](frontend/src/app/student/student-lecture-list.html)
Implemented list-page UI surface for student workflows.
 - Added join-by-code form with refresh action.
 - Added joined-lecture cards showing progress hints (`answeredCount/questionCount`) and room navigation action.
 - Added empty-state and loading-state rendering.

[Routes and workspace integration](frontend/src/app/app.routes.ts)
Integrated the new list page into existing scaffold routes.
 - `/student/lectures` now renders `StudentLectureList`.
 - `/student/lectures/:lectureId` remains on existing student room component for non-breaking incremental delivery.
 - Extended `LectureService` + `StudentWorkspaceService` with student lecture listing API methods.

[Frontend test coverage](frontend/src/app/student/student-lecture-list.spec.ts)
Added test coverage for student list page behavior.
 - Verifies list rendering from API data.
 - Verifies join-by-code navigation into lecture room route.
 - Verifies re-entry navigation from existing lecture rows.
