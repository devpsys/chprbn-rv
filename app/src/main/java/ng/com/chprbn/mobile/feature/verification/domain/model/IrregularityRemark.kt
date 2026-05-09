package ng.com.chprbn.mobile.feature.verification.domain.model

/**
 * Officer-selected irregularity category. Domain-pure: [apiValue] is the
 * stable wire identifier sent to the API; the user-facing label lives in
 * the presentation layer (see `IrregularityRemark.displayLabel(...)` in
 * `ReportIrregularityScreen.kt`) so domain stays Android-free.
 */
enum class IrregularityRemark(val apiValue: String) {
    Fake("fake"),
    OverDue("over_due"),
    LongOverDue("long_over_due")
}
