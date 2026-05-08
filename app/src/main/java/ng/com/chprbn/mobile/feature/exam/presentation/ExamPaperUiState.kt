package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamPaperUiState(
    val institutionHeroImageUrl: String,
    val institutionShortCode: String,
    val institutionCodeLabel: String,
    val institutionName: String,
    val institutionLocation: String,
    val sessionLabel: String,
    val paperTitle: String,
    val totalCandidates: String,
    val verifiedPresent: String,
    val attendanceProgressFraction: Float,
    val attendanceProgressLabel: String,
    val attendancePercentLabel: String,
    val lastUpdatedLabel: String,
    val syncStatusLabel: String,
    val infoTitle: String,
    val infoMessage: String
) {
    companion object {
        fun placeholder(): ExamPaperUiState = ExamPaperUiState(
            institutionHeroImageUrl = EXAM_PAPER_HERO_IMAGE_URL,
            institutionShortCode = "NIT-405",
            institutionCodeLabel = "Institution Code",
            institutionName = "National Institute of Technology",
            institutionLocation = "Bangalore, Karnataka • Sector 4, HSR Layout",
            sessionLabel = "Upcoming Session",
            paperTitle = "Mathematics - Paper II",
            totalCandidates = "120",
            verifiedPresent = "85",
            attendanceProgressFraction = 0.7f,
            attendanceProgressLabel = "85 of 120 candidates checked in",
            attendancePercentLabel = "70%",
            lastUpdatedLabel = "Last updated: 2 mins ago",
            syncStatusLabel = "Cloud Synced",
            infoTitle = "Verification ongoing",
            infoMessage = "Please ensure all biometric data and QR codes are scanned before the session start time (10:00 AM)."
        )
    }
}

internal const val EXAM_PAPER_HERO_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAusAPQ6YXAm1g0vtx7KTUAm8_NpP3uXARQrWkiFrb_2eEChpofuOUSaPVOwL_Z6I5rKLiYZsnvyWD0v5fX9TtyDehLqy0si_d8wc05n68lwaxaPehjHQMjwhxbbKCjrTNM2HUGxofNlpyY4s3UtYwywtwYyT76-gTE-TXT_3HdKr0w_ZHI3moIdIWR4bfMNJh-kiIHYRNPScgjScEyFAPg68airbmIhvP35etm5E7bVWIbg72Xypa_vffqZgE-eynJ_JU94IRKwEaI"
