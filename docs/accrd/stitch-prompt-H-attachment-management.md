# Google Stitch Prompt H — Attachment Capture & Management (Reusable)

## Context
Generate the **Attachment Capture & Management** component/screen for the CHPRBN Mobile accreditation feature.

## Design System Reference (MANDATORY)
Same design system as Prompt A. Reuse `ReportIrregularityScreen` snapshot pattern exactly: `OutlinedTextField`-style drop zone, gallery pick (`PickVisualMedia`), camera capture (`TakePicture` + camera permission), preview thumbnail with remove button, `AsyncImage` (Coil) for display, `ContentScale.Fit`.

## Component — `AttachmentManager`
- Header: "Supporting Documents" (`labelSmall` SemiBold PrimaryGreen alpha 0.75).
- Drop zone: `Surface` 200.dp height, `RoundedCornerShape(12.dp)`, border 1dp `PrimaryGreen.copy(alpha=0.15f)`, empty state shows `Icons.Filled.Image` + "No files attached".
- When files attached: grid of preview thumbnails (each ~88.dp, `RoundedCornerShape(8.dp)`, `ContentScale.Crop`), each with a close/remove `Icons.Filled.Close` overlay (top-right).
- Action row: OutlinedButton "Gallery" + OutlinedButton "Camera" (side by side, `Arrangement.spacedBy(12.dp)`, `RoundedCornerShape(12.dp)`).
- Error state: `ErrorRed` border + error `supportingText` (pattern from `FormOutlinedField`).
- Returns list of attachment URIs/paths to the parent form for local storage + later sync upload.

## States
- empty
- files selected
- upload-in-progress (per-item spinner)
- error

## Output
Compose Kotlin component function + `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`.