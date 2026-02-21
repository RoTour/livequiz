[InstructorHome Template](frontend/src/app/instructor/instructor-home.html)
Refactored the workspace markup into explicit layout regions without changing visual styling tokens.
 - Wrapped the page with `lq-workspace-layout` and added `lq-workspace-content` to control wireframe-level structure.
 - Split content into three semantic areas: `lq-workspace-question`, `lq-workspace-sidebar`, and `lq-workspace-state`.
 - Preserved all existing panel components and bindings; only their placement changed.
 - Continued using `InstructorHome` as the orchestrating class through template event bindings to non-trivial methods: `addQuestion()`, `unlockNextQuestion()`, `refreshLectureState()`, `createInvite()`, `refreshInvites()`, `revokeInvite()`, `unlockQuestion()`, `refreshQuestionAnalytics()`, `openQuestionAnswerHistory()`, and `closeQuestionAnswerHistory()`.

[InstructorHome Layout Styles](frontend/src/app/instructor/instructor-home.css)
Added component-scoped layout CSS to implement the new wireframe while preserving existing global design language.
 - Introduced a two-column CSS Grid for desktop/tablet using `grid-template-areas` to anchor question/state on the left and invite management on the right.
 - Added explicit area assignments for `.lq-workspace-question`, `.lq-workspace-state`, and `.lq-workspace-sidebar`.
 - Added responsive collapse at `max-width: 960px` with mobile order `question -> sidebar -> state` to keep invite actions reachable earlier during narrow-screen usage.
 - Kept all color, typography, component variant, and button styling unchanged; this file only defines positional/layout behavior.
