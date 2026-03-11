package net.lausi95.citygame.infrastructure.web.controller.game

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

data class PatchGameRequest(

    @field:Parameter(name = "title", description = "Title of the game", required = false)
    @field:JsonProperty("title")
    var title: String?,

    @field:Parameter(name = "startTime", description = "Start date of the game", required = false)
    @field:JsonProperty("startTime")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: OffsetDateTime?,

    @field:Parameter(name = "endTime", description = "End date of the game", required = false)
    @field:JsonProperty("endTime")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: OffsetDateTime?,
)
