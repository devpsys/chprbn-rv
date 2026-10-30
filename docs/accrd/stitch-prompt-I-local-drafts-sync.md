# Google Stitch Prompt I — Local Drafts & Sync Status

## Context
Generate the **Local Drafts** and **Sync Status** screens for the CHPRBN Mobile accreditation feature.

## Design System Reference (MANDATORY)
Same design system. Reuse `CachedRecordsScreen`/`CachedRecordsViewModel` pattern (tabs Pending/Failed, search, filter chips, `combine` of flows), `StatusBadge`, `PrimaryButton`, `OutlinedButton`, `SyncingOverlay`, `LoadingState`, `ConfirmationDialog`, `SuccessDialog`, `ErrorDialog`.

## Clarified Requirements (MANDATORY)
- Offline-first: assessments are stored locally as drafts before sync.
- Drafts show per-institution sync status: `Pending Sync` / `Syncing` / `Synced` / `Sync Failed`.
- Background sync uses the existing `core.sync.SyncBatchRunner` + `SyncWorker` + WorkManager outbox queue pattern (cross-feature `sync_jobs` queue).

## Screen 1 — Local Drafts
- Header: AppTopBar back arrow + title "LOCAL DRAFTS".
- Search bar + filter chips: All / Pending Sync / Synced / Sync Failed.
- List rows: institution name, section progress (e.g., "3 of 6 sections complete"), attachment count, draft status chip, last modified date, trailing chevron → resume assessment.
- Bottom action: PrimaryButton "Sync All Drafts" + OutlinedButton "Discard Selected" (with ConfirmationDialog guard).
- Empty state: centered icon + "No local drafts" + subtitle.

## Screen 2 — Sync Status
- Header: AppTopBar back arrow + title "SYNC STATUS".
- Overall sync progress bar + status (`Syncing…` / `Synced` / `Sync Failed` / `Offline`).
- Per-draft sync detail: institution, status, attempt count, last error (sanitised via `core.network.UserFacingError.stripHostsAndUrls()` — no hostnames/URLs leaked to UI), retry action per failed draft.
- Bottom: PrimaryButton "Retry Failed" + OutlinedButton "Close".

## States
- loading
- offline (show last successful sync)
- syncing (SyncingOverlay)
- success (SuccessDialog)
- error (ErrorDialog with sanitised message)

## Output
Compose Kotlin screen functions + `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`.