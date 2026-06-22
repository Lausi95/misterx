package net.lausi95.citygame.adapter.`in`.web

import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

/**
 * Builds absolute frontend URLs (the targets encoded into QR codes).
 *
 * The origin (`scheme://host[:port]`) is the tenant itself — the tenant *is* the frontend
 * origin (see ADR 0011), so QR origin and tenant identity are the same string and cannot drift.
 *
 * Query values are percent-encoded via URI-variable expansion — only the variables are encoded,
 * never the structural parts of the URL.
 */
@Component
class FrontendUriFactory {

    fun buildUrl(tenant: Tenant, path: String, queryParams: Map<String, String>): String {
        val builder = UriComponentsBuilder.fromUriString(tenant.value)
            .path(path)
        queryParams.keys.forEach { name -> builder.queryParam(name, "{$name}") }
        return builder
            .encode()
            .buildAndExpand(queryParams)
            .toUriString()
    }
}
