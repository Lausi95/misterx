package net.lausi95.citygame.adapter.out.persistence.agent

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

internal interface AgentEntityRepository : JpaRepository<AgentEntity, String> {

    fun findByIdAndTenant(id: String, tenant: String): AgentEntity?

    fun existsByIdAndTenant(id: String, tenant: String): Boolean

    fun findByGameIdAndTenant(gameId: String, tenant: String, pageable: Pageable): Page<AgentEntity>
}
