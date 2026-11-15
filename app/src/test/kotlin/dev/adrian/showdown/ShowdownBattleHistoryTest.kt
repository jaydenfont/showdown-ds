package dev.adrian.showdown

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShowdownBattleHistoryTest {
    private val roomHeader = listOf("|init|battle", "|title|A vs. B", "|player|p1|A|1|", "|player|p2|B|2|", "|gametype|singles")
    private val openingSwitches = listOf("|start", "|switch|p1a: X|Dodrio, L85, F|100/100", "|switch|p2a: Y|Sandaconda, L84, F|100/100")

    @Test
    fun ShowdownBattleHistory_FreshBattleAtTurnOne_IsNotInProgress() {
        val lines = roomHeader + openingSwitches + listOf("|turn|1")

        val inProgress = ShowdownBattleHistory.isInProgress(lines)

        assertFalse(inProgress)
    }

    @Test
    fun ShowdownBattleHistory_BattleRejoinedAtTurnThree_IsInProgress() {
        val lines = roomHeader + openingSwitches + listOf("|turn|1", "|move|p1a: X|Return|p2a: Y", "|turn|2", "|turn|3")

        val inProgress = ShowdownBattleHistory.isInProgress(lines)

        assertTrue(inProgress)
    }

    @Test
    fun ShowdownBattleHistory_RejoinedAfterMovesWithinTurnOne_IsInProgress() {
        val lines = roomHeader + openingSwitches + listOf("|turn|1", "|move|p1a: X|Return|p2a: Y", "|-damage|p2a: Y|60/100")

        val inProgress = ShowdownBattleHistory.isInProgress(lines)

        assertTrue(inProgress)
    }

    @Test
    fun ShowdownBattleHistory_LiveTurnPacketWithoutInit_IsNotHistory() {
        val lines = listOf("|move|p1a: X|Return|p2a: Y", "|-damage|p2a: Y|60/100", "|turn|5")

        val inProgress = ShowdownBattleHistory.isInProgress(lines)

        assertFalse(inProgress)
    }

    @Test
    fun ShowdownBattleHistory_EmptyPacket_IsNotInProgress() {
        val inProgress = ShowdownBattleHistory.isInProgress(emptyList())

        assertFalse(inProgress)
    }

    @Test
    fun ShowdownBattleHistory_MalformedTurnNumber_IsNotInProgress() {
        val lines = roomHeader + openingSwitches + listOf("|turn|abc", "|turn|")

        val inProgress = ShowdownBattleHistory.isInProgress(lines)

        assertFalse(inProgress)
    }
}
