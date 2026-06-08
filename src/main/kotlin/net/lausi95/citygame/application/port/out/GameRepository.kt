package net.lausi95.citygame.application.port.out

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GameRepository {

    fun save(game: Game, tenant: Tenant)

    fun findById(id: GameId, tenant: Tenant): Game?

    fun existsByTitle(title: GameTitle, tenant: Tenant): Boolean

    fun find(pageable: Pageable, tenant: Tenant): Page<Game>
}