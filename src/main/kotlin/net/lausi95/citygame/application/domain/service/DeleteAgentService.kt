package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.agentNotFound
import net.lausi95.citygame.application.port.`in`.agent.DeleteAgentUseCase
import net.lausi95.citygame.application.port.out.agent.DeleteAgentPort
import net.lausi95.citygame.application.port.out.agent.GetAgentPort
import net.lausi95.citygame.application.port.out.agentlocation.DeleteAgentLocationsPort
import net.lausi95.citygame.application.port.out.finding.DeleteAgentFindingsPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class DeleteAgentService(
    private val getAgentPort: GetAgentPort,
    private val deleteAgentFindingsPort: DeleteAgentFindingsPort,
    private val deleteAgentLocationsPort: DeleteAgentLocationsPort,
    private val deleteAgentPort: DeleteAgentPort,
) : DeleteAgentUseCase {

    @Transactional
    override fun deleteAgent(command: DeleteAgentUseCase.Command, tenant: Tenant) {
        log.info { "Deleting agent ${command.agentId.value}..." }

        val agent = getAgentPort.getAgentOrNull(command.agentId, tenant) ?: return

        if (agent.gameId != command.gameId) {
            agentNotFound(command.agentId)
        }

        deleteAgentFindingsPort.deleteAgentFindings(command.agentId, tenant)
        deleteAgentLocationsPort.deleteAgentLocations(command.agentId, tenant)
        deleteAgentPort.deleteAgent(command.agentId, tenant)

        log.info { "Agent ${command.agentId.value} deleted." }
    }
}
