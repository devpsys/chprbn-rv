# CHPRBN Mobile — Accreditation API Documentation

**Audience:** Backend, mobile, QA.
**Status:** All endpoints below are **To Be Implemented (backend)**. Mobile Phase 2 already ships Retrofit contracts, DTOs, Composite Api+Fake remotes, and outbox upload against these paths.
**Canonical client source:** `app/src/main/java/ng/com/chprbn/mobile/feature/accreditation/data/api/AccreditationApiService.kt` and DTOs under `…/data/dto/`.
**Also mirrored in:** [`full-api-documentation.md`](./full-api-documentation.md) §10.

When this doc and the Retrofit/DTO code disagree, **the code is right** — file a doc-drift PR.

---

## Contents

1. [Overview](#1-overview)
2. [Conventions](#2-conventions)
3. [Endpoint catalogue](#3-endpoint-catalogue)
4. [Shared enums & draft status](#4-shared-enums--draft-status)
5. [Dependency flow](#5-dependency-flow)
6. [Outbox sync](#6-outbox-sync)
7. [Endpoint index](#7-endpoint-index)
8. [Open gaps](#8-open-gaps)

---

## 1. Overview

### 1.1 Purpose

HTTP contract for the **Accreditation** officer role: hub assignments, institution profile, local-first SSQ / criteria / score drafts, attachment upload, draft sync, submit-for-review, and server-owned assessment decisions (grade / period / quota).

Mobile does **not** compute grades or quotas. Those fields appear only on decision responses.

### 1.2 Base URL

All paths are relative to the **main mobile API** base (same host as auth / verification):

| Environment | Base URL |
|---|---|
| Production | `https://app.chprbn.gov.ng/api/v1/mobile/` |
| Staging (when available) | `https://staging.app.chprbn.gov.ng/api/v1/mobile/` |

Feature prefix: **`accreditation/`**  
Example full URL: `https://app.chprbn.gov.ng/api/v1/mobile/accreditation/assignments`

### 1.3 Authentication & RBAC

| Element | Value |
|---|---|
| **Scheme** | `Authorization: Bearer <token>` |
| **Login** | `POST /adhoc/login` (field / accreditation officer) |
| **Required role** | `accreditation` |
| **403** | Wrong role — mobile shows a generic “Not allowed” toast |

Also send `Accept: application/json`. JSON bodies use `Content-Type: application/json`. Attachment upload uses `multipart/form-data`.

### 1.4 Response envelope

Every JSON endpoint returns:

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

| Field | Type | Notes |
|---|---|---|
| `success` | boolean | Canonical flag. Mobile also accepts legacy `status` as an alias during migration. |
| `message` | string \| null | Human-readable; shown in error toasts. |
| `data` | object \| null | Per-endpoint payload. |

Non-2xx: prefer the same envelope with `success: false`. Certificate download (§3.13) is raw PDF, not this envelope.

Wire field names: **`snake_case`**.

---

## 2. Conventions

| Topic | Rule |
|---|---|
| **IDs** | Opaque strings (`inst-abuth`, `assess-inst-abuth`, `dec-inst-abuth-2024`). |
| **Timestamps** | Epoch millis UTC (`updated_at`, `synced_at`). |
| **SSQ keys** | Path/body `section_key` / `{key}` = `A` … `F`. |
| **Draft payloads** | Complex form state travels as `payload_json` (stringified JSON). Empty string is valid for seed stubs. |
| **Grading** | Server-only on submit / decision. Mobile never sends a computed grade. |
| **Idempotency** | Draft sync (§3.9) SHOULD treat repeated POSTs for the same assessment id + client `updated_at` as upserts. Optional `Idempotency-Key` header (UUIDv4) recommended for sync retries. |

---

## 3. Endpoint catalogue

### 3.1 Get Assignments (hub)

| Property | Value |
|---|---|
| **Purpose** | Officer banner, feature-grid subtitles, assigned + upcoming institutions. |
| **Method / path** | `GET /accreditation/assignments` |
| **Auth** | Bearer + role `accreditation` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getAssignments` |

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "officer": {
      "initials": "DB",
      "display_name": "Insp. Officer: D. Bello",
      "registration_label": "CHPRBN REG #OFF-9942",
      "is_verified": true
    },
    "feature_grid": {
      "start_subtitle": "Protocol 2024/25",
      "continue_subtitle": "Resume section 4 of 9",
      "drafts_subtitle": "Ready for sync pipeline",
      "sync_subtitle": "Last synced 5m ago",
      "is_online": true
    },
    "assigned_institutions": [
      {
        "id": "inst-abuth",
        "name": "Ahmadu Bello University Teaching Hospital",
        "location_label": "Zaria, Kaduna • North West",
        "protocol_label": "Accreditation Cycle 2024/2025",
        "last_synced_label": "Today, 09:30 AM",
        "sync_status": "synced",
        "is_active": true
      }
    ],
    "upcoming_institutions": [
      {
        "id": "inst-niht",
        "name": "National Institute of Health Technology",
        "schedule_label": "Scheduled dispatch • Inspection window starts tomorrow"
      }
    ]
  }
}
```

##### `data.assigned_institutions[].sync_status`

| Wire value | Meaning |
|---|---|
| `synced` | No pending local draft for this assignment. |
| `draft_pending_sync` | Local draft waiting to upload. |
| `sync_failed` | Last upload failed. |
| `never_synced` | No sync yet. |

---

### 3.2 Get Institution Profile

| Property | Value |
|---|---|
| **Method / path** | `GET /accreditation/institutions/{id}` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getInstitution` |

#### Path parameters

| Name | Type | Required | Description |
|---|---|---|---|
| `id` | string | Yes | Institution id from assignments. |

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": "inst-abuth",
    "name": "Ahmadu Bello University Teaching Hospital",
    "ownership_subtitle": "Federal Government • Tertiary Healthcare Institution",
    "photo_url": "https://…",
    "sync_status": "synced",
    "last_synced_label": "09:30 AM",
    "facility_code": "CHPRBN-NW-2024-08",
    "last_visit_label": "14 Nov 2021",
    "ownership_type": "Federal Government",
    "state": "Kaduna State",
    "zone": "North West",
    "campus_address": "Main Campus, Shika, Zaria",
    "programmes": ["BCHS / CHO", "CHEW", "HND Health Tech", "JCHEW", "ND Health Info Management"],
    "programmes_accredited_count": 5,
    "academic_staff_count": 128,
    "technical_staff_count": 42,
    "admin_staff_count": 85,
    "tech_to_academic_ratio": "1:3.0",
    "capital_base_label": "₦450,000,000",
    "history": [
      {
        "id": "hist-2021",
        "cycle_label": "Cycle 2021 / 2022",
        "date_label": "14 Nov 2021",
        "score_label": "84.5%",
        "grade_label": "Grade A",
        "decision_label": "Full Accreditation (5 Years)"
      }
    ]
  }
}
```

#### Errors

| Status | When |
|---|---|
| `404` | Unknown institution. |
| `401` / `403` | Missing token / wrong role. |

---

### 3.3 Get Current Assessment (seed / overview aggregate)

| Property | Value |
|---|---|
| **Method / path** | `GET /accreditation/institutions/{id}/assessments/current` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getCurrentAssessment` |

Returns the officer’s in-progress (or server-seeded) assessment plus SSQ / criteria / score stubs. Mobile seeds Room from this on first open for an institution. Empty `payload_json` is allowed.

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "draft": {
      "id": "assess-inst-abuth",
      "institution_id": "inst-abuth",
      "institution_name": "Ahmadu Bello University Teaching Hospital",
      "cycle_label": "Cycle 2024/2025",
      "facility_code": "ABUTH-ZR-049",
      "location_label": "Zaria, Kaduna State",
      "status": "SavedLocally",
      "updated_at": 1730000000000
    },
    "sections": [
      {
        "section_key": "A",
        "code_label": "Section A",
        "range_label": "SSQ A1 + A2",
        "title": "General Information",
        "subtitle": "24 mandatory baseline fields",
        "status": "NotStarted",
        "payload_json": "",
        "items_completed": 0,
        "total_items": 24
      }
    ],
    "criteria": {
      "payload_json": "",
      "evaluated_count": 0,
      "total_criteria": 8,
      "status": "NotStarted"
    },
    "scores": {
      "payload_json": "",
      "completed_count": 0,
      "total_groups": 6,
      "status": "NotStarted"
    }
  }
}
```

Prefer six section rows (`A`–`F`) so the overview checklist is complete on first open.

---

### 3.4 Upsert SSQ Section Draft

| Property | Value |
|---|---|
| **Method / path** | `PUT /accreditation/assessments/{id}/sections/{key}` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.upsertSection` |

| Path | Description |
|---|---|
| `id` | Assessment draft id. |
| `key` | `A`–`F`. |

#### Request body (JSON)

| Field | Type | Required | Description |
|---|---|---|---|
| `status` | string | Yes | See [§4](#4-shared-enums--draft-status). |
| `payload_json` | string | Yes | Section form state (may be `""`). |
| `items_completed` | int | Yes | Progress numerator. |
| `total_items` | int | Yes | Progress denominator. |
| `code_label` | string | No | e.g. `Section A`. |
| `range_label` | string | No | e.g. `SSQ A1 + A2`. |
| `title` | string | No | Display title. |
| `subtitle` | string | No | Display subtitle. |

For offline campaigns prefer batched sync ([§3.9](#39-sync-assessment-draft)); this route is for optional immediate upsert when online.

#### Success — `200 OK`

```json
{ "success": true, "message": "Saved", "data": {} }
```

---

### 3.5 Upsert Criteria Checklist

| Property | Value |
|---|---|
| **Method / path** | `PUT /accreditation/assessments/{id}/criteria` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.upsertCriteria` |

#### Request body (JSON)

| Field | Type | Required | Description |
|---|---|---|---|
| `status` | string | Yes | Draft status. |
| `payload_json` | string | Yes | Checklist state. |
| `evaluated_count` | int | Yes | Progress numerator. |
| `total_criteria` | int | Yes | Progress denominator. |

---

### 3.6 Upsert Score Entry

| Property | Value |
|---|---|
| **Method / path** | `PUT /accreditation/assessments/{id}/scores` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.upsertScores` |

#### Request body (JSON)

| Field | Type | Required | Description |
|---|---|---|---|
| `status` | string | Yes | Draft status. |
| `payload_json` | string | Yes | Raw score groups only — **no grade**. |
| `completed_count` | int | Yes | Groups completed. |
| `total_groups` | int | Yes | Expected groups (typically 6). |

---

### 3.7 Upload Attachment (multipart)

| Property | Value |
|---|---|
| **Method / path** | `POST /accreditation/assessments/{id}/attachments` |
| **Content-Type** | `multipart/form-data` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.uploadAttachment` |

#### Parts

| Name | Type | Required | Description |
|---|---|---|---|
| `parent_key` | text | Yes | Parent context (e.g. `ssq-A`, `criteria`, `scores`). |
| `title` | text | Yes | Display title. |
| `location_label` | text | No | Optional place / geotag label. |
| `file` | file | Yes | Photo or document bytes. |

Mobile stores files under durable `filesDir/accreditation_attachments/` before enqueue. The sync handler uploads pending binaries, then posts [§3.9](#39-sync-assessment-draft).

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "Uploaded",
  "data": { "attachment_id": "att-…", "upload_state": "Uploaded" }
}
```

(`data` shape is flexible; mobile currently only requires `success`.)

---

### 3.8 Delete Attachment

| Property | Value |
|---|---|
| **Method / path** | `DELETE /accreditation/assessments/{id}/attachments/{attachmentId}` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.deleteAttachment` |

#### Success — `200 OK`

```json
{ "success": true, "message": "Deleted", "data": {} }
```

---

### 3.9 Sync Assessment Draft

| Property | Value |
|---|---|
| **Method / path** | `POST /accreditation/assessments/{id}/sync` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.syncDraft` |
| **Outbox** | `SyncEntityType.AccreditationDraft` → `AccreditationDraftSyncHandler` |

Primary write path for offline-edited drafts: one request carrying sections, criteria, scores, and attachment metadata (binaries already uploaded via §3.7 when pending).

#### Request body (JSON)

| Field | Type | Required | Description |
|---|---|---|---|
| `institution_id` | string | Yes | Owning institution. |
| `cycle_label` | string | Yes | Accreditation cycle label. |
| `facility_code` | string | No | Facility / assignment code. |
| `location_label` | string | No | Campus / city label. |
| `sections` | array | Yes | SSQ A–F drafts (see below). |
| `criteria` | object | No | Checklist payload + progress. |
| `scores` | object | No | Raw score groups + progress. |
| `attachments` | array | Yes | Metadata for files (may be empty). |
| `updated_at` | long | No | Client epoch millis. |

##### `sections[]` item

| Field | Type | Required |
|---|---|---|
| `section_key` | string | Yes (`A`–`F`) |
| `status` | string | Yes |
| `payload_json` | string | Yes |
| `items_completed` | int | Yes |
| `total_items` | int | Yes |
| `code_label` | string | No |
| `range_label` | string | No |
| `title` | string | No |
| `subtitle` | string | No |

##### `attachments[]` item

| Field | Type | Required |
|---|---|---|
| `id` | string | Yes |
| `parent_key` | string | Yes |
| `title` | string | Yes |
| `is_photo` | boolean | Yes |
| `size_label` | string | No |
| `location_label` | string | No |
| `upload_state` | string | No |

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "Synced",
  "data": {
    "assessment_id": "assess-inst-abuth",
    "synced_at": 1730000500000
  }
}
```

On non-2xx or `success: false`, mobile marks the draft `SyncFailed` and keeps the outbox job for retry.

---

### 3.10 Submit Assessment for Review

| Property | Value |
|---|---|
| **Method / path** | `POST /accreditation/assessments/{id}/submit` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.submitAssessment` |

No request body. Server computes grade / decision / period / quota.

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "Submitted",
  "data": { "decision_id": "dec-inst-abuth-2024" }
}
```

Mobile stores `decision_id` and opens the result screen via §3.11.

---

### 3.11 Get Assessment Decision (result)

| Property | Value |
|---|---|
| **Method / path** | `GET /accreditation/decisions/{decisionId}` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getDecision` |

#### Success — `200 OK`

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "decision_id": "dec-inst-abuth-2024",
    "institution_id": "inst-abuth",
    "institution_name": "Ahmadu Bello University Teaching Hospital",
    "location_label": "Zaria, Kaduna State",
    "facility_code": "CHPRBN-NW-2024-08",
    "assessment_date_label": "24 Oct 2024",
    "grade_label": "Excellent",
    "decision_label": "FullAccreditation",
    "accreditation_period": "5 Years Full Accreditation",
    "overall_score_percent_label": "88.0%",
    "student_quota_label": "120",
    "receipt_ref": "REC-2024-ABU-8829-SYNC",
    "raw_score_rows": [
      {
        "section_index": 1,
        "group_label": "Group 1: Location & Land Area (§1)",
        "raw_score": "9",
        "max_points": 10
      }
    ],
    "conditions": [
      "Maintain approved teacher-to-student clinical supervision ratio (1:8)."
    ]
  }
}
```

Mobile also accepts string forms `raw_score_rows_json` and `conditions_json` if structured arrays are omitted.

#### Errors

| Status | When |
|---|---|
| `404` | Unknown decision. |

---

### 3.12 Institution Documents (UI stub)

| Property | Value |
|---|---|
| **Method / path** | `GET /accreditation/institutions/{id}/documents` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getInstitutionDocuments` |

Document vault for a future View Documents screen. Response `data` shape TBD; keep envelope-compatible.

---

### 3.13 Assessment Certificate PDF (UI stub)

| Property | Value |
|---|---|
| **Method / path** | `GET /accreditation/decisions/{decisionId}/certificate` |
| **Status** | To Be Implemented |
| **Mobile** | `AccreditationApiService.getCertificatePdf` |
| **Response** | `application/pdf` (raw bytes, not the JSON envelope) |

---

## 4. Shared enums & draft status

### 4.1 Draft persistence `status` (draft / section / criteria / scores)

Align with mobile `DraftPersistenceStatus`:

| Wire / domain value | Meaning |
|---|---|
| `NotStarted` | Placeholder row, no officer input. |
| `InProgress` | Partially filled. |
| `SavedLocally` | Saved on device, not necessarily queued. |
| `PendingSync` | Enqueued for outbox upload. |
| `Synced` | Accepted by server. |
| `SyncFailed` | Last upload failed. |

### 4.2 Attachment `upload_state`

| Value | Meaning |
|---|---|
| `LocalOnly` | On device only. |
| `PendingUpload` | Queued for multipart. |
| `Uploaded` | Server accepted file. |
| `Failed` | Multipart failed. |

### 4.3 Institution assignment `sync_status`

See [§3.1](#31-get-assignments-hub).

---

## 5. Dependency flow

```
POST /adhoc/login ──▶ token (role accreditation)
            │
            ▼
GET  /accreditation/assignments
GET  /accreditation/institutions/{id}
GET  /accreditation/institutions/{id}/assessments/current
            │
            ▼  (officer edits SSQ / criteria / scores offline)
POST /accreditation/assessments/{id}/attachments   (multipart, per file)
POST /accreditation/assessments/{id}/sync            (draft aggregate)
            │
            ▼
POST /accreditation/assessments/{id}/submit ──▶ data.decision_id
            │
            ▼
GET  /accreditation/decisions/{decisionId}
GET  /accreditation/decisions/{decisionId}/certificate   (PDF; UI stub)
GET  /accreditation/institutions/{id}/documents          (vault; UI stub)
```

Optional online upserts (§3.4–§3.6) may run between edits and sync; outbox sync remains the campaign-day path.

---

## 6. Outbox sync

| Item | Value |
|---|---|
| **Entity type** | `SyncEntityType.AccreditationDraft` |
| **Entity key** | Assessment draft id |
| **Handler** | `AccreditationDraftSyncHandler` |
| **Remote** | Composite Api → Fake fallback until backend is live |
| **Local state** | `PendingSync` → `Synced` / `SyncFailed` |

User-initiated Sync All / Retry Failed backfills the queue and runs `SyncBatchRunner`. Background WorkManager uses the same handler map.

---

## 7. Endpoint index

| Method | Path | Status | Mobile | § |
|---|---|---|---|---|
| `GET` | `/accreditation/assignments` | TBI | `getAssignments` | 3.1 |
| `GET` | `/accreditation/institutions/{id}` | TBI | `getInstitution` | 3.2 |
| `GET` | `/accreditation/institutions/{id}/assessments/current` | TBI | `getCurrentAssessment` | 3.3 |
| `PUT` | `/accreditation/assessments/{id}/sections/{key}` | TBI | `upsertSection` | 3.4 |
| `PUT` | `/accreditation/assessments/{id}/criteria` | TBI | `upsertCriteria` | 3.5 |
| `PUT` | `/accreditation/assessments/{id}/scores` | TBI | `upsertScores` | 3.6 |
| `POST` | `/accreditation/assessments/{id}/attachments` | TBI | `uploadAttachment` | 3.7 |
| `DELETE` | `/accreditation/assessments/{id}/attachments/{attachmentId}` | TBI | `deleteAttachment` | 3.8 |
| `POST` | `/accreditation/assessments/{id}/sync` | TBI | `syncDraft` | 3.9 |
| `POST` | `/accreditation/assessments/{id}/submit` | TBI | `submitAssessment` | 3.10 |
| `GET` | `/accreditation/decisions/{decisionId}` | TBI | `getDecision` | 3.11 |
| `GET` | `/accreditation/institutions/{id}/documents` | TBI | `getInstitutionDocuments` | 3.12 |
| `GET` | `/accreditation/decisions/{decisionId}/certificate` | TBI | `getCertificatePdf` | 3.13 |

TBI = To Be Implemented (backend).

---

## 8. Open gaps

| Gap | Notes |
|---|---|
| Backend not live | Mobile Composite falls back to Fake remotes so demos keep working. |
| Documents / certificate UI | Endpoints stubbed in Retrofit; Compose screens not shipped. |
| `payload_json` schema | Per-section SSQ field shapes follow Guideline/SSQ PDF; treat as opaque JSON until a stricter OpenAPI schema is agreed. |
| Live geotag / OpenDocument picker | Model fields exist; capture UX deferred. |
| Institution search | Out of scope. |

---

## Document history

| Version | Date | Notes |
|---|---|---|
| 1.0 | 2026-10-31 | Standalone accreditation contract split from full API docs §10. Matches mobile Phase 2 Retrofit surface. |
