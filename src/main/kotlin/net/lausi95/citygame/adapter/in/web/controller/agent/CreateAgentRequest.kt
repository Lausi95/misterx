package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import net.lausi95.citygame.application.domain.model.agent.Agent

data class CreateAgentRequest(

    @Parameter(name = "type", description = "Type of the agent", required = true)
    @NotNull
    @JsonProperty("type")
    var type: Agent.Type?,

    @Parameter(name = "phoneNumber", description = "Phone number of the agent", required = true)
    @NotEmpty
    @JsonProperty("phoneNumber")
    var phoneNumber: String?,

    @Parameter(name = "firstName", description = "First name of the agent", required = true)
    @NotEmpty
    @JsonProperty("firstName")
    var firstName: String?,

    @Parameter(name = "lastName", description = "Last name of the agent", required = true)
    @NotEmpty
    @JsonProperty("lastName")
    var lastName: String?,

    @Parameter(name = "alias", description = "Alias for the agent", required = true)
    @NotEmpty
    @JsonProperty("alias")
    var alias: String?,

    @Parameter(name = "active", description = "Determines if the agent is active", required = true)
    @NotNull
    @JsonProperty("active")
    var active: Boolean?,
)
