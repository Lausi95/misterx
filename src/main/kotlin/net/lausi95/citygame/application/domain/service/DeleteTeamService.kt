package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.teamNotFound
import net.lausi95.citygame.application.port.`in`.team.DeleteTeamUseCase
import net.lausi95.citygame.application.port.out.finding.DeleteTeamFindingsPort
import net.lausi95.citygame.application.port.out.team.DeleteTeamMembersPort
import net.lausi95.citygame.application.port.out.team.DeleteTeamPort
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class DeleteTeamService(
    private val getTeamPort: GetTeamPort,
    private val deleteTeamFindingsPort: DeleteTeamFindingsPort,
    private val deleteTeamMembersPort: DeleteTeamMembersPort,
    private val deleteTeamPort: DeleteTeamPort,
) : DeleteTeamUseCase {

    @Transactional
    override fun deleteTeam(command: DeleteTeamUseCase.Command, tenant: Tenant) {
        log.info { "Deleting team ${command.teamId.value}..." }

        val team = getTeamPort.getTeamOrNull(command.teamId, tenant) ?: return

        if (team.gameId != command.gameId) {
            teamNotFound(command.teamId)
        }

        deleteTeamFindingsPort.deleteTeamFindings(command.teamId, tenant)
        deleteTeamMembersPort.deleteTeamMembers(command.teamId, tenant)
        deleteTeamPort.deleteTeam(command.teamId, tenant)

        log.info { "Team ${command.teamId.value} deleted." }
    }
}
