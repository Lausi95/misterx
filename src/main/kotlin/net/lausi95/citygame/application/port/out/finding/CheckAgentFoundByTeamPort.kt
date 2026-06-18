package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface CheckAgentFoundByTeamPort {

    fun teamHasFoundAgent(teamId: TeamId, agentId: AgentId, tenant: Tenant): Boolean
}
