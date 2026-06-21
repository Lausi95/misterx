package net.lausi95.citygame.application.domain.model.board

import net.lausi95.citygame.application.domain.model.agent.Agent
import net.lausi95.citygame.application.domain.model.game.Cell
import net.lausi95.citygame.application.domain.model.game.Game

/**
 * The live, caller-specific view of a [Game]'s playfield: the map (via [game]) overlaid with the
 * currently-visible agent positions.
 *
 * UTILITY agents appear in [utilityAgents] with their exact last-known location; MISTERX agents
 * appear in [misterxAgents] resolved only to the grid [Cell] containing their last-known location.
 */
class Board(
    val game: Game,
    val utilityAgents: List<Agent>,
    val misterxAgents: List<MisterXOnBoard>,
)

/**
 * A MISTERX [Agent] as seen on the [Board]: its position obfuscated to the grid [cell] containing
 * its last-known location, never the exact coordinates.
 */
class MisterXOnBoard(
    val agent: Agent,
    val cell: Cell,
)
