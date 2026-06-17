package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter

data class UpdateTeamRequest(

    @Parameter(name = "name", description = "Name of the team")
    @JsonProperty("name")
    var name: String?,
)
