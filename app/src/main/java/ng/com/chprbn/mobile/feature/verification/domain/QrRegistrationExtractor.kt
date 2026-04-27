package ng.com.chprbn.mobile.feature.verification.domain

private val registrationAfterHashLine = Regex("""#\s*:\s*(\S+)""", RegexOption.IGNORE_CASE)

/**
 * Pulls the license/registration id from a practitioner QR payload, e.g.:
 * ```
 * #: B2320135
 * Cadre: CHEW
 * Expiry: 2025-10-24
 * ```
 * Returns the token after `#:` (e.g. `B2320135`).
 *
 * If the payload is a single-line plain id (no `#`), returns the trimmed string when it looks like an id
 * (alphanumeric plus `_`, `.`, `-`) for compatibility with manual-style values.
 */
fun extractRegistrationFromQrPayload(raw: String): String? {
    val text = raw.trim()
    if (text.isEmpty()) return null

    registrationAfterHashLine.find(text)?.groupValues?.getOrNull(1)?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { return it }

    if (!text.contains('#') && !text.contains('\n') && !text.contains('\r')) {
        if (text.matches(Regex("^[A-Za-z0-9][A-Za-z0-9_.-]*$"))) return text
    }

    return null
}
