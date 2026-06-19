package net.lausi95.citygame.application.service

import io.mockk.every
import io.mockk.mockk
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.AgentFinding
import net.lausi95.citygame.application.domain.model.finding.FindingId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.service.GetTeamFoundAgentsService
import net.lausi95.citygame.application.port.out.agent.GetAgentPort
import net.lausi95.citygame.application.port.out.finding.GetTeamFindingsPort
import net.lausi95.citygame.common.Tenant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class GetTeamFoundAgentsServiceTest {

    private val getTeamFindingsPort = mockk<GetTeamFindingsPort>()
    private val getAgentPort = mockk<GetAgentPort>()
    private val service = GetTeamFoundAgentsService(getTeamFindingsPort, getAgentPort)

    private val tenant = Tenant("acme")
    private val gameId = GameId()
    private val teamId = TeamId()

    private fun finding(agentId: AgentId, foundAt: ZonedDateTime = ZonedDateTime.now()) =
        AgentFinding(FindingId(), gameId, teamId, agentId, foundAt, null, null)

    private fun agent(agentId: AgentId, alias: String) =
        Agent(agentId, gameId, Agent.Type.MISTERX, "phone", "first", "last", alias, true)

    @Test
    fun `exposes found agents as id, alias and found time`() {
        val agentId = AgentId()
        val foundAt = ZonedDateTime.now()
        every { getTeamFindingsPort.getFindingsByTeam(teamId, tenant) } returns listOf(finding(agentId, foundAt))
        every { getAgentPort.getAgentOrNull(agentId, tenant) } returns agent(agentId, "Shadow")

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
        every { getTeamFindingsPort.getFindingsByTeam(teamId, tenant) } returns
            listOf(finding(present), finding(missing))
        every { getAgentPort.getAgentOrNull(present, tenant) } returns agent(present, "Shadow")
        every { getAgentPort.getAgentOrNull(missing, tenant) } returns null

        val foundAgents = service.getFoundAgents(teamId, tenant)

        assertThat(foundAgents).extracting<AgentId> { it.agentId }.containsExactly(present)
    }
}
