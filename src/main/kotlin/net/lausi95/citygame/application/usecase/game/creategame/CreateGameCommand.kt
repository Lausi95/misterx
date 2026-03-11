package net.lausi95.citygame.application.usecase.game.creategame

import net.lausi95.citygame.domain.game.GameTitle
import java.time.OffsetDateTime

data class CreateGameCommand(
    val title: GameTitle,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime,
)