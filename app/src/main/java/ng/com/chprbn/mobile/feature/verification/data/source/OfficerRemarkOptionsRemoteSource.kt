package ng.com.chprbn.mobile.feature.verification.data.source

/**
 * Read-side abstraction for the officer-remark choices. Returns the
 * list on a successful 2xx + non-null data, or `null` on any failure
 * (network, non-2xx, empty body). The repository converts `null` into
 * the bundled fallback list owned by the resources layer.
 *
 * No `Fake` / `Composite` companions — the form already has a sensible
 * bundled fallback (`R.array.officer_remark_options`); a fake here
 * would be redundant.
 */
interface OfficerRemarkOptionsRemoteSource {
    suspend fun fetchOptions(): List<String>?
}
