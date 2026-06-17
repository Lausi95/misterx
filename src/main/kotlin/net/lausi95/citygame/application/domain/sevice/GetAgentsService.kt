package net.lausi95.citygame.application.domain.sevice

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.out.agent.GetAgentsPort
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetAgentsService(
    private val getAgentsPort: GetAgentsPort,
    private val getAgentLocationPort: GetAgentLocationPort
) : GetAgentsUseCase {

    override fun getAgents(
        gameId: GameId,
        pageable: Pageable,
        tenant: Tenant
    ): Page<Agent> {
        log.info { "Fetching agents..." }
        val agents = getAgentsPort.getAgents(pageable, gameId, tenant)
        agents.forEach { agent ->
            getAgentLocationPort.getAgentLocation(agent.id)?.also {
                agent.setLocation(it)
            }
        }
        log.info { "Agents fetches." }
        return agents
    }
}