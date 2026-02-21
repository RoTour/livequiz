[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing route-driven instructor detail context.
 - Marked `Iteration 06 - Instructor detail route param source of truth` as complete.

[InstructorHome](frontend/src/app/instructor/instructor-home.ts)
Refactored instructor detail page to derive selected lecture from route params.
 - Implemented `OnInit` with route hydration and subscription using `ActivatedRoute.paramMap`.
 - Added non-trivial helper `loadLectureContext(lectureId)` to centralize route-based state loading and reset behavior.
 - `loadLectureContext(...)` now handles missing lecture route params explicitly by clearing state and setting guidance status.
 - Existing instructor detail actions (`addQuestion`, `unlockNextQuestion`, `unlockQuestion`, `createInvite`, `revokeInvite`, refresh methods) remain intact.
 - Replaced ad-hoc in-method selection behavior with route-derived selection flow and hard-refresh-compatible hydration.

[InstructorHome Template](frontend/src/app/instructor/instructor-home.html)
Adjusted detail layout to remove list-page responsibilities.
 - Removed create-lecture panel from detail screen to keep create/select entry in lecture list page.
 - Kept question flow, invite management, and lecture state panels unchanged.

[InstructorHome Spec](frontend/src/app/instructor/instructor-home.spec.ts)
Reworked tests around route-driven detail behavior.
 - Added route-param hydration coverage for existing lecture detail route.
 - Added missing-param guidance coverage (`Lecture not selected...`).
 - Preserved mutation-flow coverage for add/unlock/invite/revoke pathways.
 - Verified service interactions remain correct under route-driven selection.
