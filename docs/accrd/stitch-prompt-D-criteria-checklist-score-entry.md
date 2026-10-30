# Google Stitch Prompt D — Criteria Checklist + Score Entry (Raw Numbers Only)

## Context
Generate the **Criteria Checklist** and **Score Entry** screens for the CHPRBN Mobile accreditation feature, based on the Accreditation Guideline minimum requirements.

## Design System Reference (MANDATORY)
Same as Prompt A–C. Reuse `StatusBadge`, `PrimaryButton`, `OutlinedButton`, `ConfirmationDialog`, `SuccessDialog`, `ErrorDialog`, `DownloadingOverlay`, `SyncingOverlay`, `LoadingState`, `AppTopBar`, `DetailRow`-style rows, `LinearProgressIndicator`.

## Clarified Requirements (MANDATORY)
- **Officer enters raw numbers/data only.** The server computes grades, accreditation period, and quotas. **Do not** display a computed grade chip, percentage, or accreditation period on the client.
- **Attachments required** per criterion (photos/docs) — gallery pick + camera capture + preview + remove.
- **Local state + sync**: checklist saved locally as draft before sync.

## Screen 1 — Criteria Checklist (Guideline §1–10)
- Section label "ACCREDITATION CRITERIA" + criteria saved/total count.
- Criterion groups, each a card (surface, `RoundedCornerShape(12.dp)`, 1dp border):
  1. Location & Land Area: rural/semi-urban, PHC clinic, ≥5 hectares, ownership evidence (checkboxes + evidence notes + attachments).
  2. Head of Department: PHC tutor trained (checkbox), post-grad qualification (checkbox).
  3. Human Resources: 6–8 full-time PHC tutors + 4 part-time; 10 full-time lecturers; visiting lecturers; admin staff (clerk 1, messengers/cleaners 1).
  4. Existing Departments: ≥2 other approved health programmes.
  5. Practice Area: 4 health facilities; MoU signed; PHC Coordinator designated.
  6. Physical Facilities: summary linking to Section E (admin block, classrooms, offices, common room, auditorium 500, library 5000 vols/30 PCs, skills lab, medical lab, toilets 15, PHC clinic, recreational, hostel, computer lab 75 units).
  7. Capital Base: amount field (N50,000,000 minimum); statement of account (checkbox + attachment).
  8. Staff Welfare: salary confirmation fields — HOD N150,000; PHC Tutor with degree N130,000; without degree N100,000.
- Each criterion: Met / Not Met toggle, Evidence/Notes `OutlinedTextField`, attachment thumbnails.
- Bottom: PrimaryButton "Save Checklist Draft" + OutlinedButton "Save as Draft".

## Screen 2 — Score Entry (raw numbers, server computes grade)
- Raw score entry fields per criterion group (officer types numbers; no auto-computed percentage shown).
- Each field: label, number `OutlinedTextField`, max-score hint (from Guideline), attachment area.
- Bottom: PrimaryButton "Submit for Server Review" + OutlinedButton "Save Draft".
- On submit: SyncingOverlay → upload local draft + attachments → server returns grade/decision → navigate to Assessment Result screen.

## States
- loading
- local save success
- sync-in-progress (SyncingOverlay)
- validation error (required fields empty)
- error (ErrorDialog)

## Output
Compose Kotlin screen functions + `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`. Do not compute or display grades client-side.