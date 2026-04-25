# CHPRBN Field Mobile App — User Manual

**Audience:** Field officers using the Android app for license lookup, on-site verification, and sync to the central API.  
**Scope:** End-to-end features and **UI states** as implemented in the current codebase (including error and loading paths).  
**Screenshots:** Placeholders are marked with **`[SCREENSHOT: …]`** — capture the described screen or state on a device or emulator and insert images there.

---

## 1. App overview

### 1.1 Purpose

The app supports:

- **Authentication** with adhoc (field-officer) accounts (`username` + `password`).
- **License lookup** by **QR scan** or **manual license number entry**.
- **Record detail** with local-first cache and optional background refresh.
- **Verification capture** (location, remarks, practitioner-present) saved **locally**.
- **Verified practitioners list** with filters and sync status.
- **Sync** of pending/failed verifications to the server (`adhoc/verified-sync`).
- **Profile** and **dashboard** hub.

### 1.2 Information architecture (main routes)

| Area | Typical entry |
|------|----------------|
| Splash | App launch |
| Login | No valid saved session |
| Dashboard | After successful login or splash with valid session |
| Scan QR | Dashboard / bottom nav |
| Manual license entry | From scan screen |
| Record detail | After scan or manual entry |
| Verification form | “Proceed to Verification” on record detail |
| Verified list | Dashboard / bottom nav |
| Sync | Dashboard / bottom nav |
| Sync history | From sync screen |
| Profile | Dashboard / bottom nav |

`[SCREENSHOT: Navigation map or app icon on home screen]`

---

## 2. Launch & session (Splash)

### 2.1 States

| State | What the user sees | What happens next |
|--------|---------------------|-------------------|
| **Initializing** | Splash branding, progress (“INITIALIZING SECURE RECORDS”), ~2.5s delay | App reads cached user from local database |
| **Valid session** | Same splash, then automatic navigation | **Dashboard** — in-memory token restored from saved user |
| **No / invalid session** | Same splash, then navigation | **Login** — e.g. blank/placeholder token, or no user row |

Invalid saved tokens (e.g. legacy demo placeholders) are **not** treated as logged in; user must sign in online to obtain a real API token.

`[SCREENSHOT: Splash screen — full frame]`  
`[SCREENSHOT: Transition to Login after cold start with no session]`

---

## 3. Login

### 3.1 Fields & actions

- **Username** (trimmed).
- **Password**.
- **Sign In** — submits credentials to `adhoc/login`, then loads profile via `adhoc/profile` and caches user + token locally.
- Links **Forgot Password** and **Request Access** are present in the UI; navigation targets are **not implemented** (TODO in code).

`[SCREENSHOT: Login — empty fields]`  
`[SCREENSHOT: Login — filled fields before Sign In]`

### 3.2 States during login

| State | UI | Notes |
|--------|-----|--------|
| **Idle** | No inline error, button enabled | Initial or after clearing errors |
| **Validation error** | Red/error text: **“Username and password are required.”** | Shown if username (after trim) or password is blank |
| **Loading** | Loading indicator on primary button | While network login runs |
| **Success** | Navigation to **Dashboard** | User + token stored; success state consumed after navigation |
| **Server / parse error** | Inline message from server or fallback | Examples below |

### 3.3 Error messages (online login)

Messages are driven by the API and repository logic. Users may see (non-exhaustive):

| Message (or pattern) | When |
|----------------------|------|
| From API JSON `message` | e.g. invalid credentials, validation errors |
| **“Login failed.”** | Unsuccessful HTTP response with no parseable body |
| **“Invalid login response.”** | 200 body missing token |
| **“Could not load profile.”** | Profile HTTP failure, generic |
| From API / **“Invalid profile response.”** | Profile missing `data` or `status != true` |
| **“Network unavailable. Please try again or use manual verification.”** | IOException or offline during online attempt, and no usable cached session |
| **Throwable message** | Other exceptions, surfaced as `t.message` |

`[SCREENSHOT: Login — validation error state]`  
`[SCREENSHOT: Login — inline server error after failed sign-in]`  
`[SCREENSHOT: Login — loading state on button]`

### 3.4 Offline login

| Outcome | Message |
|---------|---------|
| **Success** | Same as online success; uses **cached** user for the same **username** with a **valid** stored token |
| **Failure** | **“No cached session available for offline login.”** |

`[SCREENSHOT: Login — offline with no cache error]`

---

## 4. Dashboard

### 4.1 States

| State | UI |
|--------|-----|
| **Loading** | Centered circular progress; welcome area without user details |
| **Success** | Welcome card with user info; **feature grid** (Scan QR, Verified List, Sync, Profile, etc.) |
| **Error** | Error container with message and **Retry** button |

Error text is `e.message` or **“Unknown error”** if null.

