package net.lausi95.citygame.adapter.`in`.web

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger { }

/**
 * Puts the caller's tenant origin into the logging MDC for request correlation. This is a
 * best-effort logging concern only — it never validates or rejects. Tenant validation (and the
 * 400 on a missing/malformed origin) happens in `TenantArgumentResolver`, inside the
 * DispatcherServlet, so it flows through the standard `HttpExceptionHandler`. See ADR 0011.
 */
@Component
class TenantFilter(
    private val tenantOriginExtractor: TenantOriginExtractor,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val origin = tenantOriginExtractor.extract(request)
        MDC.put("tenant", origin ?: "unknown")
        log.info { "Tenant origin: ${origin ?: "unknown"}" }
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("tenant")
        }
    }
}
