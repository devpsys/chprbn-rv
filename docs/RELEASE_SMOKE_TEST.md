# Release APK — Runtime Smoke Test

**Purpose:** Walk an unsigned release-mode APK through every feature path that has shipped, on a real device, before declaring the release build production-ready. R8 + SQLCipher + Hilt + Gson reflection failures only surface at runtime, never at build time. This walkthrough is the only gate that catches them before users do.

**Artifact under test:** `app/build/outputs/apk/release/app-release-unsigned.apk` (~32 MB).
**Build verified on:** 2026-05-15 — `:app:assembleRelease` green; R8 + ProGuard rules applied; SQLCipher 4.15.0; `BuildConfig.BASE_URL = https://app.chprbn.gov.ng/api/v1/mobile/`.

> **The exam + assessment features are not testable end-to-end yet** — their `Composite*RemoteSource` falls back to the in-memory `Fake*RemoteSource` because the backend endpoints don't ship. The screens render, but the data is synthetic. Marked **(fake data — UI-only)** below.

---

## Prerequisites

1. A real Android device (API 24+) with USB debugging enabled, OR an emulator with arm64 image. Avoid x86 emulators if you want to validate the 16 KB page-size native code path.
2. `adb` on PATH.
3. A working CHPRBN test account (registered practitioner credentials for the production API).
4. Network on the device (for the verification + sync flows). Toggle airplane mode mid-flow for the offline test.
5. A QR scan target — printout or screen with a registration number encoded as a QR code.

## Install

```powershell
# Uninstall any prior debug build to start clean
adb uninstall ng.com.chprbn.mobile

# Install the unsigned release APK
adb install -t app\build\outputs\apk\release\app-release-unsigned.apk
```

The `-t` flag allows test/unsigned APKs. Play uploads require a signed variant (see `keystore.properties.example`).

## Logcat watch

Run this in a second terminal *before* launching the app. Stop the test and capture the trace immediately if any of these appear:

```powershell
adb logcat -c
adb logcat *:E ng.com.chprbn.mobile:V | Select-String "JsonSyntaxException|IllegalStateException|cannot find adapter|ClassNotFoundException|UnsatisfiedLinkError|net.sqlcipher|SQLiteException|Could not resolve|RoomDatabase"
```

Known R8 / SQLCipher failure signatures and what they mean:

| Signature | Likely cause | Fix |
|---|---|---|
| `JsonSyntaxException` | R8 stripped a DTO field referenced by Gson | Add a `-keepclassmembers` rule for the offending package |
| `cannot find adapter for class` (Room) | Entity stripped by R8 | Add a `-keep class ...entity.* { *; }` rule |
| `UnsatisfiedLinkError: libsqlcipher` | SQLCipher native load failed | Confirm `System.loadLibrary("sqlcipher")` runs in `ChprbnApplication.onCreate` |
| `SQLiteException: file is not a database` | Wrong passphrase / mixing encrypted + unencrypted DB | Confirm `DatabaseKeyProvider` + `DatabaseMigrationGuard` ran |
| `Could not resolve …Composite…RemoteSource` | Hilt graph broken | Re-check `LicenseDataModule` / `AuthDataModule` |

---

## Test matrix

Tick each row as you pass it. If any row fails, stop the walkthrough and capture the full logcat — partial passes are not a release signal.

### 1. Cold start + splash + auth

- [ ] First launch shows the splash screen for ~2.5 s, then routes to Login (no cached user).
- [ ] Force-quit the app, relaunch, confirm splash still routes to Login (no stale state).
- [ ] Enter empty credentials → inline error appears (`R.string.login_error_missing_credentials`).
- [ ] Enter valid credentials → login succeeds → Verification dashboard renders within 5 s.
- [ ] Kill the app, relaunch, confirm splash now routes straight to Verification (cached token survives, SQLCipher unlocked).

### 2. Profile

- [ ] Navigate to Profile tab. User name + role + photo render. Photo loads from a `data:` or `https:` URL without fallback to placeholder unless the server has no photo.
- [ ] Pull to refresh (if wired) does not crash; otherwise rotate / re-enter the tab.
- [ ] Sign out → returns to Login. Cached token is cleared.
- [ ] Sign in again → returns to Verification.

### 3. Scan + license lookup

- [ ] Tap the FAB → camera opens; permission prompt appears on first launch and is granted.
- [ ] Scan a valid QR encoding `MED-XXXXX` (or whatever the production registration format is) → app navigates to `CandidateScanResult` and renders the practitioner record.
- [ ] Photo loads. Status chip renders (`Active`/`Expired`/etc).
- [ ] Tap back, scan a clearly invalid code → graceful NotFound state, no crash.

### 4. Manual entry + record detail

- [ ] From Verification tab → tap Manual Entry → enter a known registration number → submit → record detail screen renders.
- [ ] Background → foreground the app while on record detail. State preserved (no crash, no blank state).
- [ ] Disconnect network → tap retry / re-open the same record. Cached data still renders.

