package ng.com.chprbn.mobile.core.network

/**
 * Converts API `photo` values to a string Coil can load: HTTPS URL or `data:image/...;base64,...`.
 */
fun String?.normalizeApiPhotoToDataUri(): String? {
    if (isNullOrBlank()) return null
    val t = trim()
    if (t.startsWith("http://", ignoreCase = true) || t.startsWith("https://", ignoreCase = true)) {
        return t
    }
    if (t.startsWith("data:image", ignoreCase = true)) {
        return t
    }
    return "data:image/jpeg;base64,$t"
}
