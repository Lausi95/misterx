package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty

data class CreateTeamRequest(

    @Parameter(name = "name", description = "Name of the team", required = true)
    @NotEmpty
    @JsonProperty("name")
    var name: String?,
)
