package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.Tenant

interface GetMyTeamUseCase {

    data class Query(val gameId: GameId, val teamId: TeamId, val memberId: TeamMemberId?)

    fun getMyTeam(query: Query, tenant: Tenant): Team
}
