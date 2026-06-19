package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface DeleteTeamUseCase {

    data class Command(
        val gameId: GameId,
        val teamId: TeamId,
    )

    fun deleteTeam(command: Command, tenant: Tenant)
}
