package net.lausi95.citygame.adapter.web

import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.WebSecurityConfiguration
import net.lausi95.citygame.adapter.`in`.web.controller.game.GameController
import net.lausi95.citygame.application.port.`in`.game.GameUseCase
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Verifies the OAuth2 resource server rules from [WebSecurityConfiguration]: the "/games"
 * tree requires a token, everything else is public. Filters are left enabled (no
 * addFilters = false) so the security chain actually runs. See ADR 0007.
 */
@WebMvcTest(GameController::class)
@Import(WebSecurityConfiguration::class, TenantOriginExtractor::class, WebMvcConfig::class)
class WebSecurityConfigurationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var gameUseCase: GameUseCase

    // The oauth2ResourceServer { jwt {} } DSL requires a JwtDecoder bean. The jwt()
    // post-processor injects the authentication directly, so the decoder is never invoked.
    @MockkBean
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `games endpoint rejects unauthenticated requests`() {
        mockMvc.get("/games")
            .andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `games endpoint is reachable with a valid token`() {
        every { gameUseCase.getGames(any(), any()) } returns Page.empty()

        mockMvc.get("/games") {
            with(jwt())
            // TenantFilter is not part of this slice; supply the attribute it would set.
            header("Origin", "https://test.city-game.net")
        }.andExpect { status { isOk() } }
    }

    @Test
    fun `non-games paths are public`() {
        // /board is not mapped in this slice, so the downstream status is irrelevant —
        // what matters is the security chain did NOT reject the tokenless request.
        val status = mockMvc.get("/board").andReturn().response.status

        assertThat(status).isNotIn(401, 403)
    }
}
