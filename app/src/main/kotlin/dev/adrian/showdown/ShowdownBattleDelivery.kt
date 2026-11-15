package dev.adrian.showdown

/**
 * How a live battle-room packet reaches the app and the web scene. The scene has its own animation queue and
 * paces itself, so a packet is never held back and replayed in steps; the only choice is whether it is history.
 */
object ShowdownBattleDelivery {
    enum class Mode {
        /** Apply every event now and let the scene queue its animations. */
        APPLY_NOW,

        /** The packet is a battle's past turns: apply them and show the current state without animating them. */
        CATCH_UP_FROM_HISTORY
    }

    fun forRoomPacket(lines: List<String>, fastForward: Boolean): Mode =
        if (!fastForward && ShowdownBattleHistory.isInProgress(lines)) Mode.CATCH_UP_FROM_HISTORY else Mode.APPLY_NOW
}
