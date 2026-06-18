package net.lausi95.citygame.adapter.`in`.web.controller.find

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.domain.model.team.TeamMemberId
import net.lausi95.citygame.application.port.`in`.finding.FindAgentUseCase
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Tag(name = "Find")
@RestController
@RequestMapping("/find")
internal class FindController(
    private val findAgentUseCase: FindAgentUseCase,
) {

    @Operation(summary = "Records that a team has found an agent")
    @ApiResponse(
        responseCode = "201",
        description = "The find was recorded successfully",
        headers = [
            Header(name = "Location", description = "URI of the team the find belongs to"),
            Header(name = "X-FindId", description = "Identifier of the recorded find"),
        ],
        content = [],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Game, team, member or agent not found (or not part of the given game/team)",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "409",
        description = "The team has already found this agent",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "422",
        description = "The game is not active, or the agent is not findable",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @PostMapping(
        headers = ["X-GameId", "X-TeamId", "X-MemberId", "X-AgentId"]
    )
    fun find(
        @Parameter(description = "Identifier of the game", required = true)
        @RequestHeader(name = "X-GameId") gameId: String,
        @Parameter(description = "Identifier of the finding team", required = true)
        @RequestHeader(name = "X-TeamId") teamId: String,
        @Parameter(description = "Identifier of the team member performing the find", required = true)
        @RequestHeader(name = "X-MemberId") memberId: String,
        @Parameter(description = "Identifier of the agent being found", required = true)
        @RequestHeader(name = "X-AgentId") agentId: String,
        @RequestAttribute tenant: Tenant,
        @RequestBody(required = false) request: FindRequest?,
    ): ResponseEntity<Unit> {
        val reportedLocation = request
            ?.takeIf { it.latitude != null && it.longitude != null }
            ?.let { GeoLocation(it.latitude!!, it.longitude!!) }

        val command = FindAgentUseCase.Command(
            gameId = GameId(gameId),
            teamId = TeamId(teamId),
            memberId = TeamMemberId(memberId),
            agentId = AgentId(agentId),
            reportedLocation = reportedLocation,
        )

        val findingId = findAgentUseCase.findAgent(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/${gameId}/teams/${teamId}")
            .build().toUri()

        return ResponseEntity.created(uri).headers {
            it.set("X-FindId", findingId.value)
        }.build()
    }
}
