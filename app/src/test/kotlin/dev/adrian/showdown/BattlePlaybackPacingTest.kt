package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattlePlaybackPacingTest {
    private val moveChunk = listOf("|move|p1a: Pikachu|Tackle|p2a: Eevee", "|-damage|p2a: Eevee|60/100")
    private val switchChunk = listOf("|switch|p1a: Pikachu|Pikachu, L80|100/100")
    private val setupChunk = listOf("|player|p2|Opponent|169|1000", "|teamsize|p1|6", "|gametype|singles", "|gen|9")

    @Test
    fun BattlePlaybackTiming_MoveChunkPacedByTheScene_HasNoFixedPause() {
        val pause = BattlePlaybackTiming.pauseBeforeNextChunk(moveChunk, generatedMessageCount = 2, sceneSetsThePace = true)

        assertEquals(0L, pause)
    }

    @Test
    fun BattlePlaybackTiming_SwitchAndSetupChunksPacedByTheScene_HaveNoFixedPause() {
        val switchPause = BattlePlaybackTiming.pauseBeforeNextChunk(switchChunk, generatedMessageCount = 1, sceneSetsThePace = true)
        val setupPause = BattlePlaybackTiming.pauseBeforeNextChunk(setupChunk, generatedMessageCount = 0, sceneSetsThePace = true)

        assertEquals(0L, switchPause)
        assertEquals(0L, setupPause)
    }

    @Test
    fun BattlePlaybackTiming_MoveChunkWithoutAScene_KeepsTheFixedPause() {
        val pause = BattlePlaybackTiming.pauseBeforeNextChunk(moveChunk, generatedMessageCount = 2, sceneSetsThePace = false)

        assertEquals(BattlePlaybackTiming.pauseAfter(moveChunk, 2), pause)
        assertTrue(pause > 0L)
    }
}
