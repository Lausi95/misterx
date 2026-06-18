package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.common.Tenant

interface SaveTeamMemberPort {

    fun saveTeamMember(teamMember: TeamMember, tenant: Tenant)
}
