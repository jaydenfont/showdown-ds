package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShowdownOngoingBattlesTest {
    private val heldRoom = "battle-gen9randombattle-11"
    private val openRoom = "battle-gen9ou-12"
    private val laterRoom = "battle-gen9vgc-13"
    private val games = linkedMapOf(heldRoom to "gen9randombattle*", openRoom to "gen9ou", laterRoom to "gen9vgc")
    private val held = listOf(ShowdownUnfinishedBattles.Entry(heldRoom, "gen9randombattle", "Ash vs. Gary"))

    @Test
    fun ShowdownOngoingBattles_NoGames_HasNoRows() {
        val rows = ShowdownOngoingBattles.rows(emptyMap(), emptyList(), openRoomId = null)

        assertTrue(rows.isEmpty())
    }

    @Test
    fun ShowdownOngoingBattles_EveryGameOnTheAccount_GetsARowInServerOrder() {
        val rows = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom)

        assertEquals(listOf(heldRoom, openRoom, laterRoom), rows.map { it.roomId })
    }

    @Test
    fun ShowdownOngoingBattles_BattleHeldFromLogin_IsOfferedForResumeOrForfeit() {
        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom).first { it.roomId == heldRoom }

        assertEquals(ShowdownOngoingBattles.Kind.HELD, row.kind)
        assertTrue(row.label.contains("Ash vs. Gary"))
    }

    @Test
    fun ShowdownOngoingBattles_BattleOpenOnThisDevice_IsMarkedOpen() {
        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom).first { it.roomId == openRoom }

        assertEquals(ShowdownOngoingBattles.Kind.OPEN, row.kind)
        assertTrue(row.label.contains("open"))
    }

    @Test
    fun ShowdownOngoingBattles_BattleStartedElsewhereAfterLogin_CanBeJoined() {
        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom).first { it.roomId == laterRoom }

        assertEquals(ShowdownOngoingBattles.Kind.JOINABLE, row.kind)
        assertTrue(row.label.contains("gen9vgc"))
    }

    @Test
    fun ShowdownOngoingBattles_FormatWithTheNoRenamesMarker_ShowsTheBareFormat() {
        val row = ShowdownOngoingBattles.rows(mapOf(laterRoom to "gen9vgc*"), emptyList(), openRoomId = null).single()

        assertTrue(!row.label.contains("*"))
    }

    @Test
    fun ShowdownOngoingBattles_BattleOpenInTheBackground_CanBeSwitchedToAndSaysWhenItIsYourTurn() {
        val rooms = mapOf(laterRoom to ShowdownOngoingBattles.OpenRoom("Ash vs. Gary", yourTurn = true, finished = false))

        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom, rooms = rooms).first { it.roomId == laterRoom }

        assertEquals(ShowdownOngoingBattles.Kind.SWITCHABLE, row.kind)
        assertTrue(row.label.contains("Ash vs. Gary"))
        assertTrue(row.label.contains("your turn"))
    }

    @Test
    fun ShowdownOngoingBattles_BackgroundBattleWaitingOnTheOpponent_SaysWaiting() {
        val rooms = mapOf(laterRoom to ShowdownOngoingBattles.OpenRoom("Ash vs. Gary", yourTurn = false, finished = false))

        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom, rooms = rooms).first { it.roomId == laterRoom }

        assertTrue(row.label.contains("waiting"))
    }

    @Test
    fun ShowdownOngoingBattles_FinishedBackgroundBattleNoLongerOnTheServer_IsStillListed() {
        val finishedRoom = "battle-gen9ou-99"
        val rooms = mapOf(finishedRoom to ShowdownOngoingBattles.OpenRoom("Ash vs. Gary", yourTurn = false, finished = true))

        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom, rooms = rooms).first { it.roomId == finishedRoom }

        assertEquals(ShowdownOngoingBattles.Kind.SWITCHABLE, row.kind)
        assertTrue(row.label.contains("finished"))
    }

    @Test
    fun ShowdownOngoingBattles_FocusedBattleThatIsYourTurn_StaysMarkedOpen() {
        val rooms = mapOf(openRoom to ShowdownOngoingBattles.OpenRoom("Ash vs. Gary", yourTurn = true, finished = false))

        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom, rooms = rooms).first { it.roomId == openRoom }

        assertEquals(ShowdownOngoingBattles.Kind.OPEN, row.kind)
    }

    @Test
    fun ShowdownOngoingBattles_FocusedBattle_SaysItIsOpenAndWhoseTurnItIs() {
        val rooms = mapOf(openRoom to ShowdownOngoingBattles.OpenRoom("Ash vs. Gary", yourTurn = true, finished = false))

        val row = ShowdownOngoingBattles.rows(games, held, openRoomId = openRoom, rooms = rooms).first { it.roomId == openRoom }

        assertTrue(row.label.contains("open on this device"))
        assertTrue(row.label.contains("your turn"))
    }
}
