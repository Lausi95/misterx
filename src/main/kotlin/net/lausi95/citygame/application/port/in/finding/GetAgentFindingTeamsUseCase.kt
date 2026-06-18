package net.lausi95.citygame.application.port.`in`.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import net.lausi95.citygame.common.Tenant

interface GetAgentFindingTeamsUseCase {

    fun getFindingTeams(agentId: AgentId, tenant: Tenant): List<FindingTeam>
}
