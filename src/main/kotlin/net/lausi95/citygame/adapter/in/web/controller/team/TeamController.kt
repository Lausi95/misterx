package net.lausi95.citygame.adapter.`in`.web.controller.team

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.lausi95.citygame.application.domain.model.game.GameId
import net.lausi95.citygame.application.domain.model.team.TeamId
import net.lausi95.citygame.application.port.`in`.finding.GetTeamFoundAgentsUseCase
import net.lausi95.citygame.application.port.`in`.team.CreateTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamMembersUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamUseCase
import net.lausi95.citygame.application.port.`in`.team.GetTeamsUseCase
import net.lausi95.citygame.application.port.`in`.team.UpdateTeamUseCase
import net.lausi95.citygame.common.Tenant
import net.lausi95.citygame.common.qrCodeImage
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder
import java.awt.image.BufferedImage

@Tag(name = "Teams")
@RestController
@RequestMapping("/games/{gameId}/teams")
class TeamController(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val getTeamFoundAgentsUseCase: GetTeamFoundAgentsUseCase,
) {

    @Operation(summary = "Returns a paginated collection of teams for a game")
    @ApiResponse(
        responseCode = "200",
        description = "Collection of teams",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = TeamCollection::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping
    fun getTeams(
        @PageableDefault pageable: Pageable,
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
    ): TeamCollection {
        val teams = getTeamsUseCase.getTeams(GameId(gameId), pageable, tenant)
        return TeamCollection(teams) { team -> getTeamMembersUseCase.countTeamMembers(team.id, tenant) }
    }

    @Operation(summary = "Creates a new team in a game")
    @ApiResponse(
        responseCode = "201",
        description = "Team was created successfully",
        headers = [
            Header(name = "Location", description = "URI of the new team"),
            Header(name = "X-TeamId", description = "Identifier of the created team"),
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
    fun createTeam(
        @PathVariable gameId: String,
        @RequestAttribute tenant: Tenant,
        @RequestBody @Valid request: CreateTeamRequest,
    ): ResponseEntity<Unit> {
        val command = CreateTeamUseCase.Command(
            GameId(gameId),
            requireNotNull(request.name),
        )

        val teamId = createTeamUseCase.createTeam(command, tenant)

        val uri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/games/${gameId}/teams/${teamId.value}")
            .build().toUri()

        return ResponseEntity.created(uri).headers {
            it.set("X-TeamId", teamId.value)
        }.build()
    }

    @Operation(summary = "Returns a specific team")
    @ApiResponse(
        responseCode = "200",
        description = "Team with the given ID",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = TeamResource::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{teamId}")
    fun getTeam(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        @RequestAttribute tenant: Tenant,
    ): TeamResource {
        val team = getTeamUseCase.getTeam(TeamId(teamId), tenant)
        val memberCount = getTeamMembersUseCase.countTeamMembers(TeamId(teamId), tenant)
        val foundAgents = getTeamFoundAgentsUseCase.getFoundAgents(TeamId(teamId), tenant)
        return TeamResource(team, memberCount, foundAgents)
    }

    @Operation(summary = "Updates the fields of a team")
    @ApiResponse(responseCode = "200", description = "Team was updated successfully", content = [])
    @ApiResponse(
        responseCode = "400",
        description = "Input validation errors",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @PatchMapping("/{teamId}")
    fun updateTeam(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        @Valid @RequestBody request: UpdateTeamRequest,
        @RequestAttribute tenant: Tenant,
    ) {
        val command = UpdateTeamUseCase.Command(
            teamId = TeamId(teamId),
            name = request.name,
        )

        updateTeamUseCase.updateTeam(command, tenant)
    }

    @Operation(summary = "Generates a setup QR code for a team")
    @ApiResponse(
        responseCode = "200",
        description = "PNG image of the team's setup QR code",
        content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Team not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @GetMapping("/{teamId}/setup-qr", produces = [MediaType.IMAGE_PNG_VALUE])
    fun getSetupQr(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        @RequestAttribute tenant: Tenant,
    ): BufferedImage {
        val team = getTeamUseCase.getTeam(TeamId(teamId), tenant)

        val scheme = ServletUriComponentsBuilder.fromCurrentRequest().build().scheme

        val setupUrl = UriComponentsBuilder.newInstance()
            .scheme(scheme)
            .host(tenant.value)
            .path("/setup-team")
            .queryParam("gameId", team.gameId.value)
            .queryParam("teamId", team.id.value)
            .build()
            .toUriString()

        return qrCodeImage(setupUrl)
    }
}
