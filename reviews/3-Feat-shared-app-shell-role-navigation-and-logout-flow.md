[App](frontend/src/app/app.ts)
Added app-level auth-aware shell controller responsibilities.
 - `App` class now injects `AuthService` and `Router` so layout can react to session state and trigger navigation.
 - Non-trivial method `logout()` performs coordinated session teardown (`authService.logout()`) and route transition to `/auth/login`.

[App template](frontend/src/app/app.html)
Introduced a shared authenticated shell with role-sensitive navigation.
 - Wrapped the page in a consistent layout (`min-h-screen`, centered main container) and kept `router-outlet` as primary content host.
 - Added conditional header rendering with `@if (authService.isAuthenticated())` so nav appears only for signed-in users.
 - Added role badge using `authService.role()` and role-specific menu entries for instructor/student paths.
 - Added `Log out` action wired to `logout()` for explicit user session exit.

[App spec](frontend/src/app/app.spec.ts)
Expanded root component tests to validate role-aware shell behavior.
 - Uses reactive test state with `signal(...)` for auth/role and mocks `AuthService` contract (`isAuthenticated`, `role`, `logout`, `routeForCurrentUser`).
 - Verifies authenticated header content, unauthenticated header absence, and logout redirect flow.
 - Non-trivial test scenario: `logs out and redirects to login` spies on `Router.navigate` and confirms both service-side logout and route navigation happen.

[BackendStatus](frontend/src/app/shared/backend-status/backend-status.ts)
Refined status component internals while preserving polling behavior.
 - `BackendStatus` class keeps dev-only live timer updates via `timer(0, 1000)` with `takeUntilDestroyed()` to avoid subscription leaks.
 - Non-trivial computed signals:
   - `backendStatus` derives human-readable status from `backendService.backendUp()`.
   - `timeInStatus` computes elapsed seconds since `backendService.lastChange()`.
   - `styles.bg` computes visual state token (`bg-emerald-50` or `bg-rose-50`) from service state.

[BackendStatus template](frontend/src/app/shared/backend-status/backend-status.html)
Adjusted status bar rendering to match computed style model.
 - Keeps dev-mode-only visibility with `@if (devMode)`.
 - Renders dynamic background class from `styles.bg()` and displays both backend status and elapsed time.

[Roadmap](ROADMAP.md)
Roadmap updated context reviewed against current frontend rollout.
 - Phase progression remains intact and Step 1 in Phase 6 is checked.
 - Step 2 (`shared app shell + role-based navigation + logout flow`) is still unchecked; this appears to lag behind implemented frontend shell changes and may need synchronization in a follow-up decision.
