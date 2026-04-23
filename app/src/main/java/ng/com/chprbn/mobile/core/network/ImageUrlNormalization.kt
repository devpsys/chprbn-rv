package ng.com.chprbn.mobile.core.network

/**
 * Treats API photo values as Base64 image bytes (per mobile API: no `data:` prefix in JSON).
 * Strips whitespace, returns a `data:image/...` string for Coil.
 * If the payload already starts with `data:image`, it is returned unchanged (aside from whitespace removal).
 */
fun String?.normalizeApiPhotoToDataUri(): String? {
    if (isNullOrBlank()) return null
    val t = trim().replace(Regex("\\s"), "")
    if (t.isEmpty()) return null
    if (t.startsWith("data:image", ignoreCase = true)) return t
    return "data:image/jpeg;base64,$t"
}
