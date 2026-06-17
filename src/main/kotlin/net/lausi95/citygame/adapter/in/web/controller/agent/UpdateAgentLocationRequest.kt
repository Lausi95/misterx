package net.lausi95.citygame.adapter.`in`.web.controller.agent

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateAgentLocationRequest(

    @field:JsonProperty("latitude")
    val latitude: Double?,

    @field:JsonProperty("longitude")
    val longitude: Double?,
)
