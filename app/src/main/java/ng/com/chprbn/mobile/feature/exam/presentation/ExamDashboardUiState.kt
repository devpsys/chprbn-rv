package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamTaskCardUiState(
    val imageUrl: String,
    val imageContentDescription: String,
    val chipPrimaryLabel: String,
    val chipSecondaryLabel: String,
    val title: String,
    val description: String,
    val primaryActionLabel: String
)

data class ExamDashboardUiState(
    val institutionSectionLabel: String,
    val institutionName: String,
    val institutionCode: String,
    val institutionLocation: String,
    val heroImageUrl: String,
    val attendanceTask: ExamTaskCardUiState,
    val practicalTask: ExamTaskCardUiState,
    /** True once a dossier has been successfully downloaded to this device. */
    val hasDownloadedData: Boolean = false,
    /** True when the downloaded center has at least one paper scheduled today. */
    val hasSchedules: Boolean = true,
    /** True when the dossier's `sections` array was non-empty — gates the Practical Assessment card. */
    val hasPracticalAssessment: Boolean = true,
) {
    companion object {
        fun placeholder(): ExamDashboardUiState = ExamDashboardUiState(
            institutionSectionLabel = "Institution Details",
            institutionName = "National Institute of Health Sciences",
            institutionCode = "#NIH-2024",
            institutionLocation = "Lagos Central Campus",
            heroImageUrl = HERO_IMAGE_URL,
            attendanceTask = ExamTaskCardUiState(
                imageUrl = ATTENDANCE_CARD_IMAGE_URL,
                imageContentDescription = "Workspace with calendar and tablet",
                chipPrimaryLabel = "CBT",
                chipSecondaryLabel = "Active Session",
                title = "Attendance Monitoring",
                description = "Verify and log the candidate's presence for the current CBT. View historical attendance logs for compliance checks.",
                primaryActionLabel = "Log Attendance"
            ),
            practicalTask = ExamTaskCardUiState(
                imageUrl = PRACTICAL_CARD_IMAGE_URL,
                imageContentDescription = "Laboratory practical assessment",
                chipPrimaryLabel = "Practical",
                chipSecondaryLabel = "Pending Grading",
                title = "Practical Assessment",
                description = "Input performance scores based on defined rubric criteria. Record examiner observations and finalize the practical evaluation.",
                primaryActionLabel = "Grade Practical"
            )
        )
    }
}

internal const val HERO_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAusAPQ6YXAm1g0vtx7KTUAm8_NpP3uXARQrWkiFrb_2eEChpofuOUSaPVOwL_Z6I5rKLiYZsnvyWD0v5fX9TtyDehLqy0si_d8wc05n68lwaxaPehjHQMjwhxbbKCjrTNM2HUGxofNlpyY4s3UtYwywtwYyT76-gTE-TXT_3HdKr0w_ZHI3moIdIWR4bfMNJh-kiIHYRNPScgjScEyFAPg68airbmIhvP35etm5E7bVWIbg72Xypa_vffqZgE-eynJ_JU94IRKwEaI"

internal const val ATTENDANCE_CARD_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCL5dl_MgSsuFCGLcwDKSBe9ZRc4JDKLTzyUzOoeTLX_Ae9eGg1ydcRrFU4lRp4WEiiW5alNg6xeVWMfprK8WO69ajlvmpJ8kTuOcS7Ej2aADm884zsPDi9FAB6i4ei2CQPYh9i8J1SiCelsDy4F6cyp2Eng3mRcMk_bjaLo_j9S2OKjKwhsEsnvhuPFUlcfpR2wvOhTitvJV0zTMJ3w4V8nt2upo1i-_X8MM1w2quWUgNZbxzcyqW1HoOr3Ag8nQO9PQpT53KrIqY5"

internal const val PRACTICAL_CARD_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuD1JlMDIyi1hRlGQqjXUHB6SMOC81grLcAmoPY_OmsDXXQcYsqVyxaMXW-WBXsTVyLzlTswJZuEd36Ym-Qab4u90DAaAerd83qb2JsaaVEoGZXsO94Bk6ctY_DMp2WHcJw1XxLqfSWIwuCCvnpdmSN6E0qfslirAxIANyL1ItrLWZl8mvs4YsDLOIpfWHdNmLahmsd3emNfUvOF75SsrshKfKGZ7AdWJ22vCK5fdeSbyDDeRIb7awjJprAV8lMg4XK2jbwjnjMpWiBe"
