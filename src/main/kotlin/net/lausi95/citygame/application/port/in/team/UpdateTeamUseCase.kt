package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface UpdateTeamUseCase {

    data class Command(
        val teamId: TeamId,
        val name: String?,
    )

    fun updateTeam(command: Command, tenant: Tenant)
}
