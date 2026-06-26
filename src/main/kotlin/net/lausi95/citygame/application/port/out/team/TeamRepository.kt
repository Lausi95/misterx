package net.lausi95.citygame.application.port.out.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * The persistence seam for the [Team] aggregate. One repository per aggregate (ADR 0017),
 * replacing the former single-method ports. [TeamMember] keeps its own repository pending its
 * own aggregate review.
 */
interface TeamRepository {

    fun save(team: Team, tenant: Tenant)

    fun getOrNull(teamId: TeamId, tenant: Tenant): Team?

    fun get(teamId: TeamId, tenant: Tenant): Team =
        getOrNull(teamId, tenant) ?: teamNotFound(teamId)

    fun forGame(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Team>

    fun exists(teamId: TeamId, tenant: Tenant): Boolean

    fun requireExists(teamId: TeamId, tenant: Tenant) =
        require(exists(teamId, tenant)) { "Team does not exist." }

    fun countByGame(gameId: GameId, tenant: Tenant): Int

    fun delete(teamId: TeamId, tenant: Tenant)
}
