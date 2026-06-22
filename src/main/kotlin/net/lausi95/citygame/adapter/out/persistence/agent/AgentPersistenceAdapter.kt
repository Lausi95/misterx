package net.lausi95.citygame.adapter.out.persistence.agent

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.out.agent.CheckAgentExistsPort
import net.lausi95.citygame.application.port.out.agent.CountAgentsByGamePort
import net.lausi95.citygame.application.port.out.agent.DeleteAgentPort
import net.lausi95.citygame.application.port.out.agent.GetAgentPort
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.application.port.out.agent.SaveAgentPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class AgentPersistenceAdapter(
    private val agentEntityRepository: AgentEntityRepository,
) : SaveAgentPort, GetAgentPort, GetAgentsPort, CheckAgentExistsPort, CountAgentsByGamePort, DeleteAgentPort {

    override fun saveAgent(agent: Agent, tenant: Tenant) {
        agentEntityRepository.save(AgentEntity(agent, tenant))
    }

    override fun getAgentOrNull(
        agentId: AgentId,
        tenant: Tenant
    ): Agent? {
        return agentEntityRepository.findByIdAndTenant(agentId.value, tenant.value)?.toAgent()
    }

    override fun agentExists(
        agentId: AgentId,
        tenant: Tenant
    ): Boolean {
        return agentEntityRepository.existsByIdAndTenant(agentId.value, tenant.value)
    }

    override fun getAgents(
        pageable: Pageable,
        gameId: GameId,
        tenant: Tenant,
    ): Page<Agent> {
        return agentEntityRepository.findByGameIdAndTenant(gameId.value, tenant.value, pageable).map { it.toAgent() }
    }

    override fun countAgentsByGame(gameId: GameId, tenant: Tenant): Int {
        return agentEntityRepository.countByGameIdAndTenant(gameId.value, tenant.value)
    }

    override fun deleteAgent(agentId: AgentId, tenant: Tenant) {
        agentEntityRepository.deleteByIdAndTenant(agentId.value, tenant.value)
    }
}
