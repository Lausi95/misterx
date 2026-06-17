package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.common.Tenant

interface SaveTeamPort {

    fun saveTeam(team: Team, tenant: Tenant)
}
