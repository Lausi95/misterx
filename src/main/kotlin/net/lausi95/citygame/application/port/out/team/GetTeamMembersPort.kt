package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetTeamMembersPort {

    fun getTeamMembers(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember>

    fun countTeamMembers(teamId: TeamId, tenant: Tenant): Long
}
