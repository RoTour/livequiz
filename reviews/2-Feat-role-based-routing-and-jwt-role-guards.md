[routes](./frontend/src/app/app.routes.ts)
Introduced role-aware route map and default redirects.
 - Replaced monolithic `dashboard` route usage with dedicated `instructor`, `student`, and `student/join/:token` paths.
 - Added `authGuard` + role-specific guards (`instructorGuard`, `studentGuard`) to enforce access by JWT-derived role.
 - Added `dashboard -> instructor` compatibility redirect and wildcard fallback to `/auth/login`.

[authGuard](frontend/src/app/login/auth-guard.ts)
Guard now checks authenticated state through reactive auth API.
 - Updated guard logic from direct token read to `AuthService.isAuthenticated()` computed signal.
 - Retains redirect behavior to `/auth/login` when not authenticated.

[authGuard spec](frontend/src/app/login/auth-guard.spec.ts)
Expanded unit coverage for authenticated and unauthenticated navigation.
 - Added `configure(authenticated: boolean)` helper using Angular `signal(...).asReadonly()` to mock reactive auth state.
 - Verifies allowed path does not call router and denied path redirects to login.

[AuthService](frontend/src/app/login/auth.service.ts)
Added session/role derivation primitives and role-based route resolution.
 - Added computed members `role` and `isAuthenticated` on top of `_token` signal.
 - Added `logout()` to clear `LocalStorageKeys.authorization` and reset token signal.
 - Added `routeForCurrentUser()` delegating to `routeForRole(...)`.
 - Added non-trivial `extractRole(token)` method that decodes Base64URL JWT payload, parses JSON claims, and validates role via `isUserRole(...)`.

[login template](frontend/src/app/login/login.html)
Login view remains simple and form-driven.
 - Keeps `ReactiveFormsModule` bindings and submit button disable logic based on validity + disabled state.
 - Includes inline validation block for username required state.

[Login component](frontend/src/app/login/login.ts)
Login success flow now routes by resolved role destination.
 - `submit()` still disables the form before network request and re-enables on error.
 - Success callback changed from hardcoded `/dashboard` to `this.authService.routeForCurrentUser()`.
 - Non-trivial method: `submit()` orchestrates form validation, mutation of disabled state, async login, and navigation.

[Login spec](frontend/src/app/login/login.spec.ts)
Added behavioral tests for role-based post-login routing.
 - Injects mocked `AuthService` and `Router` with `vi.fn()` spies.
 - Tests successful login path with `routeForCurrentUser()` and navigation call.
 - Tests failure path to ensure form is re-enabled after login error.

[role routing helper](frontend/src/app/auth/application/role-routing.ts)
Centralized role-to-route mapping.
 - Non-trivial function `routeForRole(role)` maps `INSTRUCTOR` -> `/instructor`, `STUDENT` -> `/student`, fallback -> `/auth/login`.

[user role domain](frontend/src/app/auth/domain/user-role.ts)
Introduced typed role model and guard function.
 - `USER_ROLES` constant defines allowed role literals.
 - `UserRole` union type is derived from constant values.
 - Non-trivial type guard `isUserRole(value)` validates unknown values safely at runtime.

[role guards](frontend/src/app/login/role-guard.ts)
Added route-level role authorization guard pair.
 - `instructorGuard` and `studentGuard` check `AuthService.role()` before allowing navigation.
 - When unauthorized, each guard redirects using shared `routeForRole(...)` for deterministic fallback behavior.

[role guard spec](frontend/src/app/login/role-guard.spec.ts)
Added tests for positive/negative role access scenarios.
 - Uses `configure(role)` helper to provide mocked reactive role state.
 - Verifies instructor-only and student-only access as well as fallback redirect to `/auth/login` for unknown role.

[AuthService spec](frontend/src/app/login/auth.service.spec.ts)
Added focused unit tests for token-derived auth state.
 - `createToken(role)` helper builds test JWT-like strings.
 - Covers `isAuthenticated()`, `role()`, and `routeForCurrentUser()` behavior for valid role payloads.
 - Covers malformed token fallback and `logout()` storage/state reset behavior.

[InstructorHome component](frontend/src/app/instructor/instructor-home.ts)
Added standalone placeholder class for instructor landing.
 - `InstructorHome` class currently serves as routing target with template/style wiring.

[InstructorHome template](frontend/src/app/instructor/instructor-home.html)
Added initial instructor workspace placeholder content.
 - Communicates role-aware navigation is active and future features will follow.

[InstructorHome styles](frontend/src/app/instructor/instructor-home.css)
Created stylesheet scaffold for instructor page.
 - File is currently empty and reserved for upcoming styling.

[StudentHome component](frontend/src/app/student/student-home.ts)
Added standalone placeholder class for student landing.
 - `StudentHome` class currently acts as route destination and shell.

[StudentHome template](frontend/src/app/student/student-home.html)
Added initial student workspace placeholder content.
 - Notes role-aware routing readiness and future student flow screens.

[StudentHome styles](frontend/src/app/student/student-home.css)
Created stylesheet scaffold for student page.
 - File is currently empty and reserved for upcoming styling.

[StudentJoinToken component](frontend/src/app/student/student-join-token.ts)
Added standalone placeholder for invite-token enrollment route.
 - `StudentJoinToken` class is currently a routed shell component.

[StudentJoinToken template](frontend/src/app/student/student-join-token.html)
Added placeholder content for token-based lecture join screen.
 - Confirms route wiring and indicates enrollment UX implementation remains pending.

[StudentJoinToken styles](frontend/src/app/student/student-join-token.css)
Created stylesheet scaffold for token join page.
 - File is currently empty and reserved for upcoming styling.
