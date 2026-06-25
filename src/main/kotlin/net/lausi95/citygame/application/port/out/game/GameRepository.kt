package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * The persistence seam for the [Game] aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports. Adapters implement the primitives ([getOrNull],
 * [exists], [titleAvailable]); [get], [requireExists] and [requireTitleAvailable] are defaults
 * expressed in terms of them.
 */
interface GameRepository {

    fun save(game: Game, tenant: Tenant)

    fun getOrNull(gameId: GameId, tenant: Tenant): Game?

    fun get(gameId: GameId, tenant: Tenant): Game =
        getOrNull(gameId, tenant) ?: gameNotFound(gameId)

    fun getAll(pageable: Pageable, tenant: Tenant): Page<Game>

    fun exists(gameId: GameId, tenant: Tenant): Boolean

    fun requireExists(gameId: GameId, tenant: Tenant) =
        require(exists(gameId, tenant)) { "Game with ID '${gameId.value}' does not exist" }

    fun titleAvailable(title: GameTitle, tenant: Tenant): Boolean

    fun requireTitleAvailable(title: GameTitle, tenant: Tenant) {
        if (!titleAvailable(title, tenant)) {
            error("Game with title ${title.value} already exist")
        }
    }
}
