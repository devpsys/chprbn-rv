package ng.com.chprbn.mobile.feature.assessment.data.sync

/**
 * Slash-delimited entityKey encoders for the cross-feature `core.sync`
 * queue. The queue treats the key as an opaque string; only the feature's
 * own [SyncEntityHandler] interprets it.
 *
 * Assumes IDs themselves are slash-free (true for every ID shape today —
 * schedule codes like "PE-2024", question IDs like "PE-2024-sec-A-q1").
 * If a future ID format introduces slashes, switch to a different
 * delimiter; the change is local to these two objects.
 */
internal object PracticalScoreKey {
    fun encode(scheduleId: String, candidateId: String, questionId: String): String =
        "$scheduleId$SEPARATOR$candidateId$SEPARATOR$questionId"

    /** Returns `(scheduleId, candidateId, questionId)` or `null` for a malformed key. */
    fun decode(key: String): Triple<String, String, String>? {
        val parts = key.split(SEPARATOR)
        return if (parts.size == 3) Triple(parts[0], parts[1], parts[2]) else null
    }
}

internal object ProjectScoreKey {
    fun encode(scheduleId: String, candidateId: String): String =
        "$scheduleId$SEPARATOR$candidateId"

    /** Returns `(scheduleId, candidateId)` or `null` for a malformed key. */
    fun decode(key: String): Pair<String, String>? {
        val parts = key.split(SEPARATOR)
        return if (parts.size == 2) Pair(parts[0], parts[1]) else null
    }
}

private const val SEPARATOR = "/"
