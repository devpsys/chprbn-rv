# CHPRBN Mobile

Android application for **Community Health Practitioners Registration Board of Nigeria (CHPRBN)** field operations: authenticating officers, looking up practitioner licenses, capturing on-site verifications, managing verified records locally, and syncing them to a central API.

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
10. [Contributing](#contributing)  
11. [Future improvements](#future-improvements)  

---

## Project overview

### Purpose

This app supports **practitioner license verification** workflows: officers sign in, search license records (QR or manual entry), review details, complete a structured verification form when policy allows, maintain a **verified practitioners** list on-device, and **upload** pending verifications to the backend with retry and status tracking.

### Key features

| Feature | Summary |
|--------|---------|
| **Authentication** | Email/password login against API; session cached in Room; limited offline login when network unavailable and cache exists. |
| **Dashboard** | Entry hub for verified list, sync, profile; feature tiles backed by domain/use cases. |
| **QR scan & manual entry** | CameraX + ML Kit barcode scanning; manual license number entry; navigation to record detail. |
| **License record retrieval** | Remote lookup via Retrofit; result cached in `scan.db`; composite remote tries API then dev fake data if primary yields nothing. |
| **Verification** | Full `LicenseRecord` passed into form; rules (e.g. active license) enforced in UI/domain; save to `verified_licenses`. |
| **Verified records** | Local list with search/filter; sync status (pending/synced/failed); refreshed after successful saves. |
| **Sync** | Push pending/failed rows to `POST practitioners/verified-sync`; per-record status updates; sync history UI. |
| **Profile** | Officer profile screen; profile data tied to auth/local user where applicable. |

---

## Architecture

The app follows **Clean Architecture** with clear boundaries per layer.

### Layers

| Layer | Responsibility |
|-------|----------------|
| **Presentation** | Jetpack Compose screens, ViewModels, UI state (`StateFlow`), navigation (`NavHost`). Depends on domain via use cases only (no direct Retrofit/Room in Composables). |
| **Domain** | Entities, repository **interfaces**, use cases. Pure Kotlin; no Android framework or Gson/Room types. Business rules (e.g. save verification validation) live here where appropriate. |
| **Data** | Repository **implementations**, Room DAOs/entities, Retrofit APIs, DTOs, mappers (DTO/Entity ↔ Domain). Remote sources can be composed (e.g. API + fake fallback). |

### Data flow

Typical flow:

```text
UI (Composable)
    → ViewModel
    → UseCase
    → Repository (interface)
    → RepositoryImpl
    → Local: Room DAO / Remote: Retrofit (+ optional fake source)
```

- **Read path:** Data layer maps persistence/API models to domain models before the use case returns.  
- **Write path:** Domain payloads are mapped to entities/DTOs in the data layer.  
- **Dependency injection:** [Hilt](https://dagger.dev/hilt/) wires implementations to interfaces and provides databases/API clients.

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Async | Kotlin Coroutines |
| DI | Hilt (Dagger) |
| Networking | Retrofit 2, OkHttp (logging), Gson |
| Persistence | Room (`scan.db`, `auth.db`) |
| Images | Coil (Compose) |
| Scanning | CameraX, ML Kit Barcode Scanning |
| Navigation | Navigation Compose |
| Build | Kotlin DSL, Gradle Version Catalog (`libs.versions.toml`), KSP (Room, Hilt) |

> This is a **single-module** Android app (`:app`), not Flutter.

---

## Project structure

High-level layout under `app/src/main/java/ng/com/chprbn/mobile/`:

```text
ng.com.chprbn.mobile/
├── ChprbnApplication.kt          # @HiltAndroidApp
├── core/
│   ├── navigation/               # Routes, AppNavHost
│   └── designsystem/           # Theme, shared Compose components
└── feature/
    ├── auth/                     # Splash, login; User; AuthRepository; auth.db
    ├── dashboard/                # Dashboard, feature list, profile API stub
    ├── profile/                  # Profile screen & use cases
    ├── scan/                     # QR/manual, record detail; license cache; ScanRepository
    ├── verified/                 # Verification form, verified list, verified_licenses (Room)
    └── sync/                     # Sync screen, sync history, SyncRepository, push API
```

| Path | Role |
|------|------|
| `feature/*/presentation/` | Compose UI + ViewModels |
| `feature/*/domain/` | Models, repository contracts, use cases |
| `feature/*/data/` | Repositories, API, Room, DTOs, mappers, DI modules |
| `core/navigation/` | Central navigation graph |
| `docs/` | API documentation (Markdown, HTML, `openapi.yaml`) |

---

## Setup

### Prerequisites

- **Android Studio** (recent stable, aligned with AGP in `gradle/libs.versions.toml`)
- **JDK 17+** (project uses Java 11 language level for Android compile)
- Android SDK with **compileSdk 36** (or as defined in `app/build.gradle.kts`)

### Clone & open

```bash
git clone <repository-url>
cd chprbn_revamp
```

Open the folder in Android Studio as a Gradle project (**rootProject.name** = `CHPRBN`).

### Dependencies & run

Gradle downloads dependencies automatically.

```bash
# Windows
.\gradlew.bat installDebug

# macOS / Linux
./gradlew installDebug
```

Or use **Run ▸ Run 'app'** with a device/emulator (**minSdk 24**).

---

## Configuration

### Base API URL

The Retrofit base URL is provided in **`AuthDataModule`** (auth feature), currently:

`https://chprbn.gov.ng/api/v1/`

All feature Retrofit services share this `Retrofit` instance unless refactored.

To point at staging or a local server, change `BASE_URL` in:

`app/src/main/java/ng/com/chprbn/mobile/feature/auth/data/di/AuthDataModule.kt`

### Environments

There is no separate product flavor in Gradle today; API target is effectively **single-environment** via the constant above. Add `buildConfigField` or flavors if you need dev/stage/prod switching without code edits.

### Mock vs live license lookup

License lookup uses **`CompositeLicenseRecordRemoteSource`**: it calls the **real API** first (`ApiLicenseRecordRemoteSource`), then falls back to **`FakeLicenseRecordRemoteSource`** if the API returns no usable record.

To use **only** the API in production, replace the binding in `ScanModule.provideLicenseRecordRemoteSource` with the API implementation only (or gate the fake behind `BuildConfig.DEBUG` if you add a build flag).

> Verified sync uses the real Retrofit service; there is no fake sync client in the same pattern.

---

## Features

### Auth

- Login posts to `POST auth/login`; tokens and user cached in **`auth.db`**.  
- Offline: if the device has no connectivity, login may succeed using a **cached user** for the same email (password not re-validated offline).

### Dashboard

- Shows feature cards (e.g. verified list, sync, profile).  
- User display name/role may come from cached **`User`** after login.

### Scan

- **QrScanScreen:** decodes license identifier; navigates to record detail.  
- **ManualEntryScreen:** typed license number.  
- **RecordDetailScreen:** shows cached/remote `LicenseRecord`; can open **VerificationForm** with full record serialized in navigation args (JSON).

### Verification

- **VerificationFormScreen** + **VerificationFormViewModel** validate inputs and call **SaveVerifiedLicenseUseCase** → **VerifiedRepository** → Room **`verified_licenses`**.  
- Business rules include marking practitioner present and license status checks as implemented.

### Verified list

- **VerifiedListScreen** loads via **GetVerifiedLicensesUseCase**; filters (e.g. pending sync including failed); search filter.  
- Can be refreshed when returning from verification using navigation **SavedStateHandle** flags.

### Sync

- **SyncScreen** + **SyncViewModel**: counts by sync status, **Sync all** (pending + failed), **Retry failed**.  
- **SyncRepositoryImpl** posts one **`VerifiedSyncRequest`** per row and updates **syncStatus**, **lastSyncAttempt**, **syncError** in Room.

---

## Data & caching

### Room databases

| Database file | Contents |
|---------------|----------|
| **`auth.db`** | Authenticated user / session fields (e.g. token, profile fields). |
| **`scan.db`** | **`license_records`** (cached lookups), **`verified_licenses`** (verifications + sync metadata). |

Migrations exist for verified-license schema evolution; destructive migration remains as a **fallback** on failure—avoid relying on it in production without backup.

### Offline-first aspects

- License rows are **cached** after successful remote fetch for faster repeat access.  
- Verified records are **primarily local** until sync succeeds.  
- Auth uses cache for **offline session** continuation when appropriate.

### Sync mechanism

- Local status: **Pending**, **Synced**, **Failed**.  
- Upload is **sequential POSTs** (not a batch endpoint in the client).  
- Failures do not block other rows; errors stored per row for retry.

---

## API integration

| Resource | Location |
|----------|----------|
| Human-readable specs | [`docs/API_ENDPOINTS.md`](docs/API_ENDPOINTS.md) |
| HTML | [`docs/API_ENDPOINTS.html`](docs/API_ENDPOINTS.html) |
| OpenAPI 3.0 | [`docs/openapi.yaml`](docs/openapi.yaml) |

Endpoints are defined as Retrofit interfaces under each feature’s `data/api/` package (e.g. `AuthApiService`, `ScanApiService`, `VerifiedSyncApiService`). DTOs live in `data/dto/`; mapping to domain is in `data/mappers/`.

---

## Contributing

### Coding standards

- Prefer **Kotlin** idioms (data classes, sealed types for UI/error states, coroutines).  
- Keep **domain** free of Android imports and JSON framework types.  
- New screens: **Compose** + **ViewModel** + **UseCase**; avoid calling DAOs/APIs from ViewModels directly—go through repositories.  
- Follow existing **package-by-feature** layout under `feature/<name>/`.

### Architecture

- **Do not** leak Room entities or Retrofit DTOs into domain or presentation.  
- Add **repository methods** and **use cases** when introducing new business operations.  
- Register bindings in the appropriate **Hilt `@Module`** for the feature.

### Branching (optional)

Use short-lived feature branches and pull requests; align with your org’s Git policy (e.g. `main` protected, `feature/xyz`).

---

## Future improvements

Ideas that fit the current architecture without rewriting layers:

- **WorkManager** (or similar) for **background sync** batches and retry backoff.  
- **OkHttp interceptor** for `Authorization: Bearer <token>` on all protected routes.  
- **Token refresh** flow + secure storage (EncryptedSharedPreferences / Keystore).  
- **Certificate pinning** and stricter TLS for production.  
- **DB encryption** (SQLCipher) or field-level encryption for highly sensitive PII on device.  
- **Product flavors** for dev/stage/prod base URLs and toggling `FakeLicenseRecordRemoteSource`.  
- Wire **GET dashboard/profile** when backend is ready (interface exists; repository may still be cache-only).

---

## License & support

Add your organization’s license and support contacts here if the repository is public or shared externally.

---

*README reflects the codebase as of the last update; when behavior changes, update this file together with `docs/` API specs.*
