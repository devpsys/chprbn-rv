package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamStatisticsUiState(
    val recordsDownloaded: String,
    val attendanceCaptured: String,
    val syncedRecords: String,
    val recordsUpdatedLabel: String,
    val attendanceSubtitle: String,
    val syncProgressFraction: Float,
    val cachedBarFraction: Float,
    val syncedBarFraction: Float,
    val cachedCountLabel: String,
    val syncedCountLabel: String,
    val totalCountLabel: String,
    val pendingSyncLegendCount: String,
    val successfullySyncedLegendCount: String,
    val footnote: String,
    val illustrationImageUrl: String
) {
    companion object {
        fun placeholder(): ExamStatisticsUiState = ExamStatisticsUiState(
            recordsDownloaded = "1,250",
            attendanceCaptured = "1,180",
            syncedRecords = "1,050",
            recordsUpdatedLabel = "Updated 5m ago",
            attendanceSubtitle = "94.4% Completion",
            syncProgressFraction = 0.89f,
            cachedBarFraction = 0.12f,
            syncedBarFraction = 0.84f,
            cachedCountLabel = "130",
            syncedCountLabel = "1,050",
            totalCountLabel = "1,250",
            pendingSyncLegendCount = "130",
            successfullySyncedLegendCount = "1,050",
            footnote = "* 70 records remain uncaptured from the total downloaded set.",
            illustrationImageUrl = EXAM_STATISTICS_HERO_IMAGE_URL
        )
    }
}

internal const val EXAM_STATISTICS_HERO_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBFhLW1z8nlk8zFUnQ85RPVkuPpk03kepftRzIG7xb5QIJ14pMjqz2MJoGTDvLigGlit8i5LoHbjxn4m0f2QDdMcEOpJVBXZhy87LP1GmAU8YJ_k1GeO2SN7qlqh19TbZx101PDkVt4eUmIPu_9_6_A0u_BQJgQVijTjF-5scnJVtDAHnQDrY3px_GqxCvSgaQu61Grg4pvwnwd9lZWZOzK53oVJmiK1IGVpeGSBhoFlY-HuqEgGoCUlBCSU3mHw6xskS64ja016sFt"
