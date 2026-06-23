package net.lausi95.citygame.application.port.`in`.finding

import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface GetTeamFoundAgentsUseCase {

    fun getFoundAgents(teamId: TeamId, tenant: Tenant): List<FoundAgent>

    fun getFoundAgentsByTeams(teamIds: Collection<TeamId>, tenant: Tenant): Map<TeamId, List<FoundAgent>>
}
