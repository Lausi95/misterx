package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.board.GetBoardUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.application.port.out.finding.GetTeamFindingsPort
import net.lausi95.citygame.application.port.out.game.GetGamePort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class GetBoardServiceTest {

    private val getGamePort = mockk<GetGamePort>()
    private val getTeamPort = mockk<GetTeamPort>()
    private val getAgentsPort = mockk<GetAgentsPort>()
    private val getAgentLocationPort = mockk<GetAgentLocationPort>()
    private val getTeamFindingsPort = mockk<GetTeamFindingsPort>()

    private val service = GetBoardService(
        getGamePort,
        getTeamPort,
        getAgentsPort,
        getAgentLocationPort,
        getTeamFindingsPort,
    )

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId()
    private val teamId = TeamId()

    // 10x10 grid over SW(0,0)..NE(10,10): each cell spans 1.0 degree.
    private val game = Game(
        gameId,
        GameTitle("Hunt"),
        OffsetDateTime.now().minusHours(1),
        OffsetDateTime.now().plusHours(1),
        Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(10.0, 10.0), Grid(10, 10)),
    )

    private fun agent(
        id: AgentId = AgentId(),
        type: Agent.Type = Agent.Type.MISTERX,
        active: Boolean = true,
    ) = Agent(id, gameId, type, "phone", "first", "last", "alias", active)

    private fun locationOf(agentId: AgentId, latitude: Double, longitude: Double) =
        AgentLocation(AgentLocationId(), agentId, OffsetDateTime.now(), GeoLocation(latitude, longitude))

    private fun givenAgents(vararg agents: Agent) {
        every { getAgentsPort.getAgentsForGame(gameId, tenant) } returns agents.toList()
    }

    private fun givenLocations(vararg locations: Pair<AgentId, AgentLocation?>) {
        locations.forEach { (agentId, location) ->
            every { getAgentLocationPort.getAgentLocation(agentId) } returns location
        }
    }

    init {
        every { getGamePort.getGame(gameId, tenant) } returns game
    }

    @Test
    fun `utility agents are shown with their exact location`() {
        val utility = agent(type = Agent.Type.UTILITY)
        givenAgents(utility)
        givenLocations(utility.id to locationOf(utility.id, 3.5, 7.5))

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.utilityAgents).singleElement()
        assertThat(board.utilityAgents.first().location!!.geoLocation).isEqualTo(GeoLocation(3.5, 7.5))
        assertThat(board.misterxAgents).isEmpty()
    }

    @Test
    fun `misterx agents are shown as the grid cell containing their location`() {
        val misterX = agent(type = Agent.Type.MISTERX)
        givenAgents(misterX)
        givenLocations(misterX.id to locationOf(misterX.id, 3.5, 7.5))

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.misterxAgents).singleElement()
        assertThat(board.misterxAgents.first().cell.row).isEqualTo(3)
        assertThat(board.misterxAgents.first().cell.column).isEqualTo(7)
    }

    @Test
    fun `inactive agents are omitted regardless of type`() {
        val inactiveUtility = agent(type = Agent.Type.UTILITY, active = false)
        val inactiveMisterX = agent(type = Agent.Type.MISTERX, active = false)
        givenAgents(inactiveUtility, inactiveMisterX)
        givenLocations(
            inactiveUtility.id to locationOf(inactiveUtility.id, 1.0, 1.0),
            inactiveMisterX.id to locationOf(inactiveMisterX.id, 1.0, 1.0),
        )

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.utilityAgents).isEmpty()
        assertThat(board.misterxAgents).isEmpty()
    }

    @Test
    fun `agents without a known location are omitted`() {
        val utility = agent(type = Agent.Type.UTILITY)
        val misterX = agent(type = Agent.Type.MISTERX)
        givenAgents(utility, misterX)
        givenLocations(utility.id to null, misterX.id to null)

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.utilityAgents).isEmpty()
        assertThat(board.misterxAgents).isEmpty()
    }

    @Test
    fun `misterx agents located off the map are omitted`() {
        val misterX = agent(type = Agent.Type.MISTERX)
        givenAgents(misterX)
        givenLocations(misterX.id to locationOf(misterX.id, 50.0, 50.0))

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.misterxAgents).isEmpty()
    }

    @Test
    fun `without a team, all misterx agents are shown including found ones`() {
        val found = agent(type = Agent.Type.MISTERX)
        val unfound = agent(type = Agent.Type.MISTERX)
        givenAgents(found, unfound)
        givenLocations(
            found.id to locationOf(found.id, 2.5, 2.5),
            unfound.id to locationOf(unfound.id, 6.5, 6.5),
        )

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId = null), tenant)

        assertThat(board.misterxAgents).hasSize(2)
    }

    @Test
    fun `with a team, misterx agents the team has already found are excluded`() {
        val found = agent(type = Agent.Type.MISTERX)
        val unfound = agent(type = Agent.Type.MISTERX)
        val utility = agent(type = Agent.Type.UTILITY)
        givenAgents(found, unfound, utility)
        givenLocations(
            found.id to locationOf(found.id, 2.5, 2.5),
            unfound.id to locationOf(unfound.id, 6.5, 6.5),
            utility.id to locationOf(utility.id, 4.5, 4.5),
        )
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns Team(teamId, gameId, "Team A")
        every { getTeamFindingsPort.getFindingsByTeam(teamId, tenant) } returns listOf(
            AgentFinding(FindingId(), gameId, teamId, found.id, OffsetDateTime.now(), null, null),
        )

        val board = service.getBoard(GetBoardUseCase.Query(gameId, teamId), tenant)

        assertThat(board.misterxAgents).singleElement()
        assertThat(board.misterxAgents.first().agent.id).isEqualTo(unfound.id)
        // utility is unaffected by the team filter
        assertThat(board.utilityAgents).singleElement()
    }

    @Test
    fun `a team that does not belong to the game is not found`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns Team(teamId, GameId(), "Other game")

        assertThatThrownBy { service.getBoard(GetBoardUseCase.Query(gameId, teamId), tenant) }
            .isInstanceOf(TeamNotFoundException::class.java)
    }

    @Test
    fun `an unknown team is not found`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns null

        assertThatThrownBy { service.getBoard(GetBoardUseCase.Query(gameId, teamId), tenant) }
            .isInstanceOf(TeamNotFoundException::class.java)
    }
}
