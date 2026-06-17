package net.lausi95.citygame.adapter.`in`.web.controller.location

import com.fasterxml.jackson.annotation.JsonProperty

internal data class UpdateLocationRequest(

    @JsonProperty("latitude")
    val latitude: Double?,

    @JsonProperty("longitude")
    val longitude: Double?,
)