package net.lausi95.citygame.application.domain.model.leaderboard

import net.lausi95.citygame.application.domain.model.game.Game
import net.lausi95.citygame.application.domain.model.team.Team
import java.time.OffsetDateTime

/**
 * The ranked standing of a [Game]'s teams by how many MISTERX agents they have found.
 *
 * Only findings of agents that currently exist, are MISTERX, and are active are counted (see
 * ADR 0005), so a team's score is derived from current agent state, never frozen. [entries] are
 * held in rank order, best first.
 */
class Leaderboard private constructor(
    val game: Game,
    val entries: List<LeaderboardEntry>,
) {
    companion object {

        /**
         * Ranks: counted-find count descending, then the moment of the team's most-recent counted
         * finding ascending (the team that "got there first" ranks higher), then team id as a
         * stability tie-break. Teams with no counted finds (null [LeaderboardEntry.lastFoundAt])
         * fall to the bottom in team-id order.
         */
        private val byRank: Comparator<LeaderboardEntry> =
            compareByDescending<LeaderboardEntry> { it.foundCount }
                .thenBy(nullsLast(naturalOrder())) { it.lastFoundAt }
                .thenBy { it.team.id.value }

        fun of(game: Game, entries: List<LeaderboardEntry>): Leaderboard =
            Leaderboard(game, entries.sortedWith(byRank))
    }
}

/**
 * One team's standing: the MISTERX agents it has found, held chronologically (earliest first).
 */
class LeaderboardEntry(
    val team: Team,
    val foundAgents: List<FoundMisterX>,
) {
    val foundCount: Int
        get() = foundAgents.size

    /** The moment of this team's most-recent counted finding, or `null` if it has found none. */
    val lastFoundAt: OffsetDateTime?
        get() = foundAgents.maxOfOrNull { it.foundAt }
}

/**
 * A MISTERX agent as it appears on the leaderboard: its in-game [alias] and the [foundAt] moment
 * the team located it. Never carries PII.
 */
class FoundMisterX(
    val alias: String,
    val foundAt: OffsetDateTime,
)
