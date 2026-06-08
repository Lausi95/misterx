package net.lausi95.citygame.adapter.out.persistence.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.port.out.GameRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class GamePersistenceAdapter(
    private val gameEntityRepository: GameEntityRepository,
    private val mapEntityRepository: MapEntityRepository,
) : GameRepository {

    override fun save(game: Game, tenant: Tenant) {
        val gameEntity = GameEntity(game, tenant)
        val mapEntity = MapEntity(game)

        gameEntityRepository.save(gameEntity)
        mapEntityRepository.save(mapEntity)
    }

    override fun findById(id: GameId, tenant: Tenant): Game? {
        return gameEntityRepository.findByIdAndTenant(id.value, tenant.value)?.let {
            val mapEntity = mapEntityRepository.findByGameId(id.value)!!
            it.toGame(mapEntity)
        }
    }

    override fun existsByTitle(title: GameTitle, tenant: Tenant): Boolean {
        return gameEntityRepository.existsByTitleAndTenant(title.value, tenant.value)
    }

    override fun find(pageable: Pageable, tenant: Tenant): Page<Game> {
        return gameEntityRepository.findAllByTenant(pageable, tenant.value).map {
            val mapEntity = mapEntityRepository.findByGameId(it.id!!)!!
            it.toGame(mapEntity)
        }
    }
}
