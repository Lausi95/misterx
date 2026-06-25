package net.lausi95.citygame.adapter.out.persistence.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class TeamPersistenceAdapter(
    private val teamEntityJpaRepository: TeamEntityJpaRepository,
) : TeamRepository {

    override fun save(team: Team, tenant: Tenant) {
        teamEntityJpaRepository.save(TeamEntity(team, tenant))
    }

    override fun getOrNull(teamId: TeamId, tenant: Tenant): Team? {
        return teamEntityJpaRepository.findByIdAndTenant(teamId.value, tenant.value)?.toTeam()
    }

    override fun exists(teamId: TeamId, tenant: Tenant): Boolean {
        return teamEntityJpaRepository.existsByIdAndTenant(teamId.value, tenant.value)
    }

    override fun forGame(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Team> {
        return teamEntityJpaRepository.findByGameIdAndTenant(gameId.value, tenant.value, pageable).map { it.toTeam() }
    }

    override fun delete(teamId: TeamId, tenant: Tenant) {
        teamEntityJpaRepository.deleteByIdAndTenant(teamId.value, tenant.value)
    }

    override fun countByGame(gameId: GameId, tenant: Tenant): Int {
        return teamEntityJpaRepository.countByGameIdAndTenant(gameId.value, tenant.value)
    }
}
