package net.lausi95.citygame.infrastructure.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

data class CreateGameRequest(

    @field:Parameter(name = "title", description = "Title of the game", required = true)
    @field:JsonProperty("title")
    @field:NotEmpty(message = "'title' cannot be null or empty")
    var title: String?,

    @field:Parameter(name = "startTime", description = "Start date of the game", required = true)
    @field:JsonProperty("startTime")
    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @field:Parameter(name = "endTime", description = "End date of the game", required = true)
    @field:JsonProperty("endTime")
    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,
)
