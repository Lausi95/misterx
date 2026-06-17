package net.lausi95.citygame.adapter.out.persistence.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.out.team.CheckTeamExistsPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.application.port.out.team.GetTeamsPort
import net.lausi95.citygame.application.port.out.team.SaveTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class TeamPersistenceAdapter(
    private val teamEntityRepository: TeamEntityRepository,
) : SaveTeamPort, GetTeamPort, GetTeamsPort, CheckTeamExistsPort {

    override fun saveTeam(team: Team, tenant: Tenant) {
        teamEntityRepository.save(TeamEntity(team, tenant))
    }

    override fun getTeamOrNull(teamId: TeamId, tenant: Tenant): Team? {
        return teamEntityRepository.findByIdAndTenant(teamId.value, tenant.value)?.toTeam()
    }

    override fun teamExists(teamId: TeamId, tenant: Tenant): Boolean {
        return teamEntityRepository.existsByIdAndTenant(teamId.value, tenant.value)
    }

    override fun getTeams(pageable: Pageable, gameId: GameId, tenant: Tenant): Page<Team> {
        return teamEntityRepository.findByGameIdAndTenant(gameId.value, tenant.value, pageable).map { it.toTeam() }
    }
}
