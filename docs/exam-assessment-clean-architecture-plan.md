# Exam & Assessment — Clean Architecture Plan

**Status:** Authoritative blueprint for the Exam and Assessment feature rebuild.
**Audience:** Engineers implementing the data + domain layers backing the existing presentation scaffolds.
**Reference implementation:** `feature/verification/` (already shipped, full vertical slice).
**Legacy reference:** `docs/old/data-domain-modules.md` (CHPRBN v1 — Attendance + Assessment only).
**Repository constraint (durable):** single Gradle module; no `:core:*` / `:feature:*` split.

---

## 1. Executive Summary

### 1.1 Goals

1. Stand up the **domain** and **data** layers for two features whose **presentation** layers already exist as compile-clean placeholders:
   - **Exam** — examiner-side attendance + statistics + sync (driven by `ExamDashboard → ExamPapers → ExamPaper → {ExamCandidates, ExamScan→CandidateScanResult, ExamStatistics}`).
   - **Assessment** — scheduled practical and project grading (`ExaminationSchedules → AssessmentPaperDetail → {AssessmentCandidates, AssessmentScan → AssessmentPracticalSections → {AssessmentPracticalScoring, AssessmentProjectAssessment}}`).
2. Strictly observe Clean Architecture **inside a single Gradle module** — boundaries are enforced by **package**, not by module dependency. Domain code must remain free of Android, Room, Retrofit, Gson, and Compose imports.
3. Replicate the patterns proven by `feature/verification/` (sealed `*Result` types, `Api*/Fake*/Composite*` remote-source triplet, mappers as top-level extension functions, write-through-Room repositories, encrypted Room via `core/persistence/encryption`, Hilt with one module per logical sub-area).
4. Eliminate the legacy v1's structural defects: data-layer types crossing the domain boundary, god-object repositories, magic-string paper codes, mutable domain models, swallowed errors, and Room entities used as HTTP wire formats.
5. Surface — explicitly and early — that **the backend API for exam and assessment does not yet exist** (confirmed from `docs/API_ENDPOINTS.md`, `docs/openapi.yaml`, `mobile_api_v1_documentation.html`). The plan therefore depends on a backend contract negotiation, captured in section 12.

### 1.2 Key architectural decisions

| # | Decision | Rationale |
|---|---|---|
| D1 | **Single module, package-enforced layering.** | Per durable memory; matches verification feature. |
| D2 | **One feature root per feature.** `feature/exam/`, `feature/assessment/`. Cross-feature primitives (Candidate, Officer, SyncJob queue) live in `core/` not in a shared feature. | Avoids the legacy "ExamRepository conflates attendance, practical, and project" god-object trap. |
| D3 | **Domain layer holds plain Kotlin only.** Models are immutable data classes / enums / sealed hierarchies. No Flow at repository contracts (matches verification — Flow is reserved for reactive list screens via DAO `Flow<List<…>>` injected through the *data* layer to the ViewModel, with the repository returning `suspend` snapshots). | Matches the existing verification surface; keeps the boundary discoverable. |
| D4 | **Sealed `*Result` hierarchies at the domain boundary** (`Success / NotFound / Error(message)` for fetches; `Success / Error(message)` for commands; `*BatchResult(attempted, succeeded, failed, errors)` for partial-success batches). No `kotlin.Result`, no thrown exceptions cross the boundary. | Verified-feature convention; the UI gets typed, presentable failure modes. |
| D5 | **Encrypted Room via SQLCipher**, one DB per feature: `exam.db`, `assessment.db`. Share `SupportOpenHelperFactory` from `core/persistence/encryption/EncryptionModule`. Add new DB names to `DatabaseMigrationGuard.legacyDbNames` to clear any pre-encryption file (none today). | Same factory as verification's `scan.db`; consistent at-rest protection. |
| D6 | **Offline-first writes with `SyncStatus` per row.** Every locally-mutated entity (Attendance, Remark, PracticalScore, ProjectScore) carries `syncStatus`, `lastSyncAttemptAt`, `syncError`. Background sync flips status; UI counts pending/failed off Room. | Verification's `verified_licenses` model proven to work; legacy's `synced: Int` pattern carried forward in a typed form. |
| D7 | **Domain-typed paper kinds.** Replace legacy `"PA"` / `"PE"` magic strings with a sealed `PaperKind { Theory, Practical, Project }` (or enum if the discriminator is wire-stable). | Removes the worst v1 code smell. |
| D8 | **`Api*/Fake*/Composite*` remote-source triplet for any read that must render before the backend ships.** Hot-reload-friendly. | The exam/assessment endpoints don't exist yet (§12); we cannot block UI work on a backend that isn't there. |
| D9 | **Per-row sync writes**, identical contract to `POST /practitioners/verified-sync`. No bespoke "batch endpoint" assumed. When the backend ships a batch route, the engine swaps without touching the queue. | Matches the only template the API documents today. |
| D10 | **Hilt with one `@Module` per logical sub-area.** `ExamDatabaseModule`, `AttendanceModule`, `ExamSyncModule`, `AssessmentDatabaseModule`, `PracticalScoringModule`, `ProjectScoringModule`, `AssessmentSyncModule`. `@Binds` for interface→impl, `@Provides` in `companion object` for Retrofit `.create()`. | Verified-feature convention; keeps Hilt graphs reviewable. |
| D11 | **No god-object repositories.** Split by aggregate: `AttendanceRepository`, `ExamPaperRepository`, `ExamCandidateRepository`, `ExamSyncRepository`, `RemarkRepository`, `AssessmentScheduleRepository`, `PracticalScoringRepository`, `ProjectScoringRepository`, `AssessmentSyncRepository`. | Direct repudiation of legacy's 16-method `ExamRepository`. |
| D12 | **Compose presentation untouched** beyond replacing `placeholder()` in each `*ViewModel` with use-case calls, `collectAsStateWithLifecycle()`, and `consume*()` helpers for terminal arms — exactly the verification pattern. | The presentation layer is already on-design; do not refactor it. |

### 1.3 Expected outcomes

- Compiles cleanly with the existing presentation layer the day the plan is executed; no presentation-layer churn required.
- A faked backend lets every screen render realistic data offline within sprint 1, decoupling UI iteration from server delivery.
- A single `SyncWorker` (WorkManager) plus per-feature `*SyncRepository` instances drives the entire "Pending / Synced / Failed" surface used by `ExamStatistics`, `AssessmentPaperDetail`'s per-row pills, and `ExaminationSchedules`' per-schedule pills.
- A backend-contract document falls out as a side-product (§12) — the explicit gap list that needs server work.

---

## 2. Legacy Analysis (docs/old/data-domain-modules.md)

### 2.1 Summary

The legacy v1 implemented Attendance + Assessment in a three-module project (`app`, `domain`, `data`) — but the dependency was **inverted** (`domain` depended on `data`), contaminating every domain class with Room imports. The data layer modelled the exam graph as a single `attendance/fetch-record` payload hydrated into Room via a transactional insert, with attendance writes pushed back via `attendance/push-record` and (with magic-string paper codes `"PA"` / `"PE"`) practical / project scores pushed back via `practical/push-record` and `project/push-record`. The assessment side modelled a four-level hierarchy `Area → Category → Item → SubItem` against per-institution rows, with a single push endpoint `assessment/push-record`.

### 2.2 Strengths to preserve

| Pattern | Preserved as |
|---|---|
| `synced: Int` per row + server returns `data: List<Long>` of accepted IDs + `markSynced(ids)` | `SyncStatus { Pending, Synced, Failed }` enum on every locally-mutated entity (matches `VerifiedLicenseEntity` shape). |
| Single-transaction "download the world" via `@Transaction insertRecordTransaction(...)` | `DownloadAssessmentPackageUseCase` + `@Transaction` on `AssessmentSchedulePackageDao.replaceAll(...)`. Same for exam. |
| Retrofit interceptor stack (Auth, Network, Logging, Retry) | Carried over by being part of `core/network/` (already shared). |
| `Result<T>` Retrofit envelope | Stays in the data layer; never exposed past the source. |
| Distinction between *lightweight* `Candidate` and *full* `Student` shape on the practical UI | Modelled cleanly: `AssessmentCandidate` (cached directory row) vs `CandidateProfile` (full record on detail screens). |
| DAO-level aggregations (`SUM(score)`) | Used for the per-candidate aggregate score on `AssessmentCandidatesScreen` — done in a single SQL query rather than reading a million rows into the VM. |

### 2.3 Weaknesses to address

| Smell | Mitigation |
|---|---|
| `domain → data` module direction. | Single-module layout; the dependency rule is enforced by **package import lint** (no `import …data.…` from `domain.` packages — add a `lint.xml` rule in §11). |
| Repositories return Room `*Entity` types (`List<ScheduleEntity>` etc.). | Repositories return **domain types only**. Mappers live at the data-layer edge. |
| `ExamRepository` is a 16-method god-object spanning attendance + practical + project. | Split per aggregate (D11). |
| Magic strings `"PA"` / `"PE"` drive branching across DAOs, sources, repos, use cases. | Sealed `PaperKind` (D7). |
| `Scores(practicals: List<ScoreEntity>, projects: List<ProjectScoreEntity>)` uses Room entities as the wire format. | Explicit `*RequestDto` / `*ResponseDto` per endpoint; no Room types on the wire. |
| `Schedule.studentCount: var`, `Question.score: var`, `Score.score/remark: var`, `ExamHeader.*: var` — mutable domain. | Immutable `data class` everywhere; mutations happen by re-emitting state from the ViewModel. |
| `fun Student.photo(): Bitmap?` — Android framework in the domain. | Domain keeps `photoUrl: String?` (and / or `photoDataUri: String?`); decoding happens in Compose via `AsyncImage`. |
| `AssessmentEntity` has **no** `synced` flag (unlike attendance). | Every locally-mutated entity in the new design gets `SyncStatus`, including assessment scores. |
| `getAll()` returning `emptyList()`; three `*UseCase` classes with commented-out bodies; `synchronize()` returning `""` | All dead code dropped. Only ship what is implemented. |
| `SaveAssessmentsUseCase` = try/catch over a loop, returning `Boolean`. | New equivalent uses Room `@Transaction` + returns `Save*Result`. |
| `Record` domain model uses snake_case (`exam_number`, `full_name`). | All domain identifiers are camelCase. |
| `@Singleton` on every mapper + every use case (boilerplate `@Provides` × 450 lines). | Mappers are top-level extension functions (no class, no scope). Use cases are `@Inject constructor` — Hilt discovers them. |
| `fallbackToDestructiveMigration()` in production. | Explicit numbered `Migration(n, n+1)` objects, with `fallbackToDestructiveMigration()` as a safety net only (matches verification). |
| Three `Institution`-shaped DTOs in three packages. | One DTO per endpoint; one domain `Institution` shared across features (lives in `core/domain/` or `feature/exam/domain/model/`). |

