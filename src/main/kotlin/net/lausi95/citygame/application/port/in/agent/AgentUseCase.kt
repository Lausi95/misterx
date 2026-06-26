package net.lausi95.citygame.application.port.`in`.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AgentUseCase {

    data class CreateAgentCommand(
        val gameId: GameId,
        val type: Agent.Type,
        val phoneNumber: String,
        val firstName: String,
        val lastName: String,
        val alias: String,
        val active: Boolean,
    )

    data class DeleteAgentCommand(
        val gameId: GameId,
        val agentId: AgentId,
    )

    data class GetMyAgentQuery(val gameId: GameId, val agentId: AgentId)

    data class UpdateAgentCommand(
        val agentId: AgentId,
        val gameId: GameId,
        val type: Agent.Type?,
        val phoneNumber: String?,
        val firstName: String?,
        val lastName: String?,
        val alias: String?,
        val active: Boolean?,
    )

    fun createAgent(command: CreateAgentCommand, tenant: Tenant): AgentId

    fun getAgents(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Agent>

    fun getAgent(agentId: AgentId, tenant: Tenant): Agent

    fun getMyAgent(query: GetMyAgentQuery, tenant: Tenant): Agent

    fun updateAgent(command: UpdateAgentCommand, tenant: Tenant)

    fun deleteAgent(command: DeleteAgentCommand, tenant: Tenant)

    fun updateLocation(gameId: GameId, agentId: AgentId, geoLocation: GeoLocation, tenant: Tenant)
}
