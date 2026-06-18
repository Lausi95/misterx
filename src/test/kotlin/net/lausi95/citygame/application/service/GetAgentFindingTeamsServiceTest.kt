package net.lausi95.citygame.application.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.service.GetAgentFindingTeamsService
import net.lausi95.citygame.application.port.out.finding.GetAgentFindingsPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class GetAgentFindingTeamsServiceTest {

    private val getAgentFindingsPort = mockk<GetAgentFindingsPort>()
    private val getTeamPort = mockk<GetTeamPort>()
    private val service = GetAgentFindingTeamsService(getAgentFindingsPort, getTeamPort)

    private val tenant = Tenant("acme")
    private val gameId = GameId()
    private val agentId = AgentId()

    private fun finding(teamId: TeamId) =
        AgentFinding(FindingId(), gameId, teamId, agentId, ZonedDateTime.now(), null, null)

    @Test
    fun `exposes finding teams as id and name`() {
        val teamId = TeamId()
        every { getAgentFindingsPort.getFindingsByAgent(agentId, tenant) } returns listOf(finding(teamId))
        every { getTeamPort.getTeamOrNull(teamId, tenant) } returns Team(teamId, gameId, "Team A")

        val teams = service.getFindingTeams(agentId, tenant)

        assertThat(teams).singleElement().satisfies({
            assertThat(it.teamId).isEqualTo(teamId)
            assertThat(it.name).isEqualTo("Team A")
        })
    }

    @Test
    fun `drops findings whose team can no longer be resolved`() {
        val present = TeamId()
        val missing = TeamId()
        every { getAgentFindingsPort.getFindingsByAgent(agentId, tenant) } returns
            listOf(finding(present), finding(missing))
        every { getTeamPort.getTeamOrNull(present, tenant) } returns Team(present, gameId, "Team A")
        every { getTeamPort.getTeamOrNull(missing, tenant) } returns null

        val teams = service.getFindingTeams(agentId, tenant)

        assertThat(teams).extracting<TeamId> { it.teamId }.containsExactly(present)
    }
}
