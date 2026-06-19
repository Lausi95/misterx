package net.lausi95.citygame.application.port.out.finding

import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface DeleteTeamFindingsPort {

    fun deleteTeamFindings(teamId: TeamId, tenant: Tenant)
}