---

## 3. Verification Feature Review (the reference)

The verification feature is the canonical vertical slice. Implementation must match it shape-for-shape unless explicitly noted.

### 3.1 Package structure (replicate per feature)

```
feature/<name>/
├── <Name>Feature.kt                  // empty marker object
├── data/
│   ├── api/                          // *ApiService Retrofit interfaces
│   ├── di/                           // one *Module per sub-area
│   ├── dto/                          // wire DTOs; envelope colocated with payload
│   ├── local/                        // Room: <Name>Database, *Entity, *Dao
│   ├── mappers/                      // top-level extension funs grouped by topic
│   ├── repository/                   // *Impl only
│   └── source/                       // *RemoteSource interface + Api*/Fake*/Composite*
├── domain/
│   ├── model/                        // data classes, enums, sealed *Result types
│   ├── repository/                   // pure interfaces
│   └── usecase/                      // one class per single suspend operator fun invoke
└── presentation/                     // flat — Screen + Content + ViewModel + UiState
```

### 3.2 Naming conventions to inherit

| Concept | Suffix |
|---|---|
| Retrofit interface | `*ApiService` |
| Wire model | `*Dto`, envelope `*EnvelopeDto`, payload `*DataDto` |
| Room table | `*Entity` (`@Entity(tableName = "snake_case_plural")`) |
| Room DAO | `*Dao` |
| Repository contract | `*Repository` in `domain.repository/` |
| Repository impl | `*RepositoryImpl` in `data.repository/` |
| Remote source | `*RemoteSource` + `Api*`, `Fake*`, `Composite*` variants |
| Use case | `Verb*UseCase` with `operator fun invoke` |
| Domain result | `*Result` (sealed) or `*BatchResult` (data class for counters) |
| ViewModel | `*ViewModel` (`@HiltViewModel`) |
| UI state | `*UiState` (sealed if loading-phased, data class if form-shaped) |
| Screen composable | `*Screen` (public) + private `*Content` |
| Mappers file | `*Mappers.kt`, grouped by topic — functions named `toDomain()`, `toEntity()`, `toRequestDto()` |
| Hilt module | `*Module` |
| Hilt qualifier | annotation class (`@DatabaseKeyPrefs` is the only one today) |

### 3.3 Domain conventions (replicate)

- All public methods on repository interfaces are **`suspend`**. No Flow at the repository boundary.
- Sealed result for fetches: `Success(value) / NotFound / Error(message)`.
- Sealed result for commands: `Success / Error(message)`.
- Counters for partial-success batches: `data class XxxBatchResult(attempted: Int, succeeded: Int, failed: Int, errors: List<String>)`.
- Use cases own **input validation** (trim strings, reject blanks) — repositories receive sanitised input.

### 3.4 Data-layer conventions (replicate)

- DTO library: **Gson**. `@SerializedName` for snake-case wire → camelCase Kotlin. Optional fields nullable + defaulted (`= null`).
- DAOs are **`suspend`** (drop the older non-suspend style on `LicenseRecordDao`). `Flow<List<…>>` queries are allowed *inside the data layer* and consumed by the repository, which converts to a `suspend` snapshot or by a `*ListViewModel` directly if reactive list semantics are needed (rare; verification uses `suspend` snapshots even for lists).
- Repositories: `@Inject constructor`, every public method wraps body in `withContext(Dispatchers.IO) { … }`, Room is the single source of truth, every remote success is written through to the DAO before returning.
- Error mapping happens **at the repository**. `IOException → "Network error. Please check your connection."`; other `Throwable → t.message ?: "<contextual default>"`.
- `runCatching { … }.getOrNull()` for silent background refresh paths (must never crash callers).

### 3.5 Presentation conventions (already in place — do not touch)

- `@HiltViewModel class XyzViewModel @Inject constructor(…) : ViewModel()`.
- `private val _state = MutableStateFlow(…); val state: StateFlow<…> = _state.asStateFlow()`.
- `update { it.copy(…) }` for mutation.
- One-shot events are **terminal UiState arms** with `consume*()` methods, consumed in `LaunchedEffect`.
- `SavedStateHandle` for route args.
- Screen / Content split: `Screen` does Hilt + state + nav effects; `Content` is pure.

### 3.6 Encrypted Room wiring (replicate)

The `core/persistence/encryption/EncryptionModule` already exposes a `SupportOpenHelperFactory` keyed by a 256-bit SecureRandom passphrase persisted in EncryptedSharedPreferences (`@DatabaseKeyPrefs`). New databases plug in exactly like `LicenseDataModule.kt:34-46`:

```kotlin
Room.databaseBuilder(context, ExamDatabase::class.java, "exam.db")
    .openHelperFactory(supportFactory)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, …)
    .fallbackToDestructiveMigration()
    .build()
```

`DatabaseMigrationGuard` deletes legacy plaintext DBs on first run. Add `"exam.db"` and `"assessment.db"` to its `legacyDbNames` list if a prior plaintext version is ever shipped (none today).

### 3.7 Reference end-to-end flow (the template)

```
Compose Screen
   ↕ Hilt (hiltViewModel())
ViewModel  →  StateFlow<UiState>   (collectAsStateWithLifecycle())
   ↕ suspend
UseCase    →  input validation, build payload
   ↕ suspend, returns *Result
Repository →  withContext(IO) { dao first, remote second, write-through }
   ↕ suspend
RemoteSource (interface)  ←  Api / Fake / Composite
   ↕ Retrofit
ApiService
```

Mappers run at three pinch points: `Dto.toDomain()` immediately on parse; `Domain.toEntity()` before DAO insert; `Entity.toDomain()` immediately on read.

---

## 4. UI Data Requirements Analysis

The exam and assessment presentation layers compile today with hardcoded `placeholder()` UiStates. The analysis below is the **list of fields each ViewModel will actually source from a domain use case once the data layer lands**. Screen-by-screen mapping is exhaustive in the audit; this section condenses it to the data-shaping insights that drive entity design.

### 4.1 Exam screens — data summary

| Screen | Reads | Writes |
|---|---|---|
| **`UnifiedDashboard`** | `currentOfficer.{fullName,email,avatarUrl,membershipTier}`, `availableServices` (gated by entitlements). | — |
| **`ExamDashboard`** | `officerSession.center.{name,code,location,heroImageUrl}`, two `ExamTaskCardUiState` for Attendance + Practical with runtime chips (`Active Session`, `Pending Grading`). | Triggers `DownloadExamDossierUseCase` from FAB. |
| **`ExamPapers`** | `dailyOverview.{date,totalPapers,studentsLabel,statusPill}`, `papers: List<ExamPaperSummary>` (`status: Active|Upcoming|Completed`, `timeLabel`, `progressLabel`). | Sync FAB → `SyncExamRecordsUseCase`. |
| **`ExamPaper`** | `paper.{title,subtitle,startEnd,hall}`, `institution.{shortCode,name,location,heroImageUrl}`, `attendance.{totalCandidates,checkedIn,progressFraction,percentLabel}`, `sync.{lastSuccessAt,pendingCount,status}`. | "Sync Data" pill → `SyncExamRecordsUseCase`. |
| **`ExamCandidates`** | `candidates: List<ExamCandidateRow>` filterable by `AttendanceFilter { All, SignedIn, SignedOut, Flagged }`, searchable on name/`examNumber`, each row carries `attendance.{status, atLabel}` and `remarkCount`. | "Add Remark" → `AddRemarkUseCase`. Filter chip selection is local UI state. |
| **`CandidateScanResult`** | `CandidateProfile.{name,examNumber,photoUrl}`, `verification.{matchPercent}` (the QR payload is the lookup key), `paper.{date,hall}`. | "Mark Attendance" → `MarkAttendanceUseCase`. |
| **`ExamStatistics`** | `metrics.{recordsDownloaded, attendanceCaptured, recordsSynced, lastUpdatedAt, completionPercent}`, `chart.{cachedFraction,syncedFraction,counts}`, legend counters. | "Sync Now" → `SyncExamRecordsUseCase`. "Clear Cached" → `ClearExamCacheUseCase` (gated). |
| **Download Warning / Loading / Sync Loading** | `pendingUnsyncedCount`, `lastSyncedAt`, `progressFraction`, `progressLabel`, `statusCaption`. | Confirmation triggers `DownloadExamDossierUseCase`. |

### 4.2 Assessment screens — data summary

