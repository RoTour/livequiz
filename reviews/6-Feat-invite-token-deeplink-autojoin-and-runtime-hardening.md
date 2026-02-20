[ROADMAP](ROADMAP.md)
Marked Phase 6 Step 6 as completed.
 - Updated roadmap rollout state to reflect delivery of invite token deep-link auto-join flow.

[CreateLectureInviteUseCase](backend/src/main/java/com/livequiz/backend/application/CreateLectureInviteUseCase.java)
Adjusted invite URL generation from query-token to path-token format.
 - Non-trivial method `execute(...)` now builds `joinUrl` through `buildJoinUrl(token)` after invite persistence.
 - Non-trivial helper `buildJoinUrl(...)` normalizes trailing slash handling so generated URLs always become `/student/join/{token}`-style paths.

[LiveQuizProperties](backend/src/main/java/com/livequiz/backend/application/LiveQuizProperties.java)
Updated default invite base URL to the token deep-link path.
 - `LiveQuizProperties` record now defaults `inviteBaseUrl` to `http://localhost:4200/student/join` when config is empty.

[Backend application properties](backend/src/main/resources/application.properties)
Aligned environment default for invite links.
 - Changed `livequiz.invite-base-url` fallback value to `http://localhost:4200/student/join`.

[StudentFlowIT](backend/src/test/java/com/livequiz/backend/infrastructure/web/StudentFlowIT.java)
Extended integration assertions to verify the new invite URL contract.
 - Non-trivial test method `should_join_with_invite_and_submit_with_cooldown()` now validates `joinUrl` contains `/student/join/` and no `?token=`.
 - Replaced helper `createInvite(...)` with non-trivial `createInviteResponse(...)` to assert both join code and join URL from API response payload.

[Frontend package scripts](frontend/package.json)
Hardened script execution against broken global Node installations.
 - Updated `ng`, `start`, `build`, `watch`, `test`, and `test:coverage` scripts to route Angular CLI calls through `./scripts/with-node.sh` and local `./node_modules/.bin/ng`.

[App routes](frontend/src/app/app.routes.ts)
Expanded route map for token join and design preview.
 - Added `DesignSystem` component route at `/design-system`.
 - Kept `/student/join/:token` route as auth-protected with `authGuard` + `studentGuard`.

[authGuard](frontend/src/app/login/auth-guard.ts)
Preserved intended destination for unauthenticated users.
 - Non-trivial guard function `authGuard` now redirects to login with `returnUrl: state.url` query param.

[authGuard spec](frontend/src/app/login/auth-guard.spec.ts)
Updated guard test coverage for return URL behavior.
 - Verifies unauthenticated navigation triggers redirect payload with `returnUrl` set to the original deep-link URL.

[Login](frontend/src/app/login/login.ts)
Completed post-login deep-link resume behavior.
 - `Login` component now injects `ActivatedRoute`.
 - Non-trivial method `submit()` uses role route fallback but prioritizes `returnUrl` when present.
 - `submit()` now uses `router.navigateByUrl(returnUrl)` for full URL restoration (including query parameters) and `router.navigate([roleRoute])` for default role routing.

[Login spec](frontend/src/app/login/login.spec.ts)
Expanded navigation behavior tests for mixed navigation APIs.
 - Added `navigateByUrl` mock and assertions to differentiate return URL flow from default role flow.
 - Added dynamic `queryParamMap` setup and test case that validates deep-link restoration after login.

[StudentWorkspaceService](frontend/src/app/student/application/student-workspace.service.ts)
Added token-based join adapter in application layer.
 - Non-trivial method `joinLectureByToken(token)` wraps `LectureService.joinLecture({ token })` with `firstValueFrom(...)`.

[StudentHome](frontend/src/app/student/student-home.ts)
Enabled student workspace hydration from auto-join redirect state.
 - `StudentHome` now implements `OnInit` and injects `ActivatedRoute`.
 - Non-trivial method `ngOnInit()` reads `lectureId` and `alreadyEnrolled` query params, then initializes selected lecture and status signals.
 - Existing non-trivial methods `joinLecture()`, `loadNextQuestion()`, and `submitAnswer()` remain responsible for mutation flow, error mapping, and cooldown UX.

[StudentHome spec](frontend/src/app/student/student-home.spec.ts)
Extended component tests for deep-link hydration behavior.
 - Added dynamic `queryParamMap` wiring via `ActivatedRoute` mock getter.
 - Added test validating query-param bootstrapping of lecture context and status text.

[StudentJoinToken template](frontend/src/app/student/student-join-token.html)
Replaced placeholder content with real auto-join status UI.
 - Shows live status text, actionable error banner, and success-path redirect hint.

[StudentJoinToken](frontend/src/app/student/student-join-token.ts)
Implemented token deep-link auto-join orchestration.
 - `StudentJoinToken` class now implements `OnInit` and manages route token resolution, join execution, and redirect to `/student`.
 - Non-trivial method `ngOnInit()` handles missing token, successful join redirect with query params, and known error mapping for `INVITE_NOT_FOUND`.

[StudentJoinToken spec](frontend/src/app/student/student-join-token.spec.ts)
Added behavior tests for auto-join route.
 - Verifies success path (`joinLectureByToken` + navigation to `/student` with lecture context).
 - Verifies invalid token path renders actionable failure message and prevents navigation.

[Node wrapper script](frontend/scripts/with-node.sh)
Added runtime bootstrap for stable frontend CLI execution.
 - Non-trivial shell flow scans `~/.nvm/versions/node/*/bin/node`, sorts versions, picks the highest executable candidate, prepends it to `PATH`, then `exec`s target command.
 - Prevents session-to-session failures caused by stale global `/usr/local/bin/node` symlinks.

[DesignSystem component](frontend/src/app/design-system/design-system.ts)
Added standalone visual preview entry component.
 - Introduced `DesignSystem` class with template and stylesheet wiring for design token showcase route.

[DesignSystem template](frontend/src/app/design-system/design-system.html)
Added comprehensive UI language showcase page.
 - Demonstrates hero, typography scale, form controls, button styles, tables, pills, and card/tile variants across light/dark surfaces.

[DesignSystem stylesheet](frontend/src/app/design-system/design-system.css)
Added cohesive tokenized visual system and responsive styles.
 - Defines non-trivial token layer (`--ds-*`), component-level style primitives, interaction states, and animation keyframes (`ds-rise`).
 - Includes responsive adjustments for compact screens and consistent neo-brutal visual identity.
