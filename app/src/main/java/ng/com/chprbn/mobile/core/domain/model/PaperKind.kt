package ng.com.chprbn.mobile.core.domain.model

/**
 * The kind of an exam paper. Replaces the legacy v1 magic strings `"PA"` (project)
 * and `"PE"` (practical) that leaked across DAOs, repositories, and use cases.
 */
enum class PaperKind {
    Theory,
    Practical,
    Project;

    companion object {
        fun fromWireCode(code: String?): PaperKind = when (code?.uppercase()) {
            "PE" -> Practical
            "PA" -> Project
            else -> Theory
        }
    }
}
