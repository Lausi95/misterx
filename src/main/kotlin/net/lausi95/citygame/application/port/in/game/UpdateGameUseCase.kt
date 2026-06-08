package net.lausi95.citygame.application.port.`in`.game

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import java.time.OffsetDateTime

interface UpdateGameUseCase {

    data class Command(
        val gameId: GameId,
        val title: GameTitle?,
        val startTime: OffsetDateTime?,
        val endTime: OffsetDateTime?,
        val map: MapDto?,
    )

    data class MapDto(
        val cornerA: GeoLocation?,
        val cornerB: GeoLocation?,
        val grid: Grid?,
    )

    fun updateGame(command: Command, tenant: Tenant)
}
