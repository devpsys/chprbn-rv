package ng.com.chprbn.mobile.feature.exam.presentation

enum class ExamPaperAttendanceStatus {
    Completed,
    Active,
    Upcoming
}

enum class ExamPaperIconKind {
    Description,
    EditNote,
    Science
}

data class ExamPaperCardUiState(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: ExamPaperAttendanceStatus,
    val timeLabel: String,
    val groupOrLocationLabel: String,
    val iconKind: ExamPaperIconKind,
    val primaryActionLabel: String? = null,
)

data class ExamPapersUiState(
    val dailyOverviewTitle: String,
    val dailyDateLabel: String,
    val totalPapersLabel: String,
    val studentsLabel: String,
    val statusPillLabel: String,
    val papers: List<ExamPaperCardUiState>
) {
    companion object {
        fun placeholder(): ExamPapersUiState = ExamPapersUiState(
            dailyOverviewTitle = "Daily Overview",
            dailyDateLabel = "Monday, June 12",
            totalPapersLabel = "03",
            studentsLabel = "142",
            statusPillLabel = "In Progress",
            papers = listOf(
                ExamPaperCardUiState(
                    id = "p1",
                    title = "Paper I (P1)",
                    subtitle = "General English & Literacy",
                    status = ExamPaperAttendanceStatus.Completed,
                    timeLabel = "09:00 - 11:00 AM",
                    groupOrLocationLabel = "142 Present",
                    iconKind = ExamPaperIconKind.Description
                ),
                ExamPaperCardUiState(
                    id = "p2",
                    title = "Paper II (P2)",
                    subtitle = "Advanced Mathematics",
                    status = ExamPaperAttendanceStatus.Active,
                    timeLabel = "12:00 - 02:00 PM",
                    groupOrLocationLabel = "89/142 Checked",
                    iconKind = ExamPaperIconKind.EditNote,
                    primaryActionLabel = "Mark Attendance"
                ),
                ExamPaperCardUiState(
                    id = "p3",
                    title = "Paper III (P3)",
                    subtitle = "Physical Sciences",
                    status = ExamPaperAttendanceStatus.Upcoming,
                    timeLabel = "03:00 - 05:00 PM",
                    groupOrLocationLabel = "Main Hall A",
                    iconKind = ExamPaperIconKind.Science
                )
            )
        )
    }
}

