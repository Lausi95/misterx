package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.gameNotFound
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMember
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.domain.model.team.teamMemberNotFound
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.team.TeamUseCase
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val log = KotlinLogging.logger { }

@Service
class TeamService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val findingRepository: FindingRepository,
) : TeamUseCase {

    @Transactional
    override fun createTeam(command: TeamUseCase.CreateTeamCommand, tenant: Tenant): TeamId {
        log.info { "Creating new team..." }

        gameRepository.requireExists(command.gameId, tenant)

        val team = Team(
            TeamId(),
            command.gameId,
            command.name,
        )

        teamRepository.save(team, tenant)

        log.info { "Team created." }

        return team.id
    }

    override fun getTeams(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Team> {
        log.info { "Fetching teams..." }
        val teams = teamRepository.forGame(gameId, pageable, tenant)
        log.info { "Teams fetched." }
        return teams
    }

    override fun getTeam(teamId: TeamId, tenant: Tenant): Team {
        log.info { "Fetching team..." }
        val team = teamRepository.get(teamId, tenant)
        log.info { "Team fetched." }
        return team
    }

    override fun getMyTeam(query: TeamUseCase.GetMyTeamQuery, tenant: Tenant): Team {
        log.info { "Fetching my team..." }

        val team = teamRepository.getOrNull(query.teamId, tenant) ?: teamNotFound(query.teamId)
        if (team.gameId != query.gameId) teamNotFound(query.teamId)

        query.memberId?.let { memberId ->
            val member = teamMemberRepository.getOrNull(memberId, tenant) ?: teamMemberNotFound(memberId)
            if (member.teamId != query.teamId || member.gameId != query.gameId) teamMemberNotFound(memberId)
        }

        log.info { "My team fetched." }

        return team
    }

    @Transactional
    override fun updateTeam(command: TeamUseCase.UpdateTeamCommand, tenant: Tenant) {
        log.info { "Updating team..." }

        val team = teamRepository.get(command.teamId, tenant)

        command.name?.also { team.updateName(it) }

        teamRepository.save(team, tenant)

        log.info { "Team updated." }
    }

    @Transactional
    override fun deleteTeam(command: TeamUseCase.DeleteTeamCommand, tenant: Tenant) {
        log.info { "Deleting team ${command.teamId.value}..." }

        val team = teamRepository.getOrNull(command.teamId, tenant) ?: return

        if (team.gameId != command.gameId) {
            teamNotFound(command.teamId)
        }

        findingRepository.deleteByTeam(command.teamId, tenant)
        teamMemberRepository.deleteByTeam(command.teamId, tenant)
        teamRepository.delete(command.teamId, tenant)

        log.info { "Team ${command.teamId.value} deleted." }
    }

    override fun getTeamMembers(teamId: TeamId, gameId: GameId, pageable: Pageable, tenant: Tenant): Page<TeamMember> {
        log.info { "Fetching team members..." }
        return teamMemberRepository.forTeam(teamId, gameId, pageable, tenant)
    }

    override fun countTeamMembers(teamId: TeamId, tenant: Tenant): Long {
        return teamMemberRepository.countByTeam(teamId, tenant)
    }

    @Transactional
    override fun registerTeamMember(command: TeamUseCase.RegisterTeamMemberCommand, tenant: Tenant): TeamMemberId {
        log.info { "Registering team member..." }

        if (!gameRepository.exists(command.gameId, tenant)) gameNotFound(command.gameId)
        if (!teamRepository.exists(command.teamId, tenant)) teamNotFound(command.teamId)

        val member = TeamMember(
            TeamMemberId(),
            command.teamId,
            command.gameId,
            OffsetDateTime.now(ZoneOffset.UTC),
        )

        teamMemberRepository.save(member, tenant)

        log.info { "Team member registered." }

        return member.id
    }
}
