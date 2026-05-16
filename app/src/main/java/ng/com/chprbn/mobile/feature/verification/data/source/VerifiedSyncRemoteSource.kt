package ng.com.chprbn.mobile.feature.verification.data.source

import ng.com.chprbn.mobile.feature.verification.data.dto.VerifiedSyncRequestDto

/**
 * Batched upload abstraction for verified-license rows. The current wire
 * endpoint is still per-row (`POST /practitioners/verified-sync`); the
 * impl loops internally and presents a batched contract to the handler
 * so the cross-feature `SyncEntityHandler.uploadBatch` contract is
 * uniform. When the server ships
 * `POST /practitioners/verified-sync/batch` (see
 * `docs/api/full-api-documentation.md` §7.4) the impl swaps to a single
 * HTTP call without touching the handler.
 *
 * Map keys are each row's `license_number` (the row's primary key in the
 * verified-license table). Every input row produces exactly one map
 * entry.
 */
fun interface VerifiedSyncRemoteSource {
    suspend fun uploadVerifiedBatch(
        rows: List<VerifiedSyncRequestDto>,
    ): Map<String, Result<Unit>>
}