### 5. Verify a license

- [ ] Open a record detail. Tap "Save" / "Verify" CTA. Fill verification form: select Remark, enter Location, toggle "Practitioner present". Submit.
- [ ] Toast / success state appears.
- [ ] Navigate to Verified List → the new record appears with `Pending` sync chip.
- [ ] Open the Verified record → all fields persisted correctly.

### 6. Sync queue

- [ ] On Sync tab → counts show ≥ 1 pending. Tap "Sync All" with network on.
- [ ] Sync count drops to 0 (or surfaces a meaningful error if the server rejects the payload).
- [ ] Disconnect network → save another verification → confirm it queues as Pending → reconnect → re-trigger Sync → confirm it goes through.
- [ ] Force a failure (e.g. corrupt the registration number then re-save) → confirm the row moves to Failed and surfaces the server error.
- [ ] Tap "Retry failed" → confirm retry path executes.

### 7. Irregularity report

- [ ] From a record detail tap "Report Irregularity" → form pre-fills with re-fetched record fields (verifies the type-safe nav arg + re-fetch).
- [ ] Submit. Confirm it queues onto the outbox (`Sync` tab pending count ticks up).
- [ ] Background sync runs (WorkManager) — leave the app for ~30 s with network on, then check Sync tab. The report should have moved Pending → Synced.

### 8. Verified list interactions

- [ ] Scroll the Verified List. Each row reads as one TalkBack focus unit (semantic merge from the U1 pass).
- [ ] Tap a verified row → record detail re-renders (re-fetches by ID).

### 9. Sync history

- [ ] Navigate to Sync History (if surfaced from Sync screen). Rows render with relative timestamps.

### 10. Dashboard

- [ ] Home / Dashboard tab renders user header + feature tiles. No crash on rotate.

### 11. Exam feature (fake data — UI-only)

- [ ] Tap into Exam from Dashboard. ExamDashboard renders with fake center / two task cards.
- [ ] Open ExamPapers → list renders. Tap a paper → ExamPaper detail renders (fake content).
- [ ] Open ExamCandidates → list renders. Filter chips work.
- [ ] FAB → "Download Dossier" → loading overlay appears → completes within timeout, no crash.
- [ ] **Do not** report exam features as production-passing — this is a UI-only smoke test until C1/C2/C3 backend contracts land.

### 12. Assessment feature (fake data — UI-only)

- [ ] Tap into Assessment from Dashboard. AssessmentSchedules list renders.
- [ ] Open a schedule → AssessmentPaperDetail renders. Per-row pills show Pending/Synced/Failed counts (synthetic).
- [ ] Open the clinical-regulatory grading screen → entry of a fake score does not crash.
- [ ] Same caveat as exam: not a production gate.

### 13. Background + lifecycle

- [ ] Background the app for 60 s on every screen above. Foreground. No crash, no blank state, scroll position preserved where expected.
- [ ] Rotate every screen. No crash, no state loss for in-flight forms.
- [ ] Press Back from a root tab → app exits cleanly (no Hilt-tear-down crash).

### 14. Storage + data integrity

After the walkthrough, with the device still plugged in:

```powershell
adb shell run-as ng.com.chprbn.mobile ls databases
```

Expected: `auth.db`, `scan.db`, `sync.db`, `exam.db`, `assessment.db`. Each with `-journal` or `-wal` sidecars.

```powershell
# Confirm DBs really are encrypted (sqlite3 should fail to open them)
adb shell run-as ng.com.chprbn.mobile sh -c "sqlite3 databases/scan.db '.tables'"
```

Expected: `file is not a database` or `unable to open` — confirms SQLCipher encryption is engaged.

---

## What to do on failure

1. **Stop the walkthrough.** Don't continue — symptoms compound.
2. Capture `adb logcat -d > smoke-fail-<step>.log` and the screen state (`adb exec-out screencap -p > smoke-fail-<step>.png`).
3. Common fixes:
   - **R8 stripped something:** add a targeted `-keep` rule to `app/proguard-rules.pro`, re-`assembleRelease`, retry from the failing step.
   - **SQLCipher init order:** check `ChprbnApplication.onCreate` runs `System.loadLibrary("sqlcipher")` before any Hilt-injected DB is touched.
   - **Hilt graph breakage post-R8:** R8 can shrink `@Provides` functions if reachability is unclear; add `-keep class ...di.* { *; }` for the affected module.
4. After the fix, re-build the release APK and restart from step 1 — don't trust partial state.

---

## Pass criteria

A release is smoke-test-passing when **every row in §1–10 + §13–14 is ticked, no logcat watchword fires, and no crash dialog appears.** §11 + §12 are informational only until backend C1–C3 closes.

When all rows pass, record the pass in `docs/CODE_REVIEW_PROGRESS.md` (Phase 1 release-hardening row, PR B + SQLCipher status → 🟢), and the release APK becomes the candidate for signing + Play upload.
