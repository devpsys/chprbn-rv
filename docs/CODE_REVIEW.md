# CHPRBN Mobile — Android Code Review

**Reviewer:** Senior Android Engineer (independent audit)
**Date:** 2026-05-08
**Branch reviewed:** `main`
**Scope:** Code structure (Clean Architecture), security, and adherence to Android industry best practices.

---

## 1. Executive Summary

Overall the project is in **good shape** for an early-stage / pre-release Android app. It demonstrates a deliberate, well-layered Clean Architecture, modern tooling (Kotlin 2.2, AGP 9.2, Compose BOM, Hilt, Room, Retrofit/OkHttp, CameraX + ML Kit), and sound token-handling fundamentals via `EncryptedSharedPreferences`.

However, there are a handful of issues that must be addressed **before a production release**, the most important being:

1. **R8 / minification is disabled for `release` builds** — the production APK will ship as effectively un-obfuscated bytecode.
2. **No automated tests of substance** — only the IDE-generated `ExampleUnitTest` / `ExampleInstrumentedTest` placeholders exist.
3. **Sensitive Room data (verified practitioner records, license records) is stored in the clear** with no DB-level encryption.
4. **No `network_security_config.xml`** — there is no certificate pinning and no explicit cleartext-traffic block.
5. **Backup is enabled with permissive defaults** — `android:allowBackup="true"` and the `data_extraction_rules.xml` is the auto-generated stub with a `TODO` and no exclusions.

A summary scorecard:

| Area | Grade | Notes |
|------|-------|-------|
| Architecture & Layering | **A−** | Clean, consistent, well-disciplined per feature |
| Dependency Injection | **A** | Hilt set up correctly throughout |
| Networking | **B+** | HTTPS + bearer auth solid; no pinning, no refresh flow |
| Persistence (tokens) | **A−** | EncryptedSharedPreferences used correctly |
| Persistence (DB) | **C+** | PII in unencrypted Room; destructive fallback migration |
| Manifest & Permissions | **B** | Lean permissions; backup config left as stub |
| Build / Release Hardening | **D** | Minification off; ProGuard rules empty |
| Testing | **F** | Effectively no tests |
| Compose / UI quality | **B+** | Good lifecycle awareness, weak accessibility |
| Navigation | **B** | Functional; not type-safe routes; many `TODO` callbacks |
| Code Quality | **B** | Some commented-out code, hard-coded strings |
| Git Hygiene | **A−** | `.gitignore` is appropriate, no secrets in VCS |

---

## 2. Project at a Glance

- **Module structure:** single-module (`:app`).
- **Package root:** `ng.com.chprbn.mobile`
- **Top-level packages:**
  - `core/` → `designsystem`, `navigation`, `network`, `persistence`
  - `feature/` → `auth`, `dashboard`, `exam`, `profile`, `scan`, `verification`
- **Approx. ~162 Kotlin files**, ~16k LOC.
- **Stack:** Jetpack Compose, Hilt, Room, Retrofit + OkHttp + Gson, CameraX + ML Kit, AndroidX Security Crypto.
- **SDK levels:** `minSdk 24`, `targetSdk 36`, `compileSdk 36.1`.
- **Tooling:** Kotlin `2.2.10`, AGP `9.2.1`, Hilt `2.59.2`, Compose BOM `2024.09.00`, Retrofit `2.11.0`, OkHttp `4.12.0`, Room `2.6.1`, CameraX `1.3.4`. All versions managed via `gradle/libs.versions.toml`.

---

## 3. Architecture & Layering

### Strengths

- **Clean Architecture is honored.** Each feature module (most clearly `auth`, `verification`, `profile`) is split into:
  - `data/` (api, dto, di, local, repository, mappers, source)
  - `domain/` (model, repository interface, usecase)
  - `presentation/` (screens, viewmodels)
