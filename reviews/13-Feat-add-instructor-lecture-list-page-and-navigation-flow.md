[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after shipping the instructor lecture list page slice.
 - Marked `Iteration 05 - Instructor lecture list page` as complete.

[LectureService](frontend/src/app/lecture.service.ts)
Extended frontend API contracts to support instructor list rendering.
 - Added `listInstructorLectures()` for `GET /api/lectures`.
 - Added `InstructorLectureSummaryResponse` type with `lectureId`, `title`, `createdAt`, `questionCount`, and `unlockedCount`.

[InstructorWorkspaceService](frontend/src/app/instructor/application/instructor-workspace.service.ts)
Exposed lecture listing to instructor UI orchestration layer.
 - Added `listLectures()` to wrap `LectureService.listInstructorLectures()` through `firstValueFrom`.
 - Preserved existing separation: component orchestration in UI, API calls in service layer.

[InstructorLectureList](frontend/src/app/instructor/instructor-lecture-list.ts)
Introduced dedicated instructor list/create/select page component.
 - Non-trivial method `ngOnInit()` calls `refreshLectures()` to populate page immediately.
 - Non-trivial method `createLecture()` creates a lecture, refreshes list, sets status messaging, then navigates to detail route.
 - Non-trivial method `refreshLectures()` handles empty-state status, list hydration, and retry-friendly error states.
 - Method `openLecture(lectureId)` navigates to `/instructor/lectures/:lectureId`.

[InstructorLectureList Template](frontend/src/app/instructor/instructor-lecture-list.html)
Implemented list page UX for empty/list/create/select journey.
 - Added create lecture form (`Create and open`) with validation-backed form controls.
 - Added list rendering with per-lecture summary metadata (`questionCount`, `unlockedCount`).
 - Added explicit empty state and refresh action.

[InstructorLectureList Styles](frontend/src/app/instructor/instructor-lecture-list.css)
Added local styles for list card actions and metadata readability.
 - Styled click-through lecture items with project token-aligned colors/borders.
 - Added hover affordance on list actions for deliberate selection UX.

[App Routes](frontend/src/app/app.routes.ts)
Switched instructor list route to dedicated page while keeping backward compatibility.
 - `/instructor/lectures` now maps to `InstructorLectureList`.
 - `/instructor/lectures/:lectureId` remains mapped to `InstructorHome` for detail operations.
 - Legacy `/instructor` redirect behavior remains intact from prior iteration.

[App Routes Spec](frontend/src/app/app.routes.spec.ts)
Updated route contract tests for new list-page component mapping.
 - Asserts `/instructor/lectures` uses `InstructorLectureList`.
 - Asserts `/instructor/lectures/:lectureId` still uses `InstructorHome`.

[InstructorHome](frontend/src/app/instructor/instructor-home.ts)
Added compatibility hydration for detail route usage.
 - Implemented `OnInit` and route-param hydration for `lectureId`.
 - Non-trivial method `ngOnInit()` now preloads lecture state/invites when opened from `/instructor/lectures/:lectureId`.

[InstructorHome Spec](frontend/src/app/instructor/instructor-home.spec.ts)
Updated test setup to provide `ActivatedRoute` for route-param-capable component initialization.
 - Added `ActivatedRoute` mock with empty `paramMap` to preserve existing behavior-focused test suite.

[InstructorLectureList Spec](frontend/src/app/instructor/instructor-lecture-list.spec.ts)
Added behavior-focused tests for the new list page.
 - Covers empty state rendering.
 - Covers populated list rendering.
 - Covers create-and-navigate flow.
 - Covers explicit selection navigation.
