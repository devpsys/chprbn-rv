package ng.com.chprbn.mobile.feature.accreditation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationAssignmentRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetHubResult
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationAssignmentRepository
import javax.inject.Inject

class AccreditationAssignmentRepositoryImpl @Inject constructor(
    private val remote: AccreditationAssignmentRemoteSource,
) : AccreditationAssignmentRepository {

    override suspend fun getHub(): GetHubResult = withContext(Dispatchers.IO) {
        try {
            GetHubResult.Success(remote.getHub())
        } catch (t: Throwable) {
            GetHubResult.Error(t.message ?: "Unable to load accreditation hub.")
        }
    }
}
