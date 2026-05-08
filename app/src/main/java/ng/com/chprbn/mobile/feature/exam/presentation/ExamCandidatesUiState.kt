package ng.com.chprbn.mobile.feature.exam.presentation

data class ExamCandidateUiState(
    val avatarUrl: String,
    val name: String,
    val idLabel: String,
    val statusPillLabel: String,
    val statusSubLabel: String,
)

data class ExamCandidatesUiState(
    val searchQuery: String,
    val activeFilterLabel: String,
    val filterLabels: List<String>,
    val candidates: List<ExamCandidateUiState>
) {
    companion object {
        fun placeholder(): ExamCandidatesUiState = ExamCandidatesUiState(
            searchQuery = "",
            activeFilterLabel = "All",
            filterLabels = listOf("All", "Signed In", "Signed Out", "Flagged"),
            candidates = listOf(
                ExamCandidateUiState(
                    avatarUrl = MARCUS_AVATAR_URL,
                    name = "Marcus Thompson",
                    idLabel = "ID: EX-2024-0892",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "08:45 AM"
                ),
                ExamCandidateUiState(
                    avatarUrl = SARAH_AVATAR_URL,
                    name = "Sarah Jenkins",
                    idLabel = "ID: EX-2024-1023",
                    statusPillLabel = "Signed Out",
                    statusSubLabel = "Pending"
                ),
                ExamCandidateUiState(
                    avatarUrl = DAVID_AVATAR_URL,
                    name = "David Chen",
                    idLabel = "ID: EX-2024-0741",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "09:12 AM"
                ),
                ExamCandidateUiState(
                    avatarUrl = ELENA_AVATAR_URL,
                    name = "Elena Rodriguez",
                    idLabel = "ID: EX-2024-0556",
                    statusPillLabel = "Signed In",
                    statusSubLabel = "08:58 AM"
                )
            )
        )
    }
}

internal const val MARCUS_AVATAR_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuDXW4F_85x__4fZk3jOpu1b3j9L2DciqqcPtN6cOc9mL5aAjAg7XmS6YI1QIUenspwZ2nvrCBAFlhn1KwBldxNKudMqlF36hLqPiehcbMzjTpAbIggbgW2kOgv6WUijtpIeDATBxHlHB3BqW2kLKPguisImudTj_gqPEfAOFsXriGZ8B8uU2ldgdO3jxidSxc_unIOUG-wEPRfh1eZV7lnF1UpJ6JcycPqSXjhPlOReU3PGx73O59CblJzdaVAyAyMZw_9qqiDDw3Gp"

internal const val SARAH_AVATAR_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuB8ZO_wKoSL_vn7A4B-6N9p1CBXDJyb6FG4dXcYaqib3zwbgzKM0REcgpbXdbW0uPCgsiJDOGs-T4t5XTBkhF5Q0y4QnXyD8CGMpzsA1W76wzIqlYgSuku6UbGUV8LZpRS9gEjoOCHg-aHeg94cXBM53iYUbrTlVMRo1QujXjUNApcsRTpzAbjldW-grR3xBnsvyTiTEizsihQIiet_tPFjuEV8y0GBpmevwzmGxL3EEUh-2WU2TF3S-kYaB2Dhpx8uUGnM9NZCAJci"

internal const val DAVID_AVATAR_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuDjDU71JNOYpP-WmaF3nhKTlJhdAXqUf0CWwHV8hR43IntY4tQecqsULhq5wdijviUvEAlcPQWc8tCOTpktNuj64Pzs11KFqw0ziSs2m9eoWan8WY9dQCFhE5WM8CFrdg9PiPCOFmGN1qPmZVUouEf-KMQoYxQvwNtag6nAH3pBktL1kEm8FODMHAZZWKQv0sJek5MWpYtaPuDeMDoLaS-"

internal const val ELENA_AVATAR_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBY07q9sgrbrb8bSgtSprRBbS0fu7Kr2ERITz3nTLe0RiSL79d2KPPAX74R0zlRSELA2i-lrDGtcK5tNeX9Or3xOz_zha5Z5xS7YdH5V4N-yq0wQOCZI53isYXavALkBppTltpvNxV73a_N-VbpFV9qrbPJDeYKECSJWqcwLpESPWD-TbCvhnimg1KI8f995dViTL3YpleocepDXALC3ZtkZU5k5T_-l_7dDUfrazCXs-AYlt1KJ4YR6-oTaX8Ws_AuB9Hwnatb1kt0"