`[SCREENSHOT: Dashboard — loading]`  
`[SCREENSHOT: Dashboard — success with feature tiles]`  
`[SCREENSHOT: Dashboard — error banner with Retry]`

### 4.2 Bottom navigation

From dashboard-related screens: **Home**, **Verified** (search entry), **Scan**, **Sync**, **Profile** — exact routing depends on current route.

`[SCREENSHOT: Bottom navigation bar highlighted on Home]`

---

## 5. License lookup — QR scan

### 5.1 Flow

1. Open **Scan** from dashboard or nav.  
2. App requests **camera permission** (system dialog).  
3. Camera preview with framing UI and **torch** toggle.  
4. On successful decode, registration id is extracted from structured QR text (`#: <id>` line when present).  
5. Navigation to **record detail** with that id.

`[SCREENSHOT: Scan — camera permission prompt]`  
`[SCREENSHOT: Scan — camera preview with frame]`  
`[SCREENSHOT: Scan — torch on]`

### 5.2 QR payload (expected shape)

Example:

```text
#: B2320135
Cadre: CHEW
Expiry: 2025-10-24
```

The app uses the token after **`#:`** for lookup. If that cannot be parsed, the scan is **ignored** (camera can scan again) — error details are primarily in **Logcat** (`QrScan` tag), not as a blocking in-app dialog.

`[SCREENSHOT: Logcat filtered by QrScan — optional appendix]`

### 5.3 Manual entry path

**“Enter License Manually”** opens **Manual license entry**:

- Single field **License Number**.
- **Verify License** submits and navigates to **record detail** with entered text.

`[SCREENSHOT: Manual entry screen]`  
`[SCREENSHOT: Manual entry — license field filled]`

### 5.4 Camera / scan failure (technical)

- Permission denied: logged; preview may not start.  
- Camera start exception: logged (`Failed to start camera`).  
- ML Kit failure: logged (`QR barcode processing failed`).

`[SCREENSHOT: System settings — camera denied, if documenting permissions]`

---

## 6. Record detail (license lookup result)

Data is **local-first**: cached row in Room if present; otherwise remote fetch; then **silent background refresh** when online.

### 6.1 States

| State | User-visible UI |
|--------|------------------|
| **Loading** | Skeleton / “Verification in progress” style panel with spinner |
| **Success** | Photo (or fallback image), name, subtitle, status card, detail rows, **Proceed to Verification** |
| **Not found** | Illustration, **“No record found”**, explanatory copy with license id when known, **Try Again**, **Enter manually** |
| **Error** | **“Connection lost”** heading, generic connectivity copy, **Retry connection** (uses `RecordDetailUiState.Error` message from domain but **fixed** marketing copy on screen — server text may not be shown verbatim) |

#### Not found copy (abbrev.)

- With license: *We couldn't find a practitioner matching license “…”.*  
- Without: *We couldn't find a practitioner matching this license number…*

`[SCREENSHOT: Record detail — loading]`  
`[SCREENSHOT: Record detail — success Active license]`  
`[SCREENSHOT: Record detail — success non-active status]`  
`[SCREENSHOT: Record detail — not found]`  
`[SCREENSHOT: Record detail — error / connection lost]`

### 6.2 Domain-driven error text (behind Error state)

Repository / use case may set messages such as:

- **“License number is required.”** (empty input path from use case)
- **“Network error. Please check your connection.”** (`IOException`)
- Other **throwable messages** from remote layer

The **screen** still shows the **connection-lost** layout for `RecordDetailUiState.Error`.

`[SCREENSHOT: Optional — same error state; note fixed headline in caption]`

### 6.3 Proceed to verification

Bottom bar **Proceed to Verification** passes the full **LicenseRecord** into the verification form (JSON in navigation). Shown whenever `record != null` (including non-active licenses; server-side rules may differ).

`[SCREENSHOT: Record detail — bottom CTA Proceed to Verification]`

---

## 7. Verification form

### 7.1 Fields

- Read-only: **Practitioner name**, **License number**, **Profession**, **License status**, **Expiry** (when present on record).
- **Verification location** (required to save).
- **Officer remarks** (required to save).
- **Mark Verified** switch — **Practitioner must be marked as verified** (on) to save successfully.

Initial switch state: **on** when license status is **Active** (case-insensitive); otherwise **off**.

`[SCREENSHOT: Verification form — empty editable fields]`  
`[SCREENSHOT: Verification form — Active license with switch on]`  
`[SCREENSHOT: Verification form — inactive license switch off]`

### 7.2 Save states

| State | UI |
|--------|-----|
| **Idle** | No save spinner on primary action |
| **Saving** | Handled in ViewModel while save runs |
| **Success** | Navigates back / clears stack per `AppNavHost` (toward verified list refresh flag) |
| **Error** | Red **bodySmall** text with message |

