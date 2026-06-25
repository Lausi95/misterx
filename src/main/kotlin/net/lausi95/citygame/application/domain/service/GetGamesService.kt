package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.GameSummary
import net.lausi95.citygame.application.port.`in`.game.GetGamesUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.game.GetGamesPort
import net.lausi95.citygame.application.port.out.team.CountTeamsByGamePort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetGamesService(
    private val getGamesPort: GetGamesPort,
    private val countTeamsByGamePort: CountTeamsByGamePort,
    private val agentRepository: AgentRepository,
) : GetGamesUseCase {

    override fun getGames(
        pageable: Pageable,
        tenant: Tenant
    ): Page<GameSummary> {
        log.info { "Fetching games..." }
        val games = getGamesPort.getGames(pageable, tenant)
        log.info { "Games fetched." }
        return games.map { game ->
            GameSummary(
                game = game,
                teamsCount = countTeamsByGamePort.countTeamsByGame(game.id, tenant),
                agentsCount = agentRepository.countByGame(game.id, tenant),
            )
        }
    }
}
