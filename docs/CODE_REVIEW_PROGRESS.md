# CHPRBN Mobile — Code-Review Progress Tracker

**Companion to:** [`CODE_REVIEW.md`](./CODE_REVIEW.md)
**Last updated:** 2026-05-09 (PR A + PR B + SQLCipher + 16 KB compliance landed; C1 BASE_URL buildConfigField landed)
**Branch:** `main`

This document tracks what has been done, what is pending, and what was deliberately skipped against the recommendations in `CODE_REVIEW.md`. It also records decisions the user made about scope and ordering so a future session can pick up cold.

---

## 1. Status at a Glance

| Phase | Item | Severity | Status |
|---|---|---|---|
| 1 | R8 minification + ProGuard rules | **Critical** | 🟡 Static build green (`:app:assembleRelease` produces a 22.4 MB shrunk APK + `mapping.txt`). Runtime smoke test (login → scan → manual entry → sync → verify) on a real device is still pending — Gson reflection breakage typically only surfaces at runtime. |
| 1 | Network security config + cert pinning | **High** | 🟡 Config landed (cleartext disabled, base-config + domain-config wired). SPKI pin-set still pending — needs leaf + backup fingerprints from ops. |
| 1 | Backup hardening (`data_extraction_rules.xml`) | High | 🟢 Done — `auth.db`, `scan.db`, `auth_prefs.xml` excluded from cloud-backup + device-transfer; `backup_rules.xml` mirrors for pre-Android-12. |
| 1 | `signingConfigs` for release | Medium | ⬜ Not started |
| 1 | Comprehensive tests | **Critical** | 🟢 Substantial progress (113 tests across 24 files; see §3) |
| 2 | Verification DB encryption (SQLCipher / column-level) | High | 🟡 SQLCipher wired (`auth.db` + `scan.db` open with `SupportFactory(passphrase)`); 256-bit key generated/persisted via EncryptedSharedPreferences. Static build green. Same runtime smoke-test caveat as R8 — needs device verification. |
| 2 | `buildConfigField BASE_URL` per build type | Medium | 🟢 Done — declared in `app/build.gradle.kts` for both `debug` and `release`. `AuthDataModule` now reads `BuildConfig.BASE_URL`. Both build types currently point at prod; debug has a TODO marker for when a staging API exists. |
| 2 | Detekt + ktlint + CI gate | Medium | ⬜ Not started — CI deferred per user (§2) |
| 2 | Strings to `strings.xml` + accessibility pass | Medium | ⬜ Not started |
| 2 | OkHttp `Authenticator` for token refresh | Medium | ✅ **Resolved by decision** — API has no refresh endpoint (§2). No code change needed; document the no-refresh model. |
| 3 | Gson → kotlinx.serialization | Low | ⬜ Not started |
| 3 | Type-safe Compose navigation | Low | ⬜ Not started |
| 3 | Backfill `data` + `domain` layers for `dashboard`, `exam`, `scan` | Medium | ⬜ Not started |
| 3 | Multi-module split | Low | ✅ **Resolved by decision** — single-module retained per user (§2). |
| 3 | Dependabot / Renovate | Low | ⬜ Not started |

Legend: 🟢 done · 🟡 partial · ⬜ pending · ⬛ blocked · ✅ resolved by decision

---

## 2. Decisions Made (treat as durable until the user says otherwise)

These came from the explicit Q&A at the start of implementation. They constrain what counts as "in scope" for the recommendations:

1. **Order of attack: tests first.** Build a regression net before any release-hardening or DB-encryption work. The user picked this over starting with R8 because the test foundation makes subsequent refactors safer.
2. **No refresh tokens.** The CHPRBN mobile API only issues bearer tokens; users are logged out on token expiry. Recommendation N2 (`Authenticator` for refresh) is **resolved by API constraint**, not pending. The current `AuthRepositoryImpl` behavior (clear token on failure) is intentional, not a gap.
3. **No CI yet.** Detekt / ktlint / coverage gates have nowhere to run. They are deferred until CI (GitHub Actions or similar) is wired up. Quality gates are local-only for now.
4. **Assertion library: JUnit assertions.** No migration to AssertJ / Truth / Kotest. New tests match the existing `LoginViewModelTest` style.
5. **Coverage tooling: Kover (attempted).** Plugin is wired in `gradle/libs.versions.toml` and applied in `app/build.gradle.kts`, but Kover 0.9.1 cannot resolve Android variants on AGP 9.2.1. The custom-variant block is commented out with a reactivation note — see §5 (Open Follow-Ups).
6. **No multi-module split.** Audit recommendation in `CODE_REVIEW.md` §3 weakness #2 and §14 Phase 3 step 14 is permanently de-scoped. Single-module project is the chosen structure for this codebase. Do not surface multi-module as a next move; if build performance or cross-feature coupling becomes a concern, address it without a `:core:*` / `:feature:*` split.

