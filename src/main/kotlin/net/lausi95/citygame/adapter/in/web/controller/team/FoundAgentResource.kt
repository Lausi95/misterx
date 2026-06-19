package net.lausi95.citygame.adapter.`in`.web.controller.team

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.finding.FoundAgent
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime

@Schema(description = "An agent a team has found, exposing its identity, alias and when it was found")
data class FoundAgentResource(

    @Schema(description = "Unique identifier of the found agent")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "In-game alias of the found agent")
    @JsonProperty("name")
    val name: String,

    @Schema(description = "When the team found this agent")
    @JsonProperty("foundAt")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val foundAt: ZonedDateTime,
) {
    constructor(foundAgent: FoundAgent) : this(
        id = foundAgent.agentId.value,
        name = foundAgent.name,
        foundAt = foundAgent.foundAt,
    )
}
