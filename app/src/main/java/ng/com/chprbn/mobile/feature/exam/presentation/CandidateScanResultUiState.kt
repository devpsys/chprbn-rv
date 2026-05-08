package ng.com.chprbn.mobile.feature.exam.presentation

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
        fun fromScannedPayload(scannedPayload: String): CandidateScanResultUiState {
            val examNumber =
                if (scannedPayload.isBlank()) "ABC-12345-XY" else scannedPayload.trim()
            return CandidateScanResultUiState(
                candidateName = "Johnathan Doe",
                examNumberLine = "Exam Number: $examNumber",
                verificationSectionLabel = "Identity Verification",
                identityVerifiedHeadline = "Identity Verified",
                matchLabel = "MATCH 98%",
                examDateCaption = "Exam Date",
                examDateValue = "Oct 24, 2023",
                testingCenterCaption = "Testing Center",
                testingCenterValue = "Hall B - Room 12",
            )
        }
    }
}
