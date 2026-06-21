package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class FrontendUriFactoryTest {

    @AfterEach
    fun clearRequestContext() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `uses the configured base-url when set, keeping the port intact`() {
        val factory = FrontendUriFactory("http://localhost:3000")

        val url = factory.buildUrl("/find", linkedMapOf("agentId" to "a1", "alias" to "Shadow"))

        // The colon in the host:port must survive — it must not become localhost%3A3000.
        assertThat(url).isEqualTo("http://localhost:3000/find?agentId=a1&alias=Shadow")
    }

    @Test
    fun `percent-encodes query values without touching the host or port`() {
        val factory = FrontendUriFactory("http://localhost:3000")

        val url = factory.buildUrl("/find", linkedMapOf("agentId" to "a1", "alias" to "Mr X & Co"))

        assertThat(url).isEqualTo("http://localhost:3000/find?agentId=a1&alias=Mr%20X%20%26%20Co")
    }

    @Test
    fun `derives the origin from the incoming request when no base-url is configured`() {
        val request = MockHttpServletRequest().apply {
            scheme = "https"
            serverName = "foo.city-game.net"
            serverPort = 443
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val factory = FrontendUriFactory(null)

        val url = factory.buildUrl("/find", linkedMapOf("agentId" to "a1", "alias" to "Mr X & Co"))

        // Host comes from the request (the tenant's own domain); alias is still encoded.
        assertThat(url).startsWith("https://foo.city-game.net")
        assertThat(url).contains("/find?agentId=a1&alias=Mr%20X%20%26%20Co")
        assertThat(url).doesNotContain("%3A")
    }
}
