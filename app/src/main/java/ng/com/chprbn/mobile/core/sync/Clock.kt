package ng.com.chprbn.mobile.core.sync

/**
 * Tiny injectable wall-clock abstraction. The default Hilt-provided binding
 * returns [System.currentTimeMillis]; tests substitute a [FakeClock] (in
 * test/) to pin time. Kept inside `core.sync` because nothing else needs it
 * yet; promote to `core.time` if more callers appear.
 */
fun interface Clock {
    fun nowMillis(): Long

    companion object {
        val System: Clock = Clock { java.lang.System.currentTimeMillis() }
    }
}
