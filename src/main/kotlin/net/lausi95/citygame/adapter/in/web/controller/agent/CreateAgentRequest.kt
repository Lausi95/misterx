package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import net.lausi95.citygame.application.domain.model.agent.Agent

data class CreateAgentRequest(

    @field:Parameter(name = "type", description = "Type of the agent", required = true)
    @field:NotNull
    @field:JsonProperty("type")
    var type: Agent.Type?,

    @field:Parameter(name = "phoneNumber", description = "Phone number of the agent", required = true)
    @field:NotEmpty
    @field:JsonProperty("phoneNumber")
    var phoneNumber: String?,

    @field:Parameter(name = "firstName", description = "First name of the agent", required = true)
    @field:NotEmpty
    @field:JsonProperty("firstName")
    var firstName: String?,

    @field:Parameter(name = "lastName", description = "Last name of the agent", required = true)
    @field:NotEmpty
    @field:JsonProperty("lastName")
    var lastName: String?,

    @field:Parameter(name = "alias", description = "Alias for the agent", required = true)
    @field:NotEmpty
    @field:JsonProperty("alias")
    var alias: String?,

    @field:Parameter(name = "active", description = "Determines if the agent is active", required = true)
    @field:NotNull
    @field:JsonProperty("active")
    var active: Boolean?,
)
