package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface GetTeamPort {

    fun getTeamOrNull(teamId: TeamId, tenant: Tenant): Team?

    fun getTeam(teamId: TeamId, tenant: Tenant): Team {
        return getTeamOrNull(teamId, tenant) ?: error("Team not found")
    }
}
