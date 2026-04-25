# License irregularity report (mobile API v1)

Submit an officer irregularity report for a practitioner license, including a snapshot image of the physical license or document.

This document matches the Android client contract (`IrregularityReportApiService` and related DTOs).

## Base URL and auth

| Item | Value |
|------|--------|
| Base URL | `https://app.chprbn.gov.ng/api/v1/mobile/` |
| Authentication | Bearer token (same as other mobile v1 endpoints) |
| Content type | `multipart/form-data` |

## Endpoint

| Method | Path |
|--------|------|
| `POST` | `practitioners/license-irregularity-reports` |

Full URL example:

`POST https://app.chprbn.gov.ng/api/v1/mobile/practitioners/license-irregularity-reports`

## Request (multipart form)

All non-file parts are sent as plain text (`text/plain; charset=utf-8`). Field names use **snake_case**.

| Part name | Required | Description |
|-----------|----------|-------------|
| `name_on_card` | Yes | Full name printed on the license card |
| `license_number` | Yes | License / registration number |
| `cadre` | Yes | Cadre (e.g. profession as shown on the card) |
| `gender` | Yes | Gender as reported |
| `remark` | Yes | Irregularity category (see allowed values below) |
| `reported_at` | Yes | Client timestamp as **epoch milliseconds** (decimal digits only, ASCII string) |
| `snapshot` | Yes | Image file of the license or document (`image/jpeg`, `image/png`, or `image/webp` typical) |

### Allowed `remark` values

These are the values the mobile app sends in the `remark` part (API-facing codes):

| Value | Meaning (UI label) |
|-------|---------------------|
| `fake` | Fake |
| `over_due` | Over Due |
| `long_over_due` | Long Over Due |

## Response (JSON envelope)

The response body follows the same envelope pattern as other mobile v1 endpoints (e.g. verified sync): a top-level `status` flag, optional `message`, and optional `data` object.

### Success shape (example)

```json
{
  "status": true,
  "message": "Report submitted successfully.",
  "data": {
    "id": 12345,
    "license_number": "MED-12345",
    "remark": "fake"
  }
}
```

### `data` fields (optional; server-defined)

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Server-assigned report id, if applicable |
| `license_number` | string | Echo of submitted license number |
| `remark` | string | Echo of accepted remark code |

### Failure shape (example)

```json
{
  "status": false,
  "message": "Validation failed: snapshot is required.",
  "data": null
}
```

Clients should treat `status: false` as failure and surface `message` to the user when present.

## HTTP status codes

| Code | Typical use |
|------|-------------|
| `200` | Request processed; inspect `status` and `message` in the body |
| `401` / `403` | Authentication or authorization failure |
| `422` | Validation error (prefer `status: false` and `message` in body when possible) |
| `5xx` | Server error |

## Client implementation notes

- The Android app copies the selected or captured image `Uri` to a temporary file, uploads it as the `snapshot` part, then deletes the temp file after the request completes.
- Field part names must match this document exactly for interoperability with `IrregularityReportApiService`.

## Related source files

- `app/src/main/java/ng/com/chprbn/mobile/feature/report/data/api/IrregularityReportApiService.kt`
- `app/src/main/java/ng/com/chprbn/mobile/feature/report/data/dto/IrregularityReportEnvelopeDto.kt`
- `app/src/main/java/ng/com/chprbn/mobile/feature/report/data/repository/IrregularityReportRepositoryImpl.kt`
