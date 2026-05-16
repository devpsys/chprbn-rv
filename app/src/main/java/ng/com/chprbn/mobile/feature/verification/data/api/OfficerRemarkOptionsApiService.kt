package ng.com.chprbn.mobile.feature.verification.data.api

import ng.com.chprbn.mobile.feature.verification.data.dto.OfficerRemarkOptionsEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Mobile API v1 — the canonical list of officer-remark choices shown
 * in the verification form's "Officer remark" dropdown. Bearer auth
 * required.
 *
 * Replaces the previously-hardcoded `R.array.officer_remark_options`
 * string-array. The mobile client keeps that array bundled as a
 * first-launch / offline fallback; the API is the source of truth when
 * reachable.
 */
interface OfficerRemarkOptionsApiService {

    @GET("practitioners/officer-remark-options")
    suspend fun getOfficerRemarkOptions(): Response<OfficerRemarkOptionsEnvelopeDto>
}
