package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(description = "Request body to create a new team")
data class CreateTeamRequest(

    @Schema(description = "Display name of the team", required = true)
    @NotEmpty
    @JsonProperty("name")
    var name: String?,
)
