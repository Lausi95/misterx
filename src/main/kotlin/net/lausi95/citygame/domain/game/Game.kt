package net.lausi95.citygame.domain.game

import java.time.OffsetDateTime

class Game(
    private val _id: GameId,
    private var _title: GameTitle,
    private var _startTime: OffsetDateTime,
    private var _endTime: OffsetDateTime,
) {
    val id: GameId
        get() = _id

    val title: GameTitle
        get() = _title

    val startTime: OffsetDateTime
        get() = _startTime

    val endTime: OffsetDateTime
        get() = _endTime

    fun updateTitle(newTitle: GameTitle) {
        _title = newTitle
    }

    fun updateStartTime(newStartTime: OffsetDateTime) {
        check(newStartTime.isBefore(_endTime)) {
            "Start date must be before end date."
        }
        _startTime = newStartTime
    }

    fun updateEndTime(newEndTime: OffsetDateTime) {
        check(_startTime.isBefore(newEndTime)) {
            "Start date must be before end date."
        }
        _endTime = newEndTime
    }
}
