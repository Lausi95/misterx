package net.lausi95.citygame.infrastructure.web

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import net.lausi95.citygame.domain.Tenant
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger { }

@Component
class TenantFilter(
    @Value($$"${tenant.override.enabled}") private val tenantOverrideEnabled: Boolean
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val tenant = Tenant(determineTenant(request))

        MDC.put("tenant", tenant.value)
        request.setAttribute("tenant", tenant)

        log.info { "Tenant determined" }
        log.debug { "Tenant: ${tenant.value}" }

        filterChain.doFilter(request, response)
    }

    private fun determineTenant(request: HttpServletRequest): String {
        if (tenantOverrideEnabled) {
            val tenantFromHeader: String? = request.getHeader(Tenant.OVERRIDE_TENANT_HEADER_NAME)
            if (tenantFromHeader != null) {
                return tenantFromHeader
            }
        }

        return request.remoteHost
    }
}
