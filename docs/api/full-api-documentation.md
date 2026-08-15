# CHPRBN Mobile — Full API Documentation

**Audience:** Backend, mobile, QA.
**Status:** Authoritative reference for every HTTP endpoint the CHPRBN Android client consumes today (Verification + Auth) and every endpoint required to ship the Assessment + Examination modules.
**Source of truth:** The Retrofit interfaces under `app/src/main/java/ng/com/chprbn/mobile/feature/*/data/api/` and the DTOs under `app/src/main/java/ng/com/chprbn/mobile/feature/*/data/dto/`. When this doc and the code disagree, the code is right and this doc is stale — file a doc-drift PR.

---

## Contents

1. [Overview](#1-overview)
2. [Standard Request Conventions](#2-standard-request-conventions)
3. [Standard Response Envelope](#3-standard-response-envelope)
4. [Error Response Format](#4-error-response-format)
5. [Authentication Module](#5-authentication-module)
6. [Profile Module](#6-profile-module)
7. [Verification Module](#7-verification-module)
8. [Examination Module](#8-examination-module)
9. [Assessment Module](#9-assessment-module)
10. [Cross-Endpoint Data Models](#10-cross-endpoint-data-models)
11. [API Dependency Mapping](#11-api-dependency-mapping)
12. [Security Considerations](#12-security-considerations)
13. [Open Issues and Gaps](#13-open-issues-and-gaps)
14. [Migration Notes](#14-migration-notes)
15. [Appendix A — Endpoint Index](#appendix-a--endpoint-index)

---

## 1. Overview

### 1.1 Purpose

A single implementation-ready reference for every endpoint the CHPRBN mobile client consumes or will consume. It serves three audiences simultaneously:

- **Backend developers** building the Assessment + Examination endpoints from scratch.
- **Mobile developers** confirming the wire contract their Retrofit/DTO layer encodes.
- **QA + tooling** generating Postman / OpenAPI artefacts and writing contract tests.

Every endpoint section uses the same headings, types, and example shapes so a reader can pick up any endpoint cold.

### 1.2 API versioning strategy

| Element | Convention |
|---|---|
| **Major version in path** | `/api/v1/mobile/…`. Breaking changes increment to `/api/v2/mobile/…`. Versions run side-by-side for one release cycle minimum. |
| **Minor/patch versions** | Additive only (new fields, new endpoints). Existing clients must keep working when new fields appear; new fields are nullable + ignored by old clients. |
| **Sunsetting** | A deprecated endpoint emits `Deprecation: true` and `Sunset: <RFC 7231 date>` response headers for at least one release cycle before removal. |

### 1.3 Base URLs

| Environment | Base URL | Configured by |
|---|---|---|
| **Development (local backend)** | `http://10.0.2.2:8000/api/v1/mobile/` | `app/build.gradle.kts` debug `buildConfigField "BASE_URL"` (to be added when local backend exists) |
| **Staging** | `https://staging.app.chprbn.gov.ng/api/v1/mobile/` | Debug build type `buildConfigField "BASE_URL"` (to be added when staging exists) |
| **Production** | `https://app.chprbn.gov.ng/api/v1/mobile/` | Release build type `buildConfigField "BASE_URL"` |
| **Production — exam backend** | `https://jarabawa.chprbn.gov.ng/api/v1/mobile/` | `buildConfigField "JARABAWA_BASE_URL"` (both build types) |

Today both build types point at production because no staging environment exists. The flip lands the day staging comes online; no client-code changes are required — only the `buildConfigField` value.

All paths in this document are **relative** to the base URL. Where a path is written as `/practitioners/license`, the full URL is `<base>/practitioners/license`. The three Examination-module endpoints (§8.1–§8.3) are the exception — they're relative to the **exam backend** base URL above, not the main one, and carry no bearer token (see §1.4).

### 1.4 Authentication mechanism

| Element | Convention |
|---|---|
| **Scheme** | Bearer token in `Authorization` header — **except** the exam backend (§8.1–§8.3), which takes no token at all; see below. |
| **Token type** | Laravel Sanctum personal-access token (opaque string). The mobile client does not decode it. |
| **Issuing endpoints** | `POST /adhoc/login` (field officers) and `POST /login` (practitioner tutors) both return `data.token`. |
| **Refresh** | **Not supported.** There is no refresh endpoint. Token expiry forces re-login. Mobile clears the local token on 401 and routes the user back to `LoginScreen`. |
| **Logout** | Client-initiated. Mobile POSTs `/adhoc/logout` (adhoc) or `/logout` (tutor) to revoke the bearer token, then clears local state. Endpoint specced in §5.3 — awaiting backend implementation. |
| **Token TTL** | Backend-controlled. Mobile must not assume a TTL; it treats any 401 on a protected route as "session over." |

**Exam backend exception:** `GET attendance/fetch-record`, `POST attendance/push-record`, and `POST attendance-remarks` (§8.1–§8.3) run against the separate jarabawa host and carry **no `Authorization` header**. Instead, every request carries `X-Location`, populated from the officer's `adhoc/profile` `data.location` (§6.1) and attached transparently by the mobile client's `LocationHeaderInterceptor` — it is that backend's sole request credential.

### 1.5 Standard HTTP headers

| Header | Direction | Value | Required | Notes |
|---|---|---|---|---|
| `Authorization` | Request | `Bearer <token>` | Yes on all protected routes (everything except `/login`, `/adhoc/login`). | Attached automatically by `AuthorizationInterceptor` for every URL whose path does not end in `/login`. |
| `Accept` | Request | `application/json` | Yes | Added by `AuthorizationInterceptor` on protected routes. |
| `Content-Type` | Request | `application/json` for JSON bodies; `multipart/form-data; boundary=…` for multipart uploads | Yes when a body is sent | OkHttp / Retrofit sets this automatically. |
| `User-Agent` | Request | OkHttp default (`okhttp/<version>`) | n/a | Backend should not rely on this for routing or auth. |
| `Content-Type` | Response | `application/json; charset=utf-8` | Yes | All structured responses are JSON. Binary downloads not in scope. |
| `Deprecation` | Response | `true` | When endpoint is sunsetting | Optional. |
| `Sunset` | Response | RFC 7231 date string | When endpoint is sunsetting | Optional. |

### 1.6 Content type conventions

- **Requests:** `application/json` for all create/update bodies, except the irregularity report which is `multipart/form-data` because it includes an image part.
- **Responses:** `application/json` for every endpoint. Empty body permitted only on `204 No Content` (currently not used; all writes return a JSON envelope today).
- **Character set:** UTF-8 throughout.
- **Field naming on the wire:** `snake_case` for all endpoints except the legacy login envelope which is currently `camelCase` (see §13).

### 1.7 Pagination conventions

**Status: NOT IMPLEMENTED.** No endpoint in the current client paginates. The Assessment + Examination read endpoints (`attendance/fetch-record`, `assessments/schedules/{id}/package`) are designed as single-shot fetches because the per-officer payload size is bounded (≤ ~200 candidates per centre per day).

When pagination becomes necessary (e.g. a future "all schedules history" endpoint), the convention is:

```
GET /something?page=2&per_page=20&sort=-created_at
```

- `page` (int, 1-based, default 1)
- `per_page` (int, default 20, max 100)
- Response carries `meta.pagination` per §3.

### 1.8 Sorting and filtering conventions

**Status: NOT IMPLEMENTED.** No endpoint exposes server-side sort or filter today. Mobile sorts/filters locally on the post-fetch list. When server-side sorting becomes necessary, the convention is:

- **Sort:** `sort=field` (ascending) or `sort=-field` (descending). Multiple fields: `sort=-created_at,name`.
- **Filter:** `filter[field]=value`. Multiple values: `filter[status]=active,pending`.

### 1.9 Error handling conventions

- Every non-2xx returns the standard envelope with `success: false` and a human-readable `message`.
- 422 (validation) additionally returns `errors: { field: [messages] }` per §4.3.
- 5xx responses MAY omit the envelope (raw HTML/text); mobile treats anything that isn't JSON-parseable + 5xx as "server unavailable" and surfaces a generic message.
- The mobile client never raises an exception for non-2xx — it inspects `response.isSuccessful` and degrades to the appropriate UI state (NotFound, Error, etc.).

### 1.10 Rate limiting

**Status: NOT IMPLEMENTED.** No client-side rate-limit handling is wired. When rate limits land server-side, the convention is:

| Header | Type | Meaning |
|---|---|---|
| `X-RateLimit-Limit` | int | Window size (e.g. 60 per minute). |
| `X-RateLimit-Remaining` | int | Calls left in the current window. |
| `Retry-After` | int (seconds) or HTTP-date | Sent on 429; mobile honours it via a per-endpoint backoff. |

429 returns the standard envelope with `message: "Too many requests."`.

---

## 2. Standard Request Conventions

### 2.1 Query parameters

| Convention | Detail |
|---|---|
| **Casing** | `snake_case`. Example: `?license_number=MD-99201-B`. |
| **URL-encoding** | Always RFC 3986. Spaces become `%20`, not `+`. |
| **Boolean** | `true` / `false` literals. |
| **Multi-value** | Comma-separated (`?ids=a,b,c`) — not yet used by any endpoint. |

### 2.2 Path parameters

| Convention | Detail |
|---|---|
| **Casing** | `{snake_case}` placeholder, lowercase. |
| **Encoding** | Path-encoded per RFC 3986. IDs MUST be URL-safe ASCII (UUIDs, ULIDs, opaque tokens). Do not embed `/` or `?` in IDs. |
| **Example** | `/assessments/schedules/{schedule_id}/package` |

### 2.3 Request body format

- JSON object at top level.
- `snake_case` field names.
- All optional fields may be omitted; backend must not require them.
- Unknown fields on the request are ignored (forward-compat for new clients).
- Empty body permitted for write endpoints that take no parameters.

### 2.4 Multipart/form-data usage

Used for any endpoint that ships a binary part alongside JSON-ish fields. The only such endpoint today is `POST /practitioners/license-irregularity-reports`.

- Each scalar field is sent as its own `Content-Disposition: form-data; name="<field>"` part with `Content-Type: text/plain; charset=utf-8`.
- Binary parts use `Content-Disposition: form-data; name="<field>"; filename="<name>"` and an appropriate `Content-Type` (e.g. `image/jpeg`).
- The boundary is OkHttp-generated; backend must rely on `Content-Type: multipart/form-data; boundary=…`, never assume a fixed boundary.

### 2.5 File upload conventions

| Constraint | Value | Notes |
|---|---|---|
| **Encoding** | Raw bytes inside a multipart part. | No Base64 wrapping inside multipart. |
| **Max size per file** | 5 MiB (proposed) | Backend enforces; client compresses pre-upload to stay under. |
| **Allowed image types** | `image/jpeg`, `image/png`, `image/webp` | |
| **Hash verification** | Not used | Future: `X-Content-SHA256` header to detect transit corruption. |

### 2.6 Date and time formats

| Use case | Format on the wire | Source field in code |
|---|---|---|
| **Event timestamps** (verified_at, marked_at, created_at, scored_at, start_at, end_at, date) | **Unix epoch milliseconds (UTC)** as a JSON integer (`Long`). | `VerifiedSyncRequestDto.verified_at`, `AttendanceSyncRequestDto.marked_at`, etc. |
| **Calendar dates without time** (expiry_date, issue_date, graduation_date, lastLoginAt) | ISO-8601 string (`YYYY-MM-DD` for dates, `YYYY-MM-DDTHH:mm:ssZ` for timestamps). | `LicenseRecordDataDto.expiry_date`. |
| **Display-only labels** ("Dec 2026") | Backend may return either ISO-8601 or pre-formatted display string; client passes through verbatim. | Verification screen accepts both. |

**Rule for new endpoints:** prefer **epoch millis (Long, UTC)** for any field a machine reads; prefer **ISO-8601** for any field a human reads in raw form. Avoid mixing.

### 2.7 Enum representation

Enums are sent as lowercase `snake_case` strings on the wire. The mobile client maps to its Kotlin enum via an explicit converter — never `Enum.valueOf` — so server-side renaming does not crash the client; an unknown value falls back to a documented default.

| Domain enum | Wire values | Default on unknown |
|---|---|---|
| `PaperKind` | `"theory"` (default), `"practical"` (legacy `"PE"`), `"project"` (legacy `"PA"`) | `theory` |
| `AttendanceStatus` | `"signed_in"`, `"signed_out"`, `"flagged"` | n/a — required field |
| `RemarkSeverity` | `"info"`, `"warning"`, `"critical"` | `info` |
| `SyncStatus` | `"pending"`, `"synced"`, `"failed"` | n/a — server never reads it back |

The legacy `PaperKind` codes (`PE`, `PA`) are still tolerated case-insensitively for backward compatibility with v1 backends; new backends MUST emit the long form.

### 2.8 Boolean representation

JSON `true` / `false`. Never `0` / `1` or `"yes"` / `"no"`.

### 2.9 Nullability rules

- **Request:** A field marked **Optional** may be omitted or sent as `null`; both are equivalent. A field marked **Required** must be present and non-null.
- **Response:** A field marked **Nullable** may be present and `null`, present with a value, or omitted entirely; all three are equivalent on the mobile side. Mobile DTOs default to `null` so a missing key does not throw.
- **Empty list vs null list:** treat as equivalent on response. Prefer `[]` over `null` for non-paginated lists.

---

## 3. Standard Response Envelope

### 3.1 Canonical envelope

Every endpoint returns a JSON object with this exact top-level shape:

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {},
  "meta": {
    "pagination": {
      "page": 1,
      "per_page": 20,
      "total": 200,
      "last_page": 10
    }
  }
}
```

| Field | Type | Nullable | Required | Description |
|---|---|---|---|---|
| `success` | boolean | No | Yes | `true` for 2xx, `false` for 4xx/5xx (or omit the envelope for raw 5xx). |
| `message` | string | Yes | No | Human-readable summary. Localisable in future. Mobile displays this directly in error toasts. |
| `data` | object \| array \| null | Yes | No | The endpoint's payload. Shape is per-endpoint. `null` for write endpoints that have nothing to return. |
| `meta` | object | Yes | No | Per-response metadata (pagination, debug ids). Omit entirely when no metadata applies. |
| `meta.pagination` | object | Yes | No | Present only on paginated list responses. |
| `meta.pagination.page` | int | No | Yes (when `pagination` present) | 1-based current page. |
| `meta.pagination.per_page` | int | No | Yes | Page size used to compute the response. |
| `meta.pagination.total` | int | No | Yes | Total matching records (cross-page). |
| `meta.pagination.last_page` | int | No | Yes | Last page number = `ceil(total / per_page)`. |

### 3.2 Batch idempotency header

Every batched write endpoint (`attendance/push-record`, `attendance-remarks`,
`practical-scores/batch`, `project-scores/batch`, and future
`practitioners/verified-sync/batch`) accepts an `Idempotency-Key` request
header. The mobile client generates a fresh UUIDv4 per batch attempt and
re-uses the same key on retries of that attempt. The server must dedupe
so a retried batch returns the original per-row `results[]` rather than
re-applying — combined with the row-level idempotency on the composite
key, this eliminates both mid-flight duplicates and second-attempt races.

```
POST /api/v1/mobile/attendance/push-record
X-Location: mchst213
Idempotency-Key: 5f3c98e2-8a9d-4a2b-9df1-3e6e6a0c1b21
Content-Type: application/json; charset=UTF-8
```

- **Storage:** the recommended shape is a `(user_id, idempotency_key)` row
  that captures the full response body on first success; subsequent
  requests short-circuit and return that body.
- **Retention:** 24 hours is sufficient — the mobile side gives up long
  before then (see `SyncBatchRunner.MAX_ATTEMPTS`).
- **Absent header:** treat as a request whose retries can double-apply;
  the mobile client always sends the header, so absence usually means a
  third-party caller.

### 3.3 Envelope examples

**Success — single object:**

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "usr_8f3c21a9",
    "name": "Amina Okonkwo"
  }
}
```

**Success — list (non-paginated):**

```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "id": "sch_001", "title": "Cardiology Practical" },
    { "id": "sch_002", "title": "Surgery Project" }
  ]
}
```

**Success — list (paginated):**

```json
{
  "success": true,
  "message": "OK",
  "data": [ /* 20 items */ ],
  "meta": {
    "pagination": { "page": 1, "per_page": 20, "total": 53, "last_page": 3 }
  }
}
```

**Success — empty write:**

```json
{ "success": true, "message": "Accepted.", "data": null }
```

---

## 4. Error Response Format

### 4.1 HTTP status code conventions

| Code | When | Body |
|---|---|---|
| `200 OK` | Successful read or write that returns a payload. | Standard success envelope. |
| `201 Created` | New resource created (POSTs that create rather than upsert). | Standard success envelope with the created resource in `data`. |
| `204 No Content` | Successful write that returns nothing. | **No body.** Mobile treats this as success. |
| `400 Bad Request` | Malformed request (parse error, missing required header). | Standard error envelope. |
| `401 Unauthorized` | Missing or invalid bearer token. | Standard error envelope. **Mobile clears the local token and routes to LoginScreen.** |
| `403 Forbidden` | Authenticated but not authorised for this resource. | Standard error envelope. |
| `404 Not Found` | Resource does not exist. License lookup uses 404 to indicate "no record" — mobile treats this as an empty state, not an error. | Standard error envelope (body optional on license-lookup 404). |
| `409 Conflict` | Idempotency conflict — write would violate a uniqueness constraint a re-send cannot reconcile. | Standard error envelope. |
| `422 Unprocessable Entity` | Validation failed. | Standard error envelope + `errors` field per §4.3. |
| `429 Too Many Requests` | Rate-limited. | Standard error envelope + `Retry-After` header per §1.10. |
| `500 Internal Server Error` | Unhandled server exception. Body may be empty / HTML. | Mobile shows a generic message. |
| `503 Service Unavailable` | Backend in maintenance / unhealthy. | Same as 500. |

### 4.2 Generic error example

```json
{
  "success": false,
  "message": "Invalid email or password.",
  "data": null
}
```

### 4.3 Validation error (422) example

```json
{
  "success": false,
  "message": "The given data was invalid.",
  "data": null,
  "errors": {
    "license_number": ["The license number field is required."],
    "verified_at": ["The verified at must be a valid epoch millis integer."]
  }
}
```

- `errors` (object) — map of `field_name → string[]`. Field names use the same `snake_case` as the request body.
- Each value is an array of one or more messages. The first message is the primary one; mobile may surface only the first.

### 4.4 Mobile handling matrix

| HTTP status | Repository return | UI surface |
|---|---|---|
| `200`, `201` | `Result.Success(data)` | Render data. |
| `204` | `Result.Success(Unit)` | Show toast / route forward. |
| `400`, `422` | `Result.Error(message + errors)` | Inline form errors (422) or generic toast (400). |
| `401` | `Result.Unauthorized` | Clear token, route to `LoginScreen`. |
| `403` | `Result.Error("Not allowed.")` | Toast + nav back. |
| `404` (license lookup) | `Result.Success(null)` | Empty state ("No record found"). |
| `404` (other) | `Result.Error("Not found.")` | Toast + nav back. |
| `409` | `Result.Error(message)` | Toast; sync row marked Failed. |
| `429` | `Result.Error("Slow down.")` | Toast; sync worker schedules a delayed retry honouring `Retry-After`. |
| `5xx` | `Result.Error("Server unavailable.")` | Generic toast; sync row marked Failed. |
| Network failure | `Result.Error("No connection.")` | Generic toast; sync row stays Pending. |

---

## 5. Authentication Module

> **Auth model.** Two parallel login flows exist:
> - **Adhoc** (`/adhoc/login`) — field officers using ad-hoc credentials.
> - **Tutor** (`/login`) — practitioner tutors using their CHPRBN identity.
>
> Both return the same envelope shape (`data.token`) and a corresponding profile endpoint (`/adhoc/profile`, `/dashboard/profile`).

---

### 5.1 Adhoc Login

| Property | Value |
|---|---|
| **Module** | Authentication |
| **Feature** | Field-officer (adhoc) login |
| **Purpose** | Authenticate a field officer and issue a bearer token. |
| **Authentication required** | No |
| **Authorization / roles** | None — public route. |
| **HTTP method** | `POST` |
| **URL path** | `/adhoc/login` |
| **API version** | v1 |
| **Status** | **Existing** |
| **Mobile source** | `AuthApiService.adhocLogin` |

#### Headers

| Name | Type | Required | Description |
|---|---|---|---|
| `Content-Type` | string | Yes | `application/json` |
| `Accept` | string | No | `application/json` (added by interceptor on protected routes — optional here). |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `username` | string | Yes | No | Non-empty, max 100 chars. Server treats as opaque. |
| `password` | string | Yes | No | Non-empty, max 100 chars. |

#### Request Example

```http
POST /api/v1/mobile/adhoc/login
Content-Type: application/json

{
  "username": "field.officer",
  "password": "SecurePass!2025"
}
```

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "Login successful.",
  "data": { "token": "1|aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789ABCDEFG" }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.token` | string | No | Sanctum opaque token. Mobile stores in `AuthTokenStore` + encrypted prefs. |

#### Error Responses

| Code | Cause | Body |
|---|---|---|
| `401` | Bad credentials | `{ "success": false, "message": "Invalid email or password.", "data": null }` |
| `422` | Missing/invalid fields | Standard 422 with `errors.username` / `errors.password`. |
| `429` | Brute-force protection | Standard 429. |

#### Business Rules

- Token issued is single-use **per session** — login again to rotate.
- Mobile does NOT send `Authorization` on this endpoint (`AuthorizationInterceptor` short-circuits on `/login` suffix).
- Failed logins MUST NOT distinguish "user not found" from "wrong password" in the message.

---

### 5.2 Tutor Login

| Property | Value |
|---|---|
| **Module** | Authentication |
| **Feature** | Practitioner-tutor login |
| **Purpose** | Authenticate a CHPRBN-registered practitioner-tutor and issue a bearer token. |
| **Authentication required** | No |
| **Authorization / roles** | None — public route. |
| **HTTP method** | `POST` |
| **URL path** | `/login` (alias: `/auth/login`) |
| **API version** | v1 |
| **Status** | **Existing** (server-side; mobile client does not currently wire this — only adhoc login is in use) |
| **Mobile source** | Reserved — same DTO shape as §5.1 |

Request body, response shape, and error responses are identical to §5.1. The only difference is which user table is checked (`users` vs `adhoc_users`).

---

### 5.3 Logout — Field Officer

| Property | Value |
|---|---|
| **Module** | Authentication |
| **Feature** | Field-officer sign-out |
| **Purpose** | Revoke the current bearer token server-side before the mobile client wipes local session state. Closes the "stolen device with cached token" hole described in §13.7. |
| **Authentication required** | Yes |
| **Authorization / roles** | Adhoc users only (any authenticated adhoc user can log themselves out). |
| **HTTP method** | `POST` |
| **URL path** | `/adhoc/logout` |
| **API version** | v1 |
| **Status** | **To Be Implemented** (backend) — mobile hookup follows once the endpoint lands. |
| **Mobile source** | `AuthApiService.adhocLogout` (planned; wired from `ProfileRepositoryImpl.logout` before the local `SessionCleaner` runs). |

#### Headers

| Name | Type | Required | Description |
|---|---|---|---|
| `Authorization` | string | Yes | `Bearer <token>` — the exact token that is about to be revoked. |
| `Accept` | string | Yes | `application/json` |

#### Request

No query, path, or body parameters. The token to revoke is the one on the `Authorization` header — no `token` field in the body (which would let a caller with token A revoke a colleague's token B).

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "Signed out.",
  "data": null
}
```

`204 No Content` is also acceptable — the mobile client treats any 2xx as success and does not read the body. The envelope shape above is documented so the backend can log a machine-parseable message for audit; it is not consumed by mobile.

#### Effect

- The Sanctum personal-access-token row corresponding to the `Authorization` header is deleted.
- Subsequent authenticated requests with that same token return `401 Unauthorized` (which the mobile interceptor already treats as session-expired — see §13.4).
- Other tokens the same user may have on other devices are **not** affected. This endpoint is per-token, not per-user.

#### Error Responses

| Status | Meaning | Mobile handling |
|---|---|---|
| `401 Unauthorized` | Bearer token was already invalid before the call (expired, previously revoked, or malformed). | Treat as success — the desired end state is "token no longer works," which is already true. The mobile client proceeds to clear local state without alerting the user. |
| `429 Too Many Requests` | Rare — logout is called at most once per session. | Log; proceed to clear local state anyway. Never block the user from signing out on the client. |
| `5xx` | Backend error. | Log; proceed to clear local state anyway. The client-side wipe is authoritative for the local install — a failed server revoke leaves the token usable elsewhere, which is worse than the pre-fix status quo but no worse than not calling this endpoint at all. |

The mobile contract is **best-effort**: the client does not surface any failure to the user, and never blocks logout on the network. Retrying the call would leak the token further; the safer move is to proceed with the local wipe.

#### Sample cURL

```
curl -X POST 'https://app.chprbn.gov.ng/api/v1/mobile/adhoc/logout' \
  -H 'Authorization: Bearer 1|aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789ABCDEFG' \
  -H 'Accept: application/json'
```

### 5.4 Logout — Practitioner Tutor

Identical contract to §5.3 but at `POST /logout` and mirroring the tutor auth flow at `POST /login` (§5.2). Currently no mobile flow triggers it (tutors don't have a mobile logout gesture yet); documented for parity so the backend implementation can ship both variants in one PR.

| Property | Value |
|---|---|
| **HTTP method** | `POST` |
| **URL path** | `/logout` |
| **Authentication required** | Yes (tutor bearer). |
| **Status** | **To Be Implemented** — parity with §5.3. |

---

## 6. Profile Module

### 6.1 Get Adhoc Profile

| Property | Value |
|---|---|
| **Module** | Profile |
| **Feature** | Field-officer profile |
| **Purpose** | Fetch the authenticated adhoc user's profile for display + permission checks. |
| **Authentication required** | Yes |
| **Authorization / roles** | Adhoc users only |
| **HTTP method** | `GET` |
| **URL path** | `/adhoc/profile` |
| **API version** | v1 |
| **Status** | **Existing** |
| **Mobile source** | `AuthApiService.getAdhocProfile` |

#### Headers

| Name | Type | Required | Description |
|---|---|---|---|
| `Authorization` | string | Yes | `Bearer <token>` |
| `Accept` | string | Yes | `application/json` |

#### Request

No query, path, or body parameters.

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "successful",
  "data": {
    "id": 4,
    "name": "Abba El-Muqaddas",
    "email": "abba@chprbn.gov.ng",
    "phone": "08022334455",
    "username": "abba",
    "status": 1,
    "department": "ACC",
    "location": "mchst213",
    "roles": ["Inspector", "Verify Practitioners"]
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.id` | number | Yes | Numeric user id. Mobile stores as `Double` to tolerate serialisation drift. |
| `data.name` | string | No | Display name. |
| `data.email` | string | No | Login email. |
| `data.phone` | string | Yes | Contact phone. Free-format; E.164 preferred but not enforced. |
| `data.username` | string | No | Login handle (same value sent in §5.1). |
| `data.status` | int | Yes | `1` = active, `0` = disabled. Mobile rejects login if `0`. |
| `data.department` | string | Yes | Free-text unit/department code (e.g. `"ACC"`). |
| `data.location` | string | Yes | Free-text location identifier (e.g. `"mchst213"`) — the physical centre / station the officer is assigned to. Mobile stores as `User.location` for display. |
| `data.roles` | string[] | Yes | Role labels assigned to the user (e.g. `["Inspector", "Verify Practitioners"]`). Mobile stores as `User.permissions` and picks the first as `User.role` for display. Backend SHOULD always emit the array — when omitted, mobile maps to `emptyList()`. |

#### Error Responses

| Code | Cause |
|---|---|
| `401` | Missing/invalid token. Mobile clears token + routes to login. |
| `404` | Profile orphaned (token valid but user deleted). |

---

### 6.2 Get Tutor Profile

| Property | Value |
|---|---|
| **Module** | Profile |
| **Feature** | Practitioner-tutor profile |
| **Purpose** | Fetch the authenticated tutor's profile. |
| **Authentication required** | Yes |
| **HTTP method** | `GET` |
| **URL path** | `/dashboard/profile` (alias: `/user`) |
| **Status** | **Existing** |
| **Mobile source** | `VerificationApiService.getProfile` |

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "usr_8f3c21a9",
    "photo": "iVBORw0KGgoAAAANSUhEUgA...",
    "name": "Dr. Sarah Jenkins",
    "gender": "Female",
    "username": "MD-99201-B",
    "email": "sarah@chprbn.gov.ng",
    "phone": "+2348012345678",
    "permissions": ["verify_practitioner", "sync_records"],
    "role": "Practitioner Tutor",
    "unit": "Lagos University Teaching Hospital",
    "lastLoginAt": "2026-05-15T08:30:00Z"
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.id` | string | No | Opaque user id. |
| `data.photo` | string | Yes | Raw Base64 image bytes, **no `data:` prefix**. Mobile normalises via `core/network/ImageUrlNormalization`. |
| `data.name` | string | No | Display name. |
| `data.gender` | string | Yes | Free text. |
| `data.username` | string | No | Login handle (typically the license number). |
| `data.email` | string | No | Login email. |
| `data.phone` | string | Yes | E.164 phone. |
| `data.permissions` | string[] | Yes | Permission codes (see §12.4). Backend SHOULD always emit the array (possibly empty); when omitted, mobile maps to `emptyList()`. |
| `data.role` | string | Yes | Role label. |
| `data.unit` | string | Yes | Unit / hospital. |
| `data.lastLoginAt` | string | Yes | ISO-8601 timestamp. |

---

## 7. Verification Module

### 7.1 Get License Record

| Property | Value |
|---|---|
| **Module** | Verification |
| **Feature** | Practitioner license lookup |
| **Purpose** | Resolve a license number (typed or QR-scanned) to the practitioner's license card data. |
| **Authentication required** | Yes |
| **HTTP method** | `GET` |
| **URL path** | `/practitioners/license` |
| **API version** | v1 |
| **Status** | **Existing** |
| **Mobile source** | `LicenseApiService.getLicenseRecord` |

#### Query Parameters

| Name | Type | Required | Default | Description |
|---|---|---|---|---|
| `license_number` | string | Yes | — | Trimmed registration / license number, e.g. `MD-99201-B`. Case-sensitive. |

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "registration_number": "MD-99201-B",
    "full_name": "Dr. Sarah Jenkins",
    "photo": "iVBORw0KGgoAAAANSUhEUgA...",
    "photo_url": "https://app.chprbn.gov.ng/media/practitioners/md99201b.jpg",
    "profession": "Medical Doctor",
    "certificate_no": "CERT-001",
    "email": "sarah@example.com",
    "phone": "+2348012345678",
    "license_status": "Active",
    "expiry_date": "2026-10-31",
    "subtitle": "General Practice",
    "issue_date": "2024-01-15",
    "gender": "Female",
    "graduation_date": "2010-06-30",
    "institution_attended": { "name": "University of Ibadan, College of Medicine" }
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.registration_number` | string | No | Echoes the query param. |
| `data.full_name` | string | No | Practitioner full name. |
| `data.photo` | string | Yes | Raw Base64 bytes (no `data:` prefix). EITHER `photo` OR `photo_url` should be set, not both. |
| `data.photo_url` | string | Yes | Absolute HTTPS URL to the photo. |
| `data.profession` | string | No | Cadre / specialty. |
| `data.certificate_no` | string | Yes | Issued certificate number. |
| `data.email` | string | Yes | Contact email. |
| `data.phone` | string | Yes | E.164 phone. |
| `data.license_status` | string | No | One of `Active`, `Expired`, `Suspended`, `Revoked` (free text today; should be enumerated). |
| `data.expiry_date` | string | No | ISO-8601 calendar date OR human display string. |
| `data.subtitle` | string | Yes | Free-text secondary line. |
| `data.issue_date` | string | Yes | ISO-8601 calendar date. |
| `data.gender` | string | Yes | Free text. |
| `data.graduation_date` | string | Yes | ISO-8601 calendar date. |
| `data.institution_attended` | object | Yes | Nested institution detail. |
| `data.institution_attended.name` | string | Yes | Institution name. |

#### Error Responses

| Code | Cause | Mobile behaviour |
|---|---|---|
| `200` with `data: null` OR `404` | License number not found | Empty-state UI (`NotFound`). |
| `400` | Missing/empty `license_number` | Generic error toast. |
| `401` | Invalid token | Clear + route to login. |
| `5xx` | Server error | "Server unavailable." |

#### Business Rules

- Lookup is read-only — no side effects.
- The 404 path MAY include an envelope body or be empty; both are treated as "no record."
- `photo` (Base64) vs `photo_url` (HTTPS) is a server convenience; mobile normalises both into a `data:` URI internally so Compose `AsyncImage` can render either.

---

### 7.2 Submit Verified License — per-row (legacy)

> **Deprecated:** this is the per-row template. The replacement is §7.4 (batched). The per-row endpoint stays live during the migration window described in §14.2. New backend deployments SHOULD ship the batched endpoint alongside; mobile will cut over as soon as §7.4 returns 2xx.

| Property | Value |
|---|---|
| **Module** | Verification |
| **Feature** | Verified-license upload (legacy per-row) |
| **Purpose** | Push a locally-recorded verification (officer-confirmed) to the central registry. |
| **Authentication required** | Yes |
| **HTTP method** | `POST` |
| **URL path** | `/practitioners/verified-sync` (alias for adhoc officer: `/adhoc/verified-sync`) |
| **API version** | v1 |
| **Status** | **Existing — Deprecated by §7.4** |
| **Mobile source** | `VerifiedSyncApiService.syncVerifiedLicense` |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `license_number` | string | Yes | No | Must match an existing license record. |
| `verification_location` | string | Yes | No | Free text, max 200 chars. |
| `practitioner_present` | boolean | Yes | No | Officer's "did you see the practitioner" tick. |
| `remark` | string | Yes | No | Free-text remark; max 500 chars. Empty string is invalid. |
| `verified_at` | int64 | Yes | No | **Unix epoch milliseconds, UTC.** |

#### Request Example

```http
POST /api/v1/mobile/practitioners/verified-sync
Content-Type: application/json
Authorization: Bearer 1|aBcDeFg...

{
  "license_number": "MD-99201-B",
  "verification_location": "Lagos University Teaching Hospital — Ward 4",
  "practitioner_present": true,
  "remark": "ID matched; license card presented.",
  "verified_at": 1768515600000
}
```

#### Success Response — `200 OK` or `201 Created`

```json
{
  "success": true,
  "message": "Verification accepted.",
  "data": {
    "id": 7421,
    "license_number": "MD-99201-B",
    "verified_at": 1768515600000
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.id` | int64 | Yes | Server-side row id for the persisted verification. |
| `data.license_number` | string | Yes | Echo. |
| `data.verified_at` | int64 | Yes | Echo (epoch millis). |

`204 No Content` is also acceptable; mobile only reads `response.isSuccessful`.

#### Error Responses

| Code | Body |
|---|---|
| `400`, `422` | `errors.license_number`, `errors.remark`, etc. |
| `401` | Token expired / invalid. |
| `409` | Duplicate `(license_number, verified_at)` — backend should treat as idempotent and return 200. |
| `5xx` | Mobile retains row as Failed; user can re-sync from Sync screen. |

#### Business Rules

- **Idempotency** on `(license_number, verified_at)` is recommended. A second POST with the same tuple should not create a duplicate row.
- Per-row uploads, not batched — mobile sends N sequential POSTs from `SyncBatchRunner`. **This is the load-failure mode** that motivates the batched §7.4 replacement: real campaign sync hits tens of thousands of rows from many devices in a narrow window; the per-row template cannot sustain it.
- On any non-2xx, the row is marked `Failed` locally with `syncError = <message>` and surfaced on the Sync screen for retry.

---

### 7.3 Submit Irregularity Report

| Property | Value |
|---|---|
| **Module** | Verification |
| **Feature** | Officer-reported license irregularity |
| **Purpose** | Submit a flagged license + photo evidence + remark for follow-up. |
| **Authentication required** | Yes |
| **HTTP method** | `POST` |
| **URL path** | `/practitioners/license-irregularity-reports` |
| **API version** | v1 |
| **Status** | **Existing** |
| **Mobile source** | `IrregularityReportApiService.submitIrregularityReport` |
| **Content-Type** | `multipart/form-data` |

#### Multipart Parts

| Part name | Kind | Required | Content-Type | Description |
|---|---|---|---|---|
| `name_on_card` | text | Yes | `text/plain; charset=utf-8` | Practitioner name as printed on the card. |
| `license_number` | text | Yes | `text/plain; charset=utf-8` | Stamped license number. |
| `cadre` | text | Yes | `text/plain; charset=utf-8` | Profession / cadre. |
| `gender` | text | Yes | `text/plain; charset=utf-8` | Free text. |
| `remark` | text | Yes | `text/plain; charset=utf-8` | Reason for reporting; max 1000 chars. |
| `reported_at` | text | Yes | `text/plain; charset=utf-8` | Epoch millis (UTC) as a decimal string. |
| `snapshot` | file | Yes | `image/jpeg` / `image/png` / `image/webp` | Photo of the suspect card. Multipart filename required. Max 5 MiB. |

#### Success Response — `201 Created`

```json
{
  "success": true,
  "message": "Report submitted.",
  "data": {
    "id": 318,
    "license_number": "MD-99201-B",
    "remark": "Card photo does not match the named practitioner."
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.id` | int64 | Yes | Report id. |
| `data.license_number` | string | Yes | Echo. |
| `data.remark` | string | Yes | Echo. |

#### Error Responses

Same set as §7.2. 413 (Payload Too Large) is additionally possible if the snapshot exceeds the server's upload limit.

#### Business Rules

- The snapshot is store-of-record evidence; backend MUST NOT delete it independently of the report row.
- Subsequent reports for the same `license_number` from the same officer should not be deduplicated server-side — each represents an independent observation.

---

### 7.4 Submit Verified Licenses — batch

| Property | Value |
|---|---|
| **Module** | Verification |
| **Feature** | Verified-license upload (batch — replaces §7.2) |
| **Purpose** | Upload N verifications in one HTTP request. One auth check, one DB transaction, per-row results. |
| **Authentication required** | Yes |
| **HTTP method** | `POST` |
| **URL path** | `/practitioners/verified-sync/batch` |
| **API version** | v1 |
| **Status** | **To Be Implemented** |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `items` | `VerifiedSyncItem[]` | Yes | No | 1 ≤ length ≤ 500. |

##### `VerifiedSyncItem`

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `client_id` | string | Yes | No | Stable client-side correlation key (e.g. `"<license_number>:<verified_at>"`). Echoed in the response. **Not** the dedup key. |
| `license_number` | string | Yes | No | Must match an existing license record. |
| `verification_location` | string | Yes | No | Max 200 chars. |
| `practitioner_present` | boolean | Yes | No | |
| `remark` | string | Yes | No | Max 500 chars. |
| `verified_at` | int64 | Yes | No | Epoch millis (UTC). |

#### Request Example

```json
{
  "items": [
    {
      "client_id": "MD-99201-B:1768515600000",
      "license_number": "MD-99201-B",
      "verification_location": "Lagos University Teaching Hospital — Ward 4",
      "practitioner_present": true,
      "remark": "ID matched; license card presented.",
      "verified_at": 1768515600000
    }
  ]
}
```

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "Batch processed.",
  "data": {
    "results": [
      { "client_id": "MD-99201-B:1768515600000", "accepted": true, "server_id": "vrf_7421" }
    ]
  }
}
```

See §10.3 for the shared batch-result schema.

#### Business Rules

- **Per-row idempotency** on `(license_number, verified_at)` — unchanged from §7.2. The server upserts per row inside the batch's transaction.
- **HTTP 200 even on partial failure.** Only malformed envelopes return 4xx.
- **Atomic batch is NOT required** — one bad row must not poison the other 499. Server commits each accepted row.
- **Max batch size 500.** Mobile chunks larger queues client-side.

---

### 7.5 Get Officer Remark Options

| Property | Value |
|---|---|
| **Module** | Verification |
| **Feature** | Officer-remark dropdown choices for the verification form |
| **Purpose** | Serve the canonical list of free-text remarks an officer can pick from when filing a verification. Replaces the previously-hardcoded `R.array.officer_remark_options` string-array on the client. |
| **Authentication required** | Yes |
| **HTTP method** | `GET` |
| **URL path** | `/practitioners/officer-remark-options` |
| **API version** | v1 |
| **Status** | **To Be Implemented** |
| **Mobile source** | `OfficerRemarkOptionsApiService.getOfficerRemarkOptions` |

#### Headers

| Name | Type | Required | Description |
|---|---|---|---|
| `Authorization` | string | Yes | `Bearer <token>` |
| `Accept` | string | Yes | `application/json` |

#### Request

No query, path, or body parameters.

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "options": [
      "Documents verified; identity matches register",
      "Practitioner present; credentials checked",
      "Routine verification completed",
      "License confirmed valid for practice"
    ]
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.options` | string[] | Yes | Ordered list of selectable remark labels. Order is the display order in the dropdown. Backend SHOULD always emit the array (possibly empty); when omitted, mobile falls back to bundled defaults. |

#### Error Responses

| Code | Cause |
|---|---|
| `401` | Invalid token. Mobile keeps bundled defaults visible. |
| `5xx` | Server error. Mobile silently falls back to bundled defaults; the form remains usable. |

#### Business Rules

- **Strings are the contract.** Mobile persists the selected string verbatim into the verified-license remark column and sends it through `verified-sync`. Renaming an option breaks searches over historical remarks.
- **Mobile fallback.** A bundled `R.array.officer_remark_options` ships with the APK (4 entries today). Used on cold start, offline, or any non-2xx; the bundled list is always replaced when the API succeeds with a non-empty array.
- **No client-side caching across launches** today — the fetch runs once per `VerificationFormViewModel` `init`. A persistent cache + revalidation strategy is future work.

---

## 8. Examination Module

> **Status: To Be Implemented.** None of the endpoints below exist server-side yet. All three are wired in the mobile client against speculative paths (`ExamDossierApiService`, `ExamSyncApiService`). See `docs/BACKEND_CONTRACT_AND_CERT_PINNING.md` §2.5 for the backend-team sign-off matrix.

### 8.1 Get Exam Dossier

| Property | Value |
|---|---|
| **Module** | Examination |
| **Feature** | Officer dossier (current centre + today's papers + candidates) |
| **Purpose** | Single-call fetch of everything the officer needs to run today's exam: centre, papers, candidate roster, paper↔candidate assignments. |
| **Authentication required** | No bearer token — see `X-Location` below. |
| **Authorization / roles** | Adhoc officer with an active centre assignment for the current day, identified via `X-Location`. |
| **HTTP method** | `GET` |
| **URL path** | `/attendance/fetch-record` |
| **Base URL** | Exam backend — `https://jarabawa.chprbn.gov.ng/api/v1/mobile/` (§1.3), **not** the main app base URL. |
| **API version** | v1 |
| **Status** | **Confirmed live** — captured from a real device response, 2026-08-14. Diverges substantially from the shape originally speculated here; see below. |
| **Mobile source** | `ExamDossierApiService.fetchDossier` |

#### Headers

| Name | Required | Value |
|---|---|---|
| `Accept` | Yes | `application/json` |
| `x-location` | Yes | The officer's `location` from §6.1 `data.location` (e.g. `"mchst213"`) — the sole request credential on this backend. Attached automatically by the mobile client's `LocationHeaderInterceptor`. |

#### Request

No query, path, or body parameters. Server resolves "which dossier" from `X-Location` (the centre's own `api_key` matches this value — confirmed against a real response).

#### Success Response — `200 OK`

```json
{
  "status": true,
  "message": "Successful",
  "data": {
    "centre": {
      "id": 229,
      "name": "213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY",
      "location": "213 - MK COLLEGE OF HEALTH SCIENCE AND TECHNOLOGY",
      "status": "Active",
      "api_key": "mchst213",
      "sample_token": null
    },
    "papers": [
      { "id": 12, "code": "P1", "name": "PAPER 1" }
    ],
    "schedules": [
      {
        "id": 2,
        "test_date": "Friday, 14th Aug 2026",
        "test_code": "CHEW",
        "test_type": "Regular Exam",
        "paper_candidates": [
          { "candidate_id": 27858, "paper_id": 12, "scheduled_candidate_id": 1 }
        ],
        "candidates": [
          {
            "id": 27858,
            "indexing": "B/213/104/24",
            "fullname": "MUHAMMAD MARYAM ",
            "photo": "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAYE…"
          }
        ]
      }
    ],
    "year": "2026",
    "sections": []
  }
}
```

**Envelope note:** the live envelope flag is `status`, not `success` (mobile accepts both via `alternate`). The centre key is `centre` (British spelling), not `center`.

##### `data.centre` (object, nullable — mobile field name stays `center` internally)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | int | No | Opaque centre id (mobile coerces to string). |
| `name` | string | No | Display name. |
| `location` | string | Yes | Free-text location/address — same value for both `name` and `location` in observed responses. |
| `status` | string | Yes | e.g. `"Active"`. Not currently consumed by mobile. |
| `api_key` | string | Yes | Matches the `X-Location` value the officer's `adhoc/profile` returns — this is how the server resolves "which centre" without a bearer token. Not currently consumed by mobile beyond auth. |
| `sample_token` | string | Yes | Observed `null`. Purpose unconfirmed. |

No `code` or `hero_image_url` field exists on the wire (both fields in the mobile `Center` domain model default to empty/`null` for this endpoint).

##### `data.papers` (array, may be empty)

Minimal catalogue of papers for this centre — **not** scoped per schedule.

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | int | No | Opaque paper id (mobile coerces to string). Referenced by `schedules[].paper_candidates[].paper_id`. |
| `code` | string | Yes | Short code, e.g. `"P1"`. Mapped to the mobile `Paper.subtitle`. |
| `name` | string | Yes | Display name, e.g. `"PAPER 1"`. Mapped to the mobile `Paper.title`. |

No `center_id`, `paper_kind`, `start_at`/`end_at`, `hall`, or `total_candidates` field exists on the wire. Mobile derives `total_candidates` by counting that paper's rows across every schedule's `paper_candidates[]`; `paper_kind`/timing/`hall` have no wire signal and fall back to mapper defaults (`Theory`, `0`, `0`, `""`).

##### `data.schedules` (array, may be empty)

One exam sitting for this centre — a date + qualification code, carrying its own nested candidate roster and paper assignments. **This is where the actual candidate/assignment data lives** — there is no top-level `data.candidates` or `data.assignments`.

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | int | No | Opaque schedule id. |
| `test_date` | string | Yes | Formatted display date, e.g. `"Friday, 14th Aug 2026"` — **not** epoch millis. Not currently parsed by mobile. |
| `test_code` | string | Yes | Qualification/cadre code, e.g. `"CHEW"`. Not currently consumed by mobile. |
| `test_type` | string | Yes | e.g. `"Regular Exam"`. Not currently consumed by mobile. |
| `paper_candidates` | array | Yes | The candidate↔paper join for this schedule — see below. |
| `candidates` | array | Yes | Full candidate roster for this schedule — see §10.1. |

###### `schedules[].paper_candidates[]` (paper↔candidate assignment)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `candidate_id` | int | No | FK → a `schedules[].candidates[].id` (any schedule). Mobile coerces to string. |
| `paper_id` | int | No | FK → `data.papers[].id`. Mobile coerces to string. |
| `scheduled_candidate_id` | int | Yes | Purpose unconfirmed; not currently consumed by mobile. |

Mobile flattens `paper_candidates[]` across every schedule into one assignment list, and every `candidates[]` array across every schedule into one deduplicated candidate list (dedup key: `id` — the same person can appear in more than one schedule).

#### Error Responses

| Code | Cause |
|---|---|
| `401` | Invalid `X-Location`. |
| `403` | Officer has no centre assignment for today. |
| `404` | Same as 403 (alternative) — empty-state UI. |
| `5xx` | Generic. |

#### Business Rules

- Idempotent and side-effect-free.
- The same dossier MAY change throughout the day (re-issued papers, added candidates) — mobile re-fetches on pull-to-refresh.
- Empty `papers`, `schedules`, or a schedule's `candidates`/`paper_candidates` are all valid (centre with no scheduled work today).

#### Related entities

`centres`, `papers`, `schedules`, `paper_candidates` (join), candidates nested per schedule.

---

### 8.2 Submit Attendance — batch

| Property | Value |
|---|---|
| **Module** | Examination |
| **Feature** | Attendance sync (batch) |
| **Purpose** | Upload N attendance rows in one HTTP request. One auth check + one DB transaction per batch. |
| **Authentication required** | No bearer token — see `X-Location` below. |
| **HTTP method** | `POST` |
| **URL path** | `/attendance/push-record` |
| **Base URL** | Exam backend — `https://jarabawa.chprbn.gov.ng/api/v1/mobile/` (§1.3), **not** the main app base URL. |
| **Status** | **To Be Implemented** |
| **Mobile source** | `ExamSyncApiService.uploadAttendanceBatch` |

#### Headers

| Name | Required | Value |
|---|---|---|
| `Idempotency-Key` | Yes | See §3.2. |
| `x-location` | Yes | The officer's `location` from §6.1 `data.location` — the sole request credential on this backend. Attached automatically by the mobile client's `LocationHeaderInterceptor`. |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `items` | `AttendanceSyncItem[]` | Yes | No | 1 ≤ length ≤ 500. |

##### `AttendanceSyncItem`

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `client_id` | string | Yes | No | Stable client-side correlation key — mobile sends `"<paper_id>:<candidate_id>"`. Echoed in the response. **Not** the dedup key. |
| `paper_id` | string | Yes | No | Must match a paper the officer's centre owns. |
| `candidate_id` | string | Yes | No | Must be assigned to `paper_id`. |
| `status` | enum string | Yes | No | One of `signed_in`, `signed_out`, `flagged`. |
| `marked_at` | int64 | Yes | No | Epoch millis (UTC). |

#### Request Example

```json
{
  "items": [
    {
      "client_id": "pap_001:can_001",
      "paper_id": "pap_001",
      "candidate_id": "can_001",
      "status": "signed_in",
      "marked_at": 1768502400000
    },
    {
      "client_id": "pap_001:can_002",
      "paper_id": "pap_001",
      "candidate_id": "can_002",
      "status": "signed_in",
      "marked_at": 1768502420000
    }
  ]
}
```

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "Batch processed.",
  "data": {
    "results": [
      { "client_id": "pap_001:can_001", "accepted": true,  "server_id": "att_aaaa1111" },
      { "client_id": "pap_001:can_002", "accepted": false, "error": "candidate not assigned to paper" }
    ]
  }
}
```

See §10.3 for the shared batch-result schema.

#### Error Responses

| Code | Cause |
|---|---|
| `400` / `422` | Malformed envelope, empty `items`, batch too large. |
| `401` | Invalid token. |
| `403` | Officer's scope mismatch on the *whole batch* (e.g. wrong centre). Per-row scope errors return HTTP 200 with `accepted: false` for that row. |

#### Business Rules

- **Per-row idempotency on `(paper_id, candidate_id)`** — a duplicate row inside or across batches REPLACES.
- **HTTP 200 even on partial failure.** One bad row must not poison the other 499.
- **`client_id` is correlation-only.** Server MUST NOT dedup on it; composite identity is the dedup key.
- The latest `marked_at` wins on conflicting concurrent writes.
- Backend MUST validate `marked_at` is within ±24 hours of "now"; bad rows return `accepted: false` per row, never poison the batch.

---

### 8.3 Submit Candidate Remarks — batch

| Property | Value |
|---|---|
| **Module** | Examination |
| **Feature** | Officer remarks on candidates (batch) |
| **Purpose** | Upload N remarks in one HTTP request. |
| **Authentication required** | No bearer token — see `X-Location` below. |
| **HTTP method** | `POST` |
| **URL path** | `/attendance-remarks` |
| **Base URL** | Exam backend — `https://jarabawa.chprbn.gov.ng/api/v1/mobile/` (§1.3), **not** the main app base URL. |
| **Status** | **To Be Implemented** |
| **Mobile source** | `ExamSyncApiService.uploadRemarkBatch` |

#### Headers

| Name | Required | Value |
|---|---|---|
| `Idempotency-Key` | Yes | See §3.2. |
| `x-location` | Yes | The officer's `location` from §6.1 `data.location` — the sole request credential on this backend. Attached automatically by the mobile client's `LocationHeaderInterceptor`. |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `items` | `RemarkSyncItem[]` | Yes | No | 1 ≤ length ≤ 500. |

##### `RemarkSyncItem`

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `client_id` | string | Yes | No | Mobile sends the remark's `id` (already a client-generated UUID) verbatim — no separate correlation key needed. |
| `id` | string | Yes | No | Client-generated UUID v4. Used for **server-side idempotency**. |
| `candidate_id` | string | Yes | No | Must be in officer's scope. |
| `paper_id` | string | No | Yes | Pin remark to a specific paper. Null = "general." |
| `body` | string | Yes | No | Max 1000 chars. |
| `severity` | enum string | Yes | No | One of `info`, `warning`, `critical`. |
| `created_at` | int64 | Yes | No | Epoch millis (UTC). |

#### Request Example

```json
{
  "items": [
    {
      "client_id": "3b1e4f02-9c7a-4a01-9d3a-1aef0c00b6f4",
      "id": "3b1e4f02-9c7a-4a01-9d3a-1aef0c00b6f4",
      "candidate_id": "can_001",
      "paper_id": "pap_001",
      "body": "Arrived 20 minutes late; no documents.",
      "severity": "warning",
      "created_at": 1768502600000
    }
  ]
}
```

#### Success Response — `200 OK`

Same shape as §8.2 — `{ data: { results: [{ client_id, accepted, server_id?, error? }] } }`. See §10.3.

#### Error Responses

Same as §8.2.

#### Business Rules

- **Idempotency on `id` (client UUID)** — re-send REPLACES the existing remark with the new body/severity.
- **HTTP 200 even on partial failure.** Per-row results in the response.
- Remarks are **not** soft-deletable from mobile.

---

## 9. Assessment Module

> **Status: To Be Implemented.** The single endpoint below (§9.1) doesn't
> exist server-side yet. See `docs/BACKEND_CONTRACT_AND_CERT_PINNING.md` §2.5.
>
> **Schedule discovery is not a dedicated endpoint.** Assessment schedules
> are the PE (`paper_kind = "practical"`) + PA (`paper_kind = "project"`)
> subset of the exam dossier (§8.1). Mobile filters the dossier's
> `data.papers[]` in the assessment repository —
> `AssessmentScheduleRepositoryImpl.getSchedules` reads
> `ExamPaperRepository.getAssessmentPapers()` and adapts each PE/PA
> paper into an `AssessmentSchedule` with an aggregate `syncStatus`
> derived from the local score tables. Backend only needs to ensure the
> dossier already emits these PE/PA rows.

### 9.1 Get Schedule Package

| Property | Value |
|---|---|
| **Module** | Assessment |
| **Feature** | Schedule package download |
| **Purpose** | Single-call fetch of a schedule's full reference data: paper details, practical sections, scored questions, candidate roster, assignments. |
| **Authentication required** | Yes |
| **HTTP method** | `GET` |
| **URL path** | `/assessments/schedules/{schedule_id}/package` |
| **Status** | **To Be Implemented** |
| **Mobile source** | `AssessmentPackageApiService.fetchPackage` |

#### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| `schedule_id` | string | Yes | Matches the exam dossier's `data.papers[].id` for a PE/PA paper (§8.1). Mobile derives the assessment schedules list from the dossier — there is no dedicated `/assessments/schedules` list endpoint. |

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "paper": {
      "schedule_id": "sch_001",
      "title": "Cardiology Practical — Cohort A",
      "status_label": "Active",
      "facility_name": "Lagos State Teaching Hospital",
      "facility_address": "12 Awolowo Way, Ikeja",
      "hall_name": "Skills Lab 2",
      "hall_address": "Block C, Level 1",
      "hero_image_url": "https://app.chprbn.gov.ng/media/papers/sch_001.jpg"
    },
    "sections": [
      { "id": "sec_001", "schedule_id": "sch_001", "title": "History Taking", "subtitle": "10 mins", "ordering": 1 }
    ],
    "questions": [
      { "id": "qst_001", "section_id": "sec_001", "number": 1, "prompt": "Greets the patient appropriately.", "image_url": null, "max_score": 5 }
    ],
    "candidates": [
      { "id": "can_001", "exam_number": "EX-2026-00001", "full_name": "John Adebayo", "photo_url": "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAYE…" }
    ],
    "assignments": [
      { "schedule_id": "sch_001", "candidate_id": "can_001" }
    ]
  }
}
```

##### `data.paper` (object, required)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `schedule_id` | string | No | Echo of path param. |
| `title` | string | No | Display title. |
| `status_label` | string | Yes | UI badge (e.g. `Active`, `Pending`). |
| `facility_name` | string | Yes | Facility hosting the paper. |
| `facility_address` | string | Yes | Postal address. |
| `hall_name` | string | Yes | Hall label. |
| `hall_address` | string | Yes | Hall location detail. |
| `hero_image_url` | string | Yes | Absolute HTTPS URL. |

##### `data.sections` (array of `PracticalSection`, may be empty for project-only papers)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | string | No | Opaque section id. |
| `schedule_id` | string | No | FK → `data.paper.schedule_id`. |
| `title` | string | No | Section title. |
| `subtitle` | string | Yes | Optional second line. |
| `ordering` | int | Yes | 1-based display order. |

##### `data.questions` (array of `SectionQuestion`)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `id` | string | No | Opaque question id. |
| `section_id` | string | No | FK → `data.sections[].id`. |
| `number` | int | No | 1-based number within section. |
| `prompt` | string | No | Question text. |
| `image_url` | string | Yes | Optional illustration URL. |
| `max_score` | int | No | Inclusive upper bound on the score (`0 ≤ score ≤ max_score`). |

##### `data.candidates` (array of `Candidate`)

See §10.1.

##### `data.assignments` (array of schedule↔candidate links)

| Field | Type | Nullable | Description |
|---|---|---|---|
| `schedule_id` | string | No | Echo. |
| `candidate_id` | string | No | FK → `data.candidates[].id`. |

#### Error Responses

| Code | Cause |
|---|---|
| `401` | Invalid token. |
| `403` | Officer not assigned to this schedule. |
| `404` | `schedule_id` not found. |

#### Business Rules

- The whole package is delivered in one transaction so the mobile client can persist it under `db.withTransaction` and never end up half-populated.
- For project-only papers, `sections` + `questions` are empty arrays.
- A re-download of the same schedule REPLACES reference rows; locally-pending scores are **never** touched by the replace.

---

### 9.2 Submit Practical Scores — batch

| Property | Value |
|---|---|
| **Module** | Assessment |
| **Feature** | Per-question practical scoring (batch) |
| **Purpose** | Upload N `(candidate × question)` scores in one HTTP request. |
| **Authentication required** | Yes |
| **HTTP method** | `POST` |
| **URL path** | `/assessments/practical-scores/batch` |
| **Status** | **To Be Implemented** |
| **Mobile source** | `AssessmentSyncApiService.uploadPracticalScoreBatch` |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `items` | `PracticalScoreSyncItem[]` | Yes | No | 1 ≤ length ≤ 500. |

##### `PracticalScoreSyncItem`

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `client_id` | string | Yes | No | Mobile sends `"<schedule_id>:<candidate_id>:<question_id>"`. Echoed in response; not a dedup key. |
| `schedule_id` | string | Yes | No | Must be in officer's scope. |
| `candidate_id` | string | Yes | No | Must be assigned to `schedule_id`. |
| `question_id` | string | Yes | No | Must belong to a section under `schedule_id`. |
| `score` | int | Yes | No | `0 ≤ score ≤ question.max_score`. |
| `scored_at` | int64 | Yes | No | Epoch millis (UTC). |

#### Request Example

```json
{
  "items": [
    {
      "client_id": "sch_001:can_001:qst_001",
      "schedule_id": "sch_001",
      "candidate_id": "can_001",
      "question_id": "qst_001",
      "score": 4,
      "scored_at": 1768503000000
    }
  ]
}
```

#### Success Response — `200 OK`

```json
{
  "success": true,
  "message": "Batch processed.",
  "data": {
    "results": [
      { "client_id": "sch_001:can_001:qst_001", "accepted": true, "server_id": "psc_aaaa1111" }
    ]
  }
}
```

See §10.3 for the shared batch-result schema.

#### Error Responses

Same set as §8.2. A `score > question.max_score` returns HTTP 200 with `accepted: false` and `error: "score exceeds max_score"` for that row.

#### Business Rules

- **Per-row idempotency on `(schedule_id, candidate_id, question_id)`** — duplicate row REPLACES.
- Score storage must be integer; never float.
- **HTTP 200 even on partial failure.**

---

### 9.3 Submit Project Scores — batch

| Property | Value |
|---|---|
| **Module** | Assessment |
| **Feature** | Per-candidate project scoring (batch) |
| **Purpose** | Upload N `(candidate × project paper)` scores in one HTTP request. |
| **Authentication required** | Yes |
| **HTTP method** | `POST` |
| **URL path** | `/assessments/project-scores/batch` |
| **Status** | **To Be Implemented** |
| **Mobile source** | `AssessmentSyncApiService.uploadProjectScoreBatch` |

#### Request Body Schema

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `items` | `ProjectScoreSyncItem[]` | Yes | No | 1 ≤ length ≤ 500. |

##### `ProjectScoreSyncItem`

| Field | Type | Required | Nullable | Validation |
|---|---|---|---|---|
| `client_id` | string | Yes | No | Mobile sends `"<schedule_id>:<candidate_id>"`. Echoed in response; not a dedup key. |
| `schedule_id` | string | Yes | No | |
| `candidate_id` | string | Yes | No | |
| `score` | number (double) | Yes | No | `0 ≤ score ≤ max_score`. Max 2 decimal places. |
| `max_score` | int | Yes | No | Denominator. `> 0`. |
| `scored_at` | int64 | Yes | No | Epoch millis (UTC). |

#### Request Example

```json
{
  "items": [
    {
      "client_id": "sch_002:can_001",
      "schedule_id": "sch_002",
      "candidate_id": "can_001",
      "score": 78.50,
      "max_score": 100,
      "scored_at": 1768589000000
    }
  ]
}
```

#### Success Response — `200 OK`

Same shape as §9.2.

#### Error Responses

Same set as §9.2.

#### Business Rules

- **Per-row idempotency on `(schedule_id, candidate_id)`** — duplicate row REPLACES.
- Backend MUST round-trip `score` with full precision; mobile formats display via `String.format("%.2f", …)`.
- **HTTP 200 even on partial failure.**

---

## 10. Cross-Endpoint Data Models

### 10.1 `Candidate` (shared by §8.1, §9.1)

Cross-feature candidate identity. The exam dossier and the assessment package use nearly the same shape so the local Room `candidates` table holds one canonical row per person — **but the exam dossier's live field names differ from the assessment package's**, confirmed against a real device response (2026-08-14):

| Field | Exam dossier (`/attendance/fetch-record`, live) | Assessment package (`/assessments/.../package`, speculative) | Type | Description |
|---|---|---|---|---|
| id | `id` | `id` | int/string | Opaque candidate id. Mobile coerces the exam dossier's numeric id to string. Stable across features. |
| exam number | `indexing` (e.g. `"B/213/104/24"`) | `exam_number` | string | User-visible identifier ("indexing number" in assessment UI). What QR scans resolve to. |
| full name | `fullname` (no underscore) | `full_name` | string | Display name. |
| photo | `photo` | `photo_url` | string, nullable | **Raw Base64 image bytes (no `data:` prefix).** Despite the assessment-side field name, both endpoints embed the photo inline rather than serving a URL — backend keeps the dossier self-contained for offline use after a single download. Mobile routes the value through `core/network/ImageUrlNormalization.normalizeApiPhotoToDataUri` which wraps it as `data:image/jpeg;base64,…` for Compose `AsyncImage`. Values that already begin with `data:image` pass through unchanged. |

Mobile's `CandidateDto` accepts both sets of keys via Gson `alternate` so either backend shape parses correctly — see `feature/exam/data/dto/CandidateDto.kt`.

On the exam dossier, candidates arrive nested per-schedule (`data.schedules[].candidates[]`), not as a flat top-level array — see §8.1.

**Invariant:** for any candidate served by both `/attendance/fetch-record` and `/assessments/schedules/{id}/package`, the `id`, exam number, and full name MUST be identical. Mobile has a unit test (`CandidateInvariantTest`) that enforces this on the client; the backend SHOULD enforce it at the data layer.

### 10.2 Enums

See §2.7 for the canonical wire-value table. Repeated here for visibility:

| Enum | Wire values |
|---|---|
| `PaperKind` | `theory`, `practical`, `project` (legacy: `PE`, `PA`) |
| `AttendanceStatus` | `signed_in`, `signed_out`, `flagged` |
| `RemarkSeverity` | `info`, `warning`, `critical` |
| `SyncStatus` (mobile-only) | `pending`, `synced`, `failed` |

### 10.3 Sync-write batch response shape

Every write endpoint in the verification (§7.4) + exam (§8.2, §8.3) + assessment (§9.2, §9.3) modules returns the same shape:

```json
{
  "success": true,
  "message": "Batch processed.",
  "data": {
    "results": [
      { "client_id": "<echoed>", "accepted": true,  "server_id": "<opaque>" },
      { "client_id": "<echoed>", "accepted": false, "error": "<reason>" }
    ]
  }
}
```

| Field | Type | Nullable | Description |
|---|---|---|---|
| `data.results` | array | No | One entry per submitted item. Ordering is **not** guaranteed to match the request order — clients MUST match on `client_id`. |
| `data.results[].client_id` | string | Yes | Echo of the `client_id` the client sent. Used by the mobile worker to map results back to local outbox rows. |
| `data.results[].accepted` | boolean | No | `true` if the server persisted the row, `false` otherwise. |
| `data.results[].server_id` | string | Yes | Opaque server-side row id. Present when `accepted: true`. |
| `data.results[].error` | string | Yes | Human-readable rejection reason. Present when `accepted: false`. |

Rules:

- **HTTP 200** even when individual rows fail. The batch was *processed*; per-row outcomes are reported in the body. Only malformed envelopes / batch-size violations return 4xx.
- **`client_id` is correlation-only.** Server MUST NOT use it as a dedup key — dedup is per-row composite identity (e.g. `(paper_id, candidate_id)`).
- **No partial-batch rollback.** Accepted rows commit; rejected rows do not affect them.
- **Result coverage is N rows = N results.** Server returns one result per request item, even if multiple share a composite key (last-write-wins on the server).

This uniformity keeps the `SyncEntityHandler` contract narrow: every handler can rely on the same envelope.

### 10.4 Error payload

| Field | Type | Nullable | Description |
|---|---|---|---|
| `success` | boolean | No | Always `false`. |
| `message` | string | No | Human-readable error message. |
| `data` | null | Yes | Always `null` on error. |
| `errors` | `Record<string, string[]>` | Yes | Present on 422; one key per invalid field. |

---

## 11. API Dependency Mapping

### 11.1 Authentication module

```
POST /adhoc/login     ──▶ data.token ──┐
                                       │
POST /login           ──▶ data.token ──┤
                                       ▼
                            stored in AuthTokenStore
                                       │
                                       ▼
            sent on every other request as `Authorization: Bearer <token>`
```

### 11.2 Verification module

```
GET /practitioners/license?license_number=X
            │
            ▼  (officer reviews + verifies)
POST /practitioners/verified-sync/batch          (N verifications per request — §7.4)
            │
            └─► (if officer flags irregularity)
POST /practitioners/license-irregularity-reports  (one per report — multipart, per-row)
```

### 11.3 Examination module

All three calls below run against the **exam backend** (jarabawa host, §1.3)
and carry `X-Location` instead of the bearer token from `/adhoc/login` — the
login call is shown only to establish `data.location` via `/adhoc/profile`.

```
POST /adhoc/login ──▶ token (main backend)
            │
            ▼
GET  /adhoc/profile ──▶ data.location
            │
            ▼
GET  /attendance/fetch-record   (X-Location, no bearer — exam backend)
            │
            ├─▶ data.center
            ├─▶ data.papers[]            ──┐
            ├─▶ data.candidates[]         │  (officer marks attendance, adds remarks)
            └─▶ data.assignments[]        │
                                          ▼
POST /attendance/push-record  (X-Location, no bearer — items: N attendance rows per request)
POST /attendance-remarks      (X-Location, no bearer — items: N remark rows per request)
```

### 11.4 Assessment module

```
POST /adhoc/login ──▶ token
            │
            ▼
GET  /assessments/schedules
            │
            ▼  (officer picks a schedule)
GET  /assessments/schedules/{schedule_id}/package
            │
            ├─▶ data.paper
            ├─▶ data.sections[]    ─┐
            ├─▶ data.questions[]   │  (officer scores)
            ├─▶ data.candidates[]  │
            └─▶ data.assignments[] │
                                   ▼
POST /assessments/practical-scores/batch  (items: N (candidate, question) scores per request)
POST /assessments/project-scores/batch    (items: N project scores per request)
```

### 11.5 State transitions

| Entity | Local state transitions | Trigger |
|---|---|---|
| `VerifiedLicense` | `Pending → Synced` / `Pending → Failed → Pending → …` | `/practitioners/verified-sync` 2xx / non-2xx; mobile retry. |
| `Attendance` | `Pending → Synced` / `Pending → Failed → …` | `/exam/attendance`. |
| `Remark` | `Pending → Synced` / `Pending → Failed → …` | `/exam/remarks`. |
| `PracticalScore` | `Pending → Synced` / `Pending → Failed → …` | `/assessments/practical-scores`. |
| `ProjectScore` | `Pending → Synced` / `Pending → Failed → …` | `/assessments/project-scores`. |

Server-side transitions are out of scope for this document.

---

## 12. Security Considerations

### 12.1 Authentication flow

```
1. User opens app (cold) → SplashScreen
2. If AuthTokenStore.peekToken() is valid → route to home
3. Else → LoginScreen
4. POST /adhoc/login (or /login) → 200 → data.token stored:
     - in-memory AuthTokenStore
     - encrypted Room (auth.db, SQLCipher-encrypted)
     - persisted via EncryptedSharedPreferences key
5. Subsequent requests carry Authorization: Bearer <token>
6. On 401 from any protected endpoint:
     - Clear AuthTokenStore + DAO row
     - Route to LoginScreen
```

### 12.2 Token lifecycle

- **TTL:** server-controlled. Mobile does not assume any TTL.
- **Refresh:** none. Token expiry = re-login.
- **Revocation:** client-initiated via `POST /adhoc/logout` / `POST /logout` (§5.3 + §5.4) — mobile calls this before local wipe on the logout gesture. Server may also revoke unilaterally by deleting the Sanctum row (admin action, compromised-token response). Either way, the next authenticated request returns 401 and mobile treats it as session-expired.
- **Storage at rest:** Bearer token persists inside SQLCipher-encrypted Room (`auth.db`) and a separate EncryptedSharedPreferences file. Both encrypt via a 256-bit random key generated by `DatabaseKeyProvider` and stored in EncryptedSharedPreferences.

### 12.3 Role-based access control

| Role | Endpoints |
|---|---|
| **Field officer (adhoc)** | `/adhoc/login`, `/adhoc/logout`, `/adhoc/profile`, `/practitioners/license`, `/practitioners/verified-sync`, `/practitioners/license-irregularity-reports`, `/exam/*`, `/assessments/*` |
| **Practitioner tutor** | `/login`, `/logout`, `/dashboard/profile`, `/practitioners/license` (read-only). |
| **Public** | `/login`, `/adhoc/login`. |

Mobile does NOT enforce RBAC client-side; it reacts to 403 by surfacing a generic "Not allowed" toast. Backend MUST enforce.

### 12.4 Permission matrix (mobile-visible)

| Permission code | Granted to | Required for |
|---|---|---|
| `verify_practitioner` | adhoc, tutor | `/practitioners/verified-sync` |
| `sync_records` | adhoc | the entire Sync screen |
| `report_irregularity` | adhoc | `/practitioners/license-irregularity-reports` |
| `mark_attendance` | adhoc (with active centre assignment for today) | `/exam/attendance` |
| `score_assessment` | adhoc (with active schedule assignment) | `/assessments/*-scores` |

Permission codes are returned in `data.permissions` on the profile endpoints (§6).

### 12.5 Sensitive data handling

| Data | Handling |
|---|---|
| **Bearer token** | Encrypted at rest (SQLCipher + EncryptedSharedPreferences). Never logged. Never echoed in error messages. |
| **Practitioner PII** (name, license number, photo) | Encrypted at rest inside SQLCipher-encrypted `scan.db`. Cleared on logout. |
| **Photos** | Either inline Base64 or an HTTPS URL. Backend MUST serve only over HTTPS. |
| **Cleartext traffic** | Disabled app-wide via `network_security_config.xml`. |
| **Certificate pinning** | Scaffolded in `network_security_config.xml` with the `<pin-set>` block commented out pending ops fingerprints (see `docs/BACKEND_CONTRACT_AND_CERT_PINNING.md` §6). |

---

## 13. Open Issues and Gaps

### 13.1 Inconsistent response envelope

Existing endpoints emit `status` (boolean); the standardised name going forward is `success`. Mobile DTOs currently use `status`. **Migration plan in §14.1.**

### 13.2 Inconsistent field-name casing

| Endpoint | Casing today | Should be |
|---|---|---|
| `/adhoc/login`, `/adhoc/profile`, `/dashboard/profile` | mixed (`name`, `lastLoginAt`, `userPhoto`) | `snake_case` (`name`, `last_login_at`, `user_photo`) |
| `/practitioners/license` response | snake_case (correct) | snake_case |
| Sync write endpoints | snake_case (correct) | snake_case |

Mobile already tolerates both via `@SerializedName` overrides; backend should converge.

### 13.3 Photo-field duality

`/practitioners/license` returns EITHER `photo` (Base64) OR `photo_url` (HTTPS). The mobile mapper handles both. **Decision needed:** standardise on `photo_url` and never emit Base64 inline; the latter doubles payload size and prevents lazy loading.

### 13.4 No refresh-token flow

Documented in §1.4. **Decision needed:** acceptable for v1 or required before assessment-day usage (where a token expiry mid-grading would be disastrous)?

### 13.5 No server-side pagination

Not yet a problem; will be a problem the first time an officer is assigned to a centre with > ~500 candidates. **Convention in §1.7** is ready when needed.

### 13.6 No rate-limiting headers consumed

Mobile blindly retries failed POSTs from the sync worker. A 429 today would burn cycles. **Fix:** consume `Retry-After` in `SyncBatchRunner`. Cheap.

### 13.7 Server-side logout not yet implemented

Mobile currently clears local state on logout but does not revoke the server-side token. **Risk:** stolen device with cached token can be used against the API until the token's natural expiry. **Fix:** endpoints specced at §5.3 (`POST /adhoc/logout`) + §5.4 (`POST /logout`); mobile hookup follows.

### 13.8 Validation gaps

| Endpoint | Gap |
|---|---|
| `/practitioners/verified-sync` | No documented max length on `remark`. Mobile sends arbitrary length. |
| `/exam/attendance` | `marked_at` should be validated against a `now ± 24h` window. |
| `/exam/remarks` | `id` (client UUID) should be validated as RFC 4122 v4. |
| `/assessments/practical-scores` | `score > max_score` should be 422, not silently truncated. |

### 13.9 Security concerns

- **No SPKI pinning** — covered in `docs/BACKEND_CONTRACT_AND_CERT_PINNING.md` §6.
- **Token in EncryptedSharedPreferences** is good but a rooted device defeats it; backend should still detect anomalous IPs / impossible-travel patterns.
- **Multipart upload (irregularity report)** has no integrity hash — a corrupted snapshot lands silently. Optional `X-Content-SHA256` header recommended.

### 13.10 Naming

| Confusing | Suggested |
|---|---|
| `exam_number` (assessment) vs `examNumber` (exam dossier) | snake_case both → `exam_number`. |
| `paper_kind` is duplicated on `Paper` and `Schedule` | keep both; they describe different domain entities. |
| `/practitioners/verified-sync` is verb-y | leave as-is for compat; new endpoints use noun paths (`/exam/attendance`, etc). |

---

## 14. Migration Notes

### 14.1 Envelope rename `status → success` (in progress)

The canonical envelope flag is `success` (boolean). Mobile prefers `success` but **also accepts the legacy `status` key** as an alias — every read envelope carries `@SerializedName(value = "success", alternate = ["status"])` so a backend that still emits `"status": true` decodes cleanly. Backend can migrate on its own schedule; once every endpoint emits `success`, mobile can drop the alias in a follow-up release.

**Do not remove the shim from the DTOs without first confirming every response envelope (auth, profile, verification, exam, assessment, batch writes) is emitting `"success"`.** The regression test at [`AuthApiServiceTest.adhocLogin tolerates legacy status flag during backend migration`](../../app/src/test/java/ng/com/chprbn/mobile/feature/auth/data/api/AuthApiServiceTest.kt) pins the tolerant behaviour for the login envelope.

### 14.2 New-endpoint rollout order

1. **First:** `/attendance/fetch-record` + `/attendance/push-record` + `/attendance-remarks` — exam day blocks on these. The dossier response must already include PE/PA papers (`paper_kind = "practical" | "project"`) — mobile filters them into the assessment side, no separate schedules endpoint.
2. **Second:** `/assessments/schedules/{id}/package` — assessment day needs the per-schedule reference data (sections, questions, candidates).
3. **Third:** `/assessments/practical-scores/batch` + `/assessments/project-scores/batch` — scoring is the write half of the assessment flow.
4. **Fourth (verification cutover):** `/practitioners/verified-sync/batch` — replaces the legacy per-row endpoint (§7.2). Both run side-by-side during the cutover window so existing client builds keep working.

The mobile client already wires Composite remote sources that prefer the live API and fall back to in-memory fakes (`Fake*RemoteSource`) so the UI keeps working through the backend rollout. Once an endpoint returns real data with a `200 + non-null data`, the Fake silently stops serving — no client redeploy required.

### 14.3 Suggested PR sequence (backend)

1. PR #1 — Ensure every response envelope emits `success` (boolean). Mobile no longer reads `status`; the field can be dropped from responses whenever convenient.
2. PR #2 — `POST /adhoc/logout` and `POST /logout` token revocation (§5.3 + §5.4).
3. PR #3 — `GET /attendance/fetch-record` (read-only; simplest to land first).
4. PR #4 — `POST /attendance/push-record` + `POST /attendance-remarks` (batched idempotent writes; per-row results).
5. PR #5 — `GET /assessments/schedules/{id}/package` (assessment-side per-schedule reference data — sections, questions, candidates). No standalone schedules list endpoint is needed: mobile derives the schedules list from the PE/PA rows in the exam dossier.
6. PR #6 — `POST /assessments/practical-scores/batch` + `POST /assessments/project-scores/batch` (batched idempotent writes).
7. PR #7 — `POST /practitioners/verified-sync/batch` (legacy verified-sync replacement).
8. PR #8 — Remove the per-row `/practitioners/verified-sync` route; bump to `v1.1`. (Legacy `status` field already dropped in the mobile client — backend may stop emitting it whenever convenient.)

### 14.4 Contract tests

Each backend PR must ship with a contract test that:

1. Posts a known sample request (from this doc's "Request Example" block).
2. Asserts the response matches the documented `data` schema field-by-field.
3. Asserts the envelope name (`success`) is present.

These tests are then re-runnable from the mobile CI using MockWebServer against the canonical sample (`docs/api/samples/`).

---

## Appendix A — Endpoint Index

| Module | Method | Path | Status | Mobile Retrofit interface | Doc § |
|---|---|---|---|---|---|
| Auth | `POST` | `/adhoc/login` | Existing | `AuthApiService.adhocLogin` | §5.1 |
| Auth | `POST` | `/login` | Existing (server) | — (reserved) | §5.2 |
| Auth | `POST` | `/adhoc/logout` | To Be Implemented | — (planned: `AuthApiService.adhocLogout`) | §5.3 |
| Auth | `POST` | `/logout` | To Be Implemented | — (parity with §5.3) | §5.4 |
| Profile | `GET` | `/adhoc/profile` | Existing | `AuthApiService.getAdhocProfile` | §6.1 |
| Profile | `GET` | `/dashboard/profile` | Existing | `VerificationApiService.getProfile` | §6.2 |
| Verification | `GET` | `/practitioners/license` | Existing | `LicenseApiService.getLicenseRecord` | §7.1 |
| Verification | `POST` | `/practitioners/verified-sync` | Existing — Deprecated by §7.4 | `VerifiedSyncApiService.syncVerifiedLicense` | §7.2 |
| Verification | `POST` | `/practitioners/license-irregularity-reports` | Existing | `IrregularityReportApiService.submitIrregularityReport` | §7.3 |
| Verification | `POST` | `/practitioners/verified-sync/batch` | To Be Implemented | — (TBI) | §7.4 |
| Verification | `GET` | `/practitioners/officer-remark-options` | To Be Implemented | `OfficerRemarkOptionsApiService.getOfficerRemarkOptions` | §7.5 |
| Examination | `GET` | `/attendance/fetch-record` | Confirmed live | `ExamDossierApiService.fetchDossier` | §8.1 |
| Examination | `POST` | `/attendance/push-record` | To Be Implemented | `ExamSyncApiService.uploadAttendanceBatch` | §8.2 |
| Examination | `POST` | `/attendance-remarks` | To Be Implemented | `ExamSyncApiService.uploadRemarkBatch` | §8.3 |
| Assessment | `GET` | `/assessments/schedules/{schedule_id}/package` | To Be Implemented | `AssessmentPackageApiService.fetchPackage` | §9.1 |
| Assessment | `POST` | `/assessments/practical-scores/batch` | To Be Implemented | `AssessmentSyncApiService.uploadPracticalScoreBatch` | §9.2 |
| Assessment | `POST` | `/assessments/project-scores/batch` | To Be Implemented | `AssessmentSyncApiService.uploadProjectScoreBatch` | §9.3 |

---

## Document history

| Version | Date | Author | Notes |
|---|---|---|---|
| 1.0 | 2026-05-16 | Mobile team | Initial consolidation of every Retrofit interface + DTO in the codebase. Standardises envelope to `{success, message, data, meta}` going forward; documents 7 To-Be-Implemented endpoints for the Examination + Assessment modules. |
| 1.1 | 2026-05-16 | Mobile team | Reshape all sync write endpoints (§7.4, §8.2, §8.3, and the former §9.3/§9.4 assessment batch endpoints) to batched `items[]` request + per-row `results[]` response. Reason: legacy per-row uploads cannot sustain CHPRBN's real campaign-day load (tens of thousands of records, multiple devices, narrow window). Legacy per-row `/practitioners/verified-sync` (§7.2) marked deprecated; batch replacement (§7.4) added as To Be Implemented. |
| 1.2 | 2026-08-15 | Mobile team | Removed the dedicated `GET /assessments/schedules` endpoint (formerly §9.1). Assessment schedules are the PE/PA subset of the exam dossier (§8.1) — mobile filters `data.papers[]` by `paper_kind` and adapts them to `AssessmentSchedule` in `AssessmentScheduleRepositoryImpl.getSchedules`. Renumbered §9.x accordingly (Package became §9.1; batch writes became §9.2 / §9.3). |
