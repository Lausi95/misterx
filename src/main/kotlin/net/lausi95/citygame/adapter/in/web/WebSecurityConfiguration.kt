package net.lausi95.citygame.adapter.`in`.web

import net.lausi95.citygame.common.InvalidTenantOriginException
import net.lausi95.citygame.common.Tenant
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

/**
 * OAuth2 resource server protecting only the organizer/management tree under "/games".
 * Every other endpoint is intentionally public — participant clients are identified by
 * request headers, not a security principal. See ADR 0007.
 *
 * CORS reflects the caller's origin, since the API is a single shared host called cross-origin
 * by every tenant frontend; the set of accepted origins mirrors the (currently open) set of
 * valid tenants. See ADR 0012.
 */
@Configuration
class WebSecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests {
            it.requestMatchers("/games/**").authenticated()
                .anyRequest().permitAll()
        }
        .cors(Customizer.withDefaults())
        .oauth2ResourceServer { it.jwt {} }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .csrf { it.disable() }
        .build()

    /**
     * Reflects the caller's `Origin` back as the allowed origin — but only when it is a valid
     * tenant origin (the same `Tenant` validation the resolver uses). A request whose origin is
     * not a legitimate tenant gets no CORS grant. This keeps the accepted-origin set in lockstep
     * with the accepted-tenant set: any future tenant allowlist applied in `Tenant` is inherited
     * here for free. See ADR 0012.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = CorsConfigurationSource { request ->
        val origin = request.getHeader(HttpHeaders.ORIGIN) ?: return@CorsConfigurationSource null
        val validTenantOrigin = try {
            Tenant.fromOrigin(origin).value
        } catch (e: InvalidTenantOriginException) {
            return@CorsConfigurationSource null
        }
        CorsConfiguration().apply {
            allowedOrigins = listOf(validTenantOrigin)
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
            allowCredentials = false
        }
    }
}