| Screen | Reads | Writes |
|---|---|---|
| **`ExaminationSchedules`** | `schedules: List<AssessmentScheduleSummary>` with per-row `syncStatus`. | Future overflow: download package / clear cache. |
| **`AssessmentPaperDetail`** | `paper.{title,statusLabel,facility,hall,heroImageUrl}`, `progress.{checkedIn,total,fraction}`, `candidatesPreview: List<AssessmentCandidateRow>` with per-row `SyncStatus`. | — |
| **`AssessmentCandidates`** | Full `candidates: List<AssessmentCandidateRow>`, searchable on name / indexing number, view-mode toggle, derived `aggregateScore` per candidate (`level: Normal|Low`). | "Add Remark" per row → `AddRemarkUseCase`. |
| **`AssessmentPracticalSections`** | `candidateProfile.{name,examNumber,photoUrl}`, `sections: List<PracticalSectionSummary>` (`status: NotStarted|Incomplete|Complete`, `footer`). | — |
| **`AssessmentPracticalScoring`** | `sectionTitle`, `questions: List<ScoreQuestion>` (`number,prompt,imageUrl,maxScore,currentScore,isScored`). | Each stepper tap → `RecordPracticalScoreUseCase` (writes through; debounced). "Save Scores" → `CommitPracticalSectionUseCase`. |
| **`AssessmentProjectAssessment`** | `candidateProfile`, `currentProjectScore`, `maxScore`. | "Save Score" → `RecordProjectScoreUseCase`. |

### 4.3 Implications for domain/data layers

Distilling the audit into **entity shapes** and **lifecycle classes**:

**Exam aggregate (offline-first writes, server-of-truth reads):**

- `OfficerSession` — read once at login. Identity = `(officerId, centerId, day)`.
- `Center` (or `Institution` — name unified across features) — read-only after download.
- `Paper` — read-only after download. Carries `paperKind: PaperKind`, `startAt/endAt`, `hall`, `centerId`.
- `Candidate` — read-only after download; cross-feature key is `examNumber` (== `indexingNumber` on the assessment side). Joins to `Paper` through `PaperCandidateAssignment`.
- `Attendance` — local-write. `(paperId, candidateId)` PK, `status: AttendanceStatus { SignedIn, SignedOut, Flagged }`, `markedAt`, `syncStatus`.
- `Remark` — local-write, append-only. `(candidateId, paperId?)`, `body`, `severity?`, `createdAt`, `syncStatus`.

**Assessment aggregate (offline-first writes, server-of-truth reads):**

- `AssessmentSchedule` — read-only. `id` (`"PE-2024"`), `paperKind`, `date`, `centerId`.
- `AssessmentPaper` — 1:1 with schedule for v1. Adds facility/hall/hero.
- `PracticalSection` — read-only. Belongs to schedule.
- `SectionQuestion` — read-only. Belongs to section. Carries `maxScore`, `prompt`, `imageUrl`.
- `PracticalScore` — local-write. `(scheduleId, candidateId, questionId)` PK, `score`, `scoredAt`, `syncStatus`. **Highest-volume write surface in the app.**
- `ProjectScore` — local-write. `(scheduleId, candidateId)` PK, `score: BigDecimal` (or `Double` — see D-Score precision note below), `maxScore`, `scoredAt`, `syncStatus`.
- `AssessmentCandidate` aggregate score (derived in SQL) — see `AssessmentCandidatesScreen`.

**Cross-feature concerns** that justify placement in `core/`:

- `Officer` and `OfficerSession` — shared by both features. Likely in `feature/auth/domain/model/` since it's already populated at login; both features import from there.
- `Candidate` — shared identity. Lives in `core/domain/model/` (proposed) or in whichever feature owns the canonical fetch (Exam, since its dossier download is broader).
- `SyncJob` queue + `SyncWorker` (WorkManager) — single engine in `core/sync/` driving both features' `*SyncRepository`.
- `PaperKind` enum — likely in `core/domain/model/` since it's shared.

**Score precision (D-Score) decision.** Legacy mixed `Int`, `Double`, and unmarked types. Pick **`Double`** uniformly with a max of two decimal places enforced in the UI; persist as REAL in Room. `BigDecimal` is overkill for a 0–100 rubric and complicates Room mapping.

---

## 5. Proposed Architecture

### 5.1 Package structure (final)

```
feature/exam/
├── ExamFeature.kt
├── data/
│   ├── api/                ExamDossierApiService, AttendanceApiService, ExamSyncApiService
│   ├── di/                 ExamDatabaseModule, AttendanceModule, ExamSyncModule, RemarkModule
│   ├── dto/                ExamDossierEnvelopeDto + ExamDossierDataDto, AttendanceSyncRequestDto,
│   │                       AttendanceSyncResponseDto (envelope + payload colocated)
│   ├── local/              ExamDatabase, CenterEntity, PaperEntity, CandidateEntity,
│   │                       PaperCandidateAssignmentEntity, AttendanceEntity, RemarkEntity, *Dao
│   ├── mappers/            DossierMappers.kt, AttendanceMappers.kt, RemarkMappers.kt
│   ├── repository/         ExamPaperRepositoryImpl, ExamCandidateRepositoryImpl,
│   │                       AttendanceRepositoryImpl, RemarkRepositoryImpl, ExamSyncRepositoryImpl,
│   │                       ExamStatisticsRepositoryImpl
│   └── source/             ExamDossierRemoteSource (interface) + Api*/Fake*/Composite*,
│                           AttendanceSyncRemoteSource (fun interface) + Api*
├── domain/
│   ├── model/              OfficerSession, Center, Paper, PaperKind, Candidate,
│   │                       Attendance, AttendanceStatus, Remark, RemarkSeverity,
│   │                       SyncStatus, SyncBatchResult,
│   │                       ExamPaperResult, AttendanceResult, ExamStatisticsResult, …
│   ├── repository/         ExamPaperRepository, ExamCandidateRepository,
│   │                       AttendanceRepository, RemarkRepository, ExamSyncRepository,
│   │                       ExamStatisticsRepository
│   └── usecase/            DownloadExamDossierUseCase, ClearExamCacheUseCase,
│                           GetExamDashboardUseCase, GetExamPapersUseCase,
│                           GetExamPaperDetailUseCase, GetExamCandidatesUseCase,
│                           SearchExamCandidatesUseCase, MarkAttendanceUseCase,
│                           AddRemarkUseCase, GetExamStatisticsUseCase, SyncExamRecordsUseCase
└── presentation/           (already exists; ViewModels wire to use cases)
```

```
feature/assessment/
├── AssessmentFeature.kt
├── data/
│   ├── api/                AssessmentPackageApiService, AssessmentSyncApiService
│   ├── di/                 AssessmentDatabaseModule, PracticalScoringModule,
│   │                       ProjectScoringModule, AssessmentSyncModule
│   ├── dto/                AssessmentPackageEnvelopeDto + …DataDto,
│   │                       PracticalScoreSyncRequestDto, ProjectScoreSyncRequestDto, …
│   ├── local/              AssessmentDatabase, AssessmentScheduleEntity,
│   │                       PracticalSectionEntity, SectionQuestionEntity,
│   │                       PracticalScoreEntity, ProjectScoreEntity,
│   │                       AssessmentCandidateEntity, *Dao
│   ├── mappers/            PackageMappers.kt, PracticalScoreMappers.kt, ProjectScoreMappers.kt
│   ├── repository/         AssessmentScheduleRepositoryImpl,
│   │                       AssessmentCandidateRepositoryImpl,
│   │                       PracticalScoringRepositoryImpl,
│   │                       ProjectScoringRepositoryImpl,
│   │                       AssessmentSyncRepositoryImpl
│   └── source/             AssessmentPackageRemoteSource (+ Api/Fake/Composite),
│                           AssessmentSyncRemoteSource (+ Api)
├── domain/
│   ├── model/              AssessmentSchedule, AssessmentPaper, PracticalSection, SectionQuestion,
│   │                       PracticalScore, ProjectScore, AssessmentCandidate,
│   │                       AssessmentDashboardSummary, CandidateAggregateScore,
│   │                       AssessmentScheduleResult, SaveScoreResult, …
│   ├── repository/         AssessmentScheduleRepository, AssessmentCandidateRepository,
│   │                       PracticalScoringRepository, ProjectScoringRepository,
│   │                       AssessmentSyncRepository
│   └── usecase/            DownloadAssessmentPackageUseCase, ClearAssessmentCacheUseCase,
│                           GetExaminationSchedulesUseCase, GetAssessmentPaperDetailUseCase,
│                           GetAssessmentCandidatesUseCase, GetPracticalSectionsUseCase,
│                           GetPracticalQuestionsUseCase, RecordPracticalScoreUseCase,
│                           CommitPracticalSectionUseCase, RecordProjectScoreUseCase,
│                           SyncAssessmentScoresUseCase
└── presentation/           (already exists; ViewModels wire to use cases)
```

```
core/                         (additions — only what's new)
├── domain/
│   └── model/              Candidate, PaperKind             // shared identity types
├── sync/
│   ├── SyncJob.kt, SyncStatus.kt, SyncJobDao.kt, SyncJobEntity.kt
│   ├── SyncWorker.kt        // WorkManager CoroutineWorker
│   └── di/SyncModule.kt
└── network/
    └── (existing — no changes; the ImageUrlNormalization helper already covers photo URIs)
```

### 5.2 Component responsibilities

| Layer | Responsibility | Forbidden |
|---|---|---|
| **`presentation/`** | Compose UI, ViewModel state shaping, navigation. | Direct DAO / API calls. Domain model mutation. |
| **`domain/usecase/`** | Single-purpose orchestration. Input validation. Mapping `*Result` to caller. | Touching DAO / API directly. Holding state. Android imports. |
| **`domain/repository/`** | Interface only. | Any implementation. |
| **`domain/model/`** | Immutable data, enums, sealed types. | Android, Room, Retrofit, Gson, Compose imports. |
| **`data/repository/`** | Coordinate `*RemoteSource` + DAO. Write-through cache. Error mapping. | UI concerns. Threading from the caller (always wrap in `withContext(IO)`). |
| **`data/source/`** | One concern per source. Wrap Retrofit calls; return `Domain?` for reads / `Result<Unit>` for writes (internal only). | Touching Room directly. |
| **`data/local/`** | Room schema, DAOs, migrations. | Domain logic. |
| **`data/dto/`** | Wire shapes only. | Database concerns. |
| **`data/mappers/`** | Pure conversions. | Side effects. |
| **`core/sync/`** | One queue, one worker, both features feed it. | Feature-specific business logic. |

