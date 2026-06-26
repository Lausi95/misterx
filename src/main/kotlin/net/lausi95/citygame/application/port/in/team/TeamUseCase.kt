package net.lausi95.citygame.application.port.`in`.team

import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TeamUseCase {

    data class CreateTeamCommand(val gameId: GameId, val name: String)

    data class DeleteTeamCommand(val gameId: GameId, val teamId: TeamId)

    data class GetMyTeamQuery(
        val gameId: GameId,
        val teamId: TeamId,
        val memberId: TeamMemberId?,
    )

    data class UpdateTeamCommand(val teamId: TeamId, val name: String?)

    data class RegisterTeamMemberCommand(val gameId: GameId, val teamId: TeamId)

    fun createTeam(command: CreateTeamCommand, tenant: Tenant): TeamId

    fun getTeams(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Team>

    fun getTeam(teamId: TeamId, tenant: Tenant): Team

    fun getMyTeam(query: GetMyTeamQuery, tenant: Tenant): Team

    fun updateTeam(command: UpdateTeamCommand, tenant: Tenant)

    fun deleteTeam(command: DeleteTeamCommand, tenant: Tenant)

    fun getTeamMembers(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember>

    fun countTeamMembers(teamId: TeamId, tenant: Tenant): Long

    fun registerTeamMember(command: RegisterTeamMemberCommand, tenant: Tenant): TeamMemberId
}
