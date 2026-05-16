# Pre-Production Externals — Backend Contract + Cert Pinning

**Purpose:** Detail the two production blockers from `docs/CODE_REVIEW_PROGRESS.md` that the mobile team **cannot resolve unilaterally** — they need backend and operations sign-off. Both are gates on the exam + assessment go-live; neither is a code-only fix.

| Item | Owner to drive | Mobile work required after sign-off |
|---|---|---|
| **2 — Backend contracts for exam/assessment** | Backend team | ~1 sprint of mapper / DTO reshaping per endpoint that diverges |
| **6 — SPKI cert pinning** | Operations / infra | ~1 hour to paste fingerprints + uncomment XML |

> The order matters. Items 2 and 6 are **independent** and can run in parallel — they touch different stakeholders.

---

## Item 2 — Backend contracts for exam + assessment (audit C1, C2, C3)

### 2.1 Status

The Android client has already implemented **eight HTTP endpoints** that the backend has **never confirmed**. The Retrofit interfaces, DTOs, mappers, and Hilt wiring are all in place — but every one of those endpoints is marked **`SPECULATIVE`** in source comments. On a live device today, the `Composite*RemoteSource` falls back silently to the in-memory `Fake*RemoteSource` whenever the live API returns `null`, so:

- Exam dashboards render fake centres + fake candidate rosters.
- Assessment schedules + papers are synthetic.
- Practical / project score uploads no-op against the network and only round-trip locally.
- The officer cannot distinguish "fake data" from "live data" in the UI.

This is acceptable for development; it is **not acceptable for production**. The "Composite tries API, falls back to Fake" pattern was a deliberate choice during development so the UI could be built ahead of the backend (plan §11.2 risk row 1) — its job is over once the backend ships, but until the backend ships, **anyone running a release APK against `app.chprbn.gov.ng` will see seemingly-real exam data that is in fact synthetic**.

### 2.2 The eight speculative endpoints

All paths are relative to the production base URL `https://app.chprbn.gov.ng/api/v1/mobile/`.

#### Exam feature — read

| # | Method | Path | Auth | Mobile source | Purpose |
|---|---|---|---|---|---|
| E1 | `GET` | `exam/dossier` | Bearer | `ExamDossierApiService.fetchDossier` | Pull the officer's currently-active centre + papers + candidate roster + paper↔candidate assignments in one call. Server resolves "which dossier" from the bearer token + today's date. |

#### Exam feature — write

| # | Method | Path | Auth | Mobile source | Purpose |
|---|---|---|---|---|---|
| E2 | `POST` | `exam/attendance` | Bearer | `ExamSyncApiService.uploadAttendance` | Per-row upload of an attendance mark (one HTTP request per candidate row). Idempotent on `(candidateId, paperId, markedAt)` recommended. |
| E3 | `POST` | `exam/remarks` | Bearer | `ExamSyncApiService.uploadRemark` | Per-row upload of an officer remark. Idempotent on `(remarkId)` recommended. |

#### Assessment feature — read

| # | Method | Path | Auth | Mobile source | Purpose |
|---|---|---|---|---|---|
| A1 | `GET` | `assessments/schedules` | Bearer | `AssessmentPackageApiService.fetchSchedules` | List the schedules the officer is assigned to. |
| A2 | `GET` | `assessments/schedules/{scheduleId}/package` | Bearer | `AssessmentPackageApiService.fetchPackage` | Pull a schedule's full reference data: paper + practical sections + questions + candidates + assignments. |

#### Assessment feature — write

| # | Method | Path | Auth | Mobile source | Purpose |
|---|---|---|---|---|---|
| A3 | `POST` | `assessments/practical-scores` | Bearer | `AssessmentSyncApiService.uploadPracticalScore` | Per-row upload of a practical (section-level) score. Idempotent on `(candidateId, sectionId)` recommended. |
| A4 | `POST` | `assessments/project-scores` | Bearer | `AssessmentSyncApiService.uploadProjectScore` | Per-row upload of a project score. Idempotent on `(candidateId, paperId)` recommended. |

### 2.3 Envelope convention (audit C3)

Every speculative DTO assumes a uniform envelope:

