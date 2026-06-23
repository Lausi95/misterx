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
import net.lausi95.citygame.application.port.`in`.team.*
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

@Tag(name = "Teams")
@RestController
@RequestMapping("/games/{gameId}/teams")
class TeamController(
    private val getTeamsUseCase: GetTeamsUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase,
    private val getTeamMembersUseCase: GetTeamMembersUseCase,
    private val getTeamFoundAgentsUseCase: GetTeamFoundAgentsUseCase,
    private val frontendUriFactory: FrontendUriFactory,
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
        @PageableDefault(sort = ["name"]) pageable: Pageable,
        @PathVariable gameId: String,
        tenant: Tenant,
    ): TeamCollection {
        val teams = getTeamsUseCase.getTeams(GameId(gameId), pageable, tenant)
        val foundAgentsByTeam = getTeamFoundAgentsUseCase.getFoundAgentsByTeams(teams.content.map { it.id }, tenant)
        return TeamCollection(teams, { team -> getTeamMembersUseCase.countTeamMembers(team.id, tenant) }, foundAgentsByTeam)
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
        tenant: Tenant,
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
        tenant: Tenant,
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
        tenant: Tenant,
    ) {
        val command = UpdateTeamUseCase.Command(
            teamId = TeamId(teamId),
            name = request.name,
        )

        updateTeamUseCase.updateTeam(command, tenant)
    }

    @Operation(summary = "Deletes a team and all its members and findings")
    @ApiResponse(responseCode = "204", description = "Team was deleted successfully", content = [])
    @ApiResponse(
        responseCode = "404",
        description = "Team not found or does not belong to this game",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ProblemDetail::class))],
    )
    @DeleteMapping("/{teamId}")
    fun deleteTeam(
        @PathVariable gameId: String,
        @PathVariable teamId: String,
        tenant: Tenant,
    ): ResponseEntity<Unit> {
        deleteTeamUseCase.deleteTeam(
            DeleteTeamUseCase.Command(GameId(gameId), TeamId(teamId)),
            tenant,
        )
        return ResponseEntity.noContent().build()
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
        tenant: Tenant,
    ): BufferedImage {
        val team = getTeamUseCase.getTeam(TeamId(teamId), tenant)

        val setupUrl = frontendUriFactory.buildUrl(
            tenant,
            "/setup-team",
            mapOf(
                "gameId" to team.gameId.value,
                "teamId" to team.id.value,
            ),
        )

        return qrCodeImage(setupUrl)
    }
}
