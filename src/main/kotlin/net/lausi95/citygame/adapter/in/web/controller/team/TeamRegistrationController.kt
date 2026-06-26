package net.lausi95.citygame.adapter.`in`.web.controller.team

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.team.TeamUseCase
import net.lausi95.citygame.common.Tenant
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Tag(name = "Team Registration")
@RestController
@RequestMapping("/team-register")
class TeamRegistrationController(
    private val teamUseCase: TeamUseCase,
) {

    @Operation(summary = "Register as a member of a team")
    @ApiResponse(
        responseCode = "201",
        description = "Member was registered successfully",
        headers = [
            Header(name = "Location", description = "URI of the new team member"),
            Header(name = "X-TeamMemberId", description = "Identifier of the registered member"),
        ],
        content = [],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Game or team not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @PostMapping
    fun registerTeamMember(
        @RequestHeader("X-GameId") gameId: String,
        @RequestHeader("X-TeamId") teamId: String,
        tenant: Tenant,
    ): ResponseEntity<Unit> {
        val command = TeamUseCase.RegisterTeamMemberCommand(
            GameId(gameId),
            TeamId(teamId),
        )

        val memberId = teamUseCase.registerTeamMember(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/${gameId}/teams/${teamId}/members/${memberId.value}")
            .build().toUri()

        return ResponseEntity.created(uri).headers {
            it.set("X-TeamMemberId", memberId.value)
        }.build()
    }
}
