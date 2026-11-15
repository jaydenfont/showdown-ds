package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Test

class ShowdownBattleDeliveryTest {
    private val liveTurn = listOf("|", "|move|p1a: X|Return|p2a: Y", "|-damage|p2a: Y|60/100", "|upkeep", "|turn|4")
    private val rejoinedBattle = listOf("|init|battle", "|player|p1|A|1|", "|start", "|turn|1", "|move|p1a: X|Return|p2a: Y", "|turn|2")
    private val freshBattle = listOf("|init|battle", "|player|p1|A|1|", "|player|p2|B|2|", "|start", "|turn|1")

    @Test
    fun ShowdownBattleDelivery_LiveTurnPacket_IsAppliedAtOnce() {
        val delivery = ShowdownBattleDelivery.forRoomPacket(liveTurn, fastForward = false)

        assertEquals(ShowdownBattleDelivery.Mode.APPLY_NOW, delivery)
    }

    @Test
    fun ShowdownBattleDelivery_FreshBattleOpening_IsAppliedAtOnce() {
        val delivery = ShowdownBattleDelivery.forRoomPacket(freshBattle, fastForward = false)

        assertEquals(ShowdownBattleDelivery.Mode.APPLY_NOW, delivery)
    }

    @Test
    fun ShowdownBattleDelivery_RejoinedBattleHistory_IsCaughtUpWithoutReplay() {
        val delivery = ShowdownBattleDelivery.forRoomPacket(rejoinedBattle, fastForward = false)

        assertEquals(ShowdownBattleDelivery.Mode.CATCH_UP_FROM_HISTORY, delivery)
    }

    @Test
    fun ShowdownBattleDelivery_HistoryDuringAnAlreadyRunningFastForward_IsAppliedAtOnce() {
        val delivery = ShowdownBattleDelivery.forRoomPacket(rejoinedBattle, fastForward = true)

        assertEquals(ShowdownBattleDelivery.Mode.APPLY_NOW, delivery)
    }
}