- **Domain is framework-independent.** Domain models such as `User`, `AuthResult`, `LicenseRecord`, `VerificationFeature` are pure Kotlin data classes / sealed classes — no Android imports.
- **Repository pattern enforced.** Domain exposes interfaces (e.g. `AuthRepository`, `ScanRepository`, `VerificationRepository`); concrete implementations live in `data/repository/` and are bound via Hilt `@Binds`.
- **Use cases exist and are used.** `LoginUseCase`, `LogoutUseCase`, `GetUserProfileUseCase`, `GetLicenseRecordUseCase`, `SaveVerifiedLicenseUseCase`, etc. follow the canonical `suspend operator fun invoke()` shape.
- **DI is set up correctly.**
  - `ChprbnApplication` is annotated `@HiltAndroidApp`.
  - `MainActivity` is `@AndroidEntryPoint`.
  - All ViewModels are `@HiltViewModel`.
  - Per-feature modules (`AuthDataModule`, `VerificationModule`, `LicenseDataModule`, `SyncModule`, `ReportModule`, `VerifiedModule`) install into `SingletonComponent`, with bindings via `@Binds` and providers for Retrofit/OkHttp/Gson/Room.
- **ViewModels follow the `MutableStateFlow<UiState>` + `asStateFlow()` + `update {}`** pattern (e.g. `LoginViewModel`, `SyncViewModel`, `ProfileViewModel`, `ExamDashboardViewModel`).
- **Composables are largely free of business logic;** they collect state via `collectAsStateWithLifecycle()` and forward events to the ViewModel.

### Weaknesses / Inconsistencies

1. **`dashboard`, `exam`, and `scan` features only have a `presentation/` folder.** They do not yet have `data/` or `domain/` layers. This is fine if they are purely UI compositions over other features' data, but it is worth confirming and documenting — otherwise the discipline applied to `auth` / `verification` will erode as the app grows.
2. **Single-module project.** Acceptable today, but as features multiply you will hit slow incremental builds and accidental cross-feature coupling. Consider splitting into `:core:network`, `:core:persistence`, `:core:designsystem`, `:feature:auth`, `:feature:verification`, etc.
3. **Gson parses domain types directly in the navigation layer** (see `AppNavHost.kt` decoding `LicenseRecord` from a URL-encoded JSON arg). This couples navigation to a concrete domain model and means schema changes in `LicenseRecord` silently break navigation. Prefer a `Parcelable`/`Serializable` UI-DTO or pass an ID and re-fetch from the repository.

### Recommendations

- Document the layering convention in a top-level `ARCHITECTURE.md` so the convention applied to `auth` / `verification` is enforced for new features.
- Backfill `domain` and `data` layers for `dashboard`, `exam`, and `scan` if they end up owning state, not just rendering it.
- Migrate to **Navigation Compose type-safe routes** (Kotlin serialization-based) and stop passing JSON blobs through nav arguments.
- Plan a multi-module split before the codebase doubles in size.

---

## 4. Networking Layer

### What's good

- **HTTPS-only base URL** (`https://app.chprbn.gov.ng/api/v1/mobile/`) configured in `core/network` / `AuthDataModule`. No cleartext URLs anywhere in the codebase.
- **Bearer token attached via `AuthorizationInterceptor`,** with a sensible bypass for the `/login` endpoint.
- **`SessionTokenPolicy.isValidForAuthenticatedApi()`** prevents the placeholder `"offline-token"` seed from being sent to the API — a nice defensive touch.
- **`HttpLoggingInterceptor` is gated on `BuildConfig.DEBUG`** so request/response bodies are not logged in release builds.
- **Sealed result types** (`AuthResult.Success/Error`, `LicenseRecordResult.Success/NotFound/Error`, etc.) propagate failures explicitly.

### Weaknesses

| # | Finding | Severity |
|---|---------|----------|
| N1 | **No certificate pinning.** No `network_security_config.xml`, no `CertificatePinner` on OkHttp. A compromised CA could MITM a healthcare credentials API. | **High** |
| N2 | **No token-refresh flow.** There is no `Authenticator` and no refresh endpoint usage — when the bearer expires the user is logged out. If the API supports refresh tokens this should be implemented; if not, document it as an API limitation. | Medium |
| N3 | **Unused `runBlocking` import** in `AuthorizationInterceptor.kt`. Cosmetic, but suggests a partial change was left in. | Low |
| N4 | **Gson is the JSON serializer.** Gson is reflection-heavy, has known nullability footguns with Kotlin, and is on Google's deprioritized list relative to `kotlinx.serialization` or Moshi. Not urgent, but worth planning a migration. | Low |
| N5 | **No global timeout configuration visible** on the OkHttp client (connect/read/write). Defaults are 10s which is usually OK, but explicit timeouts make production behavior predictable. | Low |

