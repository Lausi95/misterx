package net.lausi95.citygame.application.domain.service

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import net.lausi95.citygame.application.port.`in`.finding.GetAgentFindingTeamsUseCase
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
class GetAgentFindingTeamsService(
    private val findingRepository: FindingRepository,
    private val teamRepository: TeamRepository,
) : GetAgentFindingTeamsUseCase {

    override fun getFindingTeams(agentId: AgentId, tenant: Tenant): List<FindingTeam> {
        return findingRepository.byAgent(agentId, tenant).mapNotNull { finding ->
            teamRepository.getOrNull(finding.teamId, tenant)?.let { team ->
                FindingTeam(team.id, team.name, finding.foundAt)
            }
        }
    }
}
