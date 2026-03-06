## Context
The instructor lecture workspace already lets instructors generate student invite links, but sharing a long URL is slower in live classroom conditions than scanning a QR code. The existing API already returns a secure tokenized `joinUrl` for each generated invite.

## Goal
Enable instructors to generate and immediately display a real QR code on the lecture page so students can join by scanning, while keeping invite generation restricted to instructors.

## Scope and Non-goals
In scope:
- Generate a scannable QR code client-side from the existing invite `joinUrl`.
- Display the QR code in the instructor lecture page invite panel after invite generation.
- Preserve existing invite actions and instructor-only permissions.
- Add/adjust frontend automated tests for QR rendering behavior.

Non-goals:
- Changing invite domain rules (expiration, revocation, token semantics).
- Adding new backend endpoints or changing invite API contracts.
- Displaying QR codes for historical invite rows.
- Student-side QR scanning implementation in-app.

## Design principles
- Preserve DDD/Clean Architecture boundaries:
  - Domain: no behavior changes; invitation invariants remain owned by lecture/invite domain.
  - Application: existing invite orchestration remains unchanged.
  - Infrastructure: no backend adapter contract changes.
  - Interface: QR generation and rendering remain UI concerns.
- Keep implementation reversible and incremental: no schema or contract migration.
- Fail gracefully: if QR rendering fails, instructor still sees and can copy the join URL.

## Iterations
- [I1] Add invite QR generation capability in instructor invite panel
  - Type: frontend
  - Why: Introduce the smallest useful vertical slice that turns an invite URL into a visual QR artifact.
  - Domain: No impact.
  - Application: No impact; continue consuming existing `CreateInviteResponse`.
  - Infrastructure: No impact.
  - Interface:
    - Add QR generation dependency and panel logic to derive a PNG data URL from `lastCreatedInvite.joinUrl`.
    - Handle empty/no invite and generation failure states without breaking existing invite display.
  - Risks/open questions:
    - Bundle-size increase from QR dependency; keep usage localized to the invite panel.
  - Acceptance criteria:
    - When `lastCreatedInvite` exists with a valid `joinUrl`, a QR image is rendered.
    - When there is no `lastCreatedInvite`, no QR image is rendered.
    - On generation error, a fallback message is shown and the plain URL remains visible.

- [I2] Integrate and verify instructor lecture-page QR experience
  - Type: frontend
  - Why: Ensure the new QR behavior is reachable in the real lecture page flow and remains instructor-gated by existing route/API permissions.
  - Domain: No impact.
  - Application: Keep instructor workspace flow unchanged; QR display binds to current invite creation state.
  - Infrastructure: Reuse existing instructor-only guards and API authorization.
  - Interface:
    - Present QR code beneath newly generated invite metadata in invite management panel.
    - Add/adjust component and page-level tests for invite creation -> QR rendering path.
  - Risks/open questions:
    - Async rendering timing in tests; stabilize with deterministic assertions after async completion.
  - Acceptance criteria:
    - Instructor page still creates/revokes invites as before.
    - After invite creation, panel shows both `joinUrl` and a scannable QR image.
    - Existing tests pass with the updated UI contract.
