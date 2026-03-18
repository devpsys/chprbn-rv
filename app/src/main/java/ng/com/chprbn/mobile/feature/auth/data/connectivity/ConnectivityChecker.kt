package ng.com.chprbn.mobile.feature.auth.data.connectivity

/**
 * Data-layer abstraction so offline decisions can be made without leaking Android into domain.
 */
interface ConnectivityChecker {
    fun isConnected(): Boolean
}

