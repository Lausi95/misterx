package net.lausi95.citygame.application.port.`in`.board

import net.lausi95.citygame.application.domain.model.board.Board
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface GetBoardUseCase {

    /**
     * @param teamId the viewing team, or `null` for the full operator view. When present, MISTERX
     *   agents the team has already found are excluded from the board.
     */
    data class Query(
        val gameId: GameId,
        val teamId: TeamId?,
    )

    fun getBoard(query: Query, tenant: Tenant): Board
}
