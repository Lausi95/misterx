package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetTeamsPort {

    fun getTeams(pageable: Pageable, gameId: GameId, tenant: Tenant): Page<Team>
}
