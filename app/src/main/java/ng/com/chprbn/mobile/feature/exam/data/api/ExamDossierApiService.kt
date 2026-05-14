package ng.com.chprbn.mobile.feature.exam.data.api

import ng.com.chprbn.mobile.feature.exam.data.dto.ExamDossierEnvelopeDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * **SPECULATIVE** — path assumed pending backend contract confirmation
 * (plan §12, C1). Bearer auth required.
 *
 * Scope: the officer's currently-active centre + day. The server
 * resolves "which dossier" from the bearer token + date; the mobile
 * client doesn't send those explicitly.
 */
interface ExamDossierApiService {

    @GET("exam/dossier")
    suspend fun fetchDossier(): Response<ExamDossierEnvelopeDto>
}
