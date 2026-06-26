package net.lausi95.citygame.adapter.`in`.web.controller.agent

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.AgentUseCase
import net.lausi95.citygame.application.port.`in`.finding.FindingUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "My Agent")
@RestController
@RequestMapping("/my-agent")
class MyAgentController(
    private val agentUseCase: AgentUseCase,
    private val findingUseCase: FindingUseCase,
) {

    @Operation(summary = "Returns the agent the caller is, identified by request headers")
    @ApiResponse(
        responseCode = "200",
        description = "The caller's agent",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = AgentResource::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Agent not found, or the supplied agent does not belong to this game",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getMyAgent(
        @RequestHeader("X-GameId") gameId: String,
        @RequestHeader("X-AgentId") agentId: String,
        tenant: Tenant,
    ): AgentResource {
        val query = AgentUseCase.GetMyAgentQuery(GameId(gameId), AgentId(agentId))

        val agent = agentUseCase.getMyAgent(query, tenant)
        val foundByTeams = findingUseCase.getFindingTeams(agent.id, tenant)

        return AgentResource(agent, foundByTeams)
    }
}
