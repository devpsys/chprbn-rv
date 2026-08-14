package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.ExamDossierEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * **SPECULATIVE** — path assumed pending backend contract confirmation
 * (plan §12, C1). Served by the jarabawa backend (see [ng.com.chprbn.mobile.feature.exam.data.di.JarabawaNetworkModule]),
 * not the app-wide API — **no bearer token**; `x-location` is the sole
 * request credential, attached transparently by
 * [ng.com.chprbn.mobile.feature.auth.data.network.LocationHeaderInterceptor]
 * from the officer's `adhoc/profile` location — no param needed here.
 *
 * Scope: the officer's currently-active centre + day. The server
 * resolves "which dossier" from `x-location` + date; the mobile client
 * doesn't send those explicitly.
 */
interface ExamDossierApiService {

    @GET("attendance/fetch-record")
    suspend fun fetchDossier(): Response<ExamDossierEnvelopeDto>
}
