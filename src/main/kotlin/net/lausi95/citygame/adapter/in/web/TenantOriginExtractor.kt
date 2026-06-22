package net.lausi95.citygame.adapter.`in`.web

import jakarta.servlet.http.HttpServletRequest
import net.lausi95.citygame.common.Tenant
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException

/**
 * Extracts the raw frontend origin string from a request, without validating it (validation
 * belongs to the [Tenant] type). Resolution order, per ADR 0011:
 *
 * 1. The `X-TENANT-OVERRIDE` header, when `tenant.override.enabled` — for tests and tooling
 *    (curl/Postman), which carry no browser origin.
 * 2. The `Origin` header — sent by every `fetch()` call (CORS mode), including GETs.
 * 3. The origin of the `Referer` header — the only tenant signal on `<img src>` QR-code loads,
 *    which run in no-cors mode and send no `Origin`. The path/query are stripped and the origin
 *    rebuilt as `scheme://host[:port]`.
 *
 * Returns `null` when none is present; the caller decides whether that is an error.
 */
@Component
class TenantOriginExtractor(
    @Value($$"${tenant.override.enabled}") private val overrideEnabled: Boolean,
) {

    fun extract(request: HttpServletRequest): String? {
        if (overrideEnabled) {
            request.getHeader(Tenant.OVERRIDE_TENANT_HEADER_NAME)
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }
        request.getHeader(HttpHeaders.ORIGIN)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        request.getHeader(HttpHeaders.REFERER)
            ?.takeIf { it.isNotBlank() }
            ?.let { return originOf(it) }
        return null
    }

    private fun originOf(referer: String): String? =
        try {
            val uri = URI(referer)
            val scheme = uri.scheme
            val host = uri.host
            if (scheme.isNullOrBlank() || host.isNullOrBlank()) {
                null
            } else {
                buildString {
                    append(scheme).append("://").append(host)
                    if (uri.port != -1) append(":").append(uri.port)
                }
            }
        } catch (e: URISyntaxException) {
            null
        }
}
