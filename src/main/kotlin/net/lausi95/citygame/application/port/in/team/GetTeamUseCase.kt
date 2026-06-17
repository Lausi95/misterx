package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface GetTeamUseCase {

    fun getTeam(teamId: TeamId, tenant: Tenant): Team
}
