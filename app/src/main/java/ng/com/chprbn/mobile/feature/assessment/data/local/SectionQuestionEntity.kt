package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One scoreable question in a practical section. `imageUrl` may be `null`
 * for prompts that don't need an illustrative photo.
 */
@Entity(
    tableName = "section_questions",
    indices = [Index("sectionId")],
)
data class SectionQuestionEntity(
    @PrimaryKey val id: String,
    val sectionId: String,
    val number: Int,
    val prompt: String,
    val imageUrl: String? = null,
    val maxScore: Int,
)
