package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface CreateTeamUseCase {

    data class Command(
        val gameId: GameId,
        val name: String,
    )

    fun createTeam(command: Command, tenant: Tenant): TeamId
}
