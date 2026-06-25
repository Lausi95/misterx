package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.port.`in`.agent.UpdateAgentUseCase
import net.lausi95.citygame.application.port.out.agent.AgentRepository
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger { }

@Service
class UpdateAgentService(
    private val agentRepository: AgentRepository,
) : UpdateAgentUseCase {

    override fun updateAgent(
        command: UpdateAgentUseCase.Command,
        tenant: Tenant,
    ) {
        log.info { "Updating Agent..." }

        val agent = agentRepository.get(command.agentId, tenant)

        command.type?.also {
            agent.updateType(it)
        }

        command.firstName?.also {
            agent.updateFirstName(it)
        }

        command.lastName?.also {
            agent.updateLastName(it)
        }

        command.phoneNumber?.also {
            agent.updatePhoneNumber(it)
        }

        command.alias?.also {
            agent.updateAlias(it)
        }

        command.active?.also {
            agent.updateActive(it)
        }

        agentRepository.save(agent, tenant)

        log.info { "Agent updated." }
    }
}