package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import net.lausi95.citygame.application.domain.model.agent.Agent

@Schema(description = "Request body to create a new agent")
data class CreateAgentRequest(

    @Schema(description = "Type of the agent", required = true)
    @NotNull
    @JsonProperty("type")
    var type: Agent.Type?,

    @Schema(description = "Phone number of the agent", required = true)
    @NotEmpty
    @JsonProperty("phoneNumber")
    var phoneNumber: String?,

    @Schema(description = "First name of the agent", required = true)
    @NotEmpty
    @JsonProperty("firstName")
    var firstName: String?,

    @Schema(description = "Last name of the agent", required = true)
    @NotEmpty
    @JsonProperty("lastName")
    var lastName: String?,

    @Schema(description = "In-game alias for the agent", required = true)
    @NotEmpty
    @JsonProperty("alias")
    var alias: String?,

    @Schema(description = "Whether the agent is active", required = true)
    @NotNull
    @JsonProperty("active")
    var active: Boolean?,
)
