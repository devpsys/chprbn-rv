package ng.com.chprbn.mobile.feature.exam.domain.model

/**
 * Severity band for a candidate remark. Drives the icon + color on the
 * candidate row's "remark" indicator. `Info` is the default — most remarks
 * are simple notes ("arrived late, no documents").
 */
enum class RemarkSeverity { Info, Warning, Critical }
