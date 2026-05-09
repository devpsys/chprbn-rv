package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import ng.com.chprbn.mobile.R

data class CandidateScanResultUiState(
    val candidateName: String,
    val examNumberLine: String,
    val verificationSectionLabel: String,
    val identityVerifiedHeadline: String,
    val matchLabel: String,
    val examDateCaption: String,
    val examDateValue: String,
    val testingCenterCaption: String,
    val testingCenterValue: String,
) {
    companion object {
        /**
         * Builds the UiState from a (possibly URL-encoded, whitespace-padded,
         * blank) scanned payload. All user-facing copy is loaded from
         * res/values/strings.xml so labels are translatable; non-empty
         * scanned payloads are trimmed and substituted into the
         * `Exam Number: %1$s` format.
         */
        fun fromScannedPayload(
            scannedPayload: String,
            context: Context
        ): CandidateScanResultUiState {
            val examNumber = if (scannedPayload.isBlank()) {
                context.getString(R.string.candidate_scan_default_exam_number)
            } else {
                scannedPayload.trim()
            }
            return CandidateScanResultUiState(
                candidateName = context.getString(R.string.candidate_scan_default_name),
                examNumberLine = context.getString(
                    R.string.candidate_scan_exam_number_format,
                    examNumber
                ),
                verificationSectionLabel = context.getString(
                    R.string.candidate_scan_verification_section
                ),
                identityVerifiedHeadline = context.getString(
                    R.string.candidate_scan_identity_verified_headline
                ),
                matchLabel = context.getString(R.string.candidate_scan_match_label),
                examDateCaption = context.getString(R.string.candidate_scan_exam_date_caption),
                examDateValue = context.getString(R.string.candidate_scan_default_exam_date),
                testingCenterCaption = context.getString(
                    R.string.candidate_scan_testing_center_caption
                ),
                testingCenterValue = context.getString(
                    R.string.candidate_scan_default_testing_center
                ),
            )
        }
    }
}
