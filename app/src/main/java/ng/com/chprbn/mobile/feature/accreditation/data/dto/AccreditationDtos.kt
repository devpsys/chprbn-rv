package ng.com.chprbn.mobile.feature.accreditation.data.dto

import com.google.gson.annotations.SerializedName

/** Canonical `{success, message, data}` envelope (tolerates legacy `status`). */
data class AccreditationAssignmentsEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationAssignmentsDataDto? = null,
)

data class AccreditationAssignmentsDataDto(
    val officer: AccreditationOfficerDto? = null,
    @SerializedName("feature_grid")
    val featureGrid: AccreditationFeatureGridDto? = null,
    @SerializedName("assigned_institutions")
    val assignedInstitutions: List<AccreditationAssignedInstitutionDto> = emptyList(),
    @SerializedName("upcoming_institutions")
    val upcomingInstitutions: List<AccreditationUpcomingInstitutionDto> = emptyList(),
)

data class AccreditationOfficerDto(
    val initials: String? = null,
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("registration_label")
    val registrationLabel: String? = null,
    @SerializedName("is_verified")
    val isVerified: Boolean? = null,
)

data class AccreditationFeatureGridDto(
    @SerializedName("start_subtitle")
    val startSubtitle: String? = null,
    @SerializedName("continue_subtitle")
    val continueSubtitle: String? = null,
    @SerializedName("drafts_subtitle")
    val draftsSubtitle: String? = null,
    @SerializedName("sync_subtitle")
    val syncSubtitle: String? = null,
    @SerializedName("is_online")
    val isOnline: Boolean? = null,
)

data class AccreditationAssignedInstitutionDto(
    val id: String,
    val name: String,
    @SerializedName("location_label")
    val locationLabel: String? = null,
    @SerializedName("protocol_label")
    val protocolLabel: String? = null,
    @SerializedName("last_synced_label")
    val lastSyncedLabel: String? = null,
    @SerializedName("sync_status")
    val syncStatus: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
)

data class AccreditationUpcomingInstitutionDto(
    val id: String,
    val name: String,
    @SerializedName("schedule_label")
    val scheduleLabel: String? = null,
)

data class AccreditationInstitutionEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationInstitutionDataDto? = null,
)

data class AccreditationInstitutionDataDto(
    val id: String,
    val name: String,
    @SerializedName("ownership_subtitle")
    val ownershipSubtitle: String? = null,
    @SerializedName("photo_url")
    val photoUrl: String? = null,
    @SerializedName("sync_status")
    val syncStatus: String? = null,
    @SerializedName("last_synced_label")
    val lastSyncedLabel: String? = null,
    @SerializedName("facility_code")
    val facilityCode: String? = null,
    @SerializedName("last_visit_label")
    val lastVisitLabel: String? = null,
    @SerializedName("ownership_type")
    val ownershipType: String? = null,
    val state: String? = null,
    val zone: String? = null,
    @SerializedName("campus_address")
    val campusAddress: String? = null,
    val programmes: List<String> = emptyList(),
    @SerializedName("programmes_accredited_count")
    val programmesAccreditedCount: Int? = null,
    @SerializedName("academic_staff_count")
    val academicStaffCount: Int? = null,
    @SerializedName("technical_staff_count")
    val technicalStaffCount: Int? = null,
    @SerializedName("admin_staff_count")
    val adminStaffCount: Int? = null,
    @SerializedName("tech_to_academic_ratio")
    val techToAcademicRatio: String? = null,
    @SerializedName("capital_base_label")
    val capitalBaseLabel: String? = null,
    val history: List<AccreditationHistoryItemDto> = emptyList(),
)

data class AccreditationHistoryItemDto(
    val id: String,
    @SerializedName("cycle_label")
    val cycleLabel: String? = null,
    @SerializedName("date_label")
    val dateLabel: String? = null,
    @SerializedName("score_label")
    val scoreLabel: String? = null,
    @SerializedName("grade_label")
    val gradeLabel: String? = null,
    @SerializedName("decision_label")
    val decisionLabel: String? = null,
)

data class AccreditationCurrentAssessmentEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationCurrentAssessmentDataDto? = null,
)

data class AccreditationCurrentAssessmentDataDto(
    val draft: AccreditationDraftDto,
    val sections: List<AccreditationSectionDraftDto> = emptyList(),
    val criteria: AccreditationCriteriaDraftDto? = null,
    val scores: AccreditationScoreDraftDto? = null,
)

data class AccreditationDraftDto(
    val id: String,
    @SerializedName("institution_id")
    val institutionId: String,
    @SerializedName("institution_name")
    val institutionName: String? = null,
    @SerializedName("cycle_label")
    val cycleLabel: String? = null,
    @SerializedName("facility_code")
    val facilityCode: String? = null,
    @SerializedName("location_label")
    val locationLabel: String? = null,
    val status: String? = null,
    @SerializedName("updated_at")
    val updatedAt: Long? = null,
)

