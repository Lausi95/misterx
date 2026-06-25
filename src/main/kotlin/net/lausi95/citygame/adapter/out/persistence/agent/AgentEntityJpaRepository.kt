package net.lausi95.citygame.adapter.out.persistence.agent

import org.springframework.data.jpa.repository.JpaRepository

internal interface AgentEntityJpaRepository : JpaRepository<AgentEntity, String> {

    fun findByIdAndTenant(id: String, tenant: String): AgentEntity?

    fun findByIdInAndTenant(ids: Collection<String>, tenant: String): List<AgentEntity>

    fun existsByIdAndTenant(id: String, tenant: String): Boolean

    fun findByGameIdAndTenant(gameId: String, tenant: String): List<AgentEntity>

    fun countByGameIdAndTenant(gameId: String, tenant: String): Int

    fun deleteByIdAndTenant(id: String, tenant: String)
}
