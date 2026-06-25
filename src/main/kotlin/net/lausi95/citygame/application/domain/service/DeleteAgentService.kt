package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.port.`in`.agent.DeleteAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.application.port.out.agentlocation.AgentLocationRepository
import net.lausi95.citygame.application.port.out.finding.FindingRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class DeleteAgentService(
    private val agentRepository: AgentRepository,
    private val findingRepository: FindingRepository,
    private val agentLocationRepository: AgentLocationRepository,
) : DeleteAgentUseCase {

    @Transactional
    override fun deleteAgent(command: DeleteAgentUseCase.Command, tenant: Tenant) {
        log.info { "Deleting agent ${command.agentId.value}..." }

        val agent = agentRepository.getOrNull(command.agentId, tenant) ?: return

        if (agent.gameId != command.gameId) {
            agentNotFound(command.agentId)
        }

        findingRepository.deleteByAgent(command.agentId, tenant)
        agentLocationRepository.deleteByAgent(command.agentId, tenant)
        agentRepository.delete(command.agentId, tenant)

        log.info { "Agent ${command.agentId.value} deleted." }
    }
}
