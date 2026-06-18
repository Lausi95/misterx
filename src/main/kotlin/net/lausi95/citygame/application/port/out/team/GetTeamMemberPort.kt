package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.Tenant

interface GetTeamMemberPort {

    fun getTeamMemberOrNull(memberId: TeamMemberId, tenant: Tenant): TeamMember?
}
