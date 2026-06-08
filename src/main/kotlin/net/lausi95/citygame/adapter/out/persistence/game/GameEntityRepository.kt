package net.lausi95.citygame.adapter.out.persistence.game

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface GameEntityRepository : JpaRepository<GameEntity, String> {

    fun findByIdAndTenant(id: String, tenant: String): GameEntity?

    fun existsByTitleAndTenant(title: String, tenant: String): Boolean

    fun findAllByTenant(pageable: Pageable, tenant: String): Page<GameEntity>
}
