[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing instructor analytics UI integration.
 - Marked `Iteration 10 - Instructor analytics UI integration` as complete.

[LectureService API contracts](frontend/src/app/lecture.service.ts)
Extended frontend API contract surface for instructor analytics drilldown.
 - Added `getQuestionAnalytics(lectureId)` and `getQuestionAnswerHistory(lectureId, questionId)` client methods.
 - Added typed responses `QuestionAnalyticsResponse` and `StudentAnswerHistoryResponse` for strict UI typing.

[InstructorWorkspaceService](frontend/src/app/instructor/application/instructor-workspace.service.ts)
Added analytics-oriented workspace service methods used by the instructor detail page.
 - Added `listQuestionAnalytics(...)` and `listQuestionAnswerHistory(...)` wrappers over lecture API methods.

[InstructorHome](frontend/src/app/instructor/instructor-home.ts)
Integrated analytics and answer-history orchestration into route-driven instructor detail flow.
 - Added non-trivial methods `refreshQuestionAnalytics(...)`, `openQuestionAnswerHistory(...)`, and `closeQuestionAnswerHistory()`.
 - Analytics refresh now runs alongside lecture state hydration and question mutation flows.
 - Added explicit loading/error signals for analytics and history while preserving non-break behavior when analytics calls fail.

[LectureStatePanel](frontend/src/app/instructor/components/lecture-state-panel/lecture-state-panel.ts)
Extended lecture detail panel to display metrics and support history drilldown interaction.
 - Added analytics/history inputs and events (`refreshAnalytics`, `openHistory`, `closeHistory`).
 - Added non-trivial helper methods for per-question analytics lookup and history visibility routing.
 - Template now shows per-question metric chips, loading/error states, and per-student answer history rows.

[Frontend test coverage](frontend/src/app/instructor/instructor-home.spec.ts)
Updated tests to lock new instructor analytics integration behavior.
 - `InstructorHome` specs now cover analytics hydration and answer-history loading behavior.
 - `LectureStatePanel` specs now cover analytics rendering, drilldown events, and loading/error state presentation.
