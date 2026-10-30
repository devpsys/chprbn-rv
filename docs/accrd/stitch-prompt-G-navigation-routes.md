# Google Stitch Prompt G — Navigation & Route Integration

## Context
Generate the **navigation integration** for the Accreditation feature in the existing CHPRBN app.

## App Context (MANDATORY)
The app uses type-safe Compose Navigation with `@Serializable` route types in `ng.com.chprbn.mobile.core.navigation.Routes`, `AppNavHost` with `composable<Route>()`, `backStackEntry.toRoute<Route>()`, and `popUpTo<Route>()`. The dashboard already reserves an `ACCREDITATION` `GridMenuCard` (`requiredRole = "accreditation"`, icon `Icons.Outlined.WorkspacePremium`) wired to `onNavigateToAccreditation = { /* TODO: Accreditation feature */ }` in `AppNavHost.kt` (line ~120) and `UnifiedDashboardScreen.kt` (lines 65, 250–255).

## Officer-Assigned Institution Rule (MANDATORY)
An officer is **assigned** an institution for accreditation — just like an officer is assigned a centre/paper in the exam attendance flow. The officer **cannot** select or search for an institution on their own. Therefore:

- **No `InstitutionSearchSelect` route exists.** Remove it entirely.
- The officer's assigned institution(s) come from the server-synced list shown in `AccreditationHub`.
- `InstitutionProfile` is reachable only from `AccreditationHub` (tapping an assigned institution row).

## Task
1. Add `@Serializable` route declarations to `Routes.kt` for every accreditation screen:
   - `AccreditationHub`
   - `InstitutionProfile` (args: institutionId)
   - `AssessmentOverview` (args: institutionId)
   - Section forms `SectionAGeneralInformation` … `SectionFClinicalTraining` (args: institutionId, sectionKey)
   - `CriteriaChecklist` (args: institutionId)
   - `ScoreEntry` (args: institutionId)
   - `SubmitForReview` (args: institutionId, assessmentId)
   - `AssessmentResult` (args: institutionId, decisionId)
   - `LocalDrafts`
   - `SyncStatus`
   All `@Serializable` data classes with nullable String args where appropriate.

2. Add `composable<Route>()` entries to `AppNavHost` for each, each with back arrow → `navController.popBackStack()`, and correct hierarchical pop behavior (e.g., section forms pop back to Assessment Overview; ScoreEntry pops back to Criteria Checklist; AssessmentResult pops back to SubmitForReview; SyncStatus pops back to Hub).

3. Wire `onNavigateToAccreditation` in `UnifiedDashboardScreen` and `AppNavHost` to navigate to `Routes.AccreditationHub`.

4. Role-gating: the accreditation hub should only be reachable when the officer's roles include "accreditation" (reuse the dashboard `requiredRole` filtering pattern from `DashboardFeatureGridSection`).

## Output
Kotlin code snippets for `Routes.kt` additions and `AppNavHost.kt` composable additions, consistent with existing file conventions. Do not modify existing OSYVALAC/verification routes. Do not include an `InstitutionSearchSelect` route.