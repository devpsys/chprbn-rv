# CHPRBN Mobile

Android application for **Community Health Practitioners Registration Board of Nigeria (CHPRBN)** field operations: authenticating officers, looking up practitioner licenses, capturing on-site verifications, managing verified records locally, syncing them to a central API, and running practical examinations / assessments.

**Package:** `ng.com.chprbn.mobile`
**Gradle project name:** `CHPRBN`

---

## Table of contents

1. [Project overview](#project-overview)
2. [Architecture](#architecture)
3. [Tech stack](#tech-stack)
4. [Project structure](#project-structure)
5. [Setup](#setup)
6. [Configuration](#configuration)
7. [Features](#features)
8. [Data & caching](#data--caching)
9. [API integration](#api-integration)
10. [Testing](#testing)
11. [Contributing](#contributing)
12. [Future improvements](#future-improvements)

---

## Project overview

### Purpose

This app supports two operational tracks:

1. **License verification** — officers sign in, search license records (QR or manual entry), review details, complete a structured verification form when policy allows, maintain a **verified practitioners** list on-device, and **upload** pending verifications to the backend with retry and status tracking.
2. **Examination & assessment** — scheduling practical exams, indexing candidates against a paper, scoring per-section practical questions, and recording an overall project assessment score.

### Key features

| Feature | Summary |
|--------|---------|
| **Authentication** | Adhoc field-officer accounts: `POST adhoc/login` + `GET adhoc/profile` (Bearer); Sanctum token; session cached in Room; offline login when cache matches **username**. |
| **Dashboard** | Entry hub for verified list, sync, profile, exam attendance, and practical assessment; feature tiles backed by domain/use cases. |
| **QR scan & manual entry** | CameraX + ML Kit barcode scanning; manual license number entry; navigation to record detail. |
| **License record retrieval** | Remote lookup via Retrofit; result cached in `scan.db`; composite remote tries API then dev fake data if primary yields nothing. |
| **Verification** | Full `LicenseRecord` passed into form; rules (e.g. active license) enforced in UI/domain; save to `verified_licenses`. |
| **Verified records & sync** | Local list with search/filter, sync status (pending/synced/failed), and sequential `POST practitioners/verified-sync` upload with per-row error capture. |
| **Profile** | Officer profile screen; profile data tied to auth/local user where applicable. |
| **Exam attendance** | Schedules → papers → candidates flow; QR scan candidate code → attendance result; exam statistics tab. |
| **Practical assessment** | Examination Schedules, Paper Detail, Candidates Directory (list/grid), Practical Sections hub, per-section Practical Scoring (with score steppers), and Project Assessment (overall score input). |

---

## Architecture

The app follows **Clean Architecture** with clear boundaries per layer, applied progressively — features that own real persistence/network state (`auth`, `profile`, `verification`) carry the full presentation/domain/data stack; features that are still UI-only today (`dashboard`, `scan`, `exam`, `assessment`) are presentation-only and gain data/domain layers when real state lands.

### Layers

| Layer | Responsibility |
|-------|----------------|
| **Presentation** | Jetpack Compose screens, ViewModels, UI state (`StateFlow`), navigation (`NavHost`). Depends on domain via use cases only (no direct Retrofit/Room in Composables). |
| **Domain** | Entities, repository **interfaces**, use cases. Pure Kotlin; no Android framework or Gson/Room types. Business rules (e.g. save-verification validation) live here where appropriate. |
| **Data** | Repository **implementations**, Room DAOs/entities, Retrofit APIs, DTOs, mappers (DTO/Entity ↔ Domain). Remote sources can be composed (e.g. API + fake fallback). |

### Data flow

Typical flow:

```text
UI (Composable)
    → ViewModel
    → UseCase
    → Repository (interface)
    → RepositoryImpl
    → Local: Room DAO  /  Remote: Retrofit (+ optional fake source)
```

- **Read path:** the data layer maps persistence/API models to domain models before the use case returns.
- **Write path:** domain payloads are mapped to entities/DTOs in the data layer.
- **DI:** [Hilt](https://dagger.dev/hilt/) wires implementations to interfaces and provides databases / API clients.

### Navigation

Type-safe Compose Navigation. Routes are declared as `@Serializable` Kotlin objects (no args) or data classes (with args) inside [`core/navigation/Routes.kt`](app/src/main/java/ng/com/chprbn/mobile/core/navigation/Routes.kt), and `navigate()` calls pass the route instance directly. Each destination's ViewModel receives args via `SavedStateHandle.get<...>(name)` — the same property names compile directly into the route pattern, so changing a field is a compile error at every call site.

> **Single-module by decision.** The codebase is intentionally one Gradle module (`:app`); we don't split into `:core:*` / `:feature:*`.

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin **2.2.10** |
| Build | Android Gradle Plugin **9.2.1**, Gradle KTS, Version Catalog (`gradle/libs.versions.toml`), KSP **2.3.2** (Room, Hilt) |
| UI | Jetpack Compose (BOM **2024.09.00**), Material 3 |
| Navigation | Navigation Compose **2.8.4** (type-safe `@Serializable` routes) |
| Async | Kotlin Coroutines |
| DI | Hilt **2.59.2** |
| Networking | Retrofit **2.11.0**, OkHttp **4.12.0** (logging), Gson |
| Persistence | Room **2.6.1** (`scan.db`, `auth.db`) |
| Images | Coil for Compose **2.5.0** |
| Scanning | CameraX, ML Kit Barcode Scanning |
| Coverage | Kover **0.9.1** (configured; report generation paused pending AGP 9.x compatibility) |

### SDK levels

| Setting | Value |
|---------|-------|
| `compileSdk` | 36 |
| `targetSdk` | 36 |
| `minSdk` | 24 |
| Java target | 11 |

---

## Project structure

High-level layout under `app/src/main/java/ng/com/chprbn/mobile/`:

```text
ng.com.chprbn.mobile/
├── ChprbnApplication.kt          # @HiltAndroidApp
├── core/
│   ├── designsystem/             # Theme, Color, Type, Shape, shared Compose components
│   ├── navigation/               # Routes, AppNavHost
│   ├── network/                  # Image URL normalization
│   └── persistence/              # Room converters, DB key provider, migration guard
└── feature/
    ├── auth/                     # Splash, login; AuthRepository; auth.db
    ├── dashboard/                # Unified dashboard, feature tiles
    ├── profile/                  # Profile screen + use cases
    ├── scan/                     # Reusable QR scan composable (CameraX + ML Kit)
    ├── verification/             # Manual entry, record detail, verification form,
    │                             # verified list, sync, sync history, irregularity report
    ├── exam/                     # Exam dashboard, papers, paper detail, candidates,
    │                             # candidate scan result, statistics
    └── assessment/               # Examination schedules, paper detail, candidates
                                  # directory, practical sections hub, practical scoring,
                                  # project assessment
```

| Path | Role |
|------|------|
| `feature/*/presentation/` | Compose UI + ViewModels |
| `feature/*/domain/` | Models, repository contracts, use cases (where applicable) |
| `feature/*/data/` | Repositories, API, Room, DTOs, mappers, DI modules (where applicable) |
| `core/navigation/` | Central navigation graph (`Routes`, `AppNavHost`) |
| `core/designsystem/` | Material 3 theme + reusable Compose primitives |
| `core/persistence/` | Database key provider, migration guard, Room converters |
| `docs/` | API docs (`API_ENDPOINTS.md`, HTML, `openapi.yaml`), code-review notes, user manual |
| `ui-designs/` | Source HTML/Figma exports for screens (per-feature folders) |

---

## Setup

### Prerequisites

- **Android Studio** (recent stable, aligned with the AGP version above)
- **JDK 17+** (project's Java compile target is 11)
- Android SDK with **compileSdk 36**

### Clone & open

```bash
git clone <repository-url>
cd chprbn_revamp
```

Open the folder in Android Studio as a Gradle project (**rootProject.name** = `CHPRBN`).

### Build & install

```bash
# Windows
.\gradlew.bat :app:assembleDebug         # build only
.\gradlew.bat :app:installDebug          # build + install on connected device

# macOS / Linux
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

Or use **Run ▸ Run 'app'** with a device/emulator (**minSdk 24**).

---

## Configuration

### Base API URL

The Retrofit base URL is exposed as `BuildConfig.BASE_URL`, generated from `buildConfigField` entries in **`app/build.gradle.kts`** under each build type. Today both `debug` and `release` point at prod (`https://app.chprbn.gov.ng/api/v1/mobile/`) because no staging API exists yet.

`AuthDataModule` provides the singleton `Retrofit` instance using `BuildConfig.BASE_URL`; all feature Retrofit services share it unless refactored.

### Environments

To point at staging or a local server, change the `BASE_URL` `buildConfigField` for the relevant build type in `app/build.gradle.kts` — no source edit, no code change. Example for a future staging environment:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"https://staging.chprbn.gov.ng/api/v1/mobile/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://app.chprbn.gov.ng/api/v1/mobile/\"")
    }
}
```

If finer-grained switching is needed (dev / stage / prod / local), add product flavors instead of overloading the build types.

### Mock vs live license lookup

License lookup uses **`CompositeLicenseRecordRemoteSource`**: it calls the **real API** first (`ApiLicenseRecordRemoteSource`), then falls back to **`FakeLicenseRecordRemoteSource`** if the API returns no usable record.

To use **only** the API in production, replace the binding in `ScanModule.provideLicenseRecordRemoteSource` with the API implementation only (or gate the fake behind `BuildConfig.DEBUG` if you add a build flag).

> Verified sync uses the real Retrofit service; there is no fake sync client in the same pattern.

---

## Features

### Auth

- Login posts to `POST adhoc/login`; response includes `data.token`; client then calls `GET adhoc/profile` with `Authorization: Bearer` and maps adhoc profile `data` to domain. Tokens and profile are cached in **`auth.db`**.
- **OkHttp** adds Bearer via [AuthorizationInterceptor](app/src/main/java/ng/com/chprbn/mobile/feature/auth/data/network/AuthorizationInterceptor.kt) using [AuthTokenStore](app/src/main/java/ng/com/chprbn/mobile/feature/auth/data/network/AuthTokenStore.kt) (set on login / splash; cleared on logout).
- Offline: login may succeed using a **cached user** for the same **username** without password re-validation.

### Dashboard

- Unified dashboard surfaces tiles for verification, verified list, sync, profile, exam attendance, and practical assessment.
- User display name/role come from cached **`User`** after login.

### Scan

- **QrScanScreen** — reusable Compose camera screen (CameraX + ML Kit) with a manual-entry fallback. Used by both license verification and exam/assessment flows; the destination of `onQrScanned` differs per caller.

### Verification

- **ManualEntryScreen** — typed license number; `forExam` flag swaps copy.
- **RecordDetailScreen** — shows cached/remote `LicenseRecord`; can open **VerificationForm** with the full record.
- **VerificationFormScreen** + **VerificationFormViewModel** validate inputs and call **SaveVerifiedLicenseUseCase** → **VerifiedRepository** → Room **`verified_licenses`**.
- **VerifiedListScreen** loads via **GetVerifiedLicensesUseCase**; filters (e.g. pending sync including failed); search filter. Refreshes via navigation `SavedStateHandle` flags after a successful save.
- **SyncScreen** + **SyncViewModel** — counts by sync status, **Sync all** (pending + failed), **Retry failed**. **SyncRepositoryImpl** posts one **`VerifiedSyncRequest`** per row and updates **syncStatus**, **lastSyncAttempt**, **syncError** in Room.
- **SyncHistoryScreen** — timeline of past sync attempts.
- **ReportIrregularityScreen** — captures and reports irregularities tied to a license.

### Exam attendance

- **ExamDashboard / ExamPapers / ExamPaper / ExamCandidates** — drill-down from exam to paper to candidate roster.
- **ExamScan** — assessment-style camera flow that lands on **CandidateScanResult** with mark-attendance / cancel actions.
- **ExamStatistics** — counters and per-status breakdowns.
- Currently presentation-only with placeholder data; data/domain layers will land alongside the real exam API.

### Practical assessment

Reached from the dashboard's **Grade Practical** tile.

- **ExaminationSchedulesScreen** — list of scheduled exams with sync-status pills.
- **AssessmentPaperDetail** — hero card, progress, candidate directory preview, Scan QR FAB. Tap **Scan QR** to enter the assessment-side scan flow.
- **AssessmentCandidates** — single screen with a list/grid toggle.
- **AssessmentPracticalSections** — landed by the assessment-side QR scan; shows the candidate summary, three section progress cards (Complete / Incomplete / Not Started), and an "Assess Project" Extended FAB. Tapping the candidate photo morphs it from the avatar's exact on-screen position to a 3× card at screen centre (translation + scale + corner radius driven by a single `Transition`).
- **AssessmentPracticalScoring** — per-section question cards with image, prompt, status pill, and a clamped score stepper. Single "Save Scores" Extended FAB.
- **AssessmentProjectAssessment** — candidate profile + a single decimal score input (0..max with one decimal place, partial entries allowed mid-typing) and stacked Cancel / Save Score FABs.

Currently presentation-only with placeholder data.

### Profile

- **ProfileScreen** + use cases — view/edit profile, change password, logout. Backed by **`auth.db`**.

---

## Data & caching

### Room databases

| Database file | Contents |
|---------------|----------|
| **`auth.db`** | Authenticated user / session fields (e.g. token, profile fields). |
| **`scan.db`** | **`license_records`** (cached lookups), **`verified_licenses`** (verifications + sync metadata). |

Migrations exist for verified-license schema evolution; destructive migration remains as a **fallback** on failure — avoid relying on it in production without backup. `core/persistence/` houses the database key provider and a migration guard that fails fast when the schema diverges from expectations.

### Offline-first aspects

- License rows are **cached** after successful remote fetch for faster repeat access.
- Verified records are **primarily local** until sync succeeds.
- Auth uses cache for **offline session** continuation when appropriate.

### Sync mechanism

- Local status: **Pending**, **Synced**, **Failed**.
- Upload is **sequential POSTs** (no batch endpoint in the client today).
- Failures do not block other rows; errors are stored per row for retry.

---

## API integration

| Resource | Location |
|----------|----------|
| Human-readable specs | [`docs/API_ENDPOINTS.md`](docs/API_ENDPOINTS.md) |
| HTML | [`docs/API_ENDPOINTS.html`](docs/API_ENDPOINTS.html) |
| OpenAPI 3.0 | [`docs/openapi.yaml`](docs/openapi.yaml) |
| Irregularity reporting | [`docs/LICENSE_IRREGULARITY_REPORT_API.md`](docs/LICENSE_IRREGULARITY_REPORT_API.md) |

Endpoints are defined as Retrofit interfaces under each feature's `data/api/` package (e.g. `AuthApiService`, `LicenseApiService`, `VerifiedSyncApiService`). DTOs live in `data/dto/`; mapping to domain is in `data/mappers/`.

---

## Testing

```bash
./gradlew :app:test                       # JVM unit tests
./gradlew :app:connectedDebugAndroidTest  # instrumentation tests on a device
```

### Unit tests (`app/src/test/`)

~25 tests today covering:

- **Auth** — `LoginUseCase`, `AuthRepositoryImpl`, `LoginViewModel`, `SplashViewModel`.
- **Profile** — `GetUserProfileUseCase`, `UpdateUserProfileUseCase`, `LogoutUseCase`, `ProfileRepositoryImpl`, `ProfileViewModel`.
- **Verification** — `LicenseRepositoryImpl`, `VerifiedRepositoryImpl`, `GetLicenseRecordUseCase`, `SaveVerifiedLicenseUseCase`, `ManualEntryViewModel`, `RecordDetailViewModel`, `VerificationFormViewModel`, `SyncViewModel`.
- **Exam** — `ExamPapersViewModel`, `ExamPaperViewModel`, `ExamCandidatesViewModel`, `CandidateScanResultViewModel`.
- **Core persistence** — `DatabaseKeyProvider`, `DatabaseMigrationGuard`.

A `MainDispatcherRule` under `core/utils/` swaps `Dispatchers.Main` for tests.

### Instrumentation tests (`app/src/androidTest/`)

Currently a single placeholder `ExampleInstrumentedTest`. Compose UI test deps (`androidx.compose.ui:ui-test-junit4`, `espresso-core`, `androidx.junit`) are wired and ready when we expand coverage to UI.

### Coverage

Kover **0.9.1** is configured but report generation is paused while AGP 9.x compatibility is finalized.

---

## Contributing

### Coding standards

- Prefer **Kotlin** idioms (data classes, sealed types for UI/error states, coroutines).
- Keep **domain** free of Android imports and JSON framework types.
- New screens: **Compose** + **ViewModel** + **UseCase**; avoid calling DAOs/APIs from ViewModels directly — go through repositories.
- Follow existing **package-by-feature** layout under `feature/<name>/`.

### Architecture

- **Do not** leak Room entities or Retrofit DTOs into domain or presentation.
- Add **repository methods** and **use cases** when introducing new business operations.
- Register bindings in the appropriate **Hilt `@Module`** for the feature.
- Don't preemptively add `data/` / `domain/` to UI-only features (`dashboard`, `scan`, `exam`, `assessment`); add them when the corresponding API/persistence work begins.
- The codebase is intentionally **single-module**; do not propose splitting into `:core:*` / `:feature:*` modules.

### Branching

Use short-lived feature branches and pull requests; align with your org's Git policy (e.g. `main` protected, `feature/xyz`).

---

## Future improvements

Ideas that fit the current architecture without rewriting layers:

- **WorkManager** (or similar) for **background sync** batches and retry backoff.
- **Token refresh** flow + secure storage (EncryptedSharedPreferences / Keystore).
- **Certificate pinning** and stricter TLS for production.
- **DB encryption** (SQLCipher) or field-level encryption for highly sensitive PII on device.
- **Product flavors** for dev/stage/prod base URLs and toggling `FakeLicenseRecordRemoteSource`.
- **Compose UI tests** for the `*Content` composables across features (deps already wired).
- **Real exam + assessment data layer** when those endpoints land — the current presentation-only screens emit placeholder state from their VMs.
- Optional **remote profile refresh** via `DashboardApiService.getProfile()`; the dashboard repository is still cache-first today.

---

## License & support

Add your organization's license and support contacts here if the repository is public or shared externally.

---

*README reflects the codebase as of the last update; when behavior changes, update this file together with `docs/` API specs.*
