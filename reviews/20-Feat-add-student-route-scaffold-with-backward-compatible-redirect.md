[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after completing student route scaffold.
 - Marked `Iteration 12 - Student route scaffold (backward compatible)` as complete.

[Student route model](frontend/src/app/app.routes.ts)
Introduced student list/room route scaffold while preserving legacy compatibility.
 - Added `/student/lectures` and `/student/lectures/:lectureId` routes.
 - Kept `/student` as backward-compatible redirect to `/student/lectures`.
 - Kept `/student/join/:token` protected and unchanged.

[Role-based post-login routing](frontend/src/app/auth/application/role-routing.ts)
Aligned student role landing route with new student list path.
 - Updated student role target from `/student` to `/student/lectures`.

[StudentHome route hydration](frontend/src/app/student/student-home.ts)
Extended student workspace hydration to support new route-driven entry.
 - `ngOnInit()` now reads both route param `lectureId` and deep-link query params.
 - Preserves deep-link behavior (`Lecture joined from invite link`) while supporting route-param entry (`Lecture selected`).

[Route/auth test updates](frontend/src/app/app.routes.spec.ts)
Updated and expanded tests for route scaffold and role routing behavior.
 - Updated route tests for student legacy redirect + new list/detail guards.
 - Updated login/guard/app/auth-service specs to assert `/student/lectures` role route.
 - Added student home coverage for route-param lecture hydration.
