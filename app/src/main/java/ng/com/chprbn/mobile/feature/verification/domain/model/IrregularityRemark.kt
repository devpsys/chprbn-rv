package ng.com.chprbn.mobile.feature.verification.domain.model

/**
 * Officer-selected irregularity category.
 * [apiValue] is reserved for a future HTTP payload.
 */
enum class IrregularityRemark(val displayLabel: String, val apiValue: String) {
    Fake("Fake", "fake"),
    OverDue("Over Due", "over_due"),
    LongOverDue("Long Over Due", "long_over_due")
}
