package net.lausi95.citygame.application.domain.service

import io.github.oshai.kotlinlogging.KotlinLogging
import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.port.`in`.agent.CreateAgentUseCase
import net.lausi95.citygame.application.port.out.agent.SaveAgentPort
import net.lausi95.citygame.application.port.out.game.CheckGameExistsPort
import net.lausi95.citygame.common.Tenant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger { }

@Service
class CreateAgentService(
    val checkGameExistsPort: CheckGameExistsPort,
    val saveAgentPort: SaveAgentPort,
) : CreateAgentUseCase {

    @Transactional
    override fun createAgent(
        command: CreateAgentUseCase.Command,
        tenant: Tenant
    ): AgentId {
        log.info { "Creating new agent..." }

        checkGameExistsPort.requireGameExists(command.gameId, tenant)

        val agent = Agent(
            AgentId(),
            command.gameId,
            command.type,
            command.phoneNumber,
            command.firstName,
            command.lastName,
            command.alias,
            command.active
        )

        saveAgentPort.saveAgent(agent, tenant)

        log.info { "Agent created." }

        return agent.id
    }
}