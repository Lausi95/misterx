package net.lausi95.citygame.adapter.out.persistence.game

import org.springframework.data.jpa.repository.JpaRepository

internal interface MapEntityJpaRepository : JpaRepository<MapEntity, String> {

    fun findByGameId(gameId: String): MapEntity?
}