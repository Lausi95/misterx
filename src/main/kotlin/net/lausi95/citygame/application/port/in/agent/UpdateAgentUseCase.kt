package net.lausi95.citygame.application.port.`in`.agent

import net.lausi95.citygame.application.domain.model.agent.Agent.Type
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

interface UpdateAgentUseCase {

    data class Command(
        val agentId: AgentId,
        val gameId: GameId,
        val type: Type?,
        val phoneNumber: String?,
        val firstName: String?,
        val lastName: String?,
        val alias: String?,
        val active: Boolean?,
    )

    fun updateAgent(command: Command, tenant: Tenant)
}
