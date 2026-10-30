# Google Stitch Prompt C — Assessment Overview + Section Forms A–F (SSQ)

## Context
Generate the **Assessment Overview** screen and the six **SSQ self-assessment section forms** (Sections A–F) for the CHPRBN Mobile accreditation feature.

## Design System Reference (MANDATORY)
Same as Prompt A. Reuse `AppTopBar`, `PrimaryButton` (with `isLoading`), `OutlinedButton`, `OutlinedTextField` (56.dp, `RoundedCornerShape(8.dp)`, PrimaryGreen focused indicator), `ExposedDropdownMenuBox` + `ExposedDropdownMenu` + `DropdownMenuItem` (pattern from `VerificationFormScreen` and `ReportIrregularityScreen`), `FormOutlinedField` + `RemarkDropdown` (pattern from `ReportIrregularityScreen`), `StatusBadge`, `ConfirmationDialog`, `SuccessDialog`, `ErrorDialog`, `DownloadingOverlay`, `SyncingOverlay`, `LoadingState`. Use `collectAsStateWithLifecycle()`, `hiltViewModel()`, type-safe `@Serializable` routes, `onBack` lambdas. Reuse `RecordDetailScreen` header/footer pattern.

## Clarified Requirements (MANDATORY)
- **Officer-led**: officer fills in the SSQ forms locally.
- **Local state + sync**: each section is saved locally as a draft before sync. No server round-trip during form entry.
- **Attachments required**: photos and/or documents per section/criterion. Include an attachment capture/management area in every section form (reuse `ReportIrregularityScreen` snapshot pattern: gallery pick + camera capture + preview + remove).
- **No digital signature** required — omit HOD/Dean signature blocks from the SSQ forms.
- **No grade computation on client**: officer enters raw numbers/data only. Do **not** display computed grade, percentage, or accreditation period in section forms.
- Each section shows a **draft status** (Not Started / In Progress / Saved Locally / Pending Sync / Synced).

## Screen 1 — Assessment Overview (progress tracker)
- Progress bar (0–100%, `LinearProgressIndicator` 8.dp, PrimaryGreen) showing % of sections saved locally.
- Six section rows (A–F): section letter + title, right-aligned draft-status pill, trailing chevron → section form. Completed/saved sections show a checkmark.
- Bottom: PrimaryButton "Continue to Criteria Checklist" (disabled until all six sections saved locally) + OutlinedButton "Save All Drafts".

## Screen 2 — Section A: General Information (SSQ A1 + A2)
- Ownership Status: radio group (Federal / State / L.G.A / Private / Faith-Based), required.
- Campus Location, Date of Establishment, Programme Assessed (checkboxes), Faculty/School/College, Department/Institute, Year Programme Commenced.
- Current Accreditation Status: dropdown (Full / Provisional / Denial / Suspension / Others Specify).
- Date of Last Visit, Accreditation Expiry Date.
- Approved Number of Students per level (number fields).
- Indexed / Unindexed Students per level per programme (number fields).
- NBTE Accreditation (ND/HND checkboxes + counts).
- Students Enrolled; Technical / Academic / Administrative Staff counts.
- A2 PHC Tutor Roster table: add/remove rows; Name, Sex, License No., Phone, Highest Qualification, Employment Status (Full-Time/Part-Time).
- Attachment area: "Supporting Documents" — gallery pick + camera capture + preview thumbnails + remove (pattern from `ReportIrregularityScreen` snapshot).
- Field-level validation: required fields show `ErrorRed` indicator + `supportingText` error.
- Bottom: PrimaryButton "Save Section Draft" + OutlinedButton "Save as Draft".

## Screens 3–7 — Sections B–F (SSQ)
- Each section: scrollable list of assessment items. Every item: item text (`bodyMedium`), status control (Yes/No or Available/Not Available — `ExposedDropdownMenu` or two-chip toggle), Remarks field.
- **Section B:** B1 (10 philosophy/objectives Yes/No) + B2 (10 governance items).
- **Section C:** C1 (18 teaching methods, each Available / Not Available).
- **Section D:** D1 academic staffing table by rank, D2 (12 qualifications Yes/No), D3 admin/technical staff table.
- **Section E:** E1 (10 lecture facilities), E2 (15 labs), E3 (15 simulation/modern tech) — each Available / Not Available + Remarks.
- **Section F:** F1 (7 affiliated clinical/community facilities) — each Available / Not Available + Remarks + MoU status.
- Every section includes an attachment area per subsection (photos/docs).
- All sections share: section header, item dividers (1dp `PrimaryGreen.copy(alpha=0.05f)`), bottom PrimaryButton "Save Section Draft" + OutlinedButton "Save as Draft".

## States
- per-screen loading (LoadingState)
- unsaved-changes guard (ConfirmationDialog on back)
- local save success (SuccessDialog "Section saved locally")
- submit-to-sync (SyncingOverlay)
- validation errors inline

## Output
Compose Kotlin screen functions + `@Preview(showBackground = true)` for each, wrapped in `ChprbnTheme`. Do not invent fields beyond SSQ Sections A–F; do not compute grades.