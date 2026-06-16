package net.lausi95.citygame.adapter.out.persistence.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.port.out.game.*
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class GamePersistenceAdapter(
    private val gameEntityRepository: GameEntityRepository,
    private val mapEntityRepository: MapEntityRepository,
) : CheckGameExistsPort, CheckGameWithTitleDoesNotExistPort, GetGamePort, GetGamesPort, SaveGamePort {

    override fun saveGame(game: Game, tenant: Tenant) {
        val gameEntity = GameEntity(game, tenant)
        val mapEntity = MapEntity(game)

        gameEntityRepository.save(gameEntity)
        mapEntityRepository.save(mapEntity)
    }

    override fun getGameOrNull(gameId: GameId, tenant: Tenant): Game? {
        return gameEntityRepository.findByIdAndTenant(gameId.value, tenant.value)?.let {
            val mapEntity = mapEntityRepository.findByGameId(gameId.value)!!
            it.toGame(mapEntity)
        }
    }

    override fun gameWithTitleDoesNotExist(title: GameTitle, tenant: Tenant): Boolean {
        return !gameEntityRepository.existsByTitleAndTenant(title.value, tenant.value)
    }

    override fun getGames(pageable: Pageable, tenant: Tenant): Page<Game> {
        return gameEntityRepository.findAllByTenant(pageable, tenant.value).map {
            val mapEntity = mapEntityRepository.findByGameId(it.id!!)!!
            it.toGame(mapEntity)
        }
    }

    override fun gameExists(
        gameId: GameId,
        tenant: Tenant
    ): Boolean {
        return gameEntityRepository.existsByIdAndTenant(gameId.value, tenant.value)
    }
}
