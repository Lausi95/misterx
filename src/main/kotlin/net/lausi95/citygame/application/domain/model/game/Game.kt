package net.lausi95.citygame.application.domain.model.game

import net.lausi95.citygame.common.GeoLocation
import java.time.OffsetDateTime
import java.util.*

@JvmInline
value class GameId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

@JvmInline
value class GameTitle(val value: String) {

    companion object {}

    override fun toString(): String = value
}

class Game(
    private val _id: GameId,
    private var _title: GameTitle,
    private var _startTime: OffsetDateTime,
    private var _endTime: OffsetDateTime,
    private val _map: Map,
) {
    val id: GameId
        get() = _id

    val title: GameTitle
        get() = _title

    val startTime: OffsetDateTime
        get() = _startTime

    val endTime: OffsetDateTime
        get() = _endTime

    val map: Map
        get() = _map

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

    fun updateCornerA(newPoint1: GeoLocation) {
        map.updateCornerA(newPoint1)
    }

    fun updateCornerB(newPoint2: GeoLocation) {
        map.updateConerB(newPoint2)
    }

    fun updateGrid(grid: Grid) {
        map.updateGrid(grid)
    }
}

data class Grid(
    val rows: Int,
    val columns: Int,
)

@JvmInline
value class MapId(val value: String = UUID.randomUUID().toString()) {
    override fun toString(): String = value
}

class Map(
    val id: MapId,
    private var _cornerA: GeoLocation,
    private var _cornerB: GeoLocation,
    private var _grid: Grid,
) {

    val cornerA: GeoLocation
        get() = _cornerA

    val cornerB: GeoLocation
        get() = _cornerB

    val grid: Grid
        get() = _grid

    fun updateCornerA(newCornerA: GeoLocation) {
        _cornerA = newCornerA
    }

    fun updateConerB(newCornerB: GeoLocation) {
        _cornerB = newCornerB
    }

    fun updateGrid(newGrid: Grid) {
        _grid = newGrid
    }
}
