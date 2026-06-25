package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * The persistence seam for the [TeamMember] aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports. Kept separate from [TeamRepository] pending the
 * decision on whether [TeamMember] should fold into the Team aggregate root.
 */
interface TeamMemberRepository {

    fun save(teamMember: TeamMember, tenant: Tenant)

    fun getOrNull(memberId: TeamMemberId, tenant: Tenant): TeamMember?

    fun forTeam(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember>

    fun countByTeam(teamId: TeamId, tenant: Tenant): Long

    fun deleteByTeam(teamId: TeamId, tenant: Tenant)
}
