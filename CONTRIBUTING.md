# Contributing

This app is a thin REST client. If you're unsure whether something belongs
here or in the backend, it almost certainly belongs in the backend.

- **API contract questions** (endpoint shapes, new fields, new routes,
  auth behavior) belong in the sibling repo,
  [`homephone-dev-server`](../homephone-dev-server), specifically
  [`docs/api.md`](../homephone-dev-server/docs/api.md). Open an issue or
  change there first; this app should only ever be updated to match an
  already-documented API, never ahead of it.
- **Business logic** (allow/block decisions, quiet-hours evaluation,
  scheduling) does not belong in this repo at all — see `CLAUDE.md` at the
  repo root for the hard rule.
- Changes here should be limited to: UI, navigation, the Ktor API client,
  local settings storage (backend URL / token), and data classes that
  mirror the server's JSON shapes.
