package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.finding.FindingTeam
import org.springframework.format.annotation.DateTimeFormat
import java.time.OffsetDateTime

@Schema(description = "A team that has found this agent, exposing its identity, name and when it found the agent")
data class FindingTeamResource(

    @Schema(description = "Unique identifier of the team")
    @JsonProperty("id")
    val id: String,

    @Schema(description = "Display name of the team")
    @JsonProperty("name")
    val name: String,

    @Schema(description = "When this team found the agent")
    @JsonProperty("foundAt")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val foundAt: OffsetDateTime,
) {
    constructor(findingTeam: FindingTeam) : this(
        id = findingTeam.teamId.value,
        name = findingTeam.name,
        foundAt = findingTeam.foundAt,
    )
}
