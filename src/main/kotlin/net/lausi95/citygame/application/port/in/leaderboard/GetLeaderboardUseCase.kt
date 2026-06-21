package net.lausi95.citygame.application.port.`in`.leaderboard

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.common.Tenant

interface GetLeaderboardUseCase {

    fun getLeaderboard(gameId: GameId, tenant: Tenant): Leaderboard
}
