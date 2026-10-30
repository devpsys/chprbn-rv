# Google Stitch Prompt B — Institution Profile (Read-Only)

## Context
Generate the **Institution Profile** screen for the CHPRBN Mobile accreditation feature.

## Design System Reference (MANDATORY)
Same as Prompt A. Reuse `AppTopBar`, `StatusBadge`, `DetailRow`-style label/value rows (from `RecordDetailScreen.DetailRow`), `IssueDateCard`/`ExpiryDateCard` pattern, practitioner-photo-style placeholder circle (112.dp).

## Clarified Requirements (MANDATORY)
- Read-only profile populated from server-synced institution data.
- Show sync status (last synced date, sync status chip).
- **No** grade computation or quota display on the client — grades are computed on the server.

## Content (from SSQ Section A + Guideline)
- Institution name (`headlineSmall` bold PrimaryGreen centered), subtitle (ownership type, `bodySmall` onSurfaceVariant).
- Status row: sync status chip, last synced date, date of last accreditation visit (from server).
- Info cards (surface, `RoundedCornerShape(12.dp)`, 1dp border, 16dp padding):
  - Ownership type, state, zone, campus location.
  - Programmes offered (BCHS/CHO + CHEW/HND/JCHEW/ND badges).
  - Staff counts (technical / academic / administrative).
  - Capital base declared (from server).
- "Accreditation History" section: past assessments from server (date, score, grade, decision), each with trailing chevron → Assessment History detail.
- Bottom action bar: PrimaryButton "Start New Assessment" + OutlinedButton "View Documents".

## States
- loading
- not-found (reuse `RecordDetailNoRecordContent` pattern)
- error

## Output
Compose Kotlin screen function + `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`.