```json
{
  "status": true,
  "message": "OK",
  "data": { /* endpoint-specific payload */ }
}
```

This is the shape already used by `practitioners/license` (item 5.3 in `docs/API_ENDPOINTS.md`). The existing `auth/login` endpoint **breaks this convention** — it returns the user object flat at the top level. **The convention question for backend is: which form is canonical going forward?**

- **Option α (recommended):** Standardize on `{status, message, data}` for all new endpoints. Mobile DTOs already assume this. Login stays as-is (legacy); no client churn.
- **Option β:** Flat top-level objects everywhere. Mobile rewrites all 8 envelope DTOs. ~½ day of work; not load-bearing but real churn.

### 2.4 Auth model (audit C2)

`AuthorizationInterceptor` already attaches `Authorization: Bearer <token>` on every non-`/login` request (`feature/auth/data/network/AuthorizationInterceptor.kt`). The token comes from `AuthTokenStore`, which is the value the login response returns in its `accessToken` field. So **mobile is ready to authenticate** — the question for backend is:

- **Q1.** Do the exam + assessment endpoints accept the **same bearer token** the verification endpoints accept? If yes, no client change.
- **Q2.** Is the token Sanctum-personal or a JWT? Mobile is agnostic but the backend should confirm the lifecycle (does the token expire? if yes, how long?). The current mobile implementation has **no refresh path** — token expiry forces a re-login. If exam/assessment tokens have a different TTL, surface it before launch.
- **Q3.** If `exam/dossier` resolves "today's centre" from the bearer token + date, what does the response look like when the officer has no active assignment for today? Empty `data`? `data: null`? HTTP 404? Mobile currently degrades all three to "no dossier" — confirm that's the desired UX.

### 2.5 What backend needs to confirm or change

Concrete checklist to put to the backend team. Each row gates the corresponding mobile feature:

| # | Question / sign-off | Blocks |
|---|---|---|
| Q-C1.E1 | Confirm exact path for E1 (`exam/dossier`) and that it returns the full dossier in one response. | Exam dashboard |
| Q-C1.E2 | Confirm path + idempotency story for attendance upload (E2). | Attendance sync |
| Q-C1.E3 | Confirm path + idempotency story for remark upload (E3). | Remark sync |
| Q-C1.A1 | Confirm path for schedule list (A1). | Assessment schedule list |
| Q-C1.A2 | Confirm path for per-schedule package (A2) and `scheduleId` URL-encoding rules. | Assessment paper detail |
| Q-C1.A3 | Confirm path + idempotency story for practical-score upload (A3). | Practical scoring |
| Q-C1.A4 | Confirm path + idempotency story for project-score upload (A4). | Project scoring |
| Q-C2 | Bearer token + scope. Same token as verification? Different TTL? | Every protected endpoint |
| Q-C3 | Standard envelope: `{status, message, data}` vs flat? | DTO reshape, all 8 endpoints |
| Q-NULL | Behavior when officer has no work assigned today (empty `data` / `null` / 404)? | Empty-state UX |
| Q-ERR | Error body shape for non-2xx (existing convention: `{ "message": "..." }`)? | Error surfacing |
| Q-PHOTO | Are candidate `photoUrl` values absolute HTTPS URLs or relative paths or Base64 data URIs? Mobile normalises all three via `core/network/ImageUrlNormalization` (L4 fix). | Candidate avatars |

### 2.6 Acceptance — when does the contract close?

The contract is closed and mobile can ship "real" exam + assessment when:

1. All 8 endpoints exist in staging with the agreed envelope + auth.
2. A staging base URL is available so mobile can run `:app:assembleDebug` against real responses without crossing into production.
3. One end-to-end test pass: officer logs in → fetches dossier → marks attendance for ≥ 1 candidate → goes offline → marks another → comes online → sync completes → server shows both rows. Same pattern for an assessment schedule.
4. Mobile then flips the `CompositeExamDossierRemoteSource` + `CompositeAssessmentPackageRemoteSource` wiring **or keeps the Composite** if the Fake source is still useful for dev. The Composite already prefers the API, so no flip is required — the silent-fallback risk just goes away once the API succeeds.
5. Section §11 + §12 of `docs/RELEASE_SMOKE_TEST.md` get promoted from "**(fake data — UI-only)**" to real production gates.

