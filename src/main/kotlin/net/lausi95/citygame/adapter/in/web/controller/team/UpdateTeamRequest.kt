package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request body to partially update a team")
data class UpdateTeamRequest(

    @Schema(description = "Display name of the team")
    @JsonProperty("name")
    var name: String?,
)
