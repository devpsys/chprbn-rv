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

// Matches an IPv4 address optionally prefixed with '/' (OkHttp formats
// exception messages as `/216.219.94.166`) and optionally suffixed with
// `:port`. The leading `/` is consumed so `[server]` reads cleanly.
private val IPV4_PATTERN = Regex(
    """/?\b(?:\d{1,3}\.){3}\d{1,3}(?::\d{1,5})?\b""",
)

// Matches an IPv6 address in brackets (URL form) or bare with at least
// two colons — restrictive on the bare form so we don't accidentally
// eat innocent `hh:mm:ss` timestamps in the same message.
private val IPV6_PATTERN = Regex(
    """\[?[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4}){2,7}\]?(?::\d{1,5})?""",
)

// OkHttp's `SocketException` message includes phrases like "(port 443)"
// after the IP. Strips those trailing port disclosures — the port
// identifies the service (443 = HTTPS, or a non-standard proxy port)
// and shouldn't survive sanitisation on its own.
private val PORT_PHRASE_PATTERN = Regex(
    """\s*\(port\s+\d{1,5}\)""",
    RegexOption.IGNORE_CASE,
)

/**
 * Belt-and-braces sanitiser for messages the app renders through a
 * dialog verbatim (e.g. server-side envelope messages, cached
 * `syncError` rows). Strips URLs, FQDNs, IPv4 + IPv6 addresses, and
 * `"(port NNN)"` phrases — replaces each with `[server]` (or an empty
 * string, for the port suffix) so the sentence still reads. Safe to
 * call on any string; a message that carries none of those is
 * returned unchanged.
 *
 * The pre-fix Failed tab surfaced messages like
 * *"failed to connect to [server]/216.219.94.166 (port 443) from
 * /10.106.176.217 (port 38944) after 15000ms"* — the FQDN got
 * scrubbed by an earlier version of this helper, but the resolved
 * public IP + officer's private LAN IP were still both visible. That
 * exact leak is what the IPv4/IPv6/port patterns exist to close.
 *
 * Prefer [toUserFacingMessage] at the exception site — this is the
 * last-mile fallback for messages that originate outside the client
 * or that were persisted before the write-side sanitiser landed.
 */
fun String.stripHostsAndUrls(): String =
    replace(URL_PATTERN, "[server]")
        .replace(HOSTNAME_PATTERN, "[server]")
        .replace(IPV6_PATTERN, "[server]")
        .replace(IPV4_PATTERN, "[server]")
        .replace(PORT_PHRASE_PATTERN, "")
