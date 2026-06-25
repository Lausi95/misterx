package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.Team
import net.lausi95.citygame.application.port.`in`.team.GetTeamsUseCase
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetTeamsService(
    private val teamRepository: TeamRepository,
) : GetTeamsUseCase {

    override fun getTeams(gameId: GameId, pageable: Pageable, tenant: Tenant): Page<Team> {
        log.info { "Fetching teams..." }

        val teams = teamRepository.forGame(gameId, pageable, tenant)

        log.info { "Teams fetched." }

        return teams
    }
}
