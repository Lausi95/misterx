package net.lausi95.citygame.adapter.out.persistence.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

internal interface GameEntityJpaRepository : JpaRepository<GameEntity, String> {

    fun existsByIdAndTenant(id: String, tenant: String): Boolean

    fun findByIdAndTenant(id: String, tenant: String): GameEntity?

    fun existsByTitleAndTenant(title: String, tenant: String): Boolean

    fun findAllByTenant(pageable: Pageable, tenant: String): Page<GameEntity>
}
