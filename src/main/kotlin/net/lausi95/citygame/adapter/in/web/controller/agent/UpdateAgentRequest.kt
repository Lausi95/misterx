package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import net.lausi95.citygame.application.domain.model.agent.Agent

@Schema(description = "Request body to partially update an agent")
data class UpdateAgentRequest(

    @Schema(description = "Type of the agent")
    @JsonProperty("type")
    var type: Agent.Type?,

    @Schema(description = "Phone number of the agent")
    @JsonProperty("phoneNumber")
    var phoneNumber: String?,

    @Schema(description = "First name of the agent")
    @JsonProperty("firstName")
    var firstName: String?,

    @Schema(description = "Last name of the agent")
    @JsonProperty("lastName")
    var lastName: String?,

    @Schema(description = "In-game alias for the agent")
    @JsonProperty("alias")
    var alias: String?,

    @Schema(description = "Whether the agent is active")
    @JsonProperty("active")
    var active: Boolean?,
)
