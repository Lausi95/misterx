package net.lausi95.citygame.adapter.`in`.web.controller.location

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agentlocation.UpdateAgentLocationUseCase
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Location")
@RestController
@RequestMapping("/location")
internal class LocationController(
    private val updateAgentLocationUseCase: UpdateAgentLocationUseCase
) {

    @Operation(summary = "Updates the current location of an agent")
    @ApiResponse(responseCode = "202", description = "Location was updated successfully", content = [])
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
    @PostMapping(
        headers = ["X-GameId", "X-AgentId"]
    )
    fun updateLocation(
        @Parameter(description = "Identifier of the game the agent belongs to", required = true)
        @RequestHeader(name = "X-GameId") gameId: String,
        @Parameter(description = "Identifier of the agent reporting their location", required = true)
        @RequestHeader(name = "X-AgentId") agentId: String,
        tenant: Tenant,
        @Valid @RequestBody request: UpdateLocationRequest,
    ): ResponseEntity<Unit> {
        val geoLocation = GeoLocation(
            latitude = requireNotNull(request.latitude),
            longitude = requireNotNull(request.longitude),
        )
        updateAgentLocationUseCase.updateAgentLocation(
            gameId = GameId(gameId),
            agentId = AgentId(agentId),
            geoLocation = geoLocation,
            tenant = tenant,
        )
        return ResponseEntity.accepted().build()
    }
}
