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
    val attendanceRatePercent: String,
    val currentGpa: String,
    val heroImageUrl: String,
    val attendanceTask: ExamTaskCardUiState,
    val practicalTask: ExamTaskCardUiState
) {
    companion object {
        fun placeholder(): ExamDashboardUiState = ExamDashboardUiState(
            institutionSectionLabel = "Institution Details",
            institutionName = "National Institute of Health Sciences",
            institutionCode = "#NIH-2024",
            institutionLocation = "Lagos Central Campus",
            attendanceRatePercent = "94%",
            currentGpa = "3.72",
            heroImageUrl = HERO_IMAGE_URL,
            attendanceTask = ExamTaskCardUiState(
                imageUrl = ATTENDANCE_CARD_IMAGE_URL,
                imageContentDescription = "Workspace with calendar and tablet",
                chipPrimaryLabel = "Theory",
                chipSecondaryLabel = "Active Session",
                title = "Attendance Monitoring",
                description = "Verify and log the candidate's presence for the current theory examination. View historical attendance logs for compliance checks.",
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
    "https://lh3.googleusercontent.com/aida-public/AB6AXuAEr3lb0UZmx4qxWj8t1N9l6NG11fHOjJVmh0Y_LvMpbiWbJ5wSsgkz-IRkeHBBJRay-C1356wjAtP_2tG4wBg4nYwrzrGFcYLvABHlUE1pIj5ud3v5m4q6-mqXKlkDQawdAf91_YeeLGu3W6BTZp5XeDv6Nf7Mhk7yeg2__0hzb4ioLoAU4eNgUXRqX2VLIqw0HK5KXKLLPA3yfV-cU-PDCFlAqEWzR3l3mE9jihH0nN5hQPUNDSeAgchArpRr3LhGL8R1-se-Mp3y"

internal const val ATTENDANCE_CARD_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuCL5dl_MgSsuFCGLcwDKSBe9ZRc4JDKLTzyUzOoeTLX_Ae9eGg1ydcRrFU4lRp4WEiiW5alNg6xeVWMfprK8WO69ajlvmpJ8kTuOcS7Ej2aADm884zsPDi9FAB6i4ei2CQPYh9i8J1SiCelsDy4F6cyp2Eng3mRcMk_bjaLo_j9S2OKjKwhsEsnvhuPFUlcfpR2wvOhTitvJV0zTMJ3w4V8nt2upo1i-_X8MM1w2quWUgNZbxzcyqW1HoOr3Ag8nQO9PQpT53KrIqY5"

internal const val PRACTICAL_CARD_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuD1JlMDIyi1hRlGQqjXUHB6SMOC81grLcAmoPY_OmsDXXQcYsqVyxaMXW-WBXsTVyLzlTswJZuEd36Ym-Qab4u90DAaAerd83qb2JsaaVEoGZXsO94Bk6ctY_DMp2WHcJw1XxLqfSWIwuCCvnpdmSN6E0qfslirAxIANyL1ItrLWZl8mvs4YsDLOIpfWHdNmLahmsd3emNfUvOF75SsrshKfKGZ7AdWJ22vCK5fdeSbyDDeRIb7awjJprAV8lMg4XK2jbwjnjMpWiBe"
