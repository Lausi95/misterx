package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.adapter.`in`.web.WebSecurityConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest

class CorsConfigurationSourceTest {

    private val source = WebSecurityConfiguration().corsConfigurationSource()

    @Test
    fun `reflects a valid tenant origin`() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.ORIGIN, "https://foo.city-game.net")
        }

        val config = source.getCorsConfiguration(request)

        assertThat(config?.allowedOrigins).containsExactly("https://foo.city-game.net")
    }

    @Test
    fun `grants nothing when there is no Origin header`() {
        assertThat(source.getCorsConfiguration(MockHttpServletRequest())).isNull()
    }

    @Test
    fun `grants nothing for an origin that is not a valid tenant`() {
        val request = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.ORIGIN, "not-a-valid-origin")
        }

        assertThat(source.getCorsConfiguration(request)).isNull()
    }
}
