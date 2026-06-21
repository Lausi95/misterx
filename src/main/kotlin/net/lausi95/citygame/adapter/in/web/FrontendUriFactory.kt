package net.lausi95.citygame.adapter.`in`.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder

/**
 * Builds absolute frontend URLs (the targets encoded into QR codes).
 *
 * The origin (`scheme://host[:port]`) is derived from the **incoming request** via
 * [ServletUriComponentsBuilder], which honours `X-Forwarded-*` headers so the public domain is
 * used behind a proxy. In the intended production topology each tenant is served from its own
 * domain (`foo.city-game.net`) and the API is reached at that same domain, so the request host
 * already is the tenant's frontend origin.
 *
 * Where the frontend lives on a different origin than the API (notably local development: API on
 * `:8080`, frontend on `:3000`) the optional `frontend.base-url` property overrides the
 * request-derived origin. It is set in the `local` profile and left unset in production.
 *
 * Query values are percent-encoded via URI-variable expansion — only the variables are encoded,
 * never the structural parts of the URL (e.g. the `host:port` of a `localhost:3000` override).
 *
 * See ADR 0006.
 */
@Component
class FrontendUriFactory(
    @Value($$"${frontend.base-url:#{null}}") private val frontendBaseUrl: String?,
) {

    fun buildUrl(path: String, queryParams: Map<String, String>): String {
        val builder = originBuilder()
            .path(path)
        queryParams.keys.forEach { name -> builder.queryParam(name, "{$name}") }
        return builder
            .encode()
            .buildAndExpand(queryParams)
            .toUriString()
    }

    private fun originBuilder(): UriComponentsBuilder =
        if (!frontendBaseUrl.isNullOrBlank()) {
            UriComponentsBuilder.fromUriString(frontendBaseUrl)
        } else {
            ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath(null)
                .replaceQuery(null)
        }
}