### 5.3 Data-flow diagrams

**Read flow (e.g. `ExamPapersScreen` populating its list):**

```
ExamPapersScreen
        │ collectAsStateWithLifecycle
        ▼
ExamPapersViewModel ── viewModelScope.launch ──▶ GetExamPapersUseCase()
                                                       │ suspend
                                                       ▼
                                               ExamPaperRepository.getPapersForToday()
                                                       │ withContext(IO)
                                          ┌────────────┴────────────┐
                                          ▼                         ▼
                                  PaperDao.observeForDay()    (no remote in this path —
                                  → List<PaperEntity>          dossier is downloaded once;
                                                               refresh is its own flow)
                                          │
                                          ▼  PaperEntity.toDomain()
                                  Result.Success(List<Paper>)
```

**Write flow (e.g. tapping "Mark Attendance" on `CandidateScanResult`):**

```
CandidateScanResultScreen ── onMarkAttendance ──▶ MarkAttendanceUseCase(candidateId, paperId)
                                                          │ suspend
                                                          ▼
                                          AttendanceRepository.markPresent(…)
                                                          │ withContext(IO)
                                          AttendanceEntity(syncStatus = Pending) ───▶ AttendanceDao.upsert()
                                                          │
                                                          ▼
                                          enqueueSyncJob(entityType = Attendance, id = …)  ──▶ SyncJobDao
                                                          │
                                                          ▼
                                                Result.Success
                                                          │
WorkManager.OneTimeRequest("attendance-sync") ◀──── SyncWorker triggered by enqueue
```

**Sync flow (the `SyncWorker` loop):**

```
SyncWorker.doWork()
   │
   ▼
SyncJobDao.pendingAndFailed() → List<SyncJob>
   │ for each
   ▼
when (job.entityType) {
   Attendance       → AttendanceRepository.uploadOne(job.entityId)
   PracticalScore   → PracticalScoringRepository.uploadOne(…)
   ProjectScore     → ProjectScoringRepository.uploadOne(…)
   Remark           → RemarkRepository.uploadOne(…)
}
   │ each call returns SyncStatus (Synced | Failed)
   ▼
SyncJobDao.updateStatus(jobId, …)  &  EntityDao.updateSyncStatus(entityId, …)
```

### 5.4 Layer interaction rules (lint-enforceable)

1. `domain.*` must not import `data.*`, `androidx.room.*`, `retrofit2.*`, `com.google.gson.*`, `android.*`, `androidx.compose.*`.
2. `data.repository.*Impl` is the **only** type allowed to depend on both `data.source.*` and `data.local.*`.
3. `data.source.*RemoteSource` interfaces declare **domain** return types; concrete `Api*` impls do DTO → Domain mapping at the boundary.
4. `data.local.*Dao` may expose `Flow<…>` for reactive screens, but the **repository** owns whether to forward it. Default: collapse to `suspend` snapshots (matches verification).
5. `presentation.*ViewModel` may not import `data.*`. It depends only on `domain.usecase.*`.

Enforcement: a `lint.xml` `DependsOnPackages` rule in CI (Detekt `forbidden-comment`–style rule) and an Android Studio scope-based inspection. Section 11 expands.

---

## 6. Domain Design

This section enumerates every domain class to create. All types are immutable `data class` / `enum class` / `sealed`. No Android / Room / Retrofit imports.

### 6.1 Shared types (`core/domain/model/`)

```kotlin
enum class PaperKind {
    Theory,           // legacy: <no code>
    Practical,        // legacy: "PE"
    Project           // legacy: "PA"
    ;
    companion object {
        fun fromWireCode(code: String?): PaperKind = when (code?.uppercase()) {
            "PE" -> Practical
            "PA" -> Project
            else -> Theory
        }
    }
}

enum class SyncStatus { Pending, Synced, Failed }

data class Candidate(
    val id: String,                       // server-stable; equal to examNumber where unique
    val examNumber: String,               // a.k.a. indexingNumber on the assessment side
    val fullName: String,
    val photoUrl: String? = null,         // canonical wire-friendly URL
    val photoDataUri: String? = null      // optional Base64 fallback (legacy v1 inline)
)

data class SyncBatchResult(
    val attempted: Int,
    val succeeded: Int,
    val failed: Int,
    val errors: List<String> = emptyList()
)
```

### 6.2 Exam domain (`feature/exam/domain/model/`)

```kotlin
data class OfficerSession(
    val officerId: String,
    val centerId: String,
    val dayIso: String                    // e.g. "2026-06-12"
)

data class Center(
    val id: String,
    val name: String,
    val code: String,
    val location: String,
    val heroImageUrl: String? = null
)

data class Paper(
    val id: String,
    val centerId: String,
    val title: String,
    val subtitle: String,
    val paperKind: PaperKind,
    val startAt: Long,                    // epoch millis
    val endAt: Long,
    val hall: String,
    val totalCandidates: Int
)

enum class AttendanceStatus { SignedIn, SignedOut, Flagged }

data class Attendance(
    val paperId: String,
    val candidateId: String,
    val status: AttendanceStatus,
    val markedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null
)

data class Remark(
    val id: String,                       // client-generated UUID until server assigns
    val candidateId: String,
    val paperId: String? = null,
    val body: String,
    val severity: RemarkSeverity = RemarkSeverity.Info,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null
)

enum class RemarkSeverity { Info, Warning, Critical }

// Aggregated reads for the screens
data class ExamDashboardSummary(
    val session: OfficerSession,
    val center: Center,
    val attendanceCard: ExamTaskSummary,
    val practicalCard: ExamTaskSummary
)

data class ExamTaskSummary(
    val statusLabel: String,              // "Active Session", "Pending Grading"
    val countLabel: String                // "142 students"
)

data class ExamPaperDetail(
    val paper: Paper,
    val center: Center,
    val totalCandidates: Int,
    val checkedInCount: Int,
    val lastSyncAt: Long?,
    val pendingSyncCount: Int
)

data class ExamCandidateRow(
    val candidate: Candidate,
    val attendance: Attendance?,          // null = not yet marked
    val remarkCount: Int
)

data class ExamStatistics(
    val recordsDownloaded: Int,
    val attendanceCaptured: Int,
    val syncedCount: Int,
    val cachedCount: Int,
    val pendingCount: Int,
    val failedCount: Int,
    val lastUpdatedAt: Long?
)
```

**Result types:**

```kotlin
sealed interface ExamDashboardResult {
    data object Loading : ExamDashboardResult
    data class Success(val summary: ExamDashboardSummary) : ExamDashboardResult
    data class Error(val message: String) : ExamDashboardResult
}

sealed interface ExamPaperDetailResult {
    data class Success(val detail: ExamPaperDetail) : ExamPaperDetailResult
    data object NotFound : ExamPaperDetailResult
    data class Error(val message: String) : ExamPaperDetailResult
}

sealed interface MarkAttendanceResult {
    data class Success(val attendance: Attendance) : MarkAttendanceResult
    data class Error(val message: String) : MarkAttendanceResult
}

sealed interface AddRemarkResult {
    data class Success(val remark: Remark) : AddRemarkResult
    data class Error(val message: String) : AddRemarkResult
}

sealed interface DownloadDossierResult {
    data class Success(val papersCount: Int, val candidatesCount: Int) : DownloadDossierResult
    data class Error(val message: String) : DownloadDossierResult
}
```

### 6.3 Exam repository contracts (`feature/exam/domain/repository/`)

```kotlin
interface ExamPaperRepository {
    suspend fun getDashboardSummary(): ExamDashboardResult
    suspend fun getPapersForToday(): List<Paper>
    suspend fun getPaperDetail(paperId: String): ExamPaperDetailResult
}

interface ExamCandidateRepository {
    suspend fun getCandidatesForPaper(
        paperId: String,
        filter: AttendanceFilter = AttendanceFilter.All,
        query: String = ""
    ): List<ExamCandidateRow>
    suspend fun getCandidateByExamNumber(examNumber: String): Candidate?
}

enum class AttendanceFilter { All, SignedIn, SignedOut, Flagged }

interface AttendanceRepository {
    suspend fun markAttendance(
        paperId: String, candidateId: String, status: AttendanceStatus
    ): MarkAttendanceResult
}

interface RemarkRepository {
    suspend fun addRemark(
        candidateId: String, paperId: String?, body: String, severity: RemarkSeverity
    ): AddRemarkResult
    suspend fun getRemarksForCandidate(candidateId: String): List<Remark>
}

interface ExamStatisticsRepository {
    suspend fun getStatistics(): ExamStatistics
    suspend fun clearLocalCache(): SaveResult        // wipes Paper/Candidate/Attendance/Remark
}

interface ExamSyncRepository {
    suspend fun syncPending(): SyncBatchResult
    suspend fun downloadDossier(): DownloadDossierResult
}
```

### 6.4 Exam use cases (`feature/exam/domain/usecase/`)

One file per use case. Template:

```kotlin
class GetExamPapersUseCase @Inject constructor(
    private val repo: ExamPaperRepository
) {
    suspend operator fun invoke(): List<Paper> = repo.getPapersForToday()
}

class MarkAttendanceUseCase @Inject constructor(
    private val repo: AttendanceRepository
) {
    suspend operator fun invoke(
        paperId: String, candidateId: String, status: AttendanceStatus
    ): MarkAttendanceResult {
        if (paperId.isBlank() || candidateId.isBlank()) {
            return MarkAttendanceResult.Error("Paper and candidate are required.")
        }
        return repo.markAttendance(paperId.trim(), candidateId.trim(), status)
    }
}
```

