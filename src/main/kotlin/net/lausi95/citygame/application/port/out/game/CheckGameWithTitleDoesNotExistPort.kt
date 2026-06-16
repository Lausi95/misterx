package net.lausi95.citygame.application.port.out.game

import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.common.Tenant

interface CheckGameWithTitleDoesNotExistPort {

    fun gameWithTitleDoesNotExist(title: GameTitle, tenant: Tenant): Boolean

    fun assertGameWithTitleDoesNotExist(title: GameTitle, tenant: Tenant) {
        if (!gameWithTitleDoesNotExist(title, tenant)) {
            error("Game with title ${title.value} already exist")
        }
    }
}
