package net.lausi95.citygame.application.domain.model.game

import net.lausi95.citygame.common.GeoLocation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapCellTest {

    // A 10x10 grid over the rectangle SW(0,0) .. NE(10,10): each cell spans 1.0 degree.
    private fun map(
        cornerA: GeoLocation = GeoLocation(latitude = 0.0, longitude = 0.0),
        cornerB: GeoLocation = GeoLocation(latitude = 10.0, longitude = 10.0),
        grid: Grid = Grid(rows = 10, columns = 10),
    ) = Map(MapId(), cornerA, cornerB, grid)

    @Test
    fun `the south-west corner maps to cell (0, 0)`() {
        val cell = map().cellOf(GeoLocation(latitude = 0.0, longitude = 0.0))

        assertThat(cell).isEqualTo(Cell(row = 0, column = 0))
    }

    @Test
    fun `the north-east corner maps to the last cell (rows-1, columns-1)`() {
        val cell = map().cellOf(GeoLocation(latitude = 10.0, longitude = 10.0))

        assertThat(cell).isEqualTo(Cell(row = 9, column = 9))
    }

    @Test
    fun `a more northern location yields a higher row`() {
        val south = map().cellOf(GeoLocation(latitude = 1.5, longitude = 5.0))!!
        val north = map().cellOf(GeoLocation(latitude = 8.5, longitude = 5.0))!!

        assertThat(north.row).isGreaterThan(south.row)
        assertThat(south.row).isEqualTo(1)
        assertThat(north.row).isEqualTo(8)
    }

    @Test
    fun `a more eastern location yields a higher column`() {
        val west = map().cellOf(GeoLocation(latitude = 5.0, longitude = 1.5))!!
        val east = map().cellOf(GeoLocation(latitude = 5.0, longitude = 8.5))!!

        assertThat(east.column).isGreaterThan(west.column)
        assertThat(west.column).isEqualTo(1)
        assertThat(east.column).isEqualTo(8)
    }

    @Test
    fun `a location south of the map is off the board`() {
        assertThat(map().cellOf(GeoLocation(latitude = -0.01, longitude = 5.0))).isNull()
    }

    @Test
    fun `a location east of the map is off the board`() {
        assertThat(map().cellOf(GeoLocation(latitude = 5.0, longitude = 10.01))).isNull()
    }

    @Test
    fun `corners stored in NE-SW order still resolve correctly`() {
        // cornerA given as NE, cornerB as SW — opposite to the documented convention.
        val flipped = map(
            cornerA = GeoLocation(latitude = 10.0, longitude = 10.0),
            cornerB = GeoLocation(latitude = 0.0, longitude = 0.0),
        )

        assertThat(flipped.cellOf(GeoLocation(latitude = 0.5, longitude = 0.5)))
            .isEqualTo(Cell(row = 0, column = 0))
    }
}
