package net.lausi95.citygame.application.domain.model.agent

import net.lausi95.citygame.application.domain.model.agentlocation.AgentLocation
import net.lausi95.citygame.application.domain.model.game.GameId
import java.util.*

@JvmInline
value class AgentId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class Agent(
    val id: AgentId,
    private val _gameId: GameId,
    private var _type: Type,
    private var _phoneNumber: String,
    private var _firstName: String,
    private var _lastName: String,
    private var _alias: String,
    private var _active: Boolean,
) {
    val gameId: GameId
        get() = _gameId

    val type: Type
        get() = _type

    val phoneNumber: String
        get() = _phoneNumber

    val firstname: String
        get() = _firstName

    val lastName: String
        get() = _lastName

    val alias: String
        get() = _alias

    val active: Boolean
        get() = _active

    var location: AgentLocation? = null
        private set

    fun updateType(type: Type) {
        _type = type
    }

    fun updateFirstName(fistName: String) {
        _firstName = firstname
    }

    fun updateLastName(lastName: String) {
        _lastName = lastName
    }

    fun updateAlias(alias: String) {
        _alias = alias
    }

    fun updatePhoneNumber(phoneNumber: String) {
        _phoneNumber = phoneNumber
    }

    fun updateActive(active: Boolean) {
        _active = active
    }

    fun setLocation(location: AgentLocation) {
        this.location = location
    }

    enum class Type {
        MISTERX,
        UTILITY,
    }
}

