package ng.com.chprbn.mobile.core.domain.model

/**
 * Per-row sync state for every locally-mutated entity that must be uploaded to
 * the backend. `Pending` is the initial state on local insert; `Synced` is set
 * after the server accepts the row; `Failed` indicates the most recent upload
 * attempt failed and the row should be retried.
 *
 * Stored in Room as the enum's `name` string. There is also a feature-internal
 * copy at `feature.verification.domain.model.SyncStatus` that predates this
 * shared type; new features should depend on this one.
 */
enum class SyncStatus { Pending, Synced, Failed }