data class AccreditationSectionDraftDto(
    @SerializedName("section_key")
    val sectionKey: String,
    @SerializedName("code_label")
    val codeLabel: String? = null,
    @SerializedName("range_label")
    val rangeLabel: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val status: String? = null,
    @SerializedName("payload_json")
    val payloadJson: String? = null,
    @SerializedName("items_completed")
    val itemsCompleted: Int? = null,
    @SerializedName("total_items")
    val totalItems: Int? = null,
)

data class AccreditationCriteriaDraftDto(
    @SerializedName("payload_json")
    val payloadJson: String? = null,
    @SerializedName("evaluated_count")
    val evaluatedCount: Int? = null,
    @SerializedName("total_criteria")
    val totalCriteria: Int? = null,
    val status: String? = null,
)

data class AccreditationScoreDraftDto(
    @SerializedName("payload_json")
    val payloadJson: String? = null,
    @SerializedName("completed_count")
    val completedCount: Int? = null,
    @SerializedName("total_groups")
    val totalGroups: Int? = null,
    val status: String? = null,
)

data class AccreditationDraftSyncRequestDto(
    @SerializedName("institution_id")
    val institutionId: String,
    @SerializedName("cycle_label")
    val cycleLabel: String,
    @SerializedName("facility_code")
    val facilityCode: String? = null,
    @SerializedName("location_label")
    val locationLabel: String? = null,
    val sections: List<AccreditationSectionSyncItemDto> = emptyList(),
    val criteria: AccreditationCriteriaDraftDto? = null,
    val scores: AccreditationScoreDraftDto? = null,
    val attachments: List<AccreditationAttachmentMetaDto> = emptyList(),
    @SerializedName("updated_at")
    val updatedAt: Long? = null,
)

data class AccreditationSectionSyncItemDto(
    @SerializedName("section_key")
    val sectionKey: String,
    val status: String,
    @SerializedName("payload_json")
    val payloadJson: String,
    @SerializedName("items_completed")
    val itemsCompleted: Int,
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("code_label")
    val codeLabel: String? = null,
    @SerializedName("range_label")
    val rangeLabel: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
)

data class AccreditationAttachmentMetaDto(
    val id: String,
    @SerializedName("parent_key")
    val parentKey: String,
    val title: String,
    @SerializedName("is_photo")
    val isPhoto: Boolean,
    @SerializedName("size_label")
    val sizeLabel: String? = null,
    @SerializedName("location_label")
    val locationLabel: String? = null,
    @SerializedName("upload_state")
    val uploadState: String? = null,
)

data class AccreditationSyncEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationSyncDataDto? = null,
)

data class AccreditationSyncDataDto(
    @SerializedName("assessment_id")
    val assessmentId: String? = null,
    @SerializedName("synced_at")
    val syncedAt: Long? = null,
)

data class AccreditationSubmitEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationSubmitDataDto? = null,
)

data class AccreditationSubmitDataDto(
    @SerializedName("decision_id")
    val decisionId: String,
)

data class AccreditationDecisionEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: AccreditationDecisionDataDto? = null,
)

data class AccreditationDecisionDataDto(
    @SerializedName("decision_id")
    val decisionId: String,
    @SerializedName("institution_id")
    val institutionId: String,
    @SerializedName("institution_name")
    val institutionName: String? = null,
    @SerializedName("location_label")
    val locationLabel: String? = null,
    @SerializedName("facility_code")
    val facilityCode: String? = null,
    @SerializedName("assessment_date_label")
    val assessmentDateLabel: String? = null,
    @SerializedName("grade_label")
    val gradeLabel: String? = null,
    @SerializedName("decision_label")
    val decisionLabel: String? = null,
    @SerializedName("accreditation_period")
    val accreditationPeriod: String? = null,
    @SerializedName("overall_score_percent_label")
    val overallScorePercentLabel: String? = null,
    @SerializedName("student_quota_label")
    val studentQuotaLabel: String? = null,
    @SerializedName("receipt_ref")
    val receiptRef: String? = null,
    @SerializedName("raw_score_rows")
    val rawScoreRows: List<Map<String, Any?>>? = null,
    @SerializedName("raw_score_rows_json")
    val rawScoreRowsJson: String? = null,
    val conditions: List<String>? = null,
    @SerializedName("conditions_json")
    val conditionsJson: String? = null,
)

data class AccreditationUpsertEnvelopeDto(
    @SerializedName(value = "success", alternate = ["status"])
    val success: Boolean,
    val message: String? = null,
    val data: Map<String, Any?>? = null,
)