---

## 3. Test Coverage Built So Far

**Total: 113 tests across 24 files. All green at last run.**

The test foundation now covers every ViewModel, use case, and repository in the auth, profile, and (most of the) verification + exam features that has non-trivial logic.

| Feature | File | Tests | Notes |
|---|---|---|---|
| auth | `AuthRepositoryImplTest` | 5 | login success, offline-no-cache error, profile-fail clears token, status=false clears token, offline-cache success |
| auth | `LoginUseCaseTest` | 2 | success / error delegation |
| auth | `LoginViewModelTest` | 3 | empty creds, success, failure |
| auth | `SplashViewModelTest` | 4 | Verification destination, Login when no user, Login when seed token, null before delay |
| profile | `ProfileRepositoryImplTest` | 8 | DAO null/token-missing/legacy-seed/blank-token rejection, valid mapping, whitespace trim, update mapping, logout order |
| profile | `GetUserProfileUseCaseTest` | 2 | user / null delegation |
| profile | `UpdateUserProfileUseCaseTest` | 1 | passes user through |
| profile | `LogoutUseCaseTest` | 1 | delegates to repo |
| profile | `ProfileViewModelTest` | 5 | init→Success / Error("Not logged in") / Error from throw, logout→LoggedOut, logout→Error |
| verification | `LicenseRepositoryImplTest` (was `ScanRepositoryImplTest`) | 13 | empty/cached/remote miss/IOException/Throwable on `getLicenseRecord`; refresh empty/null/IOException/RuntimeException paths |
| verification | `VerifiedRepositoryImplTest` | 4 | save with Pending sync status, DAO throw → Error, list mapping, empty list |
| verification | `GetLicenseRecordUseCaseTest` | 4 | empty/whitespace/trim/NotFound delegation |
| verification | `SaveVerifiedLicenseUseCaseTest` | 4 | empty/whitespace remark, trim + delegation, default `verifiedAt` bracket |
| verification | `ManualEntryViewModelTest` | 2 | initial state, mutation via Turbine |
| verification | `RecordDetailViewModelTest` | 6 | Loading→Success/NotFound/Error, silent refresh updates / leaves alone, retry |
| verification | `VerificationFormViewModelTest` | 9 | Gson nav-arg decode (valid/missing/malformed), remark selection, save Idle→Saving→Success/Error, missing-record guard, `consumeSaveState`, options sanity |
| verification | `SyncViewModelTest` | 13 | init load + load failure, syncAll happy path + summaries, syncAll/retryFailed failure surfaces error (regression), `consumeError`, derived counts, `lastSuccessfulSyncMillis` ignores failed records, `formatRelativeLastSync(null)`, refresh re-invokes loader |
| exam | `ExamPapersViewModelTest` | 3 | placeholder identity, all three statuses, only Active has primary action |
| exam | `ExamPaperViewModelTest` | 3 | placeholder identity, hero URL constant, progress fraction in [0..1] |
| exam | `ExamCandidatesViewModelTest` | 3 | placeholder identity, filter labels, candidate avatar + ID prefix |
| exam | `CandidateScanResultViewModelTest` | 6 | nav arg decode, URL decoding, whitespace trim, missing/blank fallback, identity-verified headline + 98% match |
| persistence | `DatabaseKeyProviderTest` | 6 | first-call key generation + 32-byte length + lowercase-hex persistence + commit; second-call returns persisted key without re-write; hex round-trip identity; independent providers produce different keys; corrupt-length and non-hex inputs rejected |
| persistence | `DatabaseMigrationGuardTest` | 5 | first run deletes `auth.db` + `scan.db` and sets the `sqlcipher_migration_v1_done` marker; subsequent runs no-op; marker still set when `deleteDatabase` returns false (file didn't exist); custom legacy-DB list honored; constants pinned so a refactor can't accidentally retrigger the wipe on existing installs |
| placeholder | `ExampleUnitTest` | 1 | IDE-generated placeholder; harmless, not removed |

### Test infrastructure built

- `core/utils/MainDispatcherRule.kt` — `Dispatchers.setMain` rule. Originally had `private val testDispatcher`; **changed to `val` so callers can pass `runTest(rule.testDispatcher) { ... }`** to share the scheduler with `Dispatchers.Main`. This was needed for `SplashViewModelTest`'s `delay(2500L)` time-advancement. Other tests don't need it but the change is harmless.
- Pattern for static-mocking `android.net.Uri.decode`: see `CandidateScanResultViewModelTest` and `VerificationFormViewModelTest`. Avoids pulling in Robolectric. Use `mockkStatic(Uri::class)` in `@Before` and `unmockkStatic(Uri::class)` in `@After`.
- Pattern for mocking concrete final classes (e.g. `AuthTokenStore`, `Gson`): plain `mockk(relaxed = true)` works because MockK's JVM agent handles final classes.

### What's still missing from the test foundation

Documented as candidate next sprints:

- **Sprint 6 — remaining verification ViewModels.** `SyncHistoryViewModel`, `ReportIrregularityViewModel`, `VerifiedListViewModel`, `VerificationViewModel`, plus dashboards (`DashboardViewModel`, `ExamDashboardViewModel`, `ExamStatisticsViewModel`).
- **Room migration tests (now harder).** With SQLCipher in the path, `MigrationTestHelper` needs an encrypted helper override (or a debug-only unencrypted variant of the Room builder). Plan to address as part of Sprint 8.
- **Sprint 7 — `MockWebServer` integration tests** for the Retrofit/JSON wire. Currently repository tests mock the `*ApiService` interface, so JSON-shape regressions slip through.
- **Sprint 8 — Room `MigrationTestHelper`** for `VerificationDatabase` migrations 2→3, 3→4, 4→5. Ungated migrations are a real risk because `fallbackToDestructiveMigration()` is enabled.
- **Sprint 9 — Compose UI tests** in `androidTest`. Login happy path, manual entry, scan result.
- **Coverage measurement** — blocked by Kover/AGP-9 compat (§5).

---

## 4. Production Bugs Discovered & Fixed Along the Way

The original audit didn't catch these — testing did.

| # | Where | Fix |
|---|---|---|
| **B1** | `ProfileRepositoryImpl.getUserProfile()` would surface a fake-authenticated `User` if the auth prefs held a blank token or the legacy seed placeholder. The `SplashViewModel` checked `SessionTokenPolicy` itself, but `ProfileViewModel` did not — so a stale seed token could show a logged-in profile. | Apply `SessionTokenPolicy.isValidForAuthenticatedApi(...)` at the data boundary. Trim once at the repo. New tests pin the legacy-seed and blank-token rejection paths. |
| **B2** | `ProfileRepositoryImpl.kt` had `import kotlinx.coroutines.withContext` listed twice. Cosmetic. | Removed duplicate import. |
| **B3** | `LicenseRepositoryImpl.kt` file contained `class ScanRepositoryImpl`; the file/class names had been half-renamed but the rename never finished. Same for `LicenseRepository.kt` (interface `ScanRepository`), `LicenseDataModule.kt` (module `ScanModule`), `VerificationDatabase.kt` (class `ScanDatabase`), `LicenseApiService.kt` (interface `ScanApiService`). | **Finished the rename across 9 source files + 1 test rename.** Class names now match file names: `LicenseRepository`/`Impl`, `LicenseDataModule`, `VerificationDatabase`, `LicenseApiService`. The `feature.scan` package (QR camera UI) was intentionally left alone — it's about scanning, not license records. The underlying SQLite DB file is still `scan.db` to avoid forcing a migration on existing installs. |
| **B4** | `LicenseRepositoryImpl.refreshLicenseRecord()` propagated network errors instead of returning `null` per its KDoc spec. Silent background refresh could crash the calling ViewModel on `IOException`. | Wrapped the remote call in `runCatching { ... }.getOrNull()`. Two new tests pin the swallow behavior (`IOException` + generic `RuntimeException`). |
| **B5** | Duplicate `GetUserProfileUseCase` existed at `feature.verification.domain.usecase.GetUserProfileUseCase` and `feature.profile.domain.usecase.GetUserProfileUseCase`. The verification-feature one had no callers. | Deleted the dead duplicate. Verified by FQN grep. **Note:** `VerificationRepository.getUserProfile()` is still a semantic duplicate of the profile path and is missing the `SessionTokenPolicy` guard — flagged in §5 for a follow-up. |
| **B6** | `SyncViewModel.reloadFromDb()`'s `onSuccess` branch unconditionally cleared the `error` field. Because both `syncAll()` and `retryFailed()` call `reloadFromDb()` after their own `onFailure`, the user-facing error message from a failed sync was overwritten and the user never saw it. | Removed `error = null` from `reloadFromDb`'s `onSuccess`. Errors now survive until `consumeError()` clears them or a fresh user-driven action explicitly resets them at start. The `KNOWN_BUG` test was converted to a regression test. |

---

## 5. Open Follow-Ups Found During the Work

Items discovered during testing/refactoring that aren't in the original audit. These do not block any phase, but capture them so they don't get lost.

1. **Kover ↔ AGP 9.x compatibility.** Plugin is wired up but `kover { currentProject { createVariant("appDebug") { add("debug") } } }` cannot resolve the `debug` Android variant under AGP 9.2.1. See the commented block in `app/build.gradle.kts`. Reactivate when Kover ships AGP-9 support, or pin AGP back to 8.x for coverage.
2. **`VerificationRepository.getUserProfile()` duplication.** Used only by `GetVerificationDataUseCase`. It re-implements `ProfileRepository.getUserProfile()` against the same `UserDao`/`AuthTokenStore`, but **does not apply `SessionTokenPolicy`** — same gap that B1 fixed in profile. Recommended fix: change `GetVerificationDataUseCase` to depend on the profile feature's `GetUserProfileUseCase`, then delete `VerificationRepository.getUserProfile()`. Small follow-up, security-adjacent.
3. **SQLite file is `scan.db`** despite the renamed `VerificationDatabase` class. Renaming the file requires either copying data to a new file on first launch or accepting destructive migration. Deferred — class name now matches `VerificationDatabase.kt` semantically, and the file name is opaque to users.
4. **Six other verification-feature ViewModels untested.** `SyncHistoryViewModel`, `ReportIrregularityViewModel`, `VerifiedListViewModel`, `VerificationViewModel`, plus the dashboards. None blocking.
5. **No integration tests via `MockWebServer`.** Repository tests mock the `*ApiService` Retrofit interface, so JSON-wire bugs slip past. Worth a dedicated sprint.
6. **No Room migration tests.** `VerificationDatabase` has `MIGRATION_2_3`, `_3_4`, `_4_5` but no `MigrationTestHelper` coverage. Combined with `fallbackToDestructiveMigration()`, a broken migration could silently wipe verification records on upgrade.
7. **Verification ViewModels' `withContext(Dispatchers.IO)` is hardcoded** in repository implementations. Tests work but it makes time-control awkward and prevents using a fully-virtual scheduler. Future cleanup: inject a `CoroutineDispatcher` (with `@IODispatcher` qualifier) per repo.
8. **APK size after SQLCipher** (~32 MB unsigned). The native `.so` payload across 4 ABIs is the dominant cost. If delivery size matters, switch to AAB packaging (Play splits per-device) or add `splits { abi { … } }` to ship per-ABI APKs.

---

## 6. Files Changed Since the Audit

| File | What |
|---|---|
| `gradle/libs.versions.toml` | Added Kover plugin entry (`kover = "0.9.1"`) |
| `app/build.gradle.kts` | Applied Kover plugin; left commented-out variant config and a note |
| `app/src/main/.../feature/profile/data/repository/ProfileRepositoryImpl.kt` | Apply `SessionTokenPolicy` at boundary; trim token; removed duplicate import |
| `app/src/main/.../feature/verification/data/repository/LicenseRepositoryImpl.kt` | `runCatching` around remote refresh; renamed class `ScanRepositoryImpl` → `LicenseRepositoryImpl` |
| `app/src/main/.../feature/verification/domain/repository/LicenseRepository.kt` | Renamed interface `ScanRepository` → `LicenseRepository` |
| `app/src/main/.../feature/verification/domain/usecase/GetLicenseRecordUseCase.kt` | Updated repository type |
| `app/src/main/.../feature/verification/domain/usecase/RefreshLicenseRecordUseCase.kt` | Updated repository type |
| `app/src/main/.../feature/verification/data/api/LicenseApiService.kt` | Renamed interface `ScanApiService` → `LicenseApiService` |
| `app/src/main/.../feature/verification/data/source/ApiLicenseRecordRemoteSource.kt` | Updated import + field name |
| `app/src/main/.../feature/verification/data/local/VerificationDatabase.kt` | Renamed class `ScanDatabase` → `VerificationDatabase` |
| `app/src/main/.../feature/verification/data/di/LicenseDataModule.kt` | Renamed module + bindings + provider fns; updated all imports |
| `app/src/main/.../feature/verification/data/di/VerifiedModule.kt` | Updated `ScanDatabase` → `VerificationDatabase` import + parameter |
| `app/src/main/.../feature/verification/presentation/SyncViewModel.kt` | Don't clear `error` in `reloadFromDb`'s `onSuccess` |
| `app/src/main/.../feature/verification/domain/usecase/GetUserProfileUseCase.kt` | **Deleted** (dead duplicate) |
| `app/src/test/.../core/utils/MainDispatcherRule.kt` | Made `testDispatcher` `val` so callers can share scheduler |
| `app/src/test/.../feature/auth/data/repository/AuthRepositoryImplTest.kt` | Added 3 tests |
| `app/src/test/.../feature/auth/presentation/splash/SplashViewModelTest.kt` | New file (4 tests) |
| `app/src/test/.../feature/profile/...` | Five new test files (8 + 2 + 1 + 1 + 5 tests) |
| `app/src/test/.../feature/verification/data/repository/LicenseRepositoryImplTest.kt` | New file (13 tests; was `ScanRepositoryImplTest.kt`) |
| `app/src/test/.../feature/verification/data/repository/VerifiedRepositoryImplTest.kt` | New file (4 tests) |
| `app/src/test/.../feature/verification/domain/usecase/GetLicenseRecordUseCaseTest.kt` | New file (4 tests) |
| `app/src/test/.../feature/verification/domain/usecase/SaveVerifiedLicenseUseCaseTest.kt` | New file (4 tests) |
| `app/src/test/.../feature/verification/presentation/ManualEntryViewModelTest.kt` | New file (2 tests) |
| `app/src/test/.../feature/verification/presentation/RecordDetailViewModelTest.kt` | New file (6 tests) |
| `app/src/test/.../feature/verification/presentation/VerificationFormViewModelTest.kt` | New file (9 tests) |
| `app/src/test/.../feature/verification/presentation/SyncViewModelTest.kt` | New file (13 tests, including the regression for B6) |
| `app/src/test/.../feature/exam/presentation/ExamPapersViewModelTest.kt` | New file (3 tests) |
| `app/src/test/.../feature/exam/presentation/ExamPaperViewModelTest.kt` | New file (3 tests) |
| `app/src/test/.../feature/exam/presentation/ExamCandidatesViewModelTest.kt` | New file (3 tests) |
| `app/src/test/.../feature/exam/presentation/CandidateScanResultViewModelTest.kt` | New file (6 tests) |
| `app/src/test/.../feature/profile/domain/usecase/LogoutUseCaseTest.kt` | New file (1 test) |
| `app/src/main/res/xml/data_extraction_rules.xml` | Replaced stub with explicit excludes for `auth.db`, `scan.db`, `auth_prefs.xml` in both `cloud-backup` and `device-transfer` |
| `app/src/main/res/xml/backup_rules.xml` | Replaced stub with same excludes (legacy pre-Android-12 path) |
| `app/src/main/res/xml/network_security_config.xml` | **New** — base-config + domain-config for `app.chprbn.gov.ng`; cleartext disabled; system trust anchors; `<pin-set>` left commented with a TODO until ops supplies SPKI fingerprints |
| `app/src/main/AndroidManifest.xml` | Added `android:networkSecurityConfig="@xml/network_security_config"` on `<application>` |
| `app/build.gradle.kts` | Release build type: `isMinifyEnabled = true` + `isShrinkResources = true` |
| `app/proguard-rules.pro` | Authored R8 keep rules: app DTOs, Room entities/DAOs, Gson-serialized domain models, `@SerializedName` field preservation, anonymous `TypeToken<…>` subclasses, retrofit `@HTTP` method preservation, source-file/line-number retention. Replaces the empty boilerplate stub. |
| `gradle/libs.versions.toml` | Added `sqlcipher = "4.5.4"` (`net.zetetic:android-database-sqlcipher`) and `sqlite = "2.4.0"` (`androidx.sqlite:sqlite-ktx`) version refs + library entries |
| `app/build.gradle.kts` | `implementation(libs.sqlcipher)` + `implementation(libs.androidx.sqlite.ktx)` |
| `app/src/main/.../core/persistence/encryption/DatabaseKeyProvider.kt` | **New** — generates a 256-bit random passphrase via `SecureRandom` on first launch; persists hex-encoded into an injected `SharedPreferences` (provided as EncryptedSharedPreferences in production); `@Synchronized` so concurrent first-call DB opens cannot race; uses `commit()` not `apply()` so the key is durable before a SQLCipher DB ever uses it |
| `app/src/main/.../core/persistence/encryption/DatabaseKeyPrefs.kt` | **New** — Hilt qualifier annotation for the EncryptedSharedPreferences instance that holds the DB passphrase |
| `app/src/main/.../core/persistence/encryption/DatabaseMigrationGuard.kt` | **New** — one-shot deletion of pre-SQLCipher unencrypted `auth.db` + `scan.db`; gated by `sqlcipher_migration_v1_done` boolean in a separate `db_migration_guard` SharedPreferences file; `deleteDatabase()` removes the `-journal`/`-wal`/`-shm` sidecars too |
| `app/src/main/.../core/persistence/encryption/EncryptionModule.kt` | **New** — Hilt module that provides the `@DatabaseKeyPrefs` EncryptedSharedPreferences (file `db_keys`, MasterKey AES256-GCM) and the singleton `SupportFactory` constructed with `clearPassphrase = false` (otherwise SQLCipher zeroes the byte array after first use, corrupting in-process reopens) |
| `app/src/main/.../feature/auth/data/di/AuthDataModule.kt` | `provideAuthDatabase` now injects `SupportFactory` and calls `.openHelperFactory(supportFactory)` on the Room builder |
| `app/src/main/.../feature/verification/data/di/LicenseDataModule.kt` | Same `.openHelperFactory(supportFactory)` wiring for `VerificationDatabase` |
| `app/src/main/.../ChprbnApplication.kt` | `onCreate()`: `SQLiteDatabase.loadLibs(this)` (SQLCipher 4.x JNI init) → `DatabaseMigrationGuard(...).migrateIfNeeded()` runs before any DAO is touched |
| `app/src/main/res/xml/data_extraction_rules.xml` | Added excludes for `db_keys.xml` and `db_migration_guard.xml` (consistent with the existing `auth_prefs.xml` exclusion rationale) |
| `app/src/main/res/xml/backup_rules.xml` | Same additions for the legacy pre-Android-12 path |
| `app/src/test/.../core/persistence/encryption/DatabaseKeyProviderTest.kt` | **New** (6 tests) |
| `app/src/test/.../core/persistence/encryption/DatabaseMigrationGuardTest.kt` | **New** (5 tests) |
| `gradle/libs.versions.toml` (16 KB compliance) | Bumped `cameraX 1.3.4 → 1.5.3`, `mlkitBarcode 17.2.0 → 17.3.0`, `sqlcipher 4.5.4 → 4.15.0`, swapped artifact `net.zetetic:android-database-sqlcipher` → `net.zetetic:sqlcipher-android` |
| `app/src/main/.../ChprbnApplication.kt` | `SQLiteDatabase.loadLibs(this)` → `System.loadLibrary("sqlcipher")` (new artifact's loader) |
| `app/src/main/.../core/persistence/encryption/EncryptionModule.kt` | Import `net.sqlcipher.database.SupportFactory` → `net.zetetic.database.sqlcipher.SupportOpenHelperFactory` (class renamed in new artifact, same constructor signature) |
| `app/src/main/.../feature/auth/data/di/AuthDataModule.kt` | Same `SupportFactory` → `SupportOpenHelperFactory` rename |
| `app/src/main/.../feature/verification/data/di/LicenseDataModule.kt` | Same |
| `app/build.gradle.kts` (C1) | Added `buildConfigField "String" "BASE_URL"` to both `debug` and `release` build types. Debug points at prod for now with a TODO to retarget at a staging API once one exists. |
| `app/src/main/.../feature/auth/data/di/AuthDataModule.kt` (C1) | Removed the `private const val BASE_URL` literal; Retrofit builder now uses `BuildConfig.BASE_URL`. |
| `README.md` (C1) | Replaced the "edit `BASE_URL` in `AuthDataModule`" instructions with a `buildConfigField`-based env-switching recipe. |

---

## 7. Suggested Next Move When Resuming

Pick one based on appetite — the test foundation is broad enough now that any of these can land safely:

1. **Phase 1 release-hardening sprint** — PR A + PR B done; PR C + runtime smoke test remain.
   - ~~PR A: backup hardening + `network_security_config.xml` (cleartext disabled).~~ ✅ Landed. SPKI pin-set still TODO — once ops supplies leaf + backup fingerprints, uncomment the `<pin-set>` block in `network_security_config.xml` and optionally mirror it as an OkHttp `CertificatePinner` for defense-in-depth.
   - ~~PR B: enable R8 + author `proguard-rules.pro`.~~ ✅ Landed at the build level. **Runtime smoke test still required**: install the unsigned release APK on a device and exercise login → fetch profile → scan QR → manual license lookup → save verified record → sync → submit irregularity report. Watch logcat for `JsonSyntaxException`, `IllegalStateException` from Gson reflection, missing-class errors from Hilt, or Room `RuntimeException: cannot find adapter` — those are the typical R8 fallout patterns and will require a follow-up PR to extend the keep rules. Until smoke-tested, treat S1/S2/S3 as 🟡 not 🟢.
   - PR C: `signingConfigs` skeleton with credentials from `~/.gradle/gradle.properties`. Needs a real keystore from the user. Required to package a signed release APK; the unsigned APK from PR B is sufficient for the smoke test via `adb install -t`.
   - ~~`buildConfigField BASE_URL` per build type (audit C1).~~ ✅ Landed.
2. ~~**Verification-DB encryption** (Phase 2).~~ ✅ Landed. SQLCipher 4.15.0 (`net.zetetic:sqlcipher-android`) wired into both `auth.db` and `scan.db` via `Room.databaseBuilder(...).openHelperFactory(SupportOpenHelperFactory(passphrase, null, false))`. Passphrase is a 256-bit `SecureRandom` value persisted in a dedicated EncryptedSharedPreferences file (`db_keys`). Pre-SQLCipher unencrypted DB files are wiped one-shot by `DatabaseMigrationGuard` from `Application.onCreate()`. Trade-off: existing installs lose cached license records + unsynced verified records on first launch after upgrade — acceptable per the existing `fallbackToDestructiveMigration()` posture, and the auth token (in EncryptedSharedPreferences) survives so users do not have to re-authenticate. Runtime smoke test still pending (same caveat as PR B). The new SQLCipher artifact, along with bumped CameraX (1.5.3) and ML Kit barcode-scanning (17.3.0), also resolves the Google Play **16 KB page-size requirement** (effective 2025-11-01 for Android 15+ targets) — see §9.
3. **Sprint 6 — finish verification ViewModel coverage.** `SyncHistoryViewModel`, `ReportIrregularityViewModel`, `VerifiedListViewModel`, `VerificationViewModel`. Same pattern as Sprint 5; ~15–20 tests.
4. **Address open follow-up #2** (consolidate `VerificationRepository.getUserProfile()` with the profile path). Small security-adjacent cleanup.
5. **`MockWebServer` + Room migration tests** (Sprints 7 + 8). Higher infrastructure cost, catches regressions the unit tests can't.

If picking up cold, start by **re-reading `CODE_REVIEW.md` §13 (severity matrix)** then this file's §1 status table to choose direction. The user's preferences in §2 still apply unless they say otherwise.

---

## 8. How to Verify the Current State

```powershell
# Run full unit test suite
./gradlew :app:testDebugUnitTest

# Smoke-build the debug APK (proves Hilt DI graph still resolves)
./gradlew :app:assembleDebug

# Coverage (currently produces "No sources" — see §5 follow-up #1)
./gradlew :app:koverHtmlReport
```

Last verified: 2026-05-09 — 113 tests green; `:app:assembleDebug` and `:app:assembleRelease` both succeed with SQLCipher + bumped CameraX/ML Kit/SQLCipher-android + `BuildConfig.BASE_URL` wired in. Release APK at `app/build/outputs/apk/release/app-release-unsigned.apk` is ~32 MB with `mapping.txt` at `app/build/outputs/mapping/release/`. **All five arm64-v8a `.so` files now show ELF LOAD `Align 0x4000` (16 KB) and `zipalign -c -P 16 -v 4` reports "Verification successful"** — see §9. Runtime smoke test of the release APK is still outstanding — see §7 PR B / SQLCipher note.

---

## 9. Google Play 16 KB Page-Size Compliance

Google Play requires apps targeting Android 15+ that are submitted on or after **2025-11-01** to support 16 KB page sizes. This means every native `.so` shipped in the APK must have its ELF LOAD program-header alignment ≥ 16384 (`0x4000`). For libraries we don't build ourselves, this depends entirely on which versions ship 16 KB-aligned binaries.

The release APK from the SQLCipher PR initially failed the check because three transitive native libraries were 4 KB-aligned:

| Library | Comes from | Fix |
|---|---|---|
| `libsqlcipher.so` | `net.zetetic:android-database-sqlcipher:4.5.4` | Migrate to `net.zetetic:sqlcipher-android:4.15.0`. The artifact is renamed (the legacy one is no longer maintained), import paths move from `net.sqlcipher.database.*` to `net.zetetic.database.sqlcipher.*`, and **`SupportFactory` is renamed to `SupportOpenHelperFactory`** (constructor signature is unchanged: `(byte[], SQLiteDatabaseHook?, boolean)`). Loader call changes from `SQLiteDatabase.loadLibs(context)` to `System.loadLibrary("sqlcipher")`. |
| `libimage_processing_util_jni.so` | `androidx.camera:*:1.3.4` | Bump CameraX to `1.5.3` (latest stable as of Jan 2026). API-compatible. |
| `libbarhopper_v3.so` | `com.google.mlkit:barcode-scanning:17.2.0` | Bump to `17.3.0`. Drop-in. |

**Verification:** all five arm64-v8a `.so` files in the post-fix APK pass `llvm-readelf -l … \| grep LOAD \| awk '{print $NF}'` showing `0x4000`, and Google's `zipalign -c -P 16 -v 4 app-release-unsigned.apk` returns `Verification successful`. The other ABIs (`armeabi-v7a`, `x86`, `x86_64`) are not subject to the 16 KB requirement (no 16 KB-page kernels exist for those targets), so they were not audited.

**Re-verification command** (repeat after any dependency bump that introduces a new `.so`):

```powershell
# Locate llvm-readelf in the most recent installed NDK:
$NDK = (Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\ndk" | Sort-Object Name -Descending | Select-Object -First 1).FullName
$READELF = "$NDK\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-readelf.exe"

# Extract the APK and check every arm64-v8a .so:
$APK = "app\build\outputs\apk\release\app-release-unsigned.apk"
Expand-Archive $APK -DestinationPath build\apk-check -Force
Get-ChildItem build\apk-check\lib\arm64-v8a -Filter *.so | ForEach-Object {
    $align = & $READELF -l $_.FullName | Select-String "LOAD" | Select-Object -First 1
    "$($_.Name): $align"
}

# Or use Google's canonical CLI (build-tools/$VERSION/zipalign.exe):
$ZIPALIGN = (Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\build-tools" | Sort-Object Name -Descending | Select-Object -First 1).FullName + "\zipalign.exe"
& $ZIPALIGN -c -P 16 -v 4 $APK | Select-Object -Last 3
```
