package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface DeleteTeamPort {

    fun deleteTeam(teamId: TeamId, tenant: Tenant)
}
