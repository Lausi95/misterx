package net.lausi95.citygame.application.usecase.game.updategame

import net.lausi95.citygame.domain.game.GameId
import net.lausi95.citygame.domain.game.GameTitle

data class UpdateGameCommand(
    val gameId: GameId,
    val title: GameTitle,
)