### 2.7 Mobile-side cleanup after backend cutover

Tracked separately so this doc stays "external blockers" only. When the contract closes:

- [ ] Remove the `**SPECULATIVE**` doc comments from each `*ApiService` interface and from each `*EnvelopeDto`.
- [ ] Add a real `Api*RemoteSourceTest` per endpoint using MockWebServer (covers wire-level JSON regressions — currently mock the interface so JSON shape drift slips past CI).
- [ ] Drop the Composite + Fake sources for production builds (keep for `debug` via a `buildType`-scoped binding).
- [ ] Update `docs/API_ENDPOINTS.md` to add the eight endpoints with finalised paths, sample request/response bodies, and error shapes.
- [ ] Re-run `docs/RELEASE_SMOKE_TEST.md` §11 + §12 as real gates.

---

## Item 6 — SPKI certificate pinning

### 6.1 Status

`app/src/main/res/xml/network_security_config.xml` already pins:

- Cleartext traffic disabled app-wide and for `app.chprbn.gov.ng` (and subdomains).
- System trust anchors only.
- A `<pin-set>` placeholder block, **commented out**, awaiting fingerprints from ops:

```xml
<!--
<pin-set expiration="2027-01-01">
    <pin digest="SHA-256">REPLACE_WITH_LEAF_SPKI_PIN</pin>
    <pin digest="SHA-256">REPLACE_WITH_BACKUP_SPKI_PIN</pin>
</pin-set>
-->
```

The TLS plumbing is right; the **only** missing piece is two SHA-256 SPKI fingerprints from the ops team — one for the live server's current leaf certificate, one for a backup pin that survives a rotation.

### 6.2 Why pinning matters here

The CHPRBN app is permissions-heavy:

- It uploads identity-tied verification records (`practitioners/verified-sync`).
- It uploads attendance / remarks tied to real candidates (E2, E3).
- It uploads exam scores (A3, A4).
- It stores a bearer token, encrypted at rest, that authenticates as a regulatory officer.

A MITM on the cellular path (rogue CA, compromised system root, captive-portal-style interception) lets an attacker observe officer credentials, intercept score uploads, or inject fake practitioner records into the local cache. System trust anchors alone do not defend against any CA in the system store going rogue — only pinning does. For an app with statutory consequences attached to the data it produces, pinning is a baseline expectation.

### 6.3 What ops needs to produce

Two SHA-256 SPKI fingerprints in base64:

| # | Pin | Lifecycle |
|---|---|---|
| **P1** | **Current leaf** — fingerprint of the public key in `app.chprbn.gov.ng`'s currently-served leaf certificate. | Rotates whenever the cert is reissued. |
| **P2** | **Backup** — fingerprint of a public key that is *not yet served* but *will be served* on the next rotation. Most commonly the intermediate CA's public key, or a pre-generated next-leaf key kept in an HSM / KMS. | Stable across at least one rotation cycle. |

**Both pins must be present.** A single-pin deployment is a self-DoS waiting to happen — the day the leaf rotates without a coordinated app release, every install bricks. Two pins (current + backup) means a rotation onto the backup pin is invisible to users, and the next release can drop the retired pin and add a new backup.

### 6.4 How to extract a SPKI pin

#### From the live server

```powershell
# Print the chain and extract the leaf SPKI SHA-256 pin (base64):
openssl s_client -servername app.chprbn.gov.ng -connect app.chprbn.gov.ng:443 -showcerts < $null `
  | openssl x509 -pubkey -noout `
  | openssl pkey -pubin -outform der `
  | openssl dgst -sha256 -binary `
  | openssl enc -base64
```

The single-line output is the leaf pin (`P1`). Run the same incantation against the intermediate cert (after splitting the `-showcerts` output) to get the backup pin (`P2`).

#### From a `.crt` / `.pem` file ops can email over

```powershell
openssl x509 -in cert.pem -pubkey -noout `
  | openssl pkey -pubin -outform der `
  | openssl dgst -sha256 -binary `
  | openssl enc -base64
