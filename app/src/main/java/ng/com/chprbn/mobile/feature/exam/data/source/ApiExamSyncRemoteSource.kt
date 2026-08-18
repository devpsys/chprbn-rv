package ng.com.chprbn.mobile.feature.exam.data.source

import ng.com.chprbn.mobile.core.sync.AssessorProvider
import ng.com.chprbn.mobile.core.sync.foldBatchResults
import ng.com.chprbn.mobile.feature.exam.data.api.ExamSyncApiService
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendancePushRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.AttendanceSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncBatchRequestDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncItemDto
import ng.com.chprbn.mobile.feature.exam.data.dto.RemarkSyncResultDto
import ng.com.chprbn.mobile.feature.exam.data.mappers.attendanceClientId
import ng.com.chprbn.mobile.feature.exam.data.mappers.toSyncItemDto
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class ApiExamSyncRemoteSource @Inject constructor(
    private val api: ExamSyncApiService,
    private val assessorProvider: AssessorProvider,
) : ExamSyncRemoteSource {

    override suspend fun uploadAttendanceBatch(
        rows: List<AttendanceUploadRow>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()

        val outcomes = LinkedHashMap<String, Result<Unit>>(rows.size)
        val items = mutableListOf<AttendanceSyncItemDto>()
        val itemKeys = mutableListOf<String>()

        for (row in rows) {
            val key = attendanceClientId(row.paperId, row.candidateId)
            val dto = row.toSyncItemDtoOrNull()
            if (dto == null) {
                outcomes[key] = Result.failure(
                    IllegalStateException(
                        "Cannot push attendance for candidate ${row.candidateId}: missing/" +
                            "non-numeric scheduledCandidateId, scheduleId, candidateId, or paperId.",
                    ),
                )
                continue
            }
            items.add(dto)
            itemKeys.add(key)
        }

        if (items.isEmpty()) return outcomes

        // Server rejects the whole batch with a 4xx if `assessor` is
        // absent. Fail fast so rows stay queued for a later retry rather
        // than being marked Failed for what is really a session issue.
        val assessor = assessorProvider.current() ?: return failAllWithAssessorMissing(itemKeys, outcomes)

        // The endpoint returns no per-row results — just the batch's own
        // status + a list of processed candidate_ids (docs/mobile-api-guide.html
        // §5) — so every well-formed row in the batch shares one outcome.
        val transportOutcome: Result<Unit> = runCatching {
            val response = api.uploadAttendanceBatch(
                body = AttendancePushRequestDto(attendances = items, assessor = assessor),
            )
            response.requireSuccessOrThrow()
            val envelope = response.body()
                ?: error("Attendance batch: empty response body.")
            if (!envelope.status) {
                error(envelope.message ?: "Attendance batch rejected by server.")
            }
        }

        transportOutcome.fold(
            onSuccess = { itemKeys.forEach { outcomes[it] = Result.success(Unit) } },
            onFailure = { t -> itemKeys.forEach { outcomes[it] = Result.failure(t) } },
        )

        return outcomes
    }

    override suspend fun uploadRemarkBatch(
        rows: List<Remark>,
    ): Map<String, Result<Unit>> {
        if (rows.isEmpty()) return emptyMap()
        val items: List<RemarkSyncItemDto> = rows.map { it.toSyncItemDto() }

        val transportOutcome: Result<List<RemarkSyncResultDto>> = runCatching {
            val response = api.uploadRemarkBatch(
                idempotencyKey = UUID.randomUUID().toString(),
                body = RemarkSyncBatchRequestDto(items = items),
            )
            response.requireSuccessOrThrow()
            val envelope = response.body()
                ?: error("Remark batch: empty response body.")
            if (!envelope.success) {
                error(envelope.message ?: "Remark batch rejected by server.")
            }
            envelope.data?.results.orEmpty()
        }

        return foldBatchResults(
            clientIds = items.map { it.clientId },
            transportOutcome = transportOutcome,
            acceptedOf = { it.accepted },
            errorOf = { it.error },
            clientIdOf = { it.clientId },
        )
    }

    /** Null when any required numeric field fails to parse — see [AttendanceSyncItemDto]. */
    private fun AttendanceUploadRow.toSyncItemDtoOrNull(): AttendanceSyncItemDto? {
        val scheduledCandidateIdLong = scheduledCandidateId.toLongOrNull() ?: return null
        val scheduleIdLong = scheduleId.toLongOrNull() ?: return null
        val candidateIdLong = candidateId.toLongOrNull() ?: return null
        val paperIdLong = paperId.toLongOrNull() ?: return null
        return AttendanceSyncItemDto(
            scheduledCandidateId = scheduledCandidateIdLong,
            scheduleId = scheduleIdLong,
            candidateId = candidateIdLong,
            paperId = paperIdLong,
            signIn = if (status == AttendanceStatus.SignedIn || status == AttendanceStatus.SignedOut) 1 else 0,
            signOut = if (status == AttendanceStatus.SignedOut) 1 else 0,
            remark = remark,
            year = year,
        )
    }

    private fun Response<*>.requireSuccessOrThrow() {
        if (!isSuccessful) {
            error("Upload failed: HTTP ${code()} ${message()}")
        }
    }

    /**
     * Marks every well-formed row as failed with the same "no assessor"
     * message. The message is user-actionable ("Sign in again") because
     * the only way this fires locally is a cache carried over from
     * schema < v10 that never signed in online since — the assessor id
     * lands only through the fresh `adhoc/profile` fetch on the online
     * login path.
     */
    private fun failAllWithAssessorMissing(
        itemKeys: List<String>,
        outcomes: LinkedHashMap<String, Result<Unit>>,
    ): Map<String, Result<Unit>> {
        val error = IllegalStateException(
            "Missing assessor identity — sign in online once to refresh it, then retry sync.",
        )
        itemKeys.forEach { outcomes[it] = Result.failure(error) }
        return outcomes
    }
}
