package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.port.`in`.agent.GetMyAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class GetMyAgentService(
    private val agentRepository: AgentRepository,
    private val agentLocationRepository: AgentLocationRepository,
) : GetMyAgentUseCase {

    override fun getMyAgent(query: GetMyAgentUseCase.Query, tenant: Tenant): Agent {
        log.info { "Fetching my agent..." }

        val agent = agentRepository.getOrNull(query.agentId, tenant) ?: agentNotFound(query.agentId)
        if (agent.gameId != query.gameId) agentNotFound(query.agentId)

        agentLocationRepository.latest(query.agentId)?.also {
            agent.setLocation(it)
        }

        log.info { "My agent fetched." }

        return agent
    }
}
