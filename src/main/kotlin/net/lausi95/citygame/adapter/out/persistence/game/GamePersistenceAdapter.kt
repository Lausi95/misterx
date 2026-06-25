package net.lausi95.citygame.adapter.out.persistence.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class GamePersistenceAdapter(
    private val gameEntityJpaRepository: GameEntityJpaRepository,
    private val mapEntityJpaRepository: MapEntityJpaRepository,
) : GameRepository {

    override fun save(game: Game, tenant: Tenant) {
        val gameEntity = GameEntity(game, tenant)
        val mapEntity = MapEntity(game)

        gameEntityJpaRepository.save(gameEntity)
        mapEntityJpaRepository.save(mapEntity)
    }

    override fun getOrNull(gameId: GameId, tenant: Tenant): Game? {
        return gameEntityJpaRepository.findByIdAndTenant(gameId.value, tenant.value)?.let {
            val mapEntity = mapEntityJpaRepository.findByGameId(gameId.value)!!
            it.toGame(mapEntity)
        }
    }

    override fun titleAvailable(title: GameTitle, tenant: Tenant): Boolean {
        return !gameEntityJpaRepository.existsByTitleAndTenant(title.value, tenant.value)
    }

    override fun getAll(pageable: Pageable, tenant: Tenant): Page<Game> {
        return gameEntityJpaRepository.findAllByTenant(pageable, tenant.value).map {
            val mapEntity = mapEntityJpaRepository.findByGameId(it.id!!)!!
            it.toGame(mapEntity)
        }
    }

    override fun exists(
        gameId: GameId,
        tenant: Tenant
    ): Boolean {
        return gameEntityJpaRepository.existsByIdAndTenant(gameId.value, tenant.value)
    }
}