### Recommendations

- Add `res/xml/network_security_config.xml`:
  ```xml
  <network-security-config>
      <domain-config cleartextTrafficPermitted="false">
          <domain includeSubdomains="true">app.chprbn.gov.ng</domain>
          <pin-set expiration="2027-01-01">
              <pin digest="SHA-256">BASE64_OF_SPKI_PIN</pin>
              <pin digest="SHA-256">BASE64_OF_BACKUP_PIN</pin>
          </pin-set>
      </domain-config>
  </network-security-config>
  ```
  Reference it from `<application android:networkSecurityConfig="@xml/network_security_config" />`.
- Add an OkHttp `CertificatePinner` as defense-in-depth (works alongside the network-security-config).
- Set explicit `connectTimeout`, `readTimeout`, `writeTimeout` on the shared `OkHttpClient`.
- Replace Gson with `kotlinx.serialization` (preferred — same team as Kotlin) or Moshi over the next refactor cycle.
- Implement a refresh-token `okhttp3.Authenticator` if/when the API supports it. Until then, document the session lifecycle in code.

---

## 5. Persistence

### What's good

- **`AuthTokenStore` uses `EncryptedSharedPreferences`** with `MasterKey.KeyScheme.AES256_GCM`, `AES256_SIV` for keys and `AES256_GCM` for values. Tokens are never written in clear text.
- **Room is used correctly** with KSP, `@Database`, `@Entity`, `@Dao`, and explicit migrations `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5` in `ScanDatabase`.

### Weaknesses

| # | Finding | Severity |
|---|---------|----------|
| P1 | **Practitioner PII is stored unencrypted.** `LicenseRecordEntity` and `VerifiedLicenseEntity` (names, gender, contact info, license status) live in a plain Room SQLite DB. On a rooted device or via ADB-backup-on-debuggable builds this is recoverable. | **High** |
| P2 | **`fallbackToDestructiveMigration()` is configured on both DBs.** Any unexpected schema change wipes user data silently. For an offline verification cache that's tolerable; for the auth DB it is a UX hazard. | Medium |
| P3 | **`exportSchema = false`** on both `@Database` annotations. Schema history is therefore not under VCS, which makes future migration authoring riskier. | Low |
| P4 | **No automated migration tests.** A migration that breaks on a non-trivial dataset will not be caught until users hit it. | Medium |

### Recommendations

- **Encrypt the verification database.** Either:
  - Use `net.zetetic:android-database-sqlcipher` + Room's `SupportFactory`, with a key derived/stored via `MasterKey` + `EncryptedSharedPreferences`, **or**
  - At minimum, encrypt sensitive columns at the entity level (e.g. via a `TypeConverter` that encrypts/decrypts using a `MasterKey`-protected AES key).
- Set `exportSchema = true` and commit the generated JSON schemas under `app/schemas/` (and configure the KSP arg for the schema location).
- Replace `fallbackToDestructiveMigration()` on `AuthDatabase` with explicit migrations, or accept it explicitly with `fallbackToDestructiveMigrationOnDowngrade()` only.
- Add Room migration tests under `androidTest` using `MigrationTestHelper`.

---

## 6. Security

### Manifest

- ✅ `INTERNET`, `ACCESS_NETWORK_STATE`, `CAMERA` only — minimal and justified.
- ✅ `MainActivity` is the only exported component (`MAIN`/`LAUNCHER` only). No deep-link intent filters to abuse.
- ✅ `FileProvider` is `android:exported="false"` with scoped `file_paths.xml`.
- ⚠️ `android:allowBackup="true"` (`AndroidManifest.xml:15`).
- ⚠️ `data_extraction_rules.xml` ships as the auto-generated stub with a single `<!-- TODO: Use <include> and <exclude> ... -->` and **no exclusions**, meaning Auto Backup and device-to-device transfer can copy the encrypted prefs and Room DBs verbatim. The encryption keys (`MasterKey`) live in the device's hardware-backed keystore and **do not** travel with backups, so on a different device the encrypted prefs would be unrecoverable — but the **plain-text Room DBs would**, which exposes practitioner records.

