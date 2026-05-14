package ng.com.chprbn.mobile.feature.exam.data.sync

/**
 * Slash-delimited entityKey encoders for the cross-feature `core.sync`
 * queue. The queue treats the key as an opaque string; only the
 * feature's own `SyncEntityHandler` interprets it.
 *
 * Assumes IDs themselves are slash-free (true for every ID shape today —
 * paper codes like "p-paper-i", UUIDs). If a future ID format introduces
 * slashes, switch to a different delimiter; the change is local to these
 * two objects.
 */
internal object AttendanceKey {
    fun encode(paperId: String, candidateId: String): String =
        "$paperId$SEPARATOR$candidateId"

    /** Returns `(paperId, candidateId)` or `null` for a malformed key. */
    fun decode(key: String): Pair<String, String>? {
        val parts = key.split(SEPARATOR)
        return if (parts.size == 2) Pair(parts[0], parts[1]) else null
    }
}

/**
 * Remark keys are just the row's UUID — no composite encoding needed.
 * Wrapped in an object for symmetry with [AttendanceKey] and so the
 * handler can present a uniform `decode()` API.
 */
internal object RemarkKey {
    fun encode(id: String): String = id

    fun decode(key: String): String? = key.takeIf { it.isNotBlank() }
}

private const val SEPARATOR = "/"
