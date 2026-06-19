package net.lausi95.citygame.application.port.`in`.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface GetMyAgentUseCase {

    data class Query(val gameId: GameId, val agentId: AgentId)

    fun getMyAgent(query: Query, tenant: Tenant): Agent
}
