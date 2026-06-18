package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonUnwrapped
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.team.TeamMember
import org.springframework.data.domain.Page

@Schema(description = "Paginated collection of team members")
data class TeamMemberCollection(

    @Schema(description = "Page of team member resources")
    @JsonUnwrapped
    val members: Page<TeamMemberResource>,

    @Schema(description = "Navigation links")
    val links: Map<String, String>,
) {
    constructor(members: Page<TeamMember>) : this(
        members = members.map { TeamMemberResource(it) },
        links = mapOf(),
    )
}
