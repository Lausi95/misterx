package net.lausi95.citygame.application.domain.service

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
import net.lausi95.citygame.application.domain.model.finding.FindingId
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
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class FindingServiceTest {

    private val tenant = Tenant("https://acme.city-game.net")

    @Nested
    inner class FindAgent {

        private val gameRepository = mockk<GameRepository>()
        private val agentRepository = mockk<AgentRepository>()
        private val teamRepository = mockk<TeamRepository>()
        private val teamMemberRepository = mockk<TeamMemberRepository>()
        private val agentLocationRepository = mockk<AgentLocationRepository>()
        private val findingRepository = mockk<FindingRepository>(relaxed = true)

        private val service = FindingService(
            gameRepository,
            agentRepository,
            teamRepository,
            teamMemberRepository,
            agentLocationRepository,
            findingRepository,
        )

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
            FindingUseCase.FindAgentCommand(gameId, teamId, memberId, agentId, reportedLocation)

        @BeforeEach
        fun happyPathStubs() {
            every { gameRepository.get(gameId, tenant) } returns game()
            every { agentRepository.getOrNull(agentId, tenant) } returns agent()
            every { teamRepository.getOrNull(teamId, tenant) } returns team()
            every { teamMemberRepository.getOrNull(memberId, tenant) } returns member()
            every { findingRepository.existsByTeamAndAgent(teamId, agentId, tenant) } returns false
            every { agentLocationRepository.latest(agentId) } returns null
        }

        @Test
        fun `records a find and snapshots both the reported and the agent location`() {
            val reported = GeoLocation(52.5, 13.4)
            val agentLoc = GeoLocation(52.6, 13.5)
            every { agentLocationRepository.latest(agentId) } returns
                AgentLocation(AgentLocationId(), agentId, OffsetDateTime.now(), agentLoc)

            val saved = slot<AgentFinding>()
            every { findingRepository.save(capture(saved), tenant) } returns Unit

            val findingId = service.findAgent(command(reported), tenant)

            assertThat(findingId.value).isNotBlank()
            assertThat(saved.captured.teamId).isEqualTo(teamId)
            assertThat(saved.captured.agentId).isEqualTo(agentId)
            assertThat(saved.captured.reportedLocation).isEqualTo(reported)
            assertThat(saved.captured.agentLocation).isEqualTo(agentLoc)
            verify(exactly = 1) { findingRepository.save(any(), tenant) }
        }

        @Test
        fun `records a find with null locations when nothing reported and agent never located`() {
            val saved = slot<AgentFinding>()
            every { findingRepository.save(capture(saved), tenant) } returns Unit

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

            verify(exactly = 0) { findingRepository.save(any(), any()) }
        }

        @Test
        fun `rejects when the agent belongs to a different game`() {
            every { agentRepository.getOrNull(agentId, tenant) } returns agent(game = GameId())

            assertThatThrownBy { service.findAgent(command(), tenant) }
                .isInstanceOf(AgentNotFoundException::class.java)
        }

        @Test
        fun `rejects when the team belongs to a different game`() {
            every { teamRepository.getOrNull(teamId, tenant) } returns team(game = GameId())

            assertThatThrownBy { service.findAgent(command(), tenant) }
                .isInstanceOf(TeamNotFoundException::class.java)
        }

        @Test
        fun `rejects when the member belongs to a different team`() {
            every { teamMemberRepository.getOrNull(memberId, tenant) } returns member(team = TeamId())

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
            every { findingRepository.existsByTeamAndAgent(teamId, agentId, tenant) } returns true

            assertThatThrownBy { service.findAgent(command(), tenant) }
                .isInstanceOf(AgentAlreadyFoundException::class.java)

            verify(exactly = 0) { findingRepository.save(any(), any()) }
        }
    }

    @Nested
    inner class GetFindingTeams {

        private val findingRepository = mockk<FindingRepository>()
        private val teamRepository = mockk<TeamRepository>()
        private val service = FindingService(
            mockk(relaxed = true),
            mockk(relaxed = true),
            teamRepository,
            mockk(relaxed = true),
            mockk(relaxed = true),
            findingRepository,
        )

        private val gameId = GameId()
        private val agentId = AgentId()

        private fun finding(teamId: TeamId, foundAt: OffsetDateTime = OffsetDateTime.now()) =
            AgentFinding(FindingId(), gameId, teamId, agentId, foundAt, null, null)

        @Test
        fun `exposes finding teams as id, name and found time`() {
            val teamId = TeamId()
            val foundAt = OffsetDateTime.now()
            every { findingRepository.byAgent(agentId, tenant) } returns listOf(finding(teamId, foundAt))
            every { teamRepository.getOrNull(teamId, tenant) } returns Team(teamId, gameId, "Team A")

            val teams = service.getFindingTeams(agentId, tenant)

            assertThat(teams).singleElement().satisfies({
                assertThat(it.teamId).isEqualTo(teamId)
                assertThat(it.name).isEqualTo("Team A")
                assertThat(it.foundAt).isEqualTo(foundAt)
            })
        }

        @Test
        fun `drops findings whose team can no longer be resolved`() {
            val present = TeamId()
            val missing = TeamId()
            every { findingRepository.byAgent(agentId, tenant) } returns
                listOf(finding(present), finding(missing))
            every { teamRepository.getOrNull(present, tenant) } returns Team(present, gameId, "Team A")
            every { teamRepository.getOrNull(missing, tenant) } returns null

            val teams = service.getFindingTeams(agentId, tenant)

            assertThat(teams).extracting<TeamId> { it.teamId }.containsExactly(present)
        }
    }

    @Nested
    inner class GetFoundAgents {

        private val findingRepository = mockk<FindingRepository>()
        private val agentRepository = mockk<AgentRepository>()
        private val service = FindingService(
            mockk(relaxed = true),
            agentRepository,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            findingRepository,
        )

        private val gameId = GameId()
        private val teamId = TeamId()

        private fun finding(agentId: AgentId, foundAt: OffsetDateTime = OffsetDateTime.now()) =
            AgentFinding(FindingId(), gameId, teamId, agentId, foundAt, null, null)

        private fun agent(agentId: AgentId, alias: String) =
            Agent(agentId, gameId, Agent.Type.MISTERX, "phone", "first", "last", alias, true)

        @Test
        fun `exposes found agents as id, alias and found time`() {
            val agentId = AgentId()
            val foundAt = OffsetDateTime.now()
            every { findingRepository.byTeam(teamId, tenant) } returns listOf(finding(agentId, foundAt))
            every { agentRepository.getOrNull(agentId, tenant) } returns agent(agentId, "Shadow")

            val foundAgents = service.getFoundAgents(teamId, tenant)

            assertThat(foundAgents).singleElement().satisfies({
                assertThat(it.agentId).isEqualTo(agentId)
                assertThat(it.name).isEqualTo("Shadow")
                assertThat(it.foundAt).isEqualTo(foundAt)
            })
        }

        @Test
        fun `drops findings whose agent can no longer be resolved`() {
            val present = AgentId()
            val missing = AgentId()
            every { findingRepository.byTeam(teamId, tenant) } returns
                listOf(finding(present), finding(missing))
            every { agentRepository.getOrNull(present, tenant) } returns agent(present, "Shadow")
            every { agentRepository.getOrNull(missing, tenant) } returns null

            val foundAgents = service.getFoundAgents(teamId, tenant)

            assertThat(foundAgents).extracting<AgentId> { it.agentId }.containsExactly(present)
        }
    }
}
