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
import net.lausi95.citygame.application.domain.model.game.GameNotFoundException
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.game.MapId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamNotFoundException
import net.lausi95.citygame.application.port.`in`.game.GameUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

class GameServiceTest {

    private val tenant = Tenant("https://acme.city-game.net")

    @Nested
    inner class GetBoard {

        private val gameRepository = mockk<GameRepository>()
        private val teamRepository = mockk<TeamRepository>()
        private val agentRepository = mockk<AgentRepository>()
        private val agentLocationRepository = mockk<AgentLocationRepository>()
        private val findingRepository = mockk<FindingRepository>()

        private val service = GameService(
            gameRepository,
            teamRepository,
            agentRepository,
            agentLocationRepository,
            findingRepository,
        )

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
            every { agentRepository.forGame(gameId, tenant) } returns agents.toList()
        }

        private fun givenLocations(vararg locations: Pair<AgentId, AgentLocation?>) {
            locations.forEach { (agentId, location) ->
                every { agentLocationRepository.latest(agentId) } returns location
            }
        }

        init {
            every { gameRepository.get(gameId, tenant) } returns game
        }

        @Test
        fun `utility agents are shown with their exact location`() {
            val utility = agent(type = Agent.Type.UTILITY)
            givenAgents(utility)
            givenLocations(utility.id to locationOf(utility.id, 3.5, 7.5))

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

            assertThat(board.utilityAgents).singleElement()
            assertThat(board.utilityAgents.first().location!!.geoLocation).isEqualTo(GeoLocation(3.5, 7.5))
            assertThat(board.misterxAgents).isEmpty()
        }

        @Test
        fun `misterx agents are shown as the grid cell containing their location`() {
            val misterX = agent(type = Agent.Type.MISTERX)
            givenAgents(misterX)
            givenLocations(misterX.id to locationOf(misterX.id, 3.5, 7.5))

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

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

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

            assertThat(board.utilityAgents).isEmpty()
            assertThat(board.misterxAgents).isEmpty()
        }

        @Test
        fun `agents without a known location are omitted`() {
            val utility = agent(type = Agent.Type.UTILITY)
            val misterX = agent(type = Agent.Type.MISTERX)
            givenAgents(utility, misterX)
            givenLocations(utility.id to null, misterX.id to null)

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

            assertThat(board.utilityAgents).isEmpty()
            assertThat(board.misterxAgents).isEmpty()
        }

        @Test
        fun `misterx agents located off the map are omitted`() {
            val misterX = agent(type = Agent.Type.MISTERX)
            givenAgents(misterX)
            givenLocations(misterX.id to locationOf(misterX.id, 50.0, 50.0))

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

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

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId = null), tenant)

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
            every { teamRepository.getOrNull(teamId, tenant) } returns Team(teamId, gameId, "Team A")
            every { findingRepository.byTeam(teamId, tenant) } returns listOf(
                AgentFinding(FindingId(), gameId, teamId, found.id, OffsetDateTime.now(), null, null),
            )

            val board = service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId), tenant)

            assertThat(board.misterxAgents).singleElement()
            assertThat(board.misterxAgents.first().agent.id).isEqualTo(unfound.id)
            assertThat(board.utilityAgents).singleElement()
        }

        @Test
        fun `a team that does not belong to the game is not found`() {
            every { teamRepository.getOrNull(teamId, tenant) } returns Team(teamId, GameId(), "Other game")

            assertThatThrownBy { service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId), tenant) }
                .isInstanceOf(TeamNotFoundException::class.java)
        }

        @Test
        fun `an unknown team is not found`() {
            every { teamRepository.getOrNull(teamId, tenant) } returns null

            assertThatThrownBy { service.getBoard(GameUseCase.GetBoardQuery(gameId, teamId), tenant) }
                .isInstanceOf(TeamNotFoundException::class.java)
        }
    }

    @Nested
    inner class GetLeaderboard {

        private val gameRepository = mockk<GameRepository>()
        private val teamRepository = mockk<TeamRepository>()
        private val agentRepository = mockk<AgentRepository>()
        private val findingRepository = mockk<FindingRepository>()

        private val service = GameService(
            gameRepository,
            teamRepository,
            agentRepository,
            mockk(relaxed = true),
            findingRepository,
        )

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
            val early = team(TeamId(), "Early")
            val late = team(TeamId(), "Late")
            givenAgents(a1, a2)
            givenTeams(late, early)
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
}