Full list (Exam):

- `GetExamDashboardUseCase` (returns `ExamDashboardResult`)
- `GetExamPapersUseCase` (returns `List<Paper>`)
- `GetExamPaperDetailUseCase` (returns `ExamPaperDetailResult`)
- `GetExamCandidatesUseCase` (returns `List<ExamCandidateRow>`, takes filter + query)
- `SearchExamCandidatesUseCase` (thin wrapper around `GetExamCandidatesUseCase` with a non-empty query — could be folded into the above)
- `LookupCandidateByExamNumberUseCase` (returns `Candidate?` — used by `CandidateScanResultViewModel`)
- `MarkAttendanceUseCase`
- `AddRemarkUseCase`
- `GetRemarksForCandidateUseCase`
- `GetExamStatisticsUseCase`
- `ClearExamCacheUseCase`
- `DownloadExamDossierUseCase`
- `SyncExamRecordsUseCase` (returns `SyncBatchResult`)

### 6.5 Assessment domain (`feature/assessment/domain/model/`)

```kotlin
data class AssessmentSchedule(
    val id: String,                       // "PE-2024"
    val title: String,
    val date: Long,                       // epoch millis
    val paperKind: PaperKind,             // Practical or Project
    val centerId: String,
    val syncStatus: SyncStatus            // derived from "has any pending writes?"
)

data class AssessmentPaper(
    val scheduleId: String,
    val title: String,
    val statusLabel: String,
    val facility: Facility,
    val hall: Hall,
    val heroImageUrl: String? = null
)

data class Facility(val name: String, val address: String)
data class Hall(val name: String, val address: String)

data class PracticalSection(
    val id: String,                       // "A", "B", "C"
    val scheduleId: String,
    val title: String,
    val subtitle: String,
    val ordering: Int
)

data class SectionQuestion(
    val id: String,
    val sectionId: String,
    val number: Int,
    val prompt: String,
    val imageUrl: String? = null,
    val maxScore: Int
)

data class PracticalScore(
    val scheduleId: String,
    val candidateId: String,
    val questionId: String,
    val score: Int,
    val scoredAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null
)

data class ProjectScore(
    val scheduleId: String,
    val candidateId: String,
    val score: Double,
    val maxScore: Int,
    val scoredAt: Long,
    val syncStatus: SyncStatus = SyncStatus.Pending,
    val syncError: String? = null
)

data class AssessmentCandidateRow(
    val candidate: Candidate,
    val aggregateScore: Int,              // sum across all questions; 0 if none scored
    val level: ScoreLevel,                // Normal vs Low (threshold lives in the use case)
    val scoredQuestions: Int,             // for "Synced/Unsynced" derivation
    val totalQuestions: Int,
    val syncStatus: SyncStatus
)

enum class ScoreLevel { Normal, Low }

data class PracticalSectionSummary(
    val section: PracticalSection,
    val status: PracticalSectionStatus,
    val scoredCount: Int,
    val totalCount: Int,
    val lastUpdatedAt: Long?
)

enum class PracticalSectionStatus { NotStarted, Incomplete, Complete }
```

**Result types** mirror exam: `AssessmentScheduleResult`, `AssessmentPaperDetailResult`, `SaveScoreResult`, `DownloadAssessmentPackageResult`.

### 6.6 Assessment repository contracts

```kotlin
interface AssessmentScheduleRepository {
    suspend fun getSchedules(): List<AssessmentSchedule>
    suspend fun getPaperDetail(scheduleId: String): AssessmentPaperDetailResult
    suspend fun downloadPackage(scheduleId: String): DownloadAssessmentPackageResult
    suspend fun clearCache(scheduleId: String? = null): SaveResult
}

interface AssessmentCandidateRepository {
    suspend fun getCandidates(scheduleId: String, query: String = ""): List<AssessmentCandidateRow>
    suspend fun getCandidate(scheduleId: String, candidateId: String): Candidate?
}

interface PracticalScoringRepository {
    suspend fun getSections(scheduleId: String, candidateId: String): List<PracticalSectionSummary>
    suspend fun getQuestions(scheduleId: String, candidateId: String, sectionId: String): List<Pair<SectionQuestion, PracticalScore?>>
    suspend fun recordScore(score: PracticalScore): SaveScoreResult
    suspend fun commitSection(scheduleId: String, candidateId: String, sectionId: String): SaveScoreResult
}

interface ProjectScoringRepository {
    suspend fun getProjectScore(scheduleId: String, candidateId: String): ProjectScore?
    suspend fun recordProjectScore(score: ProjectScore): SaveScoreResult
}

interface AssessmentSyncRepository {
    suspend fun syncPending(): SyncBatchResult
}
```

### 6.7 Assessment use cases

- `GetExaminationSchedulesUseCase`
- `DownloadAssessmentPackageUseCase`
- `ClearAssessmentCacheUseCase`
- `GetAssessmentPaperDetailUseCase`
- `GetAssessmentCandidatesUseCase`
- `LookupAssessmentCandidateUseCase`
- `GetPracticalSectionsUseCase`
- `GetPracticalQuestionsUseCase`
- `RecordPracticalScoreUseCase` (debounced from stepper)
- `CommitPracticalSectionUseCase` (Save Scores FAB)
- `RecordProjectScoreUseCase` (with input validation: `0.0 ≤ score ≤ maxScore`, ≤ 2 decimal places)
- `SyncAssessmentScoresUseCase` (returns `SyncBatchResult`)

---

## 7. Data Design

### 7.1 DTOs

Verification's envelope shape is the template:

```kotlin
data class ExamDossierEnvelopeDto(
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExamDossierDataDto? = null
)

data class ExamDossierDataDto(
    @SerializedName("center") val center: CenterDto? = null,
    @SerializedName("papers") val papers: List<PaperDto> = emptyList(),
    @SerializedName("candidates") val candidates: List<CandidateDto> = emptyList(),
    @SerializedName("paper_candidates") val assignments: List<PaperCandidateAssignmentDto> = emptyList()
)

data class AttendanceSyncRequestDto(
    @SerializedName("paper_id") val paperId: String,
    @SerializedName("candidate_id") val candidateId: String,
    @SerializedName("status") val status: String,                  // "signed_in" | "signed_out" | "flagged"
    @SerializedName("marked_at") val markedAt: Long
)

data class AttendanceSyncResponseDto(
    @SerializedName("server_id") val serverId: String? = null,
    @SerializedName("accepted") val accepted: Boolean = false
)
```

Symmetric DTO families exist for assessment (`AssessmentPackageEnvelopeDto`, `PracticalScoreSyncRequestDto`, `ProjectScoreSyncRequestDto`). All snake_case fields are mapped via `@SerializedName`; Kotlin properties stay camelCase. **No Room entities on the wire.**

### 7.2 Local Room entities

Single-column natural-key PK where possible (matches `LicenseRecordEntity.registrationNumber`).

```kotlin
@Entity(tableName = "centers")
data class CenterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val location: String,
    val heroImageUrl: String? = null
)

@Entity(tableName = "papers", foreignKeys = [/* center */])
data class PaperEntity(
    @PrimaryKey val id: String,
    val centerId: String,
    val title: String,
    val subtitle: String,
    val paperKind: String,                 // PaperKind.name
    val startAt: Long,
    val endAt: Long,
    val hall: String,
    val totalCandidates: Int
)

@Entity(tableName = "candidates")
data class CandidateEntity(
    @PrimaryKey val id: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String? = null
)

@Entity(
    tableName = "paper_candidate_assignments",
    primaryKeys = ["paperId", "candidateId"]
)
data class PaperCandidateAssignmentEntity(
    val paperId: String,
    val candidateId: String
)

@Entity(
    tableName = "attendance",
    primaryKeys = ["paperId", "candidateId"]
)
data class AttendanceEntity(
    val paperId: String,
    val candidateId: String,
    val status: String,                    // AttendanceStatus.name
    val markedAt: Long,
    val syncStatus: String,                // SyncStatus.name
    val syncError: String? = null,
    val lastSyncAttemptAt: Long? = null
)

@Entity(tableName = "remarks")
data class RemarkEntity(
    @PrimaryKey val id: String,            // UUID until server assigns
    val candidateId: String,
    val paperId: String? = null,
    val body: String,
    val severity: String,
    val createdAt: Long,
    val syncStatus: String,
    val syncError: String? = null,
    val lastSyncAttemptAt: Long? = null
)
```

Assessment entities follow the same conventions: `assessment_schedules`, `practical_sections`, `section_questions`, `practical_scores` (PK `[scheduleId, candidateId, questionId]`), `project_scores` (PK `[scheduleId, candidateId]`), `assessment_candidates` (joined to schedules via an assignment table).

