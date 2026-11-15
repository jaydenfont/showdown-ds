package dev.adrian.showdown

/** Recognizes a battle-room packet that carries a battle's past turns rather than a fresh start or a live update. */
object ShowdownBattleHistory {
    private const val INIT_PREFIX = "|init|battle"
    private const val TURN_PREFIX = "|turn|"
    private const val MOVE_PREFIX = "|move|"
    private const val FIRST_TURN = 1

    /**
     * True for a room's opening packet when the battle was already underway: it reaches past turn one or already
     * contains moves. Such a packet is caught up to instantly instead of being replayed with animations.
     */
    fun isInProgress(lines: List<String>): Boolean {
        if (lines.none { it.startsWith(INIT_PREFIX) }) return false
        return lines.any { line ->
            line.startsWith(MOVE_PREFIX) ||
                (line.startsWith(TURN_PREFIX) && (line.removePrefix(TURN_PREFIX).toIntOrNull() ?: 0) > FIRST_TURN)
        }
    }
}
