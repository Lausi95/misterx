package net.lausi95.citygame.common

import java.net.URI
import java.net.URISyntaxException

/**
 * Raised when a request carries no usable tenant origin, or one that is not a canonical
 * `scheme://host[:port]` origin. Mapped to HTTP 400 by `HttpExceptionHandler`.
 */
class InvalidTenantOriginException(value: String?) : RuntimeException(
    "Tenant could not be resolved: '${value ?: "<none>"}' is not a valid frontend origin " +
        "(expected scheme://host[:port], e.g. https://foo.city-game.net)"
)

/**
 * An isolated customer/deployment partition, identified by the **frontend origin** the caller
 * is served at — `scheme://host[:port]`, e.g. `https://foo.city-game.net` or
 * `http://localhost:3000`. This is exactly what a browser emits in its `Origin` header and is
 * also the base URL of the tenant's QR codes (see ADR 0011).
 *
 * The value is a **canonical origin**: it must have a scheme and host, must not carry a path,
 * query, fragment or user-info, and must equal its own canonical reconstruction (so a trailing
 * slash is rejected, not trimmed). The invariant is enforced here, so a `Tenant` can never hold
 * an invalid origin.
 */
data class Tenant(val value: String) {

    init {
        val uri = try {
            URI(value)
        } catch (e: URISyntaxException) {
            throw InvalidTenantOriginException(value)
        }

        val scheme = uri.scheme
        val host = uri.host
        if (scheme.isNullOrBlank() || host.isNullOrBlank()) {
            throw InvalidTenantOriginException(value)
        }
        if (!uri.rawPath.isNullOrEmpty() || uri.rawQuery != null || uri.rawFragment != null || uri.rawUserInfo != null) {
            throw InvalidTenantOriginException(value)
        }

        val canonical = buildString {
            append(scheme).append("://").append(host)
            if (uri.port != -1) append(":").append(uri.port)
        }
        if (value != canonical) {
            throw InvalidTenantOriginException(value)
        }
    }

    companion object {
        const val OVERRIDE_TENANT_HEADER_NAME = "X-TENANT-OVERRIDE"

        /**
         * Builds a [Tenant] from a raw origin string, throwing [InvalidTenantOriginException]
         * when it is absent or malformed.
         */
        fun fromOrigin(rawOrigin: String?): Tenant {
            if (rawOrigin.isNullOrBlank()) {
                throw InvalidTenantOriginException(rawOrigin)
            }
            return Tenant(rawOrigin)
        }
    }
}
