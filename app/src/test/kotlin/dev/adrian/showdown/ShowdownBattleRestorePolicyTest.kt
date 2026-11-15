package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShowdownBattleRestorePolicyTest {
    private val persistedRoom = "battle-gen9randombattle-2"
    private val persistedFormat = "gen9randombattle"

    @Test
    fun ShowdownBattleRecovery_FreshLaunchWithPersistedRoom_DoesNotRestoreIt() {
        val restored = ShowdownBattleRecovery.restoredActiveRoomId(persistedRoom, recreated = false)

        assertEquals(null, restored)
    }

    @Test
    fun ShowdownBattleRecovery_RecreatedActivityWithPersistedRoom_RestoresIt() {
        val restored = ShowdownBattleRecovery.restoredActiveRoomId(persistedRoom, recreated = true)

        assertEquals(persistedRoom, restored)
    }

    @Test
    fun ShowdownBattleRecovery_NoPersistedRoom_RestoresNothing() {
        val restored = ShowdownBattleRecovery.restoredActiveRoomId(null, recreated = true)

        assertEquals(null, restored)
    }

    @Test
    fun ShowdownBattleRecovery_FreshLaunchWithPersistedSearch_DoesNotRestartIt() {
        val searching = ShowdownBattleRecovery.restoredSearchActive(persistedSearching = true, recreated = false)
        val format = ShowdownBattleRecovery.restoredSearchFormat(persistedFormat, recreated = false)

        assertFalse(searching)
        assertEquals(null, format)
    }

    @Test
    fun ShowdownBattleRecovery_RecreatedActivityWithPersistedSearch_KeepsIt() {
        val searching = ShowdownBattleRecovery.restoredSearchActive(persistedSearching = true, recreated = true)
        val format = ShowdownBattleRecovery.restoredSearchFormat(persistedFormat, recreated = true)

        assertTrue(searching)
        assertEquals(persistedFormat, format)
    }

    @Test
    fun ShowdownBattleRecovery_FreshLaunchWithSavedSearchAndChallengeCommands_DropsThem() {
        val saved = listOf("/utm null", "/search gen9randombattle", "/challenge SomeoneElse, gen9ou")

        val restored = ShowdownBattleRecovery.restoredLobbyCommands(saved, recreated = false)

        assertEquals(null, restored)
    }

    @Test
    fun ShowdownBattleRecovery_RecreatedActivityWithSavedCommands_KeepsThem() {
        val saved = listOf("/utm null", "/search gen9randombattle")

        val restored = ShowdownBattleRecovery.restoredLobbyCommands(saved, recreated = true)

        assertEquals(saved, restored)
    }
}
