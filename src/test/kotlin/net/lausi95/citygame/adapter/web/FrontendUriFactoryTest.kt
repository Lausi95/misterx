package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FrontendUriFactoryTest {

    private val factory = FrontendUriFactory()

    @Test
    fun `builds the URL from the tenant origin, keeping the port intact`() {
        val url = factory.buildUrl(
            Tenant("http://localhost:3000"),
            "/find",
            linkedMapOf("agentId" to "a1", "alias" to "Shadow"),
        )

        // The colon in the host:port must survive — it must not become localhost%3A3000.
        assertThat(url).isEqualTo("http://localhost:3000/find?agentId=a1&alias=Shadow")
    }

    @Test
    fun `percent-encodes query values without touching the host or port`() {
        val url = factory.buildUrl(
            Tenant("http://localhost:3000"),
            "/find",
            linkedMapOf("agentId" to "a1", "alias" to "Mr X & Co"),
        )

        assertThat(url).isEqualTo("http://localhost:3000/find?agentId=a1&alias=Mr%20X%20%26%20Co")
    }

    @Test
    fun `builds the URL from a production tenant origin without a port`() {
        val url = factory.buildUrl(
            Tenant("https://foo.city-game.net"),
            "/find",
            linkedMapOf("agentId" to "a1", "alias" to "Mr X & Co"),
        )

        assertThat(url).isEqualTo("https://foo.city-game.net/find?agentId=a1&alias=Mr%20X%20%26%20Co")
        assertThat(url).doesNotContain("%3A")
    }
}