**Enum storage:** as `name` String (matches verification's `syncStatus`); no `@TypeConverter`. Bridge functions live in `*Mappers.kt`.

**Indexes:** `CREATE INDEX idx_attendance_sync ON attendance(syncStatus)` for the per-status counters in `ExamStatistics`; similar on `practical_scores(syncStatus)` and `project_scores(syncStatus)`. `paper_candidate_assignments(paperId)` for the candidates-per-paper lookup.

### 7.3 DAOs

Suspend everywhere; `Flow<List<…>>` for screens that need live reactivity (the assessment scoring screen's per-question step counts, in particular).

```kotlin
@Dao
interface PaperDao {
    @Query("SELECT * FROM papers WHERE centerId = :centerId ORDER BY startAt ASC")
    suspend fun getForCenter(centerId: String): List<PaperEntity>

    @Query("SELECT * FROM papers WHERE id = :paperId")
    suspend fun getById(paperId: String): PaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(papers: List<PaperEntity>)

    @Query("DELETE FROM papers")
    suspend fun clear()
}

@Dao
interface AttendanceDao {
    @Query("""
        SELECT c.id AS candidateId, c.examNumber, c.fullName, c.photoUrl,
               a.status, a.markedAt, a.syncStatus,
               (SELECT COUNT(*) FROM remarks WHERE candidateId = c.id) AS remarkCount
        FROM candidates c
        INNER JOIN paper_candidate_assignments pca ON pca.candidateId = c.id
        LEFT JOIN attendance a ON a.candidateId = c.id AND a.paperId = pca.paperId
        WHERE pca.paperId = :paperId
        ORDER BY c.fullName
    """)
    suspend fun rowsForPaper(paperId: String): List<ExamCandidateRowProjection>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AttendanceEntity)

    @Query("UPDATE attendance SET syncStatus = :status, syncError = :error, lastSyncAttemptAt = :at WHERE paperId = :paperId AND candidateId = :candidateId")
    suspend fun updateSyncMetadata(paperId: String, candidateId: String, status: String, error: String?, at: Long)

    @Query("SELECT * FROM attendance WHERE syncStatus IN ('Pending', 'Failed')")
    suspend fun pendingAndFailed(): List<AttendanceEntity>

    @Query("SELECT COUNT(*) FROM attendance WHERE syncStatus = :status")
    suspend fun countBySyncStatus(status: String): Int
}
```

DAO projections (like `ExamCandidateRowProjection`) live next to the DAO in `data/local/` and map to domain types via mappers. This matches verification's `LicenseRecordDao` projection style.

### 7.4 Mappers

Top-level extension functions, grouped by topic (matches `ScanMappers.kt` / `VerifiedMappers.kt`):

```kotlin
// AttendanceMappers.kt

internal fun AttendanceEntity.toDomain(): Attendance = Attendance(
    paperId = paperId,
    candidateId = candidateId,
    status = AttendanceStatus.valueOf(status),
    markedAt = markedAt,
    syncStatus = SyncStatus.valueOf(syncStatus),
    syncError = syncError
)

internal fun Attendance.toEntity(): AttendanceEntity = AttendanceEntity(
    paperId = paperId,
    candidateId = candidateId,
    status = status.name,
    markedAt = markedAt,
    syncStatus = syncStatus.name,
    syncError = syncError,
    lastSyncAttemptAt = System.currentTimeMillis().takeIf { syncStatus != SyncStatus.Pending }
)

internal fun Attendance.toSyncRequestDto(): AttendanceSyncRequestDto = AttendanceSyncRequestDto(
    paperId = paperId,
    candidateId = candidateId,
    status = when (status) {
        AttendanceStatus.SignedIn  -> "signed_in"
        AttendanceStatus.SignedOut -> "signed_out"
        AttendanceStatus.Flagged   -> "flagged"
    },
    markedAt = markedAt
)
```

### 7.5 Remote sources

Read-side: `Api*/Fake*/Composite*` triple (only worth it for endpoints we're waiting on the backend to ship):

```kotlin
interface ExamDossierRemoteSource {
    suspend fun downloadDossier(): ExamDossierData?     // returns a domain aggregate
}

class ApiExamDossierRemoteSource @Inject constructor(
    private val api: ExamDossierApiService
) : ExamDossierRemoteSource {
    override suspend fun downloadDossier(): ExamDossierData? = runCatching {
        val response = api.fetchDossier()
        if (!response.isSuccessful) return@runCatching null
        response.body()?.data?.toDomain()
    }.getOrNull()
}

class FakeExamDossierRemoteSource @Inject constructor() : ExamDossierRemoteSource {
    override suspend fun downloadDossier(): ExamDossierData? {
        kotlinx.coroutines.delay(400)
        return SampleExamDossier.golden                  // hardcoded realistic shape
    }
}

class CompositeExamDossierRemoteSource(
    private val primary: ExamDossierRemoteSource,
    private val fallback: ExamDossierRemoteSource
) : ExamDossierRemoteSource {
    override suspend fun downloadDossier() = primary.downloadDossier() ?: fallback.downloadDossier()
}
```

Write-side: a `fun interface` per upload concern (`AttendanceSyncRemoteSource`, `PracticalScoreSyncRemoteSource`, `ProjectScoreSyncRemoteSource`). Each returns `Result<String>` (server-assigned ID) internally; the repository translates to `SyncStatus`.

### 7.6 Repository implementations

Template (matches `LicenseRepositoryImpl.getLicenseRecord(...)` / `SyncRepositoryImpl.syncEach(...)`):

```kotlin
class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler
) : AttendanceRepository {

    override suspend fun markAttendance(
        paperId: String, candidateId: String, status: AttendanceStatus
    ): MarkAttendanceResult = withContext(Dispatchers.IO) {
        val attendance = Attendance(
            paperId = paperId, candidateId = candidateId,
            status = status, markedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.Pending
        )
        try {
            attendanceDao.upsert(attendance.toEntity())
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.Attendance.name,
                    entityKey = "$paperId/$candidateId",
                    enqueuedAt = attendance.markedAt
                )
            )
            workScheduler.scheduleSyncWork()
            MarkAttendanceResult.Success(attendance)
        } catch (t: Throwable) {
            MarkAttendanceResult.Error(t.message ?: "Unable to save attendance.")
        }
    }
}
```

`ExamSyncRepositoryImpl.syncPending()` iterates `attendanceDao.pendingAndFailed() + remarkDao.pendingAndFailed()` (or pulls from `syncJobDao`), invokes the relevant remote source per row, persists metadata, and accumulates a `SyncBatchResult`. **Per-row failures do not abort the batch.** Same shape as `SyncRepositoryImpl.syncEach(…)` in verification.

### 7.7 Database wiring

```kotlin
@Database(
    entities = [
        CenterEntity::class, PaperEntity::class,
        CandidateEntity::class, PaperCandidateAssignmentEntity::class,
        AttendanceEntity::class, RemarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ExamDatabase : RoomDatabase() {
    abstract fun centerDao(): CenterDao
    abstract fun paperDao(): PaperDao
    abstract fun candidateDao(): CandidateDao
    abstract fun assignmentDao(): PaperCandidateAssignmentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun remarkDao(): RemarkDao

    companion object { /* MIGRATION_n_n+1 objects added as schema evolves */ }
}
```

`AssessmentDatabase` mirrors the pattern. Each gets its own Hilt module providing the singleton DB with the encrypted `SupportOpenHelperFactory` from `EncryptionModule`.

### 7.8 The shared sync queue (`core/sync/`)

```kotlin
@Entity(tableName = "sync_jobs", indices = [Index("entityType"), Index("status")])
data class SyncJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,                 // SyncEntityType.name
    val entityKey: String,                  // composite key encoded as string
    val enqueuedAt: Long,
    val status: String = SyncStatus.Pending.name,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null
)

enum class SyncEntityType { Attendance, Remark, PracticalScore, ProjectScore }

class SyncWorker @AssistedInject constructor(
    @Assisted ctx: Context, @Assisted params: WorkerParameters,
    private val attendance: AttendanceRepository,
    private val remarks: RemarkRepository,
    private val practical: PracticalScoringRepository,
    private val project: ProjectScoringRepository,
    private val syncJobDao: SyncJobDao
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val jobs = syncJobDao.pendingAndFailed(limit = 50)
        // dispatch by type; on per-row failure, update job status and continue
        // return Result.success() / retry() based on whether anything remains pending
    }
}
```

WorkManager constraints: `NetworkType.CONNECTED`. Back-off: `BackoffPolicy.EXPONENTIAL` 30s/5min cap. ExpeditedWorkRequest is *not* used (the writes are background; the screen FABs poll status via `*StatisticsRepository`).

---

## 8. Presentation Integration

The presentation layer already exists. Integration is purely "replace `placeholder()` with use case calls."

### 8.1 Per-screen wiring

| Screen | ViewModel hook |
|---|---|
| `ExamDashboardScreen` | `init { _state.update { Loading }; viewModelScope.launch { _state.value = getExamDashboardUseCase().toUiState() } }` |
| `ExamPapersScreen` | `getExamPapersUseCase()` on init + on refresh; map each `Paper.paperKind` to `iconKind`. |
| `ExamPaperScreen` | `getExamPaperDetailUseCase(paperId)`; `paperId` from `SavedStateHandle` once route is parameterised (currently `Routes.ExamPaper` takes no args — extend to `Routes.ExamPaper(paperId: String)`). |
| `ExamCandidatesScreen` | Reactive filter+query inside the VM (the UseCase takes both as parameters); pagination via Room `LIMIT`/`OFFSET` once cohort > 100. |
| `CandidateScanResultViewModel` | `lookupCandidateByExamNumberUseCase(scannedPayload)` resolves either to `Found` (full state) or triggers nav to `candidate_not_found` design. |
| `ExamStatisticsScreen` | `getExamStatisticsUseCase()` polled on resume; `onSyncNow → syncExamRecordsUseCase()` driven through a sealed `SyncOpState`. |
| `ExaminationSchedulesScreen` | `getExaminationSchedulesUseCase()`. Per-row `SyncStatus` derives from "any pending writes for this schedule". |
| `AssessmentPaperDetailScreen` | `getAssessmentPaperDetailUseCase(scheduleId)`. |
| `AssessmentCandidatesScreen` | `getAssessmentCandidatesUseCase(scheduleId, query)`; reactive query filtering. Aggregate score / `level` derived in the SQL. |
| `AssessmentPracticalSectionsScreen` | `getPracticalSectionsUseCase(scheduleId, candidateId)` + a `Flow<…>` on per-section question counts so saving a score in the scoring screen flips status without a manual refresh on the hub. |
| `AssessmentPracticalScoringScreen` | `getPracticalQuestionsUseCase(scheduleId, candidateId, sectionId)`; each `onIncrement`/`onDecrement` calls `recordPracticalScoreUseCase(...)` (debounced 250 ms). "Save Scores" calls `commitPracticalSectionUseCase(...)` which marks the section's pending writes as commit-ready. |
| `AssessmentProjectAssessmentScreen` | `recordProjectScoreUseCase(...)`. Score-text validation lives in the VM; the use case does the final numeric range/precision check. |

### 8.2 UI state shapes — change inventory

The existing `*UiState` files already encode the right shapes — they just need the placeholder defaults replaced with real values. Two structural additions:

1. **`SyncOpState`** sub-state on `ExamStatisticsUiState`, `AssessmentPaperDetailUiState`, etc., mirroring `SaveVerificationState` in `VerificationFormViewModel`:

   ```kotlin
   sealed interface SyncOpState {
       data object Idle : SyncOpState
       data object Running : SyncOpState
       data class Success(val batch: SyncBatchResult) : SyncOpState
       data class Error(val message: String) : SyncOpState
   }
   ```

2. **`consume*()` helpers** on each VM whose state has terminal arms — matches `consumeSaveState()` / `consumeSubmitState()` in verification.

### 8.3 Event models (one-shot signals)

Following verification's pattern (no `Channel`/`SharedFlow`), one-shot events are encoded as terminal `*UiState` arms consumed via `LaunchedEffect`:

```kotlin
LaunchedEffect(state.saveState) {
    if (state.saveState is SaveScoreState.Success) {
        viewModel.consumeSaveState()
        onSaveScores()              // nav back to hub
    }
}
```

Only events are: "score saved" (→ pop), "section committed" (→ pop), "project score saved" (→ pop), "attendance marked" (→ pop twice — already implemented in the scan-result screen), "dossier downloaded" (→ refresh dashboard), "sync completed" (→ snackbar with `SyncBatchResult` summary).

---

## 9. Dependency Injection Plan

One module per logical sub-area; bindings `@Singleton`; `@InstallIn(SingletonComponent::class)`.

```
core/sync/di/SyncModule.kt
    @Provides SyncJobDao (Room DAO from a shared DB? — see decision below)
    @Provides SyncWorkScheduler  (wraps WorkManager.enqueueUniqueWork)
    @Binds    SyncWorkerEntryPoint  (Hilt assisted factory bound to WorkerFactory)

feature/exam/data/di/
    ExamDatabaseModule
        @Provides @Singleton ExamDatabase (Room.databaseBuilder + supportFactory + migrations)
        @Provides            CenterDao   = db.centerDao()
        @Provides            PaperDao    = db.paperDao()
        @Provides            CandidateDao
        @Provides            PaperCandidateAssignmentDao
        @Provides            AttendanceDao
        @Provides            RemarkDao

    ExamApiModule
        @Provides ExamDossierApiService = retrofit.create(...)
        @Provides AttendanceApiService
        @Provides ExamSyncApiService

    AttendanceModule
        @Binds AttendanceRepository           ← AttendanceRepositoryImpl
        @Binds ExamPaperRepository            ← ExamPaperRepositoryImpl
        @Binds ExamCandidateRepository        ← ExamCandidateRepositoryImpl
        @Binds ExamStatisticsRepository       ← ExamStatisticsRepositoryImpl

    RemarkModule
        @Binds RemarkRepository               ← RemarkRepositoryImpl

    ExamSyncModule
        @Binds ExamSyncRepository             ← ExamSyncRepositoryImpl
        @Binds ExamDossierRemoteSource (named)← CompositeExamDossierRemoteSource
        @Provides @Singleton CompositeExamDossierRemoteSource(
            primary: ApiExamDossierRemoteSource, fallback: FakeExamDossierRemoteSource
        )
        @Binds AttendanceSyncRemoteSource     ← ApiAttendanceSyncRemoteSource

feature/assessment/data/di/
    AssessmentDatabaseModule        // mirrors ExamDatabaseModule
    AssessmentApiModule
    PracticalScoringModule          @Binds PracticalScoringRepository / Section / Question repos
    ProjectScoringModule            @Binds ProjectScoringRepository
    AssessmentSyncModule            @Binds AssessmentSyncRepository + remote sources
```

**Use-case classes** are NOT bound in any module — they have `@Inject constructor`, Hilt discovers them via constructor injection. Same for mappers (which are top-level functions; no DI needed).

**Shared `SyncJobDao` decision.** Two options:

- **A. Single shared `core.sync.SyncDatabase`** (small, only the `sync_jobs` table). Cleanest separation; both features depend only on the queue, not on each other.
- **B. One `sync_jobs` table per feature DB**, with two `SyncJobDao` instances and the worker fanning out per type.

**Recommendation: A.** A 1-table dedicated DB is cheap and lets the `SyncWorker` live in `core/sync/` without depending on either feature module.

**Hilt qualifiers.** None needed beyond the existing `@DatabaseKeyPrefs`. The two `*RemoteSource` interfaces with `Composite` bindings are kept disambiguated by being feature-scoped (each is `feature.exam.data.source.ExamDossierRemoteSource`, not the same FQN).

---

## 10. Testing Strategy

The legacy code had zero documented tests. The new layout enables three tiers cheaply.

### 10.1 Unit tests

| Target | Strategy | Library |
|---|---|---|
| Use cases | Pure JVM; mock the repository interface. Verify: input validation rejects, success bubbles, failure bubbles, mapping is correct. | JUnit 4, MockK, Turbine (for any Flow returns). |
| Mappers (`*Mappers.kt`) | Pure JVM. Round-trip: `Domain.toEntity().toDomain() == Domain` and `DataDto.toDomain()` for golden DTOs. | JUnit 4 + Kotest assertions. |
| `Result` translation in repositories | Fake `*Dao` (in-memory `MutableMap`) + fake `*RemoteSource`. Cover: cache-hit, cache-miss-then-fetch-then-cache, network error → `Result.Error`, unknown error → `Result.Error(t.message)`. | JUnit 4 + MockK + a custom in-memory DAO double in `test/`. |
| Sealed `*Result` matching | Compile-time exhaustive `when` (the Kotlin compiler does the work). | — |

### 10.2 Repository / DAO integration tests

| Target | Strategy | Library |
|---|---|---|
| Each DAO | Use Room's in-memory builder + `runTest`. Verify queries, indexes hit, projections shape. Test the destructive `clearAll()` path and the `@Transaction` package-replace path explicitly. | `androidx.room:room-testing`, `Robolectric` or AndroidX test rules. |
| Migrations | One test per `Migration(n, n+1)` using `MigrationTestHelper.runMigrationsAndValidate(...)`. | `androidx.room:room-testing`. |
| Encrypted Room | A smoke test that opens an `EncryptionModule`-built DB with a known passphrase, writes a row, closes, re-opens with the same passphrase, reads. Failing read with a wrong passphrase = pass. | SQLCipher's own test scaffolding. |

### 10.3 Sync tests

| Target | Strategy |
|---|---|
| `SyncWorker` happy path | Enqueue 5 mixed jobs (3 attendance, 2 practical scores). Fake remote returns success. Assert all rows flipped to `Synced` and `sync_jobs` rows cleared. |
| `SyncWorker` partial failure | One remote call throws `IOException`. Assert: 4 rows `Synced`, 1 row `Failed` with `lastError` populated, `attemptCount = 1`, worker `Result.retry()`. |
| Exponential backoff | Schedule a failing job, assert WorkManager's backoff request matches the configured policy. (Inspect via `WorkManagerTestInitHelper`.) |

### 10.4 Presentation snapshot tests (optional but recommended)

Compose UI tests with Hilt + fake repositories (`Fake*RemoteSource` plus an in-memory DAO). Cover the Loading / Success / Error / Empty arms of every `*UiState`. These give the team confidence to refactor mappers without breaking screens.

### 10.5 CI gating

Run unit + Room integration tests on every PR. Snapshot tests gated to a nightly job (longer running). The encryption smoke test and the migration tests run on every PR — these are the highest-impact regressions to catch.

---

## 11. Migration Plan

### 11.1 Phased order

Sequence is **assessment-first**, because assessment has heavier write semantics and exposes the sync queue earlier. Once the queue is proven on assessment, exam writes plug into the same engine.

```
Phase 0 — Foundations (1 sprint)
  P0-1  Create core/sync/ (SyncJobEntity, SyncJobDao, SyncDatabase, SyncWorker, SyncWorkScheduler).
  P0-2  Add lint.xml rule forbidding `feature/*/domain/**` from importing
        `feature/*/data/**`, `androidx.room.*`, `retrofit2.*`, `com.google.gson.*`,
        `android.*`, `androidx.compose.*`.
  P0-3  Add PaperKind, Candidate, SyncStatus, SyncBatchResult to core/domain/model/.
  P0-4  Wire SyncWorker into Hilt; smoke-test with a hand-crafted job.

Phase 1 — Assessment domain + data (2 sprints)
  P1-1  Domain models (model/), repository interfaces (repository/), use cases (usecase/).
  P1-2  AssessmentDatabase + DAOs (suspend, encrypted, migrations from 1).
  P1-3  Mappers.
  P1-4  DTOs + ApiServices (defined against the backend contract from §12).
  P1-5  Remote sources: Api + Fake + Composite for the *package download*;
        Api-only for the *score sync*.
  P1-6  Repository impls. Wire SyncWorker dispatch for PracticalScore + ProjectScore.
  P1-7  Hilt modules (AssessmentDatabaseModule, AssessmentApiModule,
        PracticalScoringModule, ProjectScoringModule, AssessmentSyncModule).
  P1-8  Replace placeholder() in every Assessment*ViewModel.
  P1-9  Unit tests (use cases, mappers), integration tests (DAOs, migrations),
        sync tests. CI green.

Phase 2 — Exam domain + data (2 sprints)
  P2-1  Domain models, repository interfaces, use cases.
  P2-2  ExamDatabase + DAOs (entities reuse Candidate identity from Phase 1).
  P2-3  Mappers.
  P2-4  DTOs + ApiServices (backend contract from §12).
  P2-5  Remote sources.
  P2-6  Repository impls. Wire SyncWorker dispatch for Attendance + Remark.
  P2-7  Hilt modules.
  P2-8  Replace placeholder() in every Exam*ViewModel. Parameterise the
        Routes.ExamPaper destination to accept `paperId: String`.
  P2-9  Tests + CI.

Phase 3 — Hardening (1 sprint)
  P3-1  ExamStatistics fully wired (counts driven by DAO queries).
  P3-2  Download Warning / Loading / Sync Loading screens wired to real
        DownloadExamDossierUseCase + DownloadAssessmentPackageUseCase.
  P3-3  Snapshot tests on every screen.
  P3-4  Manual offline-then-online QA pass with real backend stubs.
  P3-5  Backend cutover (see §12).
```

### 11.2 Risk mitigation

| Risk | Mitigation |
|---|---|
| Backend endpoints not ready. | Phase 1 + Phase 2 ship with `Fake*RemoteSource` impls bound by default in the Composite. A single Gradle build property (`-PuseLiveBackend=true`) flips the composite primary to `Api*`. UI work proceeds against fakes. |
| Sync queue starves under load (large exam day). | `SyncWorker` paginates jobs (`limit = 50`), uses constraints + exponential backoff, exposes pending counts to the UI so the user can manually re-trigger. |
| Score precision drift (`Double` rounding). | `score: Double` stored as REAL; UI formats `String.format("%.2f", …)` consistently; use cases reject scores with > 2 decimal places. |
| Schema migrations corrupt data. | Each `Migration(n, n+1)` covered by a `MigrationTestHelper` test; `fallbackToDestructiveMigration()` left enabled as a safety net (matches verification). |
| Cross-feature Candidate identity divergence (exam thinks `examNumber`, assessment thinks `indexingNumber`). | One `Candidate` type in `core/domain/model/`; both features import it. Add an explicit invariant test: `examNumber == indexingNumber` for any candidate served by both DAOs. |
| Encrypted Room key loss after factory reset / data-clear. | Treated as "user must re-login + re-download dossier"; the `DatabaseMigrationGuard` deletes the orphaned DB on next launch. |

### 11.3 Validation checkpoints

| Gate | Pass criteria |
|---|---|
| After P0 | `SyncWorker` runs a 5-job batch end-to-end against a fake repository; flips statuses correctly. |
| After P1 | Every Assessment screen renders real data (against fakes); offline-then-foreground produces a `SyncBatchResult` with the right counts; lint passes. |
| After P2 | Every Exam screen renders real data; `ExamStatistics` reflects accurate cached/synced/pending counters; lint passes. |
| After P3 | Manual QA scenarios pass: (a) full-offline grading of 30 candidates across all 3 sections + project, then sync; (b) attendance marking for 100 candidates with intermittent connectivity; (c) cache-clear-then-redownload preserves no pending writes (with a UX prompt). |
| Backend cutover | Flip the Composite primary to `Api*`; the `Fake*` source stays in the build for dev. |

---

## 12. Action Checklist

### Critical (blocks shipping the rebuild)

- [ ] **C1. Backend contract.** Get the backend team to sign off on the endpoint list — exam dossier, attendance sync, assessment package, practical-score sync, project-score sync, schedule list. The current API docs cover none of these. Without contracts, mappers and DTOs cannot be finalised.
- [ ] **C2. Auth model choice.** Decide whether exam/assessment endpoints use practitioner Sanctum or adhoc Sanctum. The current code's `OkHttpClient` does not attach a bearer interceptor by default; that has to be wired before any protected exam/assessment endpoint will work.
- [ ] **C3. Envelope convention.** Backend confirms `{status, message, data}` is the standard for all new endpoints (today's four documented endpoints disagree). Without this, every DTO has to be re-shaped after the first response is captured.
- [ ] **C4. Encrypted Room foundations carried over.** Each new feature DB uses the shared `SupportOpenHelperFactory` from `EncryptionModule` and registers its file name with `DatabaseMigrationGuard.legacyDbNames` once it ships.
- [ ] **C5. `core/sync/` engine.** Build first; both features depend on it. Without it, no offline writes work.
- [ ] **C6. Lint rule for layer boundaries.** Without enforcement, the legacy "domain imports data" smell is one PR review away from regressing.

### High

- [ ] **H1. `feature/assessment/domain/` complete.** Models, repository interfaces, use cases per §6.5–6.7.
- [ ] **H2. `feature/assessment/data/` complete.** DTOs, entities, DAOs (with `@Transaction` package-replace + migrations), mappers, remote sources, repository impls per §7.
- [ ] **H3. Hilt modules for assessment.** Per §9.
- [ ] **H4. Replace `placeholder()` in every `Assessment*ViewModel`.** Verify navigation flows still work; no presentation refactor.
- [ ] **H5. `feature/exam/domain/` complete.** Per §6.2–6.4.
- [ ] **H6. `feature/exam/data/` complete.** Per §7. Reuse the shared `Candidate` from `core/domain/model/`.
- [ ] **H7. Hilt modules for exam.** Per §9.
- [ ] **H8. Replace `placeholder()` in every `Exam*ViewModel`.**
- [ ] **H9. Parameterise `Routes.ExamPaper`** with `paperId: String` so the detail screen knows which paper it's showing. Mirror change for `Routes.ExamCandidates` if needed.
- [ ] **H10. Wire WorkManager.** `Application.onCreate` registers a periodic / one-time sync request with the `SyncWorker` Hilt entry point; `*RepositoryImpl` calls `SyncWorkScheduler.scheduleSyncWork()` after every queued write.

### Medium

- [ ] **M1. Fake remote sources** for every read endpoint that doesn't yet exist server-side, with realistic golden data covering Loading / Success / Empty / Error shapes.
- [ ] **M2. Unit tests** for all use cases (validation paths + happy paths).
- [ ] **M3. Mapper round-trip tests.**
- [ ] **M4. Room integration tests** per DAO, including the `@Transaction` package-replace and the migrations.
- [ ] **M5. Sync tests** for `SyncWorker` happy + partial-failure paths.
- [ ] **M6. `ExamStatistics` and `AssessmentPaperDetail` sync pills** sourced from real DAO counts; verify they update after the worker runs.
- [ ] **M7. Snapshot tests** for each `*UiState` arm.
- [ ] **M8. `ScoreLevel` threshold parameterised** in the use case (constructor-injected; default 50). Avoid the legacy's hardcoded `score > 0` "isScored" bug.

### Low

- [ ] **L1. Download Warning / Loading / Sync Loading screens** wired to real `DownloadExamDossierUseCase` / `SyncExamRecordsUseCase` (today they have only design files).
- [ ] **L2. Cross-feature `Candidate` invariant test** (`examNumber == indexingNumber` for any candidate served by both DAOs).
- [ ] **L3. Remove `fallbackToDestructiveMigration()`** from both feature DBs once two stable releases ship without it; replace with explicit migrations only.
- [ ] **L4. Photo handling**: ensure `core/network/ImageUrlNormalization` is invoked on every candidate `photoUrl` field at the mapper boundary so Compose `AsyncImage` gets a clean `data:` or `https:` URI.
- [ ] **L5. Drop `Bitmap` legacy idea** entirely — no domain-layer image decoding.
- [ ] **L6. Documentation**: update `README.md` to describe the new feature shapes and link to this plan.

---

## Appendix A — Reference file index

Verification (the template):

- `feature/verification/VerificationFeature.kt` — feature-root marker
- `feature/verification/data/local/VerificationDatabase.kt` — `@Database` shape + migration discipline
- `feature/verification/data/local/LicenseRecordDao.kt`, `VerifiedLicenseDao.kt` — DAO conventions
- `feature/verification/data/mappers/ScanMappers.kt`, `VerifiedMappers.kt`, `SyncPayloadMappers.kt` — mapper grouping
- `feature/verification/data/repository/LicenseRepositoryImpl.kt`, `SyncRepositoryImpl.kt` — repository conventions
- `feature/verification/data/source/CompositeLicenseRecordRemoteSource.kt` — Api/Fake/Composite pattern
- `feature/verification/data/di/LicenseDataModule.kt` — encrypted Room wiring
- `feature/verification/domain/model/LicenseRecordResult.kt`, `SaveVerifiedLicenseResult.kt`, `SyncBatchResult.kt` — result-type shapes
- `feature/verification/presentation/RecordDetailViewModel.kt` — cleanest sealed-state ViewModel
- `feature/verification/presentation/VerificationFormViewModel.kt` — composed-substate form template

Core (shared infrastructure to extend, not duplicate):

- `core/persistence/encryption/EncryptionModule.kt` — `SupportOpenHelperFactory` for all encrypted DBs
- `core/persistence/encryption/DatabaseMigrationGuard.kt` — legacy DB cleanup; add new DB names here
- `core/network/ImageUrlNormalization.kt` — photo URI normaliser used in every mapper
- `core/navigation/Routes.kt`, `AppNavHost.kt` — already wired for the exam/assessment routes

Legacy (study but don't copy verbatim):

- `docs/old/data-domain-modules.md` — full v1 reference

Presentation (already shipped placeholders — do not refactor):

- `feature/exam/presentation/` (all files)
- `feature/assessment/presentation/` (all files)

---

*This plan supersedes ad-hoc decisions in earlier reviews. Deviations require an architecture note in the PR description referencing the section number being departed from.*
