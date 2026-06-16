package net.lausi95.citygame.adapter.`in`.web.controller.agent

import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.CreateAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.`in`.agent.UpdateAgentUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/games/{gameId}/agents")
class AgentController(
    private val getAgentsUseCase: GetAgentsUseCase,
    private val createAgentUseCase: CreateAgentUseCase,
    private val getAgentUseCase: GetAgentUseCase,
    private val updateAgentUseCase: UpdateAgentUseCase,
) {

    @GetMapping
    fun getAgents(
        @PageableDefault pageable: Pageable,
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
    ): AgentCollection {
        val agents = getAgentsUseCase.getAgents(GameId(gameId), pageable, tenant)
        return AgentCollection(agents)
    }

    @PostMapping
    fun createAgent(
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
        @RequestBody @Valid request: CreateAgentRequest,
    ): ResponseEntity<Unit> {
        val command = CreateAgentUseCase.Command(
            GameId(gameId),
            requireNotNull(request.type),
            requireNotNull(request.phoneNumber),
            requireNotNull(request.firstName),
            requireNotNull(request.lastName),
            requireNotNull(request.alias),
            requireNotNull(request.active),
        )

        val agentId = createAgentUseCase.createAgent(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/${gameId}/agents/${agentId.value}")
            .build().toUri()

        return ResponseEntity.created(uri).build()
    }

    @GetMapping("/{agentId}")
    fun getAgent(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        @RequestAttribute tenant: Tenant,
    ): AgentResource {
        val agent = getAgentUseCase.getAgent(AgentId(agentId), tenant)
        return AgentResource(agent)
    }

    @PatchMapping("/{agentId}")
    fun updateAgent(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        @Valid @RequestBody request: UpdateAgentRequest,
        @RequestAttribute tenant: Tenant,
    ) {
        val command = UpdateAgentUseCase.Command(
            agentId = AgentId(agentId),
            gameId = GameId(gameId),
            type = request.type,
            phoneNumber = request.phoneNumber,
            firstName = request.firstName,
            lastName = request.lastName,
            alias = request.alias,
            active = request.active
        )

        updateAgentUseCase.updateAgent(command, tenant)
    }
}
