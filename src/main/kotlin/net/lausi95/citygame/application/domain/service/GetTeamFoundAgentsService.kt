package net.lausi95.citygame.application.domain.service

import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.finding.GetTeamFoundAgentsUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.finding.GetTeamFindingsPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
class GetTeamFoundAgentsService(
    private val getTeamFindingsPort: GetTeamFindingsPort,
    private val agentRepository: AgentRepository,
) : GetTeamFoundAgentsUseCase {

    override fun getFoundAgents(teamId: TeamId, tenant: Tenant): List<FoundAgent> {
        return getTeamFindingsPort.getFindingsByTeam(teamId, tenant).mapNotNull { finding ->
            agentRepository.getOrNull(finding.agentId, tenant)?.let { agent ->
                FoundAgent(agent.id, agent.alias, finding.foundAt)
            }
        }
    }

    override fun getFoundAgentsByTeams(teamIds: Collection<TeamId>, tenant: Tenant): Map<TeamId, List<FoundAgent>> {
        val findings = getTeamFindingsPort.getFindingsByTeams(teamIds, tenant)
        val agentsById = agentRepository.byIds(findings.map { it.agentId }.toSet(), tenant)
            .associateBy { it.id }
        return findings
            .mapNotNull { finding -> agentsById[finding.agentId]?.let { agent -> finding.teamId to FoundAgent(agent.id, agent.alias, finding.foundAt) } }
            .groupBy({ it.first }, { it.second })
    }
}