### Build hardening

| # | Finding | Severity |
|---|---------|----------|
| S1 | **`isMinifyEnabled = false`** in the `release` build type (`app/build.gradle.kts:28`). The release APK is shipped with full Kotlin metadata, all class/method names, and dead code. This makes reverse engineering trivial and increases APK size unnecessarily. | **Critical** |
| S2 | **`shrinkResources` not set.** Implicitly `false` because R8 is off. Unused resources ship in the APK. | High |
| S3 | **`proguard-rules.pro` is the boilerplate stub** with no consumer-specific keep rules. Once R8 is enabled you will need rules for Gson/Retrofit/Room/Hilt-generated code. | High (becomes blocker once S1 is fixed) |
| S4 | **No `signingConfig` defined for `release`.** Will require manual configuration before publishing; ensure keystore credentials are loaded from `~/.gradle/gradle.properties` or environment, not the repo. | Medium |
| S5 | **No debuggable / `applicationIdSuffix` separation** between debug and release. Consider `debug { applicationIdSuffix = ".debug" }` so debug and release can coexist on a device. | Low |

### Hardcoded secrets

- ✅ Searched the codebase for `api_key`, `apikey`, `secret`, `password`, `token`, `bearer`, `BuildConfig.<KEY>` — no real secrets are committed.
- ✅ `local.properties` is `.gitignore`d and only contains the SDK path.
- ✅ `gradle.properties` contains no credentials.
- ℹ️ The `LEGACY_SEED_PLACEHOLDER = "offline-token"` literal in `SessionTokenPolicy` is a sentinel, not a real token, and is explicitly rejected before being sent.

### Logging

- ✅ Application code uses `android.util.Log` sparingly (`AuthSeedCallback`, `QrScanScreen`).
- ✅ No `println()` or `System.out` calls in production paths.
- ⚠️ `HttpLoggingInterceptor` is set to `Level.BODY` in debug, which logs `Authorization: Bearer …` headers and full response bodies — fine for debug but worth being aware of.

### Other

- ✅ No `WebView` usage anywhere.
- ✅ No reflection beyond Gson's standard `TypeToken` use.
- ✅ No dynamic code loading, no `DexClassLoader`, no `Class.forName`.
- ✅ No deep links → no intent-redirect risk.
- ⚠️ One non-null assertion (`!!`) in `RecordDetailScreen.kt` (`bitmap = imageBitmap!!`) — convert to a `let { … }` to be safe under recomposition.

### Critical security recommendations

1. **Enable R8 for release builds** (highest priority):
   ```kotlin
   buildTypes {
       release {
           isMinifyEnabled = true
           isShrinkResources = true
           proguardFiles(
               getDefaultProguardFile("proguard-android-optimize.txt"),
               "proguard-rules.pro"
           )
       }
   }
   ```
   Add keep rules in `proguard-rules.pro` for all DTOs, Room entities, Retrofit interfaces, and Hilt-generated code, e.g.:
   ```
   -keep class ng.com.chprbn.mobile.feature.**.data.dto.** { *; }
   -keep class ng.com.chprbn.mobile.feature.**.data.local.entity.** { *; }
   -keepattributes Signature, *Annotation*
   ```
   Run a release build and exercise login + scan + sync flows before shipping — Gson + R8 frequently surface misconfigured keep rules at runtime.

2. **Lock down backups.** Either set `android:allowBackup="false"` or replace the stub `data_extraction_rules.xml` with explicit exclusions:
   ```xml
   <data-extraction-rules>
       <cloud-backup>
           <exclude domain="database" path="auth.db"/>
           <exclude domain="database" path="scan.db"/>
           <exclude domain="sharedpref" path="auth_prefs.xml"/>
       </cloud-backup>
       <device-transfer>
           <exclude domain="database" path="auth.db"/>
           <exclude domain="database" path="scan.db"/>
           <exclude domain="sharedpref" path="auth_prefs.xml"/>
       </device-transfer>
   </data-extraction-rules>
   ```
   Apply the same exclusions in `backup_rules.xml` (the legacy pre-Android-12 path).

