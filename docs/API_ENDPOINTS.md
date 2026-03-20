# CHPRBN Mobile — API Endpoints Documentation

This document describes the HTTP APIs consumed (or prepared for consumption) by the **chprbn_revamp** Android app. It is derived from the **actual Retrofit interfaces and DTOs** in the codebase.  

**Excluded from scope:** mock / composite fallback sources (e.g. fake license data). Only the **real** `ScanApiService` contract is specified for license lookup.

---

## Table of contents

1. [Global conventions](#1-global-conventions)  
2. [Base URL & versioning](#2-base-url--versioning)  
3. [Authentication](#3-authentication)  
4. [Standard error payload](#4-standard-error-payload)  
5. [Endpoints](#5-endpoints)  
6. [Shared data models](#6-shared-data-models)  
7. [Mobile mapping notes](#7-mobile-mapping-notes)  

---

## 1. Global conventions

| Item | Detail |
|------|--------|
| **Protocol** | HTTPS |
| **Request body** | `application/json` where a body is used |
| **Response body** | `application/json` for structured responses (unless noted) |
| **Character set** | UTF-8 |
| **Timestamps** | `verified_at` (sync) uses **epoch milliseconds** (UTC) |

### Naming (important)

| Area | JSON style expected by mobile (today) |
|------|----------------------------------------|
| **Login** request/response | **camelCase** keys (`email`, `password`, `accessToken`, `user`, …) — matches `LoginRequestDto` / `LoginResponseDto` / `UserDto` with Gson defaults |
| **License lookup** response | **snake_case** keys (`registration_number`, `full_name`, …) — matches `LicenseRecordResponseDto` Kotlin property names |
| **Verified sync** request | **snake_case** keys — explicit `@SerializedName` on `VerifiedSyncRequestDto` |

If the backend prefers snake_case for login, the mobile app must add `@SerializedName` on auth DTOs; until then, backends should return **camelCase** as below.

---

## 2. Base URL & versioning

| Environment | Base URL |
|-------------|----------|
| **Production (configured in app)** | `https://chprbn.gov.ng/api/v1/` |

All paths below are **relative** to this base (e.g. `auth/login` → `https://chprbn.gov.ng/api/v1/auth/login`).

---

## 3. Authentication

### Bearer token (recommended for protected routes)

The login response includes an **`accessToken`**. For secured endpoints (profile refresh, optional license lookup, verified sync), the backend should accept:

```http
Authorization: Bearer <access_token>
```

**Current mobile app note:** The shared `OkHttpClient` in `AuthDataModule` does not attach an `Authorization` interceptor today. Protected routes should still be implemented on the server; the app will need a small client change to send the token on those calls when required.

---

## 4. Standard error payload

Login failures are parsed with a **best-effort** shape (`ApiErrorDto`):

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `message` | string | optional | Human-readable error description |

Example:

```json
{
  "message": "Invalid email or password."
}
```

If the body is empty or not JSON, the client falls back to HTTP status / generic text.

For other endpoints (license, sync), the app may read the raw error body string or HTTP code when parsing fails.

---

## 5. Endpoints

### 5.1 Login

| | |
|---|---|
| **Name** | Login |
| **Method** | `POST` |
| **Path** | `auth/login` |

#### Request

**Headers**

| Header | Value | Required |
|--------|--------|----------|
| `Content-Type` | `application/json` | Yes |

**Body (JSON)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | string | Yes | User email |
| `password` | string | Yes | Plain password (transport over HTTPS) |

**Example**

```json
{
  "email": "field.officer@chprbn.gov.ng",
  "password": "SecurePass!2025"
}
```

#### Success response

**HTTP status:** `200 OK`

**Body (JSON)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accessToken` | string | Yes | JWT or opaque API token |
| `user` | object | Yes | Embedded user (`UserDto` shape) |

**`user` object**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | User ID |
| `email` | string | Yes | Email |
| `fullName` | string | No | Display name |
| `permissions` | array of string | No | Permission codes (default `[]`) |
| `userPhoto` | string | No | Avatar / photo URL |
| `role` | string | No | Role label (e.g. officer title) |
| `staffId` | string | No | Staff identifier |
| `unit` | string | No | Unit / team |
| `organization` | string | No | Organization name |
| `lastLoginAt` | string | No | Last login (server-side; app may overwrite for display) |

**Example**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_8f3c21a9",
    "email": "field.officer@chprbn.gov.ng",
    "fullName": "Amina Okonkwo",
    "permissions": ["verify_practitioner", "sync_records"],
    "userPhoto": "https://chprbn.gov.ng/media/users/usr_8f3c21a9.jpg",
    "role": "Senior Field Officer",
    "staffId": "CHP-44920",
    "unit": "Lagos North",
    "organization": "Council for Health Professional Regulation",
    "lastLoginAt": "2025-03-19T08:30:00Z"
  }
}
```

#### Failure response

| HTTP code | Typical use |
|-----------|-------------|
| `400` | Malformed request |
| `401` | Invalid credentials |
| `403` | Account disabled / locked |
| `422` | Validation errors |
| `429` | Rate limit |
| `500` | Server error |

**Body:** Prefer `{ "message": "..." }` as in [Standard error payload](#4-standard-error-payload).

**Example**

```json
{
  "message": "Invalid email or password."
}
```

---

### 5.2 Get user profile (dashboard API — contract prepared)

| | |
|---|---|
| **Name** | Get dashboard profile |
| **Method** | `GET` |
| **Path** | `dashboard/profile` |

**Mobile status:** `DashboardApiService.getProfile()` exists and is provided via Hilt. **`DashboardRepositoryImpl` currently reads the profile from local Room cache only** (post-login). This endpoint documents the **intended remote contract** when the app wires a refresh.

#### Request

**Headers**

| Header | Value | Required |
|--------|--------|----------|
| `Authorization` | `Bearer <access_token>` | Yes (recommended) |

**Query parameters:** none  

**Body:** none  

#### Success response

**HTTP status:** `200 OK`

**Body:** Same field set as embedded `user` in login, flat object (`ProfileResponseDto`):

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | User ID |
| `email` | string | Yes | Email |
| `fullName` | string | No | Display name |
| `permissions` | array of string | No | Permissions |
| `userPhoto` | string | No | Photo URL |
| `role` | string | No | Role |
| `staffId` | string | No | Staff ID |
| `unit` | string | No | Unit |
| `organization` | string | No | Organization |
| `lastLoginAt` | string | No | Last login |

**Example**

```json
{
  "id": "usr_8f3c21a9",
  "email": "field.officer@chprbn.gov.ng",
  "fullName": "Amina Okonkwo",
  "permissions": ["verify_practitioner", "sync_records"],
  "userPhoto": "https://chprbn.gov.ng/media/users/usr_8f3c21a9.jpg",
  "role": "Senior Field Officer",
  "staffId": "CHP-44920",
  "unit": "Lagos North",
  "organization": "Council for Health Professional Regulation",
  "lastLoginAt": "2025-03-19T08:30:00Z"
}
```

#### Failure response

| HTTP code | Description |
|-----------|-------------|
| `401` | Missing or invalid token |
| `403` | Forbidden |
| `404` | Profile not found |
| `500` | Server error |

**Body:** `{ "message": "..." }` recommended.

---

### 5.3 Fetch license record (by license number)

| | |
|---|---|
| **Name** | Practitioner license lookup |
| **Method** | `GET` |
| **Path** | `practitioners/license` |

This is the **real** remote source used by `ApiLicenseRecordRemoteSource` → `ScanApiService`.

#### Request

**Headers**

| Header | Value | Required |
|--------|--------|----------|
| `Authorization` | `Bearer <access_token>` | Optional today; **recommended** if API is protected |

**Query parameters**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `license_number` | string | Yes | License / registration number (trimmed by client; app sends exact value user entered or scanned) |

**Example**

```http
GET /api/v1/practitioners/license?license_number=MD-99201-B HTTP/1.1
Host: chprbn.gov.ng
```

#### Success response

**HTTP status:** `200 OK`

**Body (JSON)** — `LicenseRecordResponseDto`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `registration_number` | string | Yes | Same as license number in registry |
| `full_name` | string | Yes | Practitioner full name |
| `photo_url` | string | No | Passport / photo URL |
| `profession` | string | Yes | Profession / cadre |
| `authority` | string | Yes | Issuing authority |
| `license_status` | string | Yes | e.g. `Active`, `Expired` |
| `expiry_date` | string | Yes | Display / ISO date string as stored by backend |
| `subtitle` | string | No | Extra line for UI |

**Example**

```json
{
  "registration_number": "MD-99201-B",
  "full_name": "Dr. Sarah Jenkins",
  "photo_url": "https://chprbn.gov.ng/media/practitioners/md99201b.jpg",
  "profession": "Medical Doctor",
  "authority": "Medical and Dental Council of Nigeria",
  "license_status": "Active",
  "expiry_date": "2026-10-31",
  "subtitle": "General Practice"
}
```

**Mobile behavior:** Non-success responses and **404** are treated as **no record** (`null`); UI shows empty / “no record found” flow.

#### Failure response

| HTTP code | Mobile handling |
|-----------|-----------------|
| `404` | Treated as not found (no body required) |
| `401` / `403` | Treated as unsuccessful (no domain record) unless client handles auth |
| `400` | Bad query |
| `500` | Server error |

Suggested error body:

```json
{
  "message": "License not found."
}
```

---

### 5.4 Sync verified record (single upload)

| | |
|---|---|
| **Name** | Upload verified verification |
| **Method** | `POST` |
| **Path** | `practitioners/verified-sync` |

The mobile app **uploads one verification per HTTP request** (loop in `SyncRepositoryImpl`). Idempotent behavior on duplicate `license_number` + `verified_at` is recommended.

#### Request

**Headers**

| Header | Value | Required |
|--------|--------|----------|
| `Content-Type` | `application/json` | Yes |
| `Authorization` | `Bearer <access_token>` | Recommended |

**Body (JSON)** — `VerifiedSyncRequestDto`

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `license_number` | string | Yes | Registration / license number |
| `verification_location` | string | Yes | Where verification was performed |
| `practitioner_present` | boolean | Yes | Officer confirmed practitioner present |
| `remark` | string | Yes | Free-text remark |
| `verified_at` | integer (int64) | Yes | Verification instant as **Unix epoch milliseconds** (UTC) |

**Example**

```json
{
  "license_number": "MD-99201-B",
  "verification_location": "Lagos University Teaching Hospital — Ward 4",
  "practitioner_present": true,
  "remark": "ID matched; license card presented.",
  "verified_at": 1710858600000
}
```

#### Success response

**HTTP status:** `200 OK` or `201 Created` or `204 No Content`  

**Body:** The client only checks `response.isSuccessful`; **empty body is acceptable**. If JSON is returned, it should not break clients.

**Example (optional JSON)**

```json
{
  "accepted": true,
  "server_id": "vrf_9d2a7c1e"
}
```

#### Failure response

| HTTP code | Description |
|-----------|-------------|
| `400` | Validation failed |
| `401` | Unauthorized |
| `409` | Conflict (e.g. duplicate policy) |
| `422` | Semantic validation |
| `500` | Server error |

**Body:** Plain text or JSON; mobile surfaces `errorBody()` string or `HTTP <code>`.

**Example**

```json
{
  "message": "license_number is required"
}
```

On failure, the app sets local sync status to **Failed** and stores the error message.

---

## 6. Shared data models

### 6.1 User (login / profile)

Logical model: `User` / `UserDto` / `ProfileResponseDto`.

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | User ID |
| `email` | string | Email |
| `fullName` | string? | Full name |
| `accessToken` | string | Present on login only (not in `ProfileResponseDto`) |
| `permissions` | string[] | Fine-grained permissions |
| `userPhoto` | string? | Photo URL |
| `role` | string? | Role label |
| `staffId` | string? | Staff ID |
| `unit` | string? | Unit |
| `organization` | string? | Organization |
| `lastLoginAt` | string? | Last login |

### 6.2 License record (lookup)

Domain: `LicenseRecord`; API: `LicenseRecordResponseDto`.

| Field (JSON) | Type | Description |
|--------------|------|-------------|
| `registration_number` | string | License number |
| `full_name` | string | Name |
| `photo_url` | string? | Image URL |
| `profession` | string | Profession |
| `authority` | string | Authority |
| `license_status` | string | Status |
| `expiry_date` | string | Expiry |
| `subtitle` | string? | Optional subtitle |

### 6.3 Verified license (final verification record in app)

Domain: `VerifiedLicense` — **superset** of license + verification + sync metadata. Only the **sync payload** is sent remotely today.

| Field | Type | Description |
|-------|------|-------------|
| `registrationNumber` | string | License number |
| `fullName` | string | Name |
| `photoUrl` | string? | Photo |
| `profession` | string | Profession |
| `authority` | string | Authority |
| `licenseStatus` | string | License status |
| `expiryDate` | string | Expiry |
| `subtitle` | string? | Subtitle |
| `verifiedAt` | long | Local epoch millis |
| `verificationLocation` | string | Location |
| `practitionerPresent` | boolean | Present flag |
| `remark` | string | Remark |
| `syncStatus` | enum | `Pending`, `Synced`, `Failed` |
| `lastSyncAttempt` | long? | Last upload attempt millis |
| `syncError` | string? | Last error |

**API subset (sync body):** `license_number`, `verification_location`, `practitioner_present`, `remark`, `verified_at`.

### 6.4 Sync payload (API DTO)

| Field (JSON) | Type | Description |
|--------------|------|-------------|
| `license_number` | string | License number |
| `verification_location` | string | Location |
| `practitioner_present` | boolean | Present |
| `remark` | string | Remark |
| `verified_at` | int64 | Epoch ms |

---

## 7. Mobile mapping notes

| Topic | Detail |
|-------|--------|
| License lookup | Query param name must be **`license_number`** (snake_case). |
| License not found | **`404`** or any non-2xx → no record in UI. |
| Sync path | **`practitioners/verified-sync`** is defined in app; confirm final path with backend. |
| Batch sync | App sends **N sequential POSTs**, not one batch JSON. |
| Dashboard profile GET | Contract aligns with `ProfileResponseDto`; repository may switch to remote later. |

---

## Document history

| Version | Date | Notes |
|---------|------|--------|
| 1.0 | 2026-03-19 | Generated from chprbn_revamp Retrofit/DTO sources |

---

*End of document*
