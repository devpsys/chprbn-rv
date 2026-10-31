package ng.com.chprbn.mobile.feature.accreditation.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ssq_section_drafts",
    primaryKeys = ["assessmentId", "sectionKey"],
    indices = [Index("assessmentId")],
)
data class SsqSectionDraftEntity(
    val assessmentId: String,
    val sectionKey: String,
    val codeLabel: String,
    val rangeLabel: String,
    val title: String,
    val subtitle: String,
    val status: String,
    val payloadJson: String,
    val itemsCompleted: Int,
    val totalItems: Int,
)
