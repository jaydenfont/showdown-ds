package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Test

class BattleSessionTitleTest {
    @Test
    fun BattleSession_BothPlayersKnown_TitlesTheBattleWithTheirNames() {
        val session = BattleSession()
        session.applyProtocolPacket(listOf("|init|battle", "|player|p1|Ash|1|", "|player|p2|Gary|2|"))

        assertEquals("Ash vs. Gary", session.battleTitle())
    }

    @Test
    fun BattleSession_PlayersNotKnownYet_UsesAPlainTitle() {
        val session = BattleSession()

        assertEquals("Battle", session.battleTitle())
    }
}
