package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.team.DeleteTeamUseCase
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.team.TeamMemberRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class DeleteTeamService(
    private val teamRepository: TeamRepository,
    private val findingRepository: FindingRepository,
    private val teamMemberRepository: TeamMemberRepository,
) : DeleteTeamUseCase {

    @Transactional
    override fun deleteTeam(command: DeleteTeamUseCase.Command, tenant: Tenant) {
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
}
