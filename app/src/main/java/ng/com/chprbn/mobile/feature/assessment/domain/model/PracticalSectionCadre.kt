package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Maps a candidate indexing/exam number's leading letter onto the cadre
 * prefix used on practical section names, so the sections hub only shows
 * the sections that apply to that candidate.
 *
 * Candidate numbers look like `B/213/010/21`. Section names start with
 * one of CHEW, JCHEW, CHO, or BCHS:
 *
 * - A → CHO
 * - B → CHEW
 * - C → JCHEW
 * - D → BCHS
 */
object PracticalSectionCadre {

    fun prefixForExamNumber(examNumber: String): String? {
        val letter = examNumber.trim().firstOrNull()?.uppercaseChar() ?: return null
        return when (letter) {
            'A' -> "CHO"
            'B' -> "CHEW"
            'C' -> "JCHEW"
            'D' -> "BCHS"
            else -> null
        }
    }

    /**
     * True when [sectionTitle] belongs to the cadre encoded in [examNumber].
     * Matching is case-insensitive and requires the cadre token at the
     * start of the title, not as a substring of a longer token (so a
     * CHEW candidate does not see `JCHEW - …` sections).
     */
    fun matches(sectionTitle: String, examNumber: String): Boolean {
        val prefix = prefixForExamNumber(examNumber) ?: return false
        val title = sectionTitle.trim()
        if (!title.startsWith(prefix, ignoreCase = true)) return false
        val next = title.getOrNull(prefix.length) ?: return true
        return !next.isLetter()
    }

    fun filter(
        summaries: List<PracticalSectionSummary>,
        examNumber: String,
    ): List<PracticalSectionSummary> = summaries.filter { matches(it.section.title, examNumber) }
}
