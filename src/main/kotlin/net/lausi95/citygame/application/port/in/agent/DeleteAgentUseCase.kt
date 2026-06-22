package net.lausi95.citygame.application.port.`in`.agent

import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface DeleteAgentUseCase {

    data class Command(
        val gameId: GameId,
        val agentId: AgentId,
    )

    fun deleteAgent(command: Command, tenant: Tenant)
}
