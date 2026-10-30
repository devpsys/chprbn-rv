# Google Stitch Prompt E — Submit for Review + Assessment Result (Server-Computed)

## Context
Generate the **Submit for Review** and **Assessment Result** screens for the CHPRBN Mobile accreditation feature.

## Design System Reference (MANDATORY)
Same as Prompt A–D. Reuse `AppTopBar`, `PrimaryButton`, `OutlinedButton`, `StatusBadge`, `ConfirmationDialog`, `SuccessDialog`, `ErrorDialog`, `SyncingOverlay`, `LoadingState`, `DetailRow`-style rows, surface cards.

## Clarified Requirements (MANDATORY)
- Grading is computed **on the server**. The officer submits the local draft + attachments; the server returns the grade/decision.
- **No digital signature** required.
- **No quota assignment** on the client (server determines quotas).

## Screen 1 — Submit for Review
- Institution name + assessment summary (sections completed, criteria entered, attachment count).
- Confirmation: "Submit this accreditation assessment for review?" + "Submitted assessments are reviewed by the CHPRBN board; grades are determined by the server."
- Bottom: PrimaryButton "Submit Assessment" + OutlinedButton "Cancel".
- On submit: SyncingOverlay → upload → server returns result → navigate to Assessment Result.

## Screen 2 — Assessment Result (server-computed)
- Institution name + assessment date.
- Server-returned grade: Excellent / Satisfactory / Re-visit / Denial (displayed as read-only StatusBadge, color-coded).
- Server-returned accreditation period (e.g., "5 years full accreditation" or "3 years provisional").
- Server-returned decision: Full Accreditation / Provisional-Conditional / Denial / Revocation / Deferral.
- Read-only summary of officer-entered raw scores (for reference only).
- Server-returned conditions/follow-up measures (if any).
- Bottom: PrimaryButton "View Certificate" + OutlinedButton "Done" (returns to Hub).

## States
- loading
- submission in progress (SyncingOverlay)
- result received
- error (ErrorDialog with retry)

## Output
Compose Kotlin screen functions + `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`.