3. **Add a network security config + certificate pin** as described in §4.

4. **Encrypt the practitioner DB** as described in §5.

---

## 7. Build Configuration

### Strengths

- ✅ **Version catalog** (`gradle/libs.versions.toml`) is used consistently — no scattered `"androidx.core:core-ktx:…"` strings.
- ✅ **Modern toolchain:** Kotlin 2.2.10, AGP 9.2.1, JDK 11 source/target.
- ✅ **KSP** (not deprecated kapt) for Hilt, Room.
- ✅ **`buildFeatures { compose = true; buildConfig = true }`** explicit.

### Weaknesses

- ❌ No `release` minification (covered above).
- ❌ No `signingConfigs` block.
- ❌ No flavor / build type customization for staging vs prod (e.g. base URL switching). Today `BASE_URL` is a `private const val` in `AuthDataModule`, which means switching environments requires a code change.
- ⚠️ Compose BOM `2024.09.00` is fine but newer BOMs are available; consider scheduled dependency updates (Renovate / Dependabot).

### Recommendations

- Move `BASE_URL` to `buildConfigField` per build type (e.g. `debug` → staging API, `release` → prod API).
- Add `signingConfigs` with credentials sourced from environment variables / `~/.gradle/gradle.properties`.
- Enable Dependabot or Renovate for `gradle/libs.versions.toml`.
- Consider enabling `kotlin.code.style=official` and Detekt / ktlint with a pre-commit hook.

---

## 8. Testing

### Current state

- `app/src/test/java/.../ExampleUnitTest.kt` — IDE placeholder.
- `app/src/androidTest/java/.../ExampleInstrumentedTest.kt` — IDE placeholder.
- Test dependencies are wired (JUnit 4, MockK, Turbine, kotlinx-coroutines-test, Espresso, Compose UI test) — but they aren't being used.

### Severity: **High**

For an app that gates access to professional credentials, shipping without tests is a meaningful regression and compliance risk.

### Recommendations (in priority order)

1. **ViewModel tests** (cheap, high value):
   - `LoginViewModel` — input validation, state transitions on success/error/network failure (use Turbine + `runTest`).
   - `SyncViewModel` — progress / synced count state machine.
   - `ProfileViewModel` — Loading → Success / Error / LoggedOut transitions.
2. **Use case tests** with MockK fakes for repositories.
3. **Repository tests** with `MockWebServer` for the Retrofit-side and in-memory Room for the local side.
4. **Migration tests** (`MigrationTestHelper`) for `ScanDatabase` 2→3, 3→4, 4→5.
5. **Compose UI tests** for the login form, scan result content, and exam dashboard tabs.

Target ≥70% line coverage on `domain/` and `data/repository/` before a 1.0 release.

---

## 9. UI / Compose

### Strengths

- ✅ **`collectAsStateWithLifecycle`** is used in 36 sites (vs. 2 sites of the older `collectAsState`). This is the right default.
- ✅ **Material 3** + centralized theming under `core/designsystem` (`Color.kt`, `Theme.kt`, `Type.kt`).
- ✅ **`hiltViewModel()`** consistently used in screens.
- ✅ **Previews** exist (`DialogsPreview.kt`, `VerificationPreview.kt`).

### Weaknesses

| # | Finding | Severity |
|---|---------|----------|
| U1 | **Accessibility gaps.** Multiple `Image`/`Icon` calls have `contentDescription = null` (e.g. logo in `AppTopBar.kt`). Status badges convey state via color alone. | Medium |
| U2 | **Hard-coded UI strings** scattered across screens and ViewModels (e.g. `"Username and password are required."` in `LoginViewModel`). Blocks future localization. | Low |
| U3 | **Two remaining `collectAsState` call-sites** that should be migrated to the lifecycle-aware variant. | Low |

### Recommendations

- Add meaningful `contentDescription`s; for purely decorative images, use the explicit `Modifier.semantics { invisibleToUser() }` rather than `contentDescription = null`.
- Add a TalkBack pass to your QA checklist.
- Migrate user-facing strings to `res/values/strings.xml` and add `res/values-<lang>/strings.xml` placeholders for any locales you intend to support.
- Convert the last two `collectAsState` to `collectAsStateWithLifecycle`.

---

## 10. Navigation

### Strengths

- ✅ Single-activity Compose Navigation pattern.
- ✅ `Routes.kt` centralizes route strings with builder helpers.
- ✅ `Uri.encode` / `Uri.decode` handled at boundaries.
- ✅ JSON parsing for nav args is wrapped in `runCatching`.

### Weaknesses

- ❌ **String-based, not type-safe.** Nav-Compose now supports Kotlin-serializable route classes; this codebase pre-dates that pattern.
- ❌ **Domain models are passed through nav args as Gson JSON.** This couples navigation to domain shape and makes route URLs unwieldy.
- ⚠️ **Many `TODO` callbacks** for not-yet-implemented destinations (recovery, request access, settings, accreditation, practical assessment). These are placeholders, but they ship as silent no-ops if a user reaches them.

### Recommendations

- Migrate to **type-safe Compose navigation** with `@Serializable` route classes.
- Pass identifiers (e.g. `licenseId: String`) instead of full objects; re-fetch from the repository in the destination's ViewModel.
- Convert silent `TODO` lambdas into either: (a) hidden entry points in release builds, or (b) toasts/snackbars stating "Coming soon" so users get feedback.

---

## 11. Code Quality Smells

| Smell | Count / Examples | Severity |
|-------|------------------|----------|
| `TODO` / `FIXME` / `HACK` comments | ~28 across the project, mostly in `AppNavHost.kt` callbacks | Low (info) |
| Commented-out code blocks | ~9 (e.g. `AppNavHost.kt`, `VerificationRepositoryImpl.kt`) | Low |
| `!!` non-null assertions | 1 — `RecordDetailScreen.kt` (`bitmap = imageBitmap!!`) | Low |
| `runBlocking` actual usage | 0 (one stale import only) | None |
| `GlobalScope` usage | 0 | None |
| `println` / `System.out` | 0 | None |
| Hard-coded UI strings | Many (across ViewModels and screens) | Low |
| Unused imports | At least 1 (`AuthorizationInterceptor.kt`) | Trivial |

### Recommendations

- Add **Detekt** and **ktlint** with a baseline; wire them into a Git pre-commit hook and CI.
- Treat commented-out code as a code-review blocker — git history is the comment.
- Ban `!!` via Detekt's `UnsafeCallOnNullableType` rule.

---

## 12. Git Hygiene

- ✅ `.gitignore` excludes `/build`, `/local.properties`, `/.idea/`, `/ui-designs/`.
- ✅ No secrets committed.
- ✅ No build artifacts committed.
- ⚠️ A `reports/` directory exists at repo root — confirm it's intentionally tracked; if it's static analysis output, it should probably be ignored.
- ⚠️ `mobile_api_v1_documentation.html` lives at the repo root rather than under `docs/` — minor cleanliness issue.

---

## 13. Findings — Severity Matrix

Critical → must fix before any production release.
High → fix before public beta / external testers.
Medium → fix before 1.0.
Low → can defer; track in backlog.

