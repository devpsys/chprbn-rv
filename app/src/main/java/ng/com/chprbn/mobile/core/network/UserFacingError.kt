package ng.com.chprbn.mobile.core.network

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

/**
 * Turns any network-adjacent throwable into a short message that is
 * safe to render in a dialog or toast. The backend hostname, URL, or
 * TLS cert details in the raw exception message must NEVER surface to
 * an officer — those leak infra topology and are useless as user-facing
 * instructions anyway.
 *
 * Every catch-and-surface site in a remote source / repository should
 * route errors through this helper (never a bare `t.message`) — the
 * pre-fix Download Failed dialog for a DNS failure read
 * *"Unable to resolve host 'jarabawa.chprbn.gov.ng': No address
 * associated with hostname"* — that's the exact class of leak this
 * exists to prevent.
 *
 * Logcat still gets the full throwable via `Log.e(TAG, msg, t)` at the
 * call sites, so on-device debugging isn't affected.
 */
fun Throwable.toUserFacingMessage(default: String): String = when (this) {
    is UnknownHostException,
    is ConnectException,
    is SocketTimeoutException,
    -> "Network error. Please check your internet connection and try again."
    is SSLHandshakeException,
    is SSLException,
    -> "Secure connection failed. Please try again in a moment."
    // IOException is the base for the network exceptions above — check
    // it last so more specific messages win.
    is IOException -> "Network error. Please check your internet connection and try again."
    // Non-network throwables: fall back to a caller-provided default
    // rather than leaking `t.message`, which could be anything from a
    // Room SQL string to a stack-trace fragment.
    else -> default
}

private val URL_PATTERN = Regex(
    """https?://[^\s"'<>()]+""",
    RegexOption.IGNORE_CASE,
)

// Matches an FQDN like "foo.bar.example.com" or "jarabawa.chprbn.gov.ng"
// with at least one dot. Excludes trailing punctuation so it doesn't
// consume the closing quote/colon in the raw exception text.
private val HOSTNAME_PATTERN = Regex(
    """\b(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.){1,}[A-Za-z]{2,}\b""",
)

/**
 * Belt-and-braces sanitiser for messages the app renders through a
 * dialog verbatim (e.g. server-side envelope messages). Strips
 * `http(s)://…` and dotted hostnames — replaces both with
 * `[server]` so the sentence still reads. Safe to call on any string;
 * a message that carries no URL/host is returned unchanged.
 *
 * Prefer [toUserFacingMessage] at the exception site — this is the
 * last-mile fallback for messages that originate outside the client.
 */
fun String.stripHostsAndUrls(): String =
    replace(URL_PATTERN, "[server]")
        .replace(HOSTNAME_PATTERN, "[server]")
