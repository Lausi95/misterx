package net.lausi95.citygame.adapter.`in`.web

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Registers [TenantArgumentResolver] so controllers can declare a plain `tenant: Tenant`
 * parameter, resolved centrally from the request origin (see ADR 0011).
 */
@Configuration
class WebMvcConfig(
    private val tenantOriginExtractor: TenantOriginExtractor,
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(TenantArgumentResolver(tenantOriginExtractor))
    }
}
