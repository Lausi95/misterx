package net.lausi95.citygame.adapter.out.persistence.finding

import org.springframework.data.jpa.repository.JpaRepository

internal interface AgentFindingEntityRepository : JpaRepository<AgentFindingEntity, String> {

    fun existsByTeamIdAndAgentIdAndTenant(teamId: String, agentId: String, tenant: String): Boolean

    fun findByTeamIdAndTenantOrderByFoundAtDesc(teamId: String, tenant: String): List<AgentFindingEntity>

    fun findByAgentIdAndTenantOrderByFoundAtDesc(agentId: String, tenant: String): List<AgentFindingEntity>

    fun deleteByTeamIdAndTenant(teamId: String, tenant: String)

    fun deleteByAgentIdAndTenant(agentId: String, tenant: String)
}
