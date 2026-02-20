package net.lausi95.citygame.infrastructure.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty

data class UpdateGameRequest(

    @field:Parameter(name = "title", description = "Title of the game", required = true)
    @field:JsonProperty("title")
    @field:NotEmpty(message = "'title' cannot be null or empty")
    var title: String?
)
