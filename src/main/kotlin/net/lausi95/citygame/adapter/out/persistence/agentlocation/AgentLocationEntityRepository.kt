package net.lausi95.citygame.adapter.out.persistence.agentlocation

import org.springframework.data.jpa.repository.JpaRepository

internal interface AgentLocationEntityRepository : JpaRepository<AgentLocationEntity, String> {

    fun findFirstByAgentIdOrderByTimestampDesc(agentId: String): AgentLocationEntity?

    fun deleteByAgentIdAndTenant(agentId: String, tenant: String)
}