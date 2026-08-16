package ng.com.chprbn.mobile.feature.assessment.data.mappers

/**
 * Stable client-side correlation key for a practical-score row. Not sent
 * on the wire — the live push endpoints have no `client_id`.
 */
internal fun practicalScoreClientId(
    scheduleId: String,
    candidateId: String,
    questionId: String,
): String = "$scheduleId:$candidateId:$questionId"

/** Stable client-side correlation key for a project-score row. */
internal fun projectScoreClientId(scheduleId: String, candidateId: String): String =
    "$scheduleId:$candidateId"

/**
 * The assessment schema namespaces question ids as `{paperId}-sec-{sectionId}-q{wireId}`
 * when fanning the dossier's integer ids across PE/PA papers. The live
 * push contract wants the original integer `question_id`. Falls back to
 * parsing the whole string as a long for any non-namespaced id.
 */
internal fun wireQuestionId(localQuestionId: String): Long? {
    val match = WIRE_QUESTION_ID.find(localQuestionId) ?: return localQuestionId.toLongOrNull()
    return match.groupValues[1].toLongOrNull()
}

private val WIRE_QUESTION_ID = Regex("""-q(\d+)$""")
