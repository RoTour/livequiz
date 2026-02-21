[ROADMAP](ROADMAP.md)
Updated Phase 7 tracking after shipping instructor route scaffolding.
 - Marked `Iteration 04 - Instructor route scaffold (backward compatible)` as complete.

[app.routes](frontend/src/app/app.routes.ts)
Introduced instructor route structure while preserving legacy entry behavior.
 - Converted legacy `/instructor` route into a guarded redirect to `/instructor/lectures`.
 - Added `/instructor/lectures` route mapped to `InstructorHome`.
 - Added `/instructor/lectures/:lectureId` route mapped to `InstructorHome` for detail-path activation in later iterations.
 - Kept `authGuard` and `instructorGuard` on all instructor routes to preserve authorization behavior.

[app.routes.spec](frontend/src/app/app.routes.spec.ts)
Updated route contract tests to lock the new scaffold behavior.
 - Added `keeps legacy instructor path as guarded redirect` to verify `/instructor` compatibility path.
 - Added `protects instructor lecture routes...` to verify both new lecture routes use `InstructorHome` and guard chain ordering.
 - Preserved student-route and fallback redirect assertions to prevent regressions outside this iteration.
