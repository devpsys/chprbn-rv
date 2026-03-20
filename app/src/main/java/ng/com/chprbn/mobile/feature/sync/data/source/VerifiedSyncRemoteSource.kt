package ng.com.chprbn.mobile.feature.sync.data.source

import ng.com.chprbn.mobile.feature.sync.data.dto.VerifiedSyncRequestDto

/**
 * Abstraction over the network upload for verified records (easy to fake or compose).
 */
fun interface VerifiedSyncRemoteSource {
    suspend fun uploadVerifiedRecord(request: VerifiedSyncRequestDto): Result<Unit>
}
