package net.lausi95.citygame.adapter.out.persistence.team

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

internal interface TeamEntityRepository : JpaRepository<TeamEntity, String> {

    fun findByIdAndTenant(id: String, tenant: String): TeamEntity?

    fun existsByIdAndTenant(id: String, tenant: String): Boolean

    fun findByGameIdAndTenant(gameId: String, tenant: String, pageable: Pageable): Page<TeamEntity>
}
