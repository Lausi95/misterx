package net.lausi95.citygame.adapter.`in`.web.controller.agent

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.CreateAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.DeleteAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentUseCase
import net.lausi95.citygame.application.port.`in`.agent.GetAgentsUseCase
import net.lausi95.citygame.application.port.`in`.agent.UpdateAgentUseCase
import net.lausi95.citygame.application.port.`in`.finding.GetAgentFindingTeamsUseCase
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.common.Tenant
import net.lausi95.citygame.common.qrCodeImage
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.awt.image.BufferedImage

@Tag(name = "Agents")
@RestController
@RequestMapping("/games/{gameId}/agents")
class AgentController(
    private val getAgentsUseCase: GetAgentsUseCase,
    private val createAgentUseCase: CreateAgentUseCase,
    private val getAgentUseCase: GetAgentUseCase,
    private val updateAgentUseCase: UpdateAgentUseCase,
    private val deleteAgentUseCase: DeleteAgentUseCase,
    private val getAgentFindingTeamsUseCase: GetAgentFindingTeamsUseCase,
    private val frontendUriFactory: FrontendUriFactory,
) {

    @Operation(summary = "Returns a paginated collection of agents for a game")
    @ApiResponse(
        responseCode = "200",
        description = "Collection of agents",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AgentCollection::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getAgents(
        @PageableDefault pageable: Pageable,
        @PathVariable gameId: String,
        tenant: Tenant,
    ): AgentCollection {
        val agents = getAgentsUseCase.getAgents(GameId(gameId), pageable, tenant)
        return AgentCollection(agents)
    }

    @Operation(summary = "Creates a new agent in a game")
    @ApiResponse(
        responseCode = "201",
        description = "Agent was created successfully",
        headers = [
            Header(name = "Location", description = "URI of the new agent"),
            Header(name = "X-AgentId", description = "Identifier of the created agent"),
        ],
        content = [],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Input validation errors",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @PostMapping
    fun createAgent(
        @PathVariable gameId: String,
        tenant: Tenant,
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

        return ResponseEntity.created(uri).headers {
            it.set("X-AgentId", agentId.value)
        }.build()
    }

    @Operation(summary = "Returns a specific agent")
    @ApiResponse(
        responseCode = "200",
        description = "Agent with the given ID",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AgentResource::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Agent not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{agentId}")
    fun getAgent(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        tenant: Tenant,
    ): AgentResource {
        val agent = getAgentUseCase.getAgent(AgentId(agentId), tenant)
        val foundByTeams = getAgentFindingTeamsUseCase.getFindingTeams(AgentId(agentId), tenant)
        return AgentResource(agent, foundByTeams)
    }

    @Operation(summary = "Updates the fields of an agent")
    @ApiResponse(responseCode = "200", description = "Agent was updated successfully", content = [])
    @ApiResponse(
        responseCode = "400",
        description = "Input validation errors",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Agent not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @PatchMapping("/{agentId}")
    fun updateAgent(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        @Valid @RequestBody request: UpdateAgentRequest,
        tenant: Tenant,
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

    @Operation(summary = "Deletes an agent and all its locations and findings")
    @ApiResponse(responseCode = "204", description = "Agent was deleted successfully", content = [])
    @ApiResponse(
        responseCode = "404",
        description = "Agent exists but does not belong to the given game",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @DeleteMapping("/{agentId}")
    fun deleteAgent(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        tenant: Tenant,
    ): ResponseEntity<Unit> {
        deleteAgentUseCase.deleteAgent(
            DeleteAgentUseCase.Command(GameId(gameId), AgentId(agentId)),
            tenant,
        )
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Generates a setup QR code for an agent")
    @ApiResponse(
        responseCode = "200",
        description = "PNG image of the agent's setup QR code",
        content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Agent not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{agentId}/setup-qr", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getSetupQr(
        @PathVariable gameId: String,
        @PathVariable agentId: String,
        tenant: Tenant,
    ): BufferedImage {
        val agent = getAgentUseCase.getAgent(AgentId(agentId), tenant)

        val setupUrl = frontendUriFactory.buildUrl(
            tenant,
            "/setup-agent",
            mapOf(
                "type" to agent.type.name,
                "agentId" to agent.id.value,
                "gameId" to agent.gameId.value,
            ),
        )

        return qrCodeImage(setupUrl)
    }

}
