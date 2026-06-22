package net.lausi95.citygame.adapter.web.controller.leaderboard

import org.springframework.context.annotation.Import
import net.lausi95.citygame.adapter.`in`.web.WebMvcConfig
import net.lausi95.citygame.adapter.`in`.web.TenantOriginExtractor
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import net.lausi95.citygame.adapter.`in`.web.controller.leaderboard.LeaderboardController
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.leaderboard.FoundMisterX
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.application.domain.model.leaderboard.LeaderboardEntry
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.leaderboard.GetLeaderboardUseCase
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.OffsetDateTime

@WebMvcTest(LeaderboardController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TenantOriginExtractor::class, WebMvcConfig::class)
class LeaderboardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getLeaderboardUseCase: GetLeaderboardUseCase

    private val gameId = GameId("g1")

    private fun game() = Game(
        gameId,
        GameTitle("Hunt"),
        OffsetDateTime.now().minusHours(1),
        OffsetDateTime.now().plusHours(1),
        Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(10.0, 10.0), Grid(10, 10)),
    )

    @Test
    fun `returns 200 with an empty teams array`() {
        every { getLeaderboardUseCase.getLeaderboard(any(), any()) } returns Leaderboard.of(game(), emptyList())

        mockMvc.get("/leaderboard") {
            header("X-GameId", "g1")
            header("Origin", "https://acme.city-game.net")
        }.andExpect {
            status { isOk() }
            jsonPath("$.teams") { isArray() }
            jsonPath("$.game.startTime") { exists() }
        }
    }

    @Test
    fun `serializes ranked teams with their found agents`() {
        val foundAt = OffsetDateTime.parse("2026-06-21T10:00:00Z")
        val winner = LeaderboardEntry(
            Team(TeamId("t1"), gameId, "Winners"),
            listOf(FoundMisterX("shadow", foundAt), FoundMisterX("raven", foundAt.plusMinutes(5))),
        )
        val empty = LeaderboardEntry(Team(TeamId("t2"), gameId, "Empties"), emptyList())
        every { getLeaderboardUseCase.getLeaderboard(any(), any()) } returns Leaderboard.of(game(), listOf(winner, empty))

        mockMvc.get("/leaderboard") {
            header("X-GameId", "g1")
            header("Origin", "https://acme.city-game.net")
        }.andExpect {
            status { isOk() }
            jsonPath("$.teams[0].teamId") { value("t1") }
            jsonPath("$.teams[0].teamName") { value("Winners") }
            jsonPath("$.teams[0].foundCount") { value(2) }
            jsonPath("$.teams[0].agents[0].alias") { value("shadow") }
            jsonPath("$.teams[0].agents[0].foundAt") { exists() }
            jsonPath("$.teams[0].agents[1].alias") { value("raven") }
            jsonPath("$.teams[1].teamId") { value("t2") }
            jsonPath("$.teams[1].foundCount") { value(0) }
            jsonPath("$.teams[1].agents") { isEmpty() }
        }
    }

    @Test
    fun `missing X-GameId header is a 400`() {
        mockMvc.get("/leaderboard") {
            header("Origin", "https://acme.city-game.net")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `passes the game id and tenant to the use case`() {
        val capturedGameId = slot<GameId>()
        val capturedTenant = slot<Tenant>()
        every {
            getLeaderboardUseCase.getLeaderboard(capture(capturedGameId), capture(capturedTenant))
        } returns Leaderboard.of(game(), emptyList())

        mockMvc.get("/leaderboard") {
            header("X-GameId", "g1")
            header("Origin", "https://acme.city-game.net")
        }.andExpect { status { isOk() } }

        assertThat(capturedGameId.captured).isEqualTo(GameId("g1"))
        assertThat(capturedTenant.captured).isEqualTo(Tenant("https://acme.city-game.net"))
    }

    @Test
    fun `maps not-found domain exceptions to 404`() {
        every { getLeaderboardUseCase.getLeaderboard(any(), any()) } throws GameNotFoundException("missing")

        mockMvc.get("/leaderboard") {
            header("X-GameId", "g1")
            header("Origin", "https://acme.city-game.net")
        }.andExpect {
            status { isNotFound() }
        }
    }
}
