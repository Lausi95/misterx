package net.lausi95.citygame.application.port.`in`.game

import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameSummary
import net.lausi95.citygame.application.domain.model.game.GameTitle
import net.lausi95.citygame.application.domain.model.game.Grid
import net.lausi95.citygame.application.domain.model.game.Map
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime

interface GameUseCase {

    data class CreateGameCommand(
        val title: GameTitle,
        val startTime: OffsetDateTime,
        val endTime: OffsetDateTime,
        val map: MapDto,
    ) {
        data class MapDto(
            val cornerA: GeoLocation,
            val cornerB: GeoLocation,
            val grid: Grid,
        )
    }

    data class UpdateGameCommand(
        val gameId: GameId,
        val title: GameTitle?,
        val startTime: OffsetDateTime?,
        val endTime: OffsetDateTime?,
        val map: UpdateGameMapDto?,
    )

    data class UpdateGameMapDto(
        val cornerA: GeoLocation?,
        val cornerB: GeoLocation?,
        val grid: Grid?,
    )

    data class GetBoardQuery(val gameId: GameId, val teamId: TeamId?)

    fun createGame(command: CreateGameCommand, tenant: Tenant): GameId

    fun getGames(pageable: Pageable, tenant: Tenant): Page<GameSummary>

    fun getGame(gameId: GameId, tenant: Tenant): GameSummary

    fun getMap(gameId: GameId, tenant: Tenant): Map

    fun updateGame(command: UpdateGameCommand, tenant: Tenant)

    fun getBoard(query: GetBoardQuery, tenant: Tenant): Board

    fun getLeaderboard(gameId: GameId, tenant: Tenant): Leaderboard
}
