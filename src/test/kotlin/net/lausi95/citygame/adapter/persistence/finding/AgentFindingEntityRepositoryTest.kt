package net.lausi95.citygame.adapter.persistence.finding

import net.lausi95.citygame.DatabaseIntegrationTest
import net.lausi95.citygame.adapter.out.persistence.agent.AgentEntity
import net.lausi95.citygame.adapter.out.persistence.agent.AgentEntityRepository
import net.lausi95.citygame.adapter.out.persistence.finding.AgentFindingEntity
import net.lausi95.citygame.adapter.out.persistence.finding.AgentFindingEntityRepository
import net.lausi95.citygame.adapter.out.persistence.game.GameEntity
import net.lausi95.citygame.adapter.out.persistence.game.GameEntityRepository
import net.lausi95.citygame.adapter.out.persistence.team.TeamEntity
import net.lausi95.citygame.adapter.out.persistence.team.TeamEntityRepository
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
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
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@DatabaseIntegrationTest
class AgentFindingEntityRepositoryTest {

    @Autowired
    private lateinit var gameEntityRepository: GameEntityRepository

    @Autowired
    private lateinit var teamEntityRepository: TeamEntityRepository

    @Autowired
    private lateinit var agentEntityRepository: AgentEntityRepository

    @Autowired
    private lateinit var agentFindingEntityRepository: AgentFindingEntityRepository

    private val tenant = Tenant("https://acme.city-game.net")

    private fun seedGame(): GameId {
        val game = Game(
            GameId(),
            GameTitle("Hunt"),
            OffsetDateTime.now().minusHours(1),
            OffsetDateTime.now().plusHours(1),
            Map(MapId(), GeoLocation(0.0, 0.0), GeoLocation(1.0, 1.0), Grid(10, 10)),
        )
        gameEntityRepository.saveAndFlush(GameEntity(game, tenant))
        return game.id
    }

    private fun seedTeam(gameId: GameId): TeamId {
        val team = Team(TeamId(), gameId, "Team A")
        teamEntityRepository.saveAndFlush(TeamEntity(team, tenant))
        return team.id
    }

    private fun seedAgent(gameId: GameId): AgentId {
        val agent = Agent(AgentId(), gameId, Agent.Type.MISTERX, "phone", "first", "last", "alias", true)
        agentEntityRepository.saveAndFlush(AgentEntity(agent, tenant))
        return agent.id
    }

    @Test
    fun `round-trips a finding with reported location and a null agent location`() {
        val gameId = seedGame()
        val teamId = seedTeam(gameId)
        val agentId = seedAgent(gameId)
        val foundAt = OffsetDateTime.now()

        val finding = AgentFinding(
            FindingId(),
            gameId,
            teamId,
            agentId,
            foundAt,
            GeoLocation(52.5, 13.4),
            null,
        )
        agentFindingEntityRepository.saveAndFlush(AgentFindingEntity(finding, tenant))

        val reloaded = agentFindingEntityRepository.findByTeamIdAndTenantOrderByFoundAtDesc(teamId.value, tenant.value)
            .single()
            .toAgentFinding()

        assertThat(reloaded.id).isEqualTo(finding.id)
        assertThat(reloaded.gameId).isEqualTo(gameId)
        assertThat(reloaded.agentId).isEqualTo(agentId)
        assertThat(reloaded.foundAt.toInstant().truncatedTo(ChronoUnit.MILLIS))
            .isEqualTo(foundAt.toInstant().truncatedTo(ChronoUnit.MILLIS))
        assertThat(reloaded.reportedLocation).isEqualTo(GeoLocation(52.5, 13.4))
        assertThat(reloaded.agentLocation).isNull()
    }

    @Test
    fun `finds by agent and reports existence by team and agent`() {
        val gameId = seedGame()
        val teamId = seedTeam(gameId)
        val agentId = seedAgent(gameId)

        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(
                AgentFinding(FindingId(), gameId, teamId, agentId, OffsetDateTime.now(), null, null),
                tenant,
            )
        )

        assertThat(agentFindingEntityRepository.findByAgentIdAndTenantOrderByFoundAtDesc(agentId.value, tenant.value)).hasSize(1)
        assertThat(agentFindingEntityRepository.existsByTeamIdAndAgentIdAndTenant(teamId.value, agentId.value, tenant.value)).isTrue()
        assertThat(agentFindingEntityRepository.existsByTeamIdAndAgentIdAndTenant(TeamId().value, agentId.value, tenant.value)).isFalse()
    }

    @Test
    fun `enforces the unique constraint on tenant, team and agent`() {
        val gameId = seedGame()
        val teamId = seedTeam(gameId)
        val agentId = seedAgent(gameId)

        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(
                AgentFinding(FindingId(), gameId, teamId, agentId, OffsetDateTime.now(), null, null),
                tenant,
            )
        )

        assertThatThrownBy {
            agentFindingEntityRepository.saveAndFlush(
                AgentFindingEntity(
                    AgentFinding(FindingId(), gameId, teamId, agentId, OffsetDateTime.now(), null, null),
                    tenant,
                )
            )
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `returns a team's findings newest first`() {
        val gameId = seedGame()
        val teamId = seedTeam(gameId)
        val older = seedAgent(gameId)
        val newer = seedAgent(gameId)
        val now = OffsetDateTime.now()

        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(AgentFinding(FindingId(), gameId, teamId, older, now.minusHours(2), null, null), tenant)
        )
        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(AgentFinding(FindingId(), gameId, teamId, newer, now, null, null), tenant)
        )

        val ordered = agentFindingEntityRepository.findByTeamIdAndTenantOrderByFoundAtDesc(teamId.value, tenant.value)
            .map { it.toAgentFinding().agentId }

        assertThat(ordered).containsExactly(newer, older)
    }

    @Test
    fun `returns an agent's findings newest first`() {
        val gameId = seedGame()
        val olderTeam = seedTeam(gameId)
        val newerTeam = seedTeam(gameId)
        val agentId = seedAgent(gameId)
        val now = OffsetDateTime.now()

        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(AgentFinding(FindingId(), gameId, olderTeam, agentId, now.minusHours(2), null, null), tenant)
        )
        agentFindingEntityRepository.saveAndFlush(
            AgentFindingEntity(AgentFinding(FindingId(), gameId, newerTeam, agentId, now, null, null), tenant)
        )

        val ordered = agentFindingEntityRepository.findByAgentIdAndTenantOrderByFoundAtDesc(agentId.value, tenant.value)
            .map { it.toAgentFinding().teamId }

        assertThat(ordered).containsExactly(newerTeam, olderTeam)
    }
}
