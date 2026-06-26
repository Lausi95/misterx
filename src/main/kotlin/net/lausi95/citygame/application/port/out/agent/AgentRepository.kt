package net.lausi95.citygame.application.port.out.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.common.Tenant

/**
 * The persistence seam for the [Agent] aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports. Adapters implement the primitives ([getOrNull],
 * [exists]); [get] and [requireExists] are defaults expressed in terms of them, so the
 * not-found/assert behaviour has a single home.
 */
interface AgentRepository {

    fun save(agent: Agent, tenant: Tenant)

    fun getOrNull(agentId: AgentId, tenant: Tenant): Agent?

    fun get(agentId: AgentId, tenant: Tenant): Agent =
        getOrNull(agentId, tenant) ?: agentNotFound(agentId)

    fun byIds(agentIds: Collection<AgentId>, tenant: Tenant): List<Agent>

    fun forGame(gameId: GameId, tenant: Tenant): List<Agent>

    fun exists(agentId: AgentId, tenant: Tenant): Boolean

    fun requireExists(agentId: AgentId, tenant: Tenant) =
        require(exists(agentId, tenant)) { "Agent does not exist." }

    fun countByGame(gameId: GameId, tenant: Tenant): Int

    fun delete(agentId: AgentId, tenant: Tenant)
}
