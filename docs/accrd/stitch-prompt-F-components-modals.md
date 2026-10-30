# Google Stitch Prompt F — Components & Modals (Attachments + Sync)

## Context
Generate the shared accreditation **reusable components / modals** for the CHPRBN Mobile accreditation feature, updated for attachments and local sync.

## Design System Reference (MANDATORY)
Same as Prompt A. Reuse `AppTopBar`, `StatusBadge`, `PrimaryButton`, `OutlinedButton`, `ConfirmationDialog`, `SuccessDialog`, `ErrorDialog`, `LoadingState`, `DownloadingOverlay`, `SyncingOverlay`, `DetailRow`, `GridMenuCard`. Reuse `Dialogs.kt` `ChprbnModal` pattern (`Dialog` with `dismissOnBackPress = true`, dimmed background, surface `RoundedCornerShape`, centered content).

## Components
- `SectionCompletionChip` — Not Started / In Progress / Saved Locally / Pending Sync / Synced / Sync Failed pill.
- `DraftStatusBadge` — color-coded: Saved Locally (yellow/Pending), Synced (green/Active), Sync Failed (red/Expired).
- `AttachmentThumbnail` — 88.dp square preview with remove overlay.
- `SyncErrorRow` — institution name + sanitised error (hostnames/URLs stripped via `stripHostsAndUrls()`) + retry button.

## Modals
- `DiscardDraftDialog` — ConfirmationDialog: "Discard unsaved assessment?" / "Changes will be lost" / "Discard" / "Keep Editing".
- `SubmitAssessmentDialog` — ConfirmationDialog before final submit: "Submit assessment for server review?" / "Grade will be determined by the server" / "Submit" / "Cancel".
- `SyncFailedDialog` — ErrorDialog: title "Sync Failed", sanitised message, primary "Retry", secondary "Close".
- `GradingInfoDialog` — InfoDialog: explains that grades/accreditation periods are determined by the CHPRBN server after submission.

## Output
Compose Kotlin functions + `@Preview(showBackground = true)` where applicable, wrapped in `ChprbnTheme`.