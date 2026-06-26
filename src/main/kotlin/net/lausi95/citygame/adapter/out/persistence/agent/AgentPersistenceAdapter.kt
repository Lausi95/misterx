package net.lausi95.citygame.adapter.out.persistence.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

@Component
internal class AgentPersistenceAdapter(
    private val agentEntityJpaRepository: AgentEntityJpaRepository,
    private val agentLocationRepository: AgentLocationRepository,
) : AgentRepository {

    override fun save(agent: Agent, tenant: Tenant) {
        agentEntityJpaRepository.save(AgentEntity(agent, tenant))
    }

    override fun getOrNull(
        agentId: AgentId,
        tenant: Tenant
    ): Agent? {
        return agentEntityJpaRepository.findByIdAndTenant(agentId.value, tenant.value)?.toAgent()
    }

    override fun byIds(agentIds: Collection<AgentId>, tenant: Tenant): List<Agent> {
        return agentEntityJpaRepository.findByIdInAndTenant(agentIds.map { it.value }, tenant.value).map { it.toAgent() }
    }

    override fun exists(
        agentId: AgentId,
        tenant: Tenant
    ): Boolean {
        return agentEntityJpaRepository.existsByIdAndTenant(agentId.value, tenant.value)
    }

    override fun forGame(
        gameId: GameId,
        tenant: Tenant,
    ): List<Agent> {
        return agentEntityJpaRepository.findByGameIdAndTenant(gameId.value, tenant.value).map { it.toAgent() }
    }

    override fun getWithLocation(agentId: AgentId, tenant: Tenant): Agent =
        get(agentId, tenant).also { agent ->
            agentLocationRepository.latest(agentId)?.also { agent.setLocation(it) }
        }

    override fun forGameWithLocation(gameId: GameId, tenant: Tenant): List<Agent> =
        forGame(gameId, tenant).onEach { agent ->
            agentLocationRepository.latest(agent.id)?.also { agent.setLocation(it) }
        }

    override fun countByGame(gameId: GameId, tenant: Tenant): Int {
        return agentEntityJpaRepository.countByGameIdAndTenant(gameId.value, tenant.value)
    }

    override fun delete(agentId: AgentId, tenant: Tenant) {
        agentEntityJpaRepository.deleteByIdAndTenant(agentId.value, tenant.value)
    }
}
