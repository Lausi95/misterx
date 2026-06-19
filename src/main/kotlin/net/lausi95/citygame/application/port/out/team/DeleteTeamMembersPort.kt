package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.common.Tenant

interface DeleteTeamMembersPort {

    fun deleteTeamMembers(teamId: TeamId, tenant: Tenant)
}
