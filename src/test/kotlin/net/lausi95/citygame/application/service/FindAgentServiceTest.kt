package net.lausi95.citygame.application.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.AgentNotFoundException
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocationId
import net.lausi95.citygame.application.domain.model.finding.AgentAlreadyFoundException
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.AgentNotFindableException
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameNotActiveException
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.domain.model.team.TeamMemberNotFoundException
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.domain.service.FindAgentService
import net.lausi95.citygame.application.port.`in`.finding.FindAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.application.port.out.finding.CheckAgentFoundByTeamPort
import net.lausi95.citygame.application.port.out.finding.SaveAgentFindingPort
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.GetTeamMemberPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class FindAgentServiceTest {

    private val gameRepository = mockk<GameRepository>()
    private val agentRepository = mockk<AgentRepository>()
    private val getTeamPort = mockk<GetTeamPort>()
    private val getTeamMemberPort = mockk<GetTeamMemberPort>()
    private val getAgentLocationPort = mockk<GetAgentLocationPort>()
    private val checkAgentFoundByTeamPort = mockk<CheckAgentFoundByTeamPort>()
    private val saveAgentFindingPort = mockk<SaveAgentFindingPort>(relaxed = true)

    private val service = FindAgentService(
        gameRepository,
        agentRepository,
        getTeamPort,
        getTeamMemberPort,
        getAgentLocationPort,
        checkAgentFoundByTeamPort,
        saveAgentFindingPort,
    )

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId()
    private val teamId = TeamId()
    private val memberId = TeamMemberId()
    private val agentId = AgentId()

    private fun game(
        start: OffsetDateTime = OffsetDateTime.now().minusHours(1),
        end: OffsetDateTime = OffsetDateTime.now().plusHours(1),
    ) = Game(
        gameId,
        GameTitle("Hunt"),
        start,
        end,
        Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(1.0, 1.0), Grid(10, 10)),
    )

    private fun agent(
        type: Agent.Type = Agent.Type.MISTERX,
        active: Boolean = true,
        game: GameId = gameId,
    ) = Agent(agentId, game, type, "phone", "first", "last", "alias", active)

    private fun team(game: GameId = gameId) = Team(teamId, game, "Team A")

    private fun member(team: TeamId = teamId, game: GameId = gameId) =
        TeamMember(memberId, team, game, OffsetDateTime.now())

    private fun command(reportedLocation: GeoLocation? = null) =
        FindAgentUseCase.Command(gameId, teamId, memberId, agentId, reportedLocation)

    @BeforeEach
    fun happyPathStubs() {
        every { gameRepository.get(gameId, tenant) } returns game()
        every { agentRepository.getOrNull(agentId, tenant) } returns agent()
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns team()
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns member()
        every { checkAgentFoundByTeamPort.teamHasFoundAgent(teamId, agentId, tenant) } returns false
        every { getAgentLocationPort.getAgentLocation(agentId) } returns null
    }

    @Test
    fun `records a find and snapshots both the reported and the agent location`() {
        val reported = GeoLocation(52.5, 13.4)
        val agentLoc = GeoLocation(52.6, 13.5)
        every { getAgentLocationPort.getAgentLocation(agentId) } returns
            AgentLocation(AgentLocationId(), agentId, OffsetDateTime.now(), agentLoc)

        val saved = slot<AgentFinding>()
        every { saveAgentFindingPort.saveAgentFinding(capture(saved), tenant) } returns Unit

        val findingId = service.findAgent(command(reported), tenant)

        assertThat(findingId.value).isNotBlank()
        assertThat(saved.captured.teamId).isEqualTo(teamId)
        assertThat(saved.captured.agentId).isEqualTo(agentId)
        assertThat(saved.captured.reportedLocation).isEqualTo(reported)
        assertThat(saved.captured.agentLocation).isEqualTo(agentLoc)
        verify(exactly = 1) { saveAgentFindingPort.saveAgentFinding(any(), tenant) }
    }

    @Test
    fun `records a find with null locations when nothing reported and agent never located`() {
        val saved = slot<AgentFinding>()
        every { saveAgentFindingPort.saveAgentFinding(capture(saved), tenant) } returns Unit

        service.findAgent(command(reportedLocation = null), tenant)

        assertThat(saved.captured.reportedLocation).isNull()
        assertThat(saved.captured.agentLocation).isNull()
    }

    @Test
    fun `rejects when the game is not currently active`() {
        every { gameRepository.get(gameId, tenant) } returns game(
            start = OffsetDateTime.now().plusHours(1),
            end = OffsetDateTime.now().plusHours(2),
        )

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(GameNotActiveException::class.java)

        verify(exactly = 0) { saveAgentFindingPort.saveAgentFinding(any(), any()) }
    }

    @Test
    fun `rejects when the agent belongs to a different game`() {
        every { agentRepository.getOrNull(agentId, tenant) } returns agent(game = GameId())

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(AgentNotFoundException::class.java)
    }

    @Test
    fun `rejects when the team belongs to a different game`() {
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns team(game = GameId())

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(TeamNotFoundException::class.java)
    }

    @Test
    fun `rejects when the member belongs to a different team`() {
        every { getTeamMemberPort.getTeamMemberOrNull(memberId, tenant) } returns member(team = TeamId())

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(TeamMemberNotFoundException::class.java)
    }

    @Test
    fun `rejects when the agent is not a MISTERX`() {
        every { agentRepository.getOrNull(agentId, tenant) } returns agent(type = Agent.Type.UTILITY)

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(AgentNotFindableException::class.java)
    }

    @Test
    fun `rejects when the agent is inactive`() {
        every { agentRepository.getOrNull(agentId, tenant) } returns agent(active = false)

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(AgentNotFindableException::class.java)
    }

    @Test
    fun `rejects when the team has already found the agent`() {
        every { checkAgentFoundByTeamPort.teamHasFoundAgent(teamId, agentId, tenant) } returns true

        assertThatThrownBy { service.findAgent(command(), tenant) }
            .isInstanceOf(AgentAlreadyFoundException::class.java)

        verify(exactly = 0) { saveAgentFindingPort.saveAgentFinding(any(), any()) }
    }
}
