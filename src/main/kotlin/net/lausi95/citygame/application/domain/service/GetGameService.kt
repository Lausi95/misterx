package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.game.GameSummary
import net.lausi95.citygame.application.port.`in`.game.GetGameUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetGameService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
    private val agentRepository: AgentRepository,
) : GetGameUseCase {

    override fun getGame(
        gameId: GameId,
        tenant: Tenant
    ): GameSummary {
        log.info { "Fetching Game..." }
        val game = gameRepository.get(gameId, tenant)
        val teamsCount = teamRepository.countByGame(gameId, tenant)
        val agentsCount = agentRepository.countByGame(gameId, tenant)
        log.info { "Game fetched." }
        return GameSummary(game, teamsCount, agentsCount)
    }
}
