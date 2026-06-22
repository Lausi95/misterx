package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest

class TenantOriginExtractorTest {

    private val overrideEnabled = TenantOriginExtractor(overrideEnabled = true)
    private val overrideDisabled = TenantOriginExtractor(overrideEnabled = false)

    @Test
    fun `prefers the override header when override is enabled`() {
        val request = MockHttpServletRequest().apply {
            addHeader(Tenant.OVERRIDE_TENANT_HEADER_NAME, "http://localhost:3000")
            addHeader(HttpHeaders.ORIGIN, "https://foo.city-game.net")
        }

        assertThat(overrideEnabled.extract(request)).isEqualTo("http://localhost:3000")
    }

    @Test
    fun `ignores the override header when override is disabled`() {
        val request = MockHttpServletRequest().apply {
            addHeader(Tenant.OVERRIDE_TENANT_HEADER_NAME, "http://localhost:3000")
            addHeader(HttpHeaders.ORIGIN, "https://foo.city-game.net")
        }

        assertThat(overrideDisabled.extract(request)).isEqualTo("https://foo.city-game.net")
    }

    @Test
    fun `uses the Origin header when present`() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.ORIGIN, "https://foo.city-game.net")
        }

        assertThat(overrideDisabled.extract(request)).isEqualTo("https://foo.city-game.net")
    }

    @Test
    fun `falls back to the origin of the Referer when no Origin is present`() {
        val request = MockHttpServletRequest().apply {
            // <img> loads send a path-bearing Referer; only its origin must survive.
            addHeader(HttpHeaders.REFERER, "https://foo.city-game.net/some/page?x=1")
        }

        assertThat(overrideDisabled.extract(request)).isEqualTo("https://foo.city-game.net")
    }

    @Test
    fun `keeps the port when extracting the Referer origin`() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.REFERER, "http://localhost:3000/find")
        }

        assertThat(overrideDisabled.extract(request)).isEqualTo("http://localhost:3000")
    }

    @Test
    fun `returns null when neither Origin nor Referer is present`() {
        assertThat(overrideDisabled.extract(MockHttpServletRequest())).isNull()
    }
}
