package net.lausi95.citygame.application.domain.service

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import net.lausi95.citygame.application.port.`in`.finding.GetAgentFindingTeamsUseCase
import net.lausi95.citygame.application.port.out.finding.GetAgentFindingsPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
class GetAgentFindingTeamsService(
    private val getAgentFindingsPort: GetAgentFindingsPort,
    private val getTeamPort: GetTeamPort,
) : GetAgentFindingTeamsUseCase {

    override fun getFindingTeams(agentId: AgentId, tenant: Tenant): List<FindingTeam> {
        return getAgentFindingsPort.getFindingsByAgent(agentId, tenant).mapNotNull { finding ->
            getTeamPort.getTeamOrNull(finding.teamId, tenant)?.let { team ->
                FindingTeam(team.id, team.name)
            }
        }
    }
}
