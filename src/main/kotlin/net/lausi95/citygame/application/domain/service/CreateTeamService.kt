package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CreateTeamService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
) : CreateTeamUseCase {

    @Transactional
    override fun createTeam(command: CreateTeamUseCase.Command, tenant: Tenant): TeamId {
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
}
