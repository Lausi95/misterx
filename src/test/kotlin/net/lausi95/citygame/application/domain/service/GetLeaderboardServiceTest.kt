package net.lausi95.citygame.application.domain.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.GameNotFoundException
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

class GetLeaderboardServiceTest {

    private val gameRepository = mockk<GameRepository>()
    private val teamRepository = mockk<TeamRepository>()
    private val agentRepository = mockk<AgentRepository>()
    private val findingRepository = mockk<FindingRepository>()

    private val service = GetLeaderboardService(
        gameRepository,
        teamRepository,
        agentRepository,
        findingRepository,
    )

    private val tenant = Tenant("https://acme.city-game.net")
    private val gameId = GameId()

    private val game = Game(
        gameId,
        GameTitle("Hunt"),
        OffsetDateTime.now().minusHours(1),
        OffsetDateTime.now().plusHours(1),
        Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(10.0, 10.0), Grid(10, 10)),
    )

    private val t0: OffsetDateTime = OffsetDateTime.parse("2026-06-21T10:00:00Z")

    private fun agent(
        id: AgentId = AgentId(),
        alias: String = "alias",
        type: Agent.Type = Agent.Type.MISTERX,
        active: Boolean = true,
    ) = Agent(id, gameId, type, "phone", "first", "last", alias, active)

    private fun team(id: TeamId, name: String) = Team(id, gameId, name)

    private fun finding(teamId: TeamId, agentId: AgentId, foundAt: OffsetDateTime) =
        AgentFinding(FindingId(), gameId, teamId, agentId, foundAt, null, null)

    private fun givenAgents(vararg agents: Agent) {
        every { agentRepository.forGame(gameId, tenant) } returns agents.toList()
    }

    private fun givenTeams(vararg teams: Team) {
        every { teamRepository.forGame(gameId, any(), tenant) } returns PageImpl(teams.toList())
    }

    private fun givenFindings(teamId: TeamId, vararg findings: AgentFinding) {
        every { findingRepository.byTeam(teamId, tenant) } returns findings.toList()
    }

    init {
        every { gameRepository.get(gameId, tenant) } returns game
    }

    @Test
    fun `teams are ranked by number of found misterx agents, descending`() {
        val a1 = agent(alias = "x1")
        val a2 = agent(alias = "x2")
        val teamOne = team(TeamId(), "One")
        val teamTwo = team(TeamId(), "Two")
        givenAgents(a1, a2)
        givenTeams(teamOne, teamTwo)
        givenFindings(teamOne.id, finding(teamOne.id, a1.id, t0))
        givenFindings(teamTwo.id, finding(teamTwo.id, a1.id, t0), finding(teamTwo.id, a2.id, t0.plusMinutes(5)))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.map { it.team.name }).containsExactly("Two", "One")
        assertThat(leaderboard.entries.map { it.foundCount }).containsExactly(2, 1)
    }

    @Test
    fun `teams tied on count are ranked by the earlier last-found time`() {
        val a1 = agent(alias = "x1")
        val a2 = agent(alias = "x2")
        // Both teams found 2 agents. Early finished its last find at 10:10, Late at 10:20.
        val early = team(TeamId(), "Early")
        val late = team(TeamId(), "Late")
        givenAgents(a1, a2)
        givenTeams(late, early) // deliberately unsorted input
        givenFindings(early.id, finding(early.id, a1.id, t0), finding(early.id, a2.id, t0.plusMinutes(10)))
        givenFindings(late.id, finding(late.id, a1.id, t0), finding(late.id, a2.id, t0.plusMinutes(20)))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.map { it.team.name }).containsExactly("Early", "Late")
    }

    @Test
    fun `findings of inactive misterx agents are not counted`() {
        val active = agent(alias = "active")
        val inactive = agent(alias = "inactive", active = false)
        val teamOne = team(TeamId(), "One")
        givenAgents(active, inactive)
        givenTeams(teamOne)
        givenFindings(teamOne.id, finding(teamOne.id, active.id, t0), finding(teamOne.id, inactive.id, t0.plusMinutes(1)))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries).singleElement()
        assertThat(leaderboard.entries.first().foundCount).isEqualTo(1)
        assertThat(leaderboard.entries.first().foundAgents.map { it.alias }).containsExactly("active")
    }

    @Test
    fun `findings of utility agents are not counted`() {
        val misterx = agent(alias = "x")
        val utility = agent(alias = "u", type = Agent.Type.UTILITY)
        val teamOne = team(TeamId(), "One")
        givenAgents(misterx, utility)
        givenTeams(teamOne)
        givenFindings(teamOne.id, finding(teamOne.id, misterx.id, t0), finding(teamOne.id, utility.id, t0.plusMinutes(1)))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.first().foundAgents.map { it.alias }).containsExactly("x")
    }

    @Test
    fun `findings of agents that no longer exist are not counted`() {
        val present = agent(alias = "present")
        val teamOne = team(TeamId(), "One")
        givenAgents(present)
        givenTeams(teamOne)
        givenFindings(teamOne.id, finding(teamOne.id, present.id, t0), finding(teamOne.id, AgentId("ghost"), t0.plusMinutes(1)))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.first().foundCount).isEqualTo(1)
    }

    @Test
    fun `found agents within a team are ordered chronologically`() {
        val a1 = agent(alias = "first")
        val a2 = agent(alias = "second")
        val teamOne = team(TeamId(), "One")
        givenAgents(a1, a2)
        givenTeams(teamOne)
        // findings arrive out of order
        givenFindings(teamOne.id, finding(teamOne.id, a2.id, t0.plusMinutes(10)), finding(teamOne.id, a1.id, t0))

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.first().foundAgents.map { it.alias }).containsExactly("first", "second")
    }

    @Test
    fun `teams with no counted finds are included at the bottom`() {
        val a1 = agent(alias = "x1")
        val scorer = team(TeamId(), "Scorer")
        val empty = team(TeamId(), "Empty")
        givenAgents(a1)
        givenTeams(empty, scorer)
        givenFindings(scorer.id, finding(scorer.id, a1.id, t0))
        givenFindings(empty.id)

        val leaderboard = service.getLeaderboard(gameId, tenant)

        assertThat(leaderboard.entries.map { it.team.name }).containsExactly("Scorer", "Empty")
        assertThat(leaderboard.entries.last().foundCount).isEqualTo(0)
        assertThat(leaderboard.entries.last().foundAgents).isEmpty()
    }

    @Test
    fun `an unknown game is not found`() {
        val unknown = GameId()
        every { gameRepository.getOrNull(unknown, tenant) } returns null
        every { gameRepository.get(unknown, tenant) } answers { callOriginal() }

        assertThatThrownBy { service.getLeaderboard(unknown, tenant) }
            .isInstanceOf(GameNotFoundException::class.java)
    }
}
