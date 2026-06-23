package net.lausi95.citygame.adapter.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.WebSecurityConfiguration
import net.lausi95.citygame.adapter.`in`.web.controller.game.GameController
import net.lausi95.citygame.application.port.`in`.game.CreateGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.application.port.`in`.game.GetMapUseCase
import net.lausi95.citygame.application.port.`in`.game.UpdateGameUseCase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * Pins the auth-OFF behaviour of the `local` profile: with the [WebSecurityConfiguration]
 * `@Profile("local")` chain active, even the otherwise-protected `/games` tree is reachable
 * without a token. Filters are left enabled so the security chain actually runs. No `JwtDecoder`
 * is needed because the local chain never wires `oauth2ResourceServer`. See ADR 0016.
 */
@WebMvcTest(GameController::class)
@ActiveProfiles("local")
@Import(WebSecurityConfiguration::class, TenantOriginExtractor::class, WebMvcConfig::class)
class LocalProfileWebSecurityConfigurationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createGameUseCase: CreateGameUseCase

    @MockkBean
    private lateinit var getGameUseCase: GetGameUseCase

    @MockkBean
    private lateinit var getGamesUseCase: GetGamesUseCase

    @MockkBean
    private lateinit var updateGameUseCase: UpdateGameUseCase

    @MockkBean
    private lateinit var getMapUseCase: GetMapUseCase

    @Test
    fun `games endpoint is public without a token under the local profile`() {
        every { getGamesUseCase.getGames(any(), any()) } returns Page.empty()

        mockMvc.get("/games") {
            // TenantFilter is not part of this slice; supply the attribute it would set.
            header("Origin", "https://test.city-game.net")
        }.andExpect { status { isOk() } }
    }
}