| ID | Area | Finding | Severity |
|----|------|---------|----------|
| S1 | Build | `isMinifyEnabled = false` for `release` (`app/build.gradle.kts:28`) | **Critical** |
| T1 | Testing | No real unit / instrumentation tests | **Critical** |
| P1 | Persistence | Practitioner PII stored in unencrypted Room DB | **High** |
| N1 | Networking | No certificate pinning / no `network_security_config.xml` | **High** |
| S2 | Build | `shrinkResources` disabled (will be required after S1) | High |
| S3 | Build | `proguard-rules.pro` empty — no keep rules for Gson/Retrofit/Room/Hilt | High (after S1) |
| M1 | Manifest | `allowBackup="true"` + stub `data_extraction_rules.xml` exposes practitioner DBs | High |
| P2 | Persistence | `fallbackToDestructiveMigration()` on `AuthDatabase` | Medium |
| P3 | Persistence | `exportSchema = false`; no migration tests | Medium |
| N2 | Networking | No token refresh / `Authenticator` | Medium |
| S4 | Build | No `signingConfig` for release | Medium |
| U1 | UI | Accessibility — many `contentDescription = null` and color-only status | Medium |
| C1 | Build | Per-environment base URL not configurable via build types/flavors | Medium |
| A1 | Architecture | `dashboard`/`exam`/`scan` lack data + domain layers | Medium |
| A2 | Architecture | Domain models passed through nav args as JSON | Medium |
| N4 | Networking | Gson chosen over `kotlinx.serialization` / Moshi | Low |
| N5 | Networking | No explicit OkHttp timeouts | Low |
| Q1 | Code Quality | `!!` in `RecordDetailScreen.kt` | Low |
| Q2 | Code Quality | Commented-out code blocks in nav + repo impls | Low |
| Q3 | Code Quality | Hard-coded UI strings across ViewModels/screens | Low |
| Q4 | Code Quality | Unused `runBlocking` import in `AuthorizationInterceptor.kt` | Trivial |
| U2 | UI | Two remaining `collectAsState` sites should use lifecycle-aware variant | Low |
| U3 | Navigation | TODO lambda placeholders ship as silent no-ops | Low |

---

## 14. Prioritized Recommendation Plan

### Phase 1 — Pre-production blockers (1–2 weeks)

1. Enable R8 minification + resource shrinking. Add `proguard-rules.pro` keep rules for Gson DTOs, Room entities, Retrofit interfaces, Hilt-generated classes. Smoke-test a release APK end-to-end.
2. Add `network_security_config.xml` with cleartext disabled and at least pin the production API certificate (with a backup pin).
3. Replace `data_extraction_rules.xml` with explicit `<exclude>` entries for `auth.db`, `scan.db`, and `auth_prefs.xml`. Same for `backup_rules.xml`.
4. Add `signingConfigs` for release with credentials from environment / Gradle properties (never the repo).
5. Stand up automated tests for `LoginViewModel`, `LoginUseCase`, `AuthRepositoryImpl`, `ScanRepositoryImpl`, plus migration tests for `ScanDatabase`. Wire them into CI.

### Phase 2 — Hardening (2–4 weeks)

6. Encrypt the verification database via SQLCipher or column-level encryption with a `MasterKey`-protected AES key.
7. Add `buildConfigField "String", "BASE_URL", …` per build type; remove the `private const val BASE_URL` from `AuthDataModule`.
8. Add Detekt + ktlint with a CI gate; introduce a baseline on day 1 and burn it down.
9. Migrate user-facing strings to `strings.xml`; pass an accessibility (TalkBack) review.
10. Implement an OkHttp `Authenticator` for token refresh, or formally document the no-refresh model.

### Phase 3 — Cleanups & polish (4–8 weeks)

11. Migrate Gson → `kotlinx.serialization` (or Moshi) and remove Gson-based nav payloads.
12. Migrate to type-safe Compose navigation; pass IDs, not domain objects.
13. Backfill `data` + `domain` layers for `dashboard`, `exam`, `scan` if they grow stateful logic.
14. Plan a multi-module split (`:core:network`, `:core:persistence`, `:core:designsystem`, `:feature:*`).
15. Add Dependabot/Renovate for the version catalog.

---

## 15. What This Review Did Not Cover

- **Backend / API** correctness, authorization model, rate limiting, audit logging.
- **Penetration testing** (runtime instrumentation, MobSF dynamic analysis, Frida hooking, root-detection bypasses).
- **Privacy / regulatory compliance** review (NDPR, GDPR if applicable, sector-specific rules for medical practitioner data).
- **Performance profiling** (startup, frame rate, memory).
- **Crash / analytics** strategy (no Crashlytics/Sentry/PostHog wired up — consider adding one for production).

These should each be their own dedicated workstreams before 1.0.

---

## 16. Closing

This is a **well-written codebase** by the standards of an early-stage Android project: the architecture is disciplined, DI is correct, token handling is encrypted, HTTPS is mandatory, and the manifest is lean. It is closer to "ready" than most apps reviewed at this stage.

The blockers — minification, tests, DB encryption, network-security-config, and backup hardening — are well-scoped and should be addressable in one focused sprint. After Phase 1 above, this app will be in a defensible position to ship to production.
