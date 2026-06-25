package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.port.`in`.agent.GetAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.GetAgentLocationPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class GetAgentService(
    private val agentRepository: AgentRepository,
    private val getAgentLocationPort: GetAgentLocationPort,
) : GetAgentUseCase {

    override fun getAgent(
        agentId: AgentId,
        tenant: Tenant
    ): Agent {
        log.info { "Fetching game..." }

        val agent = agentRepository.get(agentId, tenant)
        getAgentLocationPort.getAgentLocation(agentId)?.also {
            agent.setLocation(it)
        }

        log.info { "Game fectched." }

        return agent
    }
}