package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Per-candidate progress through one practical section.
 *
 * - `NotStarted`: no question in the section has been scored.
 * - `Incomplete`: at least one but not all questions are scored.
 * - `Complete`: every question in the section is scored.
 *
 * Derived in the repository from the score row count vs section's question
 * count; not persisted. Mirrored by the presentation enum of the same name —
 * the mapper at the presentation seam is a 1:1 copy.
 */
enum class PracticalSectionStatus { NotStarted, Incomplete, Complete }
