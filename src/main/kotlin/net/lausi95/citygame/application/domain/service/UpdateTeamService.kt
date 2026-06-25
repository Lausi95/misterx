package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class UpdateTeamService(
    private val teamRepository: TeamRepository,
) : UpdateTeamUseCase {

    @Transactional
    override fun updateTeam(command: UpdateTeamUseCase.Command, tenant: Tenant) {
        log.info { "Updating team..." }

        val team = teamRepository.get(command.teamId, tenant)

        command.name?.also {
            team.updateName(it)
        }

        teamRepository.save(team, tenant)

        log.info { "Team updated." }
    }
}
