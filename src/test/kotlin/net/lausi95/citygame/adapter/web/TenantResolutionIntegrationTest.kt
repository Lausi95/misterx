package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.IntegrationTest
import net.lausi95.citygame.common.Tenant
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Full-context tests for tenant resolution: the real [net.lausi95.citygame.adapter.in.web.TenantFilter],
 * `TenantArgumentResolver`, security chain and CORS all boot together. A throwaway controller echoes
 * the resolved tenant so resolution can be asserted directly, without touching any use case or the DB.
 *
 * The `test` profile sets `tenant.override.enabled=true`, so the `X-TENANT-OVERRIDE` header is honoured.
 */
@IntegrationTest
class TenantResolutionIntegrationTest {

    @RestController
    class TenantEchoController {
        @GetMapping("/it/tenant")
        fun echo(tenant: Tenant): String = tenant.value
    }

    @TestConfiguration
    class TenantEchoConfig {
        @Bean
        fun tenantEchoController() = TenantEchoController()
    }

    @Autowired
    private lateinit var client: RestTestClient

    @Test
    fun `resolves the tenant from the Origin header`() {
        client.get().uri("/it/tenant")
            .header(HttpHeaders.ORIGIN, "https://foo.city-game.net")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String::class.java).isEqualTo("https://foo.city-game.net")
    }

    @Test
    fun `falls back to the origin of the Referer when there is no Origin`() {
        client.get().uri("/it/tenant")
            .header(HttpHeaders.REFERER, "https://bar.city-game.net/some/page?x=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String::class.java).isEqualTo("https://bar.city-game.net")
    }

    @Test
    fun `prefers the X-TENANT-OVERRIDE header over the Origin`() {
        client.get().uri("/it/tenant")
            .header(Tenant.OVERRIDE_TENANT_HEADER_NAME, "http://localhost:3000")
            .header(HttpHeaders.ORIGIN, "https://foo.city-game.net")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String::class.java).isEqualTo("http://localhost:3000")
    }

    @Test
    fun `returns 400 when no origin can be resolved`() {
        client.get().uri("/it/tenant")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `returns 400 when the origin is malformed`() {
        client.get().uri("/it/tenant")
            .header(HttpHeaders.ORIGIN, "not-a-valid-origin")
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `reflects a valid tenant origin in the CORS Access-Control-Allow-Origin header`() {
        client.get().uri("/it/tenant")
            .header(HttpHeaders.ORIGIN, "https://foo.city-game.net")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://foo.city-game.net")
    }
}
