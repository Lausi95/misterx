package net.lausi95.citygame.adapter.`in`.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * OAuth2 resource server protecting only the organizer/management tree under "/games".
 * Every other endpoint is intentionally public — participant clients are identified by
 * request headers, not a security principal. See ADR 0007.
 */
@Configuration
class WebSecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests {
            it.requestMatchers("/games/**").authenticated()
                .anyRequest().permitAll()
        }
        .oauth2ResourceServer { it.jwt {} }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .csrf { it.disable() }
        .build()
}
