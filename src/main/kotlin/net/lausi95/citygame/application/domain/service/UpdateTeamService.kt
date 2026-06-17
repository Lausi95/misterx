package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
import net.lausi95.citygame.application.port.out.team.GetTeamPort
import net.lausi95.citygame.application.port.out.team.SaveTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class UpdateTeamService(
    private val getTeamPort: GetTeamPort,
    private val saveTeamPort: SaveTeamPort,
) : UpdateTeamUseCase {

    @Transactional
    override fun updateTeam(command: UpdateTeamUseCase.Command, tenant: Tenant) {
        log.info { "Updating team..." }

        val team = getTeamPort.getTeam(command.teamId, tenant)

        command.name?.also {
            team.updateName(it)
        }

        saveTeamPort.saveTeam(team, tenant)

        log.info { "Team updated." }
    }
}
