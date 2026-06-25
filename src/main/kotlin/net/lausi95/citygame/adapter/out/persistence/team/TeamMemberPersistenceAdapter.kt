package net.lausi95.citygame.adapter.out.persistence.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class TeamMemberPersistenceAdapter(
    private val teamMemberEntityJpaRepository: TeamMemberEntityJpaRepository,
) : TeamMemberRepository {

    override fun save(teamMember: TeamMember, tenant: Tenant) {
        teamMemberEntityJpaRepository.save(TeamMemberEntity(teamMember, tenant))
    }

    override fun getOrNull(memberId: TeamMemberId, tenant: Tenant): TeamMember? {
        return teamMemberEntityJpaRepository.findByIdAndTenant(memberId.value, tenant.value)?.toTeamMember()
    }

    override fun forTeam(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember> {
        return teamMemberEntityJpaRepository
            .findByTeamIdAndGameIdAndTenant(teamId.value, gameId.value, tenant.value, pageable)
            .map { it.toTeamMember() }
    }

    override fun countByTeam(teamId: TeamId, tenant: Tenant): Long {
        return teamMemberEntityJpaRepository.countByTeamIdAndTenant(teamId.value, tenant.value)
    }

    override fun deleteByTeam(teamId: TeamId, tenant: Tenant) {
        teamMemberEntityJpaRepository.deleteByTeamIdAndTenant(teamId.value, tenant.value)
    }
}
