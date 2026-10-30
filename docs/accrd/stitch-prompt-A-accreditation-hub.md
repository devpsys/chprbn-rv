# Google Stitch Prompt A — Accreditation Hub (Officer-Assigned Institution)

## Context
Generate the **Accreditation Hub** screen for the CHPRBN Mobile Android app (package `ng.com.chprbn.mobile`).

## Design System Reference (MANDATORY)
Use the existing CHPRBN Compose design system under `ng.com.chprbn.mobile.core.designsystem`:

- **Colors:** `PrimaryGreen` (0xFF266C2B), `SuccessGreen`, `WarningYellow`, `ErrorRed`, `Background` (0xFFF6F8F7), `Surface` (white), `NeutralText` (0xFF1C1C1C), `LightSurfaceVariant`, `LightOnSurfaceVariant`, `LightOutline`.
- **Typography:** `ChprbnTypography` — `headlineSmall` (18sp SemiBold), `bodyMedium` (14sp), `bodySmall` (12sp), `labelMedium`, `labelSmall`.
- **Shapes:** `ChprbnShapes` — `small`/`medium` 8dp, `large` 12dp, `extraLarge` 16dp.
- **Reuse these existing components exactly:** `AppTopBar`, `GridMenuCard`, `StatusBadge`, `PrimaryButton` (loading variant supported), `OutlinedButton`, `LoadingState`, `DownloadingOverlay`, `SyncingOverlay`.
- **Navigation pattern:** type-safe Compose routes (`@Serializable` data classes in `Routes`), `hiltViewModel()`, `collectAsStateWithLifecycle()`, back arrow + `onBack` lambda, header title `titleMedium` bold PrimaryGreen centered.

## Visual/Structural Reference (MANDATORY)
Mirror existing OSYVALAC/verification screens (`RecordDetailScreen`, `ManualEntryScreen`, `VerificationFormScreen`, `ReportIrregularityScreen`) for:
- Header layout: back arrow + centered title
- Surface cards with `BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.1f))`
- `RoundedCornerShape(12.dp)`
- 16dp horizontal padding, 24dp vertical section spacing
- PrimaryGreen filled action surfaces
- `OutlinedButton` for secondary actions
- Error text in `ErrorRed`

## Clarified Requirements (MANDATORY)
- **Officer is assigned an institution** for accreditation — just like an officer is assigned a centre/paper in the exam attendance flow. The officer **cannot** select or search for an institution on their own.
- The Hub shows only the institution(s) **assigned to the officer** (synced from the server).
- Each assigned institution row shows a **sync/draft status indicator**: `Synced` (green), `Draft Pending Sync` (yellow), `Sync Failed` (red), `Never Synced` (grey). Reuse `StatusBadge`-style pills.
- **No** "Institution Search & Select" screen. Remove all search bars, state/zone filter chips, and "Select Institution" actions.
- **No** "Add Institution" capability.
- Institution master data comes **from the server** (synced locally).

## Screen — Accreditation Hub
- AppTopBar with back arrow, title "ACCREDITATION", notification + logout optional.
- Section label "FEATURE GRID" + "ONLINE" status chip (reuse dashboard feature-grid pattern).
- GridMenuCard tiles (2-column): "Start New Assessment", "Continue Assessment", "Local Drafts", "Sync Status". Use `Icons.Outlined.WorkspacePremium` family icons; non-primary tiles = surface background with PrimaryGreen alpha-0.1 border, PrimaryGreen icon, bold title, onSurfaceVariant subtitle.
- **Assigned Institution list** (replaces searchable directory): each row shows the assigned institution name, state, zone, sync/draft status chip, last synced date, trailing chevron → navigate to Institution Profile or resume assessment for that institution.
- Empty state (no assigned institutions): centered icon + "No institutions assigned to you" + subtitle "Contact your CHPRBN coordinator to be assigned an institution for accreditation." + PrimaryGreen text.
- No search bar. No filter chips.

## States
- loading (LoadingState centered spinner)
- empty (no assigned institutions — see empty state above)
- error (ErrorDialog pattern)
- sync-in-progress (SyncingOverlay)

## Output
Compose Kotlin screen function with `@Preview(showBackground = true)`, wrapped in `ChprbnTheme`. Do not invent business rules beyond what's listed.