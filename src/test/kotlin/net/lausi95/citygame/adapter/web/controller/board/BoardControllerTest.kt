package net.lausi95.citygame.adapter.web.controller.board

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import net.lausi95.citygame.adapter.`in`.web.controller.board.BoardController
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.board.MisterXOnBoard
import net.lausi95.citygame.application.domain.model.game.Cell
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.board.GetBoardUseCase
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

@WebMvcTest(BoardController::class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var getBoardUseCase: GetBoardUseCase

    private fun emptyBoard() = Board(
        game = Game(
            GameId("g1"),
            GameTitle("Hunt"),
            OffsetDateTime.now().minusHours(1),
            OffsetDateTime.now().plusHours(1),
            Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(10.0, 10.0), Grid(10, 10)),
        ),
        utilityAgents = emptyList(),
        misterxAgents = emptyList(),
    )

    @Test
    fun `returns 200 with the map and empty agent lists`() {
        every { getBoardUseCase.getBoard(any(), any()) } returns emptyBoard()

        mockMvc.get("/board") {
            header("X-GameId", "g1")
            requestAttr("tenant", Tenant("acme"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.map.grid.rows") { value(10) }
            jsonPath("$.utilityAgents") { isArray() }
            jsonPath("$.misterxAgents") { isArray() }
        }
    }

    @Test
    fun `serializes utility agents with exact location and misterx agents with a cell`() {
        val game = emptyBoard().game
        val utility = Agent(AgentId("u1"), game.id, Agent.Type.UTILITY, "p", "f", "l", "scout", true)
            .apply { setLocation(AgentLocation(AgentLocationId(), id, OffsetDateTime.now(), GeoLocation(3.5, 7.5))) }
        val misterX = Agent(AgentId("x1"), game.id, Agent.Type.MISTERX, "p", "f", "l", "shadow", true)
            .apply { setLocation(AgentLocation(AgentLocationId(), id, OffsetDateTime.now(), GeoLocation(3.5, 7.5))) }

        every { getBoardUseCase.getBoard(any(), any()) } returns Board(
            game = game,
            utilityAgents = listOf(utility),
            misterxAgents = listOf(MisterXOnBoard(misterX, Cell(row = 3, column = 7))),
        )

        mockMvc.get("/board") {
            header("X-GameId", "g1")
            requestAttr("tenant", Tenant("acme"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.utilityAgents[0].id") { value("u1") }
            jsonPath("$.utilityAgents[0].alias") { value("scout") }
            jsonPath("$.utilityAgents[0].geoLocation.latitude") { value(3.5) }
            jsonPath("$.utilityAgents[0].geoLocation.longitude") { value(7.5) }
            jsonPath("$.utilityAgents[0].lastSeenAt") { exists() }
            jsonPath("$.misterxAgents[0].id") { value("x1") }
            jsonPath("$.misterxAgents[0].alias") { value("shadow") }
            jsonPath("$.misterxAgents[0].cell.row") { value(3) }
            jsonPath("$.misterxAgents[0].cell.column") { value(7) }
            jsonPath("$.misterxAgents[0].lastSeenAt") { exists() }
            // the obfuscation guarantee: a misterx entry never leaks an exact location
            jsonPath("$.misterxAgents[0].geoLocation") { doesNotExist() }
        }
    }

    @Test
    fun `missing X-GameId header is a 400`() {
        mockMvc.get("/board") {
            requestAttr("tenant", Tenant("acme"))
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `passes the game id and optional team id to the use case`() {
        val query = slot<GetBoardUseCase.Query>()
        val tenant = slot<Tenant>()
        every { getBoardUseCase.getBoard(capture(query), capture(tenant)) } returns emptyBoard()

        mockMvc.get("/board") {
            header("X-GameId", "g1")
            header("X-TeamId", "t1")
            requestAttr("tenant", Tenant("acme"))
        }.andExpect { status { isOk() } }

        assertThat(query.captured.gameId).isEqualTo(GameId("g1"))
        assertThat(query.captured.teamId?.value).isEqualTo("t1")
        assertThat(tenant.captured).isEqualTo(Tenant("acme"))
    }

    @Test
    fun `sends a null team id when X-TeamId is omitted`() {
        val query = slot<GetBoardUseCase.Query>()
        every { getBoardUseCase.getBoard(capture(query), any()) } returns emptyBoard()

        mockMvc.get("/board") {
            header("X-GameId", "g1")
            requestAttr("tenant", Tenant("acme"))
        }.andExpect { status { isOk() } }

        assertThat(query.captured.teamId).isNull()
    }

    @Test
    fun `maps not-found domain exceptions to 404`() {
        every { getBoardUseCase.getBoard(any(), any()) } throws TeamNotFoundException("missing")

        mockMvc.get("/board") {
            header("X-GameId", "g1")
            header("X-TeamId", "t1")
            requestAttr("tenant", Tenant("acme"))
        }.andExpect {
            status { isNotFound() }
        }
    }
}
