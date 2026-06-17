package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.out.game.CheckGameExistsPort
import net.lausi95.citygame.application.port.out.team.SaveTeamPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CreateTeamService(
    private val checkGameExistsPort: CheckGameExistsPort,
    private val saveTeamPort: SaveTeamPort,
) : CreateTeamUseCase {

    @Transactional
    override fun createTeam(command: CreateTeamUseCase.Command, tenant: Tenant): TeamId {
        log.info { "Creating new team..." }

        checkGameExistsPort.requireGameExists(command.gameId, tenant)

        val team = Team(
            TeamId(),
            command.gameId,
            command.name,
        )

        saveTeamPort.saveTeam(team, tenant)

        log.info { "Team created." }

        return team.id
    }
}
