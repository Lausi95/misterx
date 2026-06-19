package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.team.TeamMember
import java.time.OffsetDateTime

@Schema(description = "Represents a team member")
data class TeamMemberResource(

    @Schema(description = "Unique identifier of the team member")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Timestamp when the member registered")
    @JsonProperty("registeredAt")
    val registeredAt: OffsetDateTime,

    @Schema(description = "Navigation links")
    @JsonProperty("links")
    val links: Map<String, String>,
) {
    constructor(member: TeamMember) : this(
        id = member.id.value,
        registeredAt = member.registeredAt,
        links = mapOf(
            "team" to "/games/${member.gameId.value}/teams/${member.teamId.value}"
        )
    )
}
