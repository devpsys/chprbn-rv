package ng.com.chprbn.mobile.feature.assessment.data.api

import ng.com.chprbn.mobile.feature.assessment.data.dto.PracticalPushRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ProjectPushRequestDto
import ng.com.chprbn.mobile.feature.assessment.data.dto.ScorePushResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Served by the jarabawa backend (see [ng.com.chprbn.mobile.feature.exam.data.di.JarabawaNetworkModule]),
 * not the app-wide API — **no bearer token**; `x-location` is attached by
 * [ng.com.chprbn.mobile.feature.auth.data.network.LocationHeaderInterceptor].
 *
 * Confirmed live per `docs/mobile-api-guide.html` §6 / §7.
 */
interface AssessmentSyncApiService {

    @POST("practical/push-record")
    suspend fun uploadPracticalScoreBatch(
        @Body body: PracticalPushRequestDto,
    ): Response<ScorePushResponseDto>

    @POST("project/push-record")
    suspend fun uploadProjectScoreBatch(
        @Body body: ProjectPushRequestDto,
    ): Response<ScorePushResponseDto>
}
