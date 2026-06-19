package net.lausi95.citygame.adapter.out.persistence.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.port.out.team.DeleteTeamMembersPort
import net.lausi95.citygame.application.port.out.team.GetTeamMemberPort
import net.lausi95.citygame.application.port.out.team.GetTeamMembersPort
import net.lausi95.citygame.application.port.out.team.SaveTeamMemberPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
internal class TeamMemberPersistenceAdapter(
    private val teamMemberEntityRepository: TeamMemberEntityRepository,
) : SaveTeamMemberPort, GetTeamMembersPort, GetTeamMemberPort, DeleteTeamMembersPort {

    override fun saveTeamMember(teamMember: TeamMember, tenant: Tenant) {
        teamMemberEntityRepository.save(TeamMemberEntity(teamMember, tenant))
    }

    override fun getTeamMemberOrNull(memberId: TeamMemberId, tenant: Tenant): TeamMember? {
        return teamMemberEntityRepository.findByIdAndTenant(memberId.value, tenant.value)?.toTeamMember()
    }

    override fun getTeamMembers(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember> {
        return teamMemberEntityRepository
            .findByTeamIdAndGameIdAndTenant(teamId.value, gameId.value, tenant.value, pageable)
            .map { it.toTeamMember() }
    }

    override fun countTeamMembers(teamId: TeamId, tenant: Tenant): Long {
        return teamMemberEntityRepository.countByTeamIdAndTenant(teamId.value, tenant.value)
    }

    override fun deleteTeamMembers(teamId: TeamId, tenant: Tenant) {
        teamMemberEntityRepository.deleteByTeamIdAndTenant(teamId.value, tenant.value)
    }
}
