package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleSessionOngoingBattlesMenuTest {
    private val ongoingBattlesMenuIndex = 15

    private fun sessionOnMenu(): BattleSession = BattleSession().apply { selectPanel(BattleSession.Panel.MENU) }

    private fun BattleSession.publishedActions(): MutableList<BattleSession.ClientAction> {
        val actions = mutableListOf<BattleSession.ClientAction>()
        addClientActionListener { actions += it }
        return actions
    }

    @Test
    fun BattleSession_MenuWithNoOngoingBattles_StillOffersOngoingBattles() {
        val session = sessionOnMenu()

        assertEquals("Ongoing battles", session.menuItems()[ongoingBattlesMenuIndex])
    }

    @Test
    fun BattleSession_MenuWithOngoingBattles_ShowsTheirCount() {
        val session = sessionOnMenu()

        session.setOngoingBattleCount(3)

        assertEquals("Ongoing battles (3)", session.menuItems()[ongoingBattlesMenuIndex])
    }

    @Test
    fun BattleSession_ConfirmingOngoingBattles_OnlyPublishesTheListAction() {
        val session = sessionOnMenu()
        val actions = session.publishedActions()
        session.setOngoingBattleCount(2)
        session.selectMenuItem(ongoingBattlesMenuIndex)

        session.confirmSelection()

        assertEquals(listOf(BattleSession.ClientAction.OPEN_ONGOING_BATTLES), actions)
    }

    @Test
    fun BattleSession_ConfirmingTheTimerItem_StillTogglesTheTimerNotTheList() {
        val session = sessionOnMenu()
        val actions = session.publishedActions()
        session.selectMenuItem(ongoingBattlesMenuIndex - 1)

        session.confirmSelection()

        assertTrue(BattleSession.ClientAction.TOGGLE_BATTLE_TIMER in actions)
        assertFalse(BattleSession.ClientAction.OPEN_ONGOING_BATTLES in actions)
    }

    @Test
    fun BattleSession_ListActionThatSetsAStatus_KeepsThatStatus() {
        val session = sessionOnMenu()
        session.addClientActionListener { session.setConnectionStatus("You have no ongoing battles.") }
        session.selectMenuItem(ongoingBattlesMenuIndex)

        session.confirmSelection()

        assertEquals("You have no ongoing battles.", session.status)
    }
}
