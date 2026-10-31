package ng.com.chprbn.mobile.feature.accreditation.domain.repository

import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetHubResult

interface AccreditationAssignmentRepository {
    suspend fun getHub(): GetHubResult
}
