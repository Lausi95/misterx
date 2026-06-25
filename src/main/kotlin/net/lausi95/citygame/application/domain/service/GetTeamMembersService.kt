package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetTeamMembersService(
    private val teamMemberRepository: TeamMemberRepository,
) : GetTeamMembersUseCase {

    override fun getTeamMembers(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember> {
        log.info { "Fetching team members..." }
        return teamMemberRepository.forTeam(teamId, gameId, pageable, tenant)
    }

    override fun countTeamMembers(teamId: TeamId, tenant: Tenant): Long {
        return teamMemberRepository.countByTeam(teamId, tenant)
    }
}
