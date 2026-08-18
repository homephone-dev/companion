# CLAUDE.md

## Core invariant: this app has no business logic

`homephone-companion` is Milestone 2 of the homephone project — a thin client
only. It must never implement:

- allow/block call decisions
- quiet-hours / schedule evaluation logic
- any other decision-making about what should happen to a call

All of that lives server-side in the sibling repo,
`../homephone-dev-server`, a Go backend fronting an Asterisk SIP PBX. That
server owns every decision and exposes a REST API
(`homephone-dev-server/docs/api.md`) for managing devices, contacts
(allowlist), schedules, and call history.

This app's job is limited to reading and writing through that REST API:

- list/create/update/delete devices
- list/create/update/delete contacts (per-device allowlist entries)
- list/create/update/delete schedules
- list/read/delete call history rows

If a feature would require this app to decide anything about call
handling rather than just display or edit data the server already has,
that feature belongs in `homephone-dev-server`, not here.

## Auth model

There is no separate account system in this app. The user enters a
backend base URL and a bearer API token (generated on the server) in the
Settings screen; both are stored locally on-device via
multiplatform-settings. Every API request sends
`Authorization: Bearer <token>`. Do not add OAuth, a login screen, or any
server-side session concept here — that decision was made deliberately to
keep this app thin.

## Before changing networking/data-model code

Read `../homephone-dev-server/docs/api.md` first. Match its endpoint
paths, request bodies, and JSON field names exactly. Do not invent
endpoints or fields.
