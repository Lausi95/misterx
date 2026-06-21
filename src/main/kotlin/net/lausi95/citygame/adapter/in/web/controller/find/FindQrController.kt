package net.lausi95.citygame.adapter.`in`.web.controller.find

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.adapter.`in`.web.FrontendUriFactory
import net.lausi95.citygame.application.domain.model.agent.AgentId
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.port.`in`.agent.GetMyAgentUseCase
import net.lausi95.citygame.common.Tenant
import net.lausi95.citygame.common.qrCodeImage
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.awt.image.BufferedImage

@Tag(name = "Find QR")
@RestController
@RequestMapping("/find-qr")
internal class FindQrController(
    private val getMyAgentUseCase: GetMyAgentUseCase,
    private val frontendUriFactory: FrontendUriFactory,
) {

    @Operation(summary = "Generates the QR code an agent presents so a team can record a find by scanning it")
    @ApiResponse(
        responseCode = "200",
        description = "PNG image of the agent's finding QR code",
        content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
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
    @GetMapping(produces = [MediaType.IMAGE_PNG_VALUE])
    fun getFindQr(
        @Parameter(description = "Identifier of the game", required = true)
        @RequestHeader("X-GameId") gameId: String,
        @Parameter(description = "Identifier of the agent the QR code is for", required = true)
        @RequestHeader("X-AgentId") agentId: String,
        @RequestAttribute tenant: Tenant,
    ): BufferedImage {
        val agent = getMyAgentUseCase.getMyAgent(
            GetMyAgentUseCase.Query(GameId(gameId), AgentId(agentId)),
            tenant,
        )

        val findUrl = frontendUriFactory.buildUrl(
            "/find",
            mapOf(
                "agentId" to agent.id.value,
                "alias" to agent.alias,
            ),
        )

        return qrCodeImage(findUrl)
    }
}