```

#### Sanity check on a candidate pin

Pins are 44 characters of base64 (28 raw bytes → base64 → 44 chars including trailing `=`). Anything that doesn't look like that is wrong.

### 6.5 What mobile does once the pins arrive

This is mechanical — should land in under an hour:

#### Step 1 — `network_security_config.xml`

Edit `app/src/main/res/xml/network_security_config.xml`:

```xml
<domain-config cleartextTrafficPermitted="false">
    <domain includeSubdomains="true">app.chprbn.gov.ng</domain>
    <pin-set expiration="2027-01-01">
        <pin digest="SHA-256">P1_BASE64_HERE</pin>
        <pin digest="SHA-256">P2_BASE64_HERE</pin>
    </pin-set>
</domain-config>
```

The `expiration` attribute should match (or precede) the leaf certificate's `notAfter`. After `expiration` Android stops enforcing the pin set, which is the failsafe that prevents a permanently bricked install if the release cadence falls behind cert rotation.

#### Step 2 — defence-in-depth: OkHttp `CertificatePinner`

`network_security_config.xml` only covers TLS handshakes performed via Android's platform stack. OkHttp uses the platform stack by default but it's worth mirroring the pins in the OkHttp config so the contract is explicit at code-review time. Edit `feature/auth/data/di/AuthDataModule.kt` → `provideOkHttpClient(...)`:

```kotlin
val pinner = CertificatePinner.Builder()
    .add("app.chprbn.gov.ng", "sha256/P1_BASE64_HERE")
    .add("app.chprbn.gov.ng", "sha256/P2_BASE64_HERE")
    .build()

return OkHttpClient.Builder()
    // ... existing timeouts + interceptors
    .certificatePinner(pinner)
    .build()
```

Keep the strings in `BuildConfig` fields so production + staging can hold different pin sets, or behind a constant in `core/network/`. **Do not put pins in `keystore.properties`** — pins are not secret and belong in version control so reviews can catch a bad change.

#### Step 3 — smoke verify on a device

Before merging:

1. Install the unsigned release APK (`docs/RELEASE_SMOKE_TEST.md`).
2. Run a single network call (log in). Pass = success.
3. Edit one pin character locally, re-install, retry. Fail = `SSLPeerUnverifiedException` in logcat. This proves the pin is enforced, not just present.
4. Revert the bad pin, re-install, confirm success. Ship.

### 6.6 Pin lifecycle / rotation playbook

For the ops + mobile teams jointly:

| Event | Action |
|---|---|
| **Cert rotation imminent (≥ 30 days notice)** | Ops generates the new leaf. Mobile ships a release that adds the new pin as a *third* entry (current + outgoing + incoming). |
| **Rotation lands** | Server now serves the new leaf. The old pin still validates (still in the set) — no client breakage. Mobile ships a follow-up release that drops the old pin, leaving (new + new-backup). |
| **Emergency rotation (compromised cert)** | The `<pin-set expiration>` failsafe kicks in eventually, but the right move is a hotfix release. The two-pin setup buys breathing room: most installs keep working as long as the new leaf was already part of the backup pin. |

### 6.7 Acceptance — when does cert pinning close?

- [ ] Ops supplies P1 (current leaf SPKI SHA-256) and P2 (backup SPKI SHA-256), both as 44-char base64.
- [ ] `network_security_config.xml` `<pin-set>` block uncommented with real pins; `expiration` set to no later than the leaf's `notAfter` date.
- [ ] `AuthDataModule.provideOkHttpClient` mirrors the pins via `CertificatePinner`.
- [ ] Bad-pin smoke test on a device fails with `SSLPeerUnverifiedException` (proves enforcement).
- [ ] Good-pin smoke test passes (login + license lookup succeed).
- [ ] Ops + mobile agree on the rotation cadence and who-tells-who when the cert is about to rotate.

---

## Document history

| Version | Date | Notes |
|---|---|---|
| 1.0 | 2026-05-15 | Initial drafting of items 2 + 6 as production blockers; grounded in `feature/exam/data/api/`, `feature/assessment/data/api/`, `network_security_config.xml`, `AuthorizationInterceptor.kt`. |