### 7.3 Validation / error messages on save

| Message | Cause |
|---------|--------|
| **“No license record found to verify.”** | Missing `LicenseRecord` in VM state |
| **“Verification location is required.”** | Location blank after trim |
| **“Officer remarks are required.”** | Remarks blank after trim |
| **“Practitioner must be marked as verified.”** | Switch off (use case or repository guard) |
| **`t.message` or “Unable to save verification.”** | Room / DB insert failure |

`[SCREENSHOT: Verification — validation error under fields]`  
`[SCREENSHOT: Verification — save success transition (list refresh)]`

---

## 8. Verified practitioners list

### 8.1 Filters

Examples include **All**, **Active**, **Expired**, **Pending Sync** (shows rows pending or failed sync).

`[SCREENSHOT: Verified list — default]`  
`[SCREENSHOT: Verified list — Pending Sync filter]`  
`[SCREENSHOT: Verified list — row with Pending Sync badge]`  
`[SCREENSHOT: Verified list — row with Failed + error snippet]`

### 8.2 Row sync labels

- **Pending Sync** — not yet successfully uploaded.  
- **Failed** — last attempt failed; may show **syncError** text or **“Sync failed”**.  
- **Synced** — no extra sync line in the simplified UI.

`[SCREENSHOT: Verified list — synced row]`

---

## 9. Sync

### 9.1 Purpose

Uploads each local verification that is **Pending** or **Failed** (`Sync all`), or retries **Failed** only (`Retry failed`). Each row is attempted independently — **partial success** is normal.

`[SCREENSHOT: Sync — main screen overview]`

### 9.2 Screen states

| State | UI |
|--------|-----|
| **Initial load** | **“Loading sync status…”** when loading and no rows yet |
| **Loaded** | Progress ring, **last success** label, stats (**Total / Synced / Pending / Failed**), actions, recent list |
| **Load error** | Red small text: `error` message or **“Unable to load records.”** |
| **Syncing** | Actions disabled (`isSyncing`); batch running |
| **After batch** | **lastBatchSummary** line in green-tinted small text |

### 9.3 Batch summary format

- No work: **`Sync all: nothing to upload.`** or **`Retry failed: nothing to upload.`**  
- Otherwise: **`Sync all: X ok, Y failed (Z attempted)`** (same pattern for **Retry failed**).

### 9.4 Sync-level errors (whole operation throws)

| Message | When |
|---------|------|
| **Throwable message** | Exception during use case |
| **“Sync failed.”** / **“Retry failed.”** | Null message on failure |

Per-record failures are stored on the row (`syncError`) and reflected in **Failed** count and list UI — not necessarily as the top-level red banner unless load/sync orchestration throws.

`[SCREENSHOT: Sync — stats with mixed Pending/Synced/Failed]`  
`[SCREENSHOT: Sync — lastBatchSummary after partial success]`  
`[SCREENSHOT: Sync — top error banner]`  
`[SCREENSHOT: Sync — loading line only]`

### 9.5 Connectivity indicator (UI)

Sync UI may show **CONNECTED** / **OFFLINE** style status where implemented — use for training materials.

`[SCREENSHOT: Sync — connected vs offline indicator]`

### 9.6 Sync history

**Sync history** screen (separate route) uses **presentation/sample-style** data in its ViewModel for design — treat as **demo** unless wired to live data in your build.

`[SCREENSHOT: Sync history — with filter chips]`  
`[SCREENSHOT: Caption: sample/demo data if applicable]`

---

## 10. Profile

- Shows cached user profile (from auth DB).  
- **Logout** clears local user and token and returns to **Login** (stack popped to splash root per nav).  
- Other actions (edit profile, change password, etc.) may be **TODO** in navigation.

`[SCREENSHOT: Profile — populated]`  
`[SCREENSHOT: Profile — after logout back to Login]`

---

## 11. Glossary

| Term | Meaning |
|------|---------|
| **Adhoc user** | Field-officer account (`adhoc/login`), not practitioner tutor login |
| **License record** | Practitioner card data from `GET practitioners/license` (+ cache) |
| **Verified row** | Local verification + sync metadata in `verified_licenses` |
| **Sync status** | **Pending** / **Synced** / **Failed** (per row) |

---

## 12. Generating a PowerPoint version

If **Python 3** and **`python-pptx`** are installed, from the repo root:

```bash
pip install python-pptx
python docs/scripts/generate_chprbn_user_manual_pptx.py
```

Output: `docs/CHPRBN_Field_App_User_Manual.pptx` (one slide per major section + screenshot placeholders).

If Python is unavailable, use **`USER_MANUAL_POWERPOINT_OUTLINE.md`** and paste each slide block into PowerPoint.

---

*Document generated to match application behavior as of the repository revision; TODO navigation items are called out where applicable.*
