package net.lausi95.citygame.adapter.`in`.web.controller.location

import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agentlocation.UpdateAgentLocationUseCase
import net.lausi95.citygame.common.GeoLocation
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/location")
internal class LocationController(
    private val updateAgentLocationUseCase: UpdateAgentLocationUseCase
) {

    @PostMapping(
        headers = ["X-GameId", "X-AgentId"]
    )
    fun updateLocation(
        @RequestHeader(name = "X-GameId") gameId: String,
        @RequestHeader(name = "X-AgentId") agentId: String,
        @RequestAttribute tenant: Tenant,
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
