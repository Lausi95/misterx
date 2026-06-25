package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.leaderboard.FoundMisterX
import net.lausi95.citygame.application.domain.model.leaderboard.Leaderboard
import net.lausi95.citygame.application.domain.model.leaderboard.LeaderboardEntry
import net.lausi95.citygame.application.port.`in`.leaderboard.GetLeaderboardUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.application.port.out.game.GameRepository
import net.lausi95.citygame.application.port.out.team.TeamRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class GetLeaderboardService(
    private val gameRepository: GameRepository,
    private val teamRepository: TeamRepository,
    private val agentRepository: AgentRepository,
    private val findingRepository: FindingRepository,
) : GetLeaderboardUseCase {

    @Transactional(readOnly = true)
    override fun getLeaderboard(gameId: GameId, tenant: Tenant): Leaderboard {
        log.info { "Building leaderboard for game $gameId..." }

        val game = gameRepository.get(gameId, tenant)

        // Only currently-active MISTERX agents score (ADR 0005). Fetch them once and index by id
        // so each team's findings resolve without an extra query per finding.
        val scoringAgents = agentRepository.forGame(gameId, tenant)
            .filter { it.type == Agent.Type.MISTERX && it.active }
            .associateBy { it.id }

        val teams = teamRepository.forGame(gameId, Pageable.unpaged(), tenant).content

        val entries = teams.map { team ->
            val foundAgents = findingRepository.byTeam(team.id, tenant)
                .mapNotNull { finding ->
                    scoringAgents[finding.agentId]?.let { agent ->
                        FoundMisterX(agent.alias, finding.foundAt)
                    }
                }
                .sortedBy { it.foundAt }
            LeaderboardEntry(team, foundAgents)
        }

        log.info { "Leaderboard built: ${entries.size} teams ranked." }
        return Leaderboard.of(game, entries)
    }
}
