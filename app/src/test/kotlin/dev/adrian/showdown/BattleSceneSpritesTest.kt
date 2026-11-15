package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleSceneSpritesTest {
    private val spriteMenuIndex = 8

    private fun sessionOnMenu(): BattleSession = BattleSession().apply { selectPanel(BattleSession.Panel.MENU) }

    private fun BattleSession.publishedActions(): MutableList<BattleSession.ClientAction> {
        val actions = mutableListOf<BattleSession.ClientAction>()
        addClientActionListener { actions += it }
        return actions
    }

    @Test
    fun BattleSceneSprites_Next_AlternatesBetweenThreeDAndDs() {
        assertEquals(BattleSceneSprites.DS, BattleSceneSprites.THREE_D.next())
        assertEquals(BattleSceneSprites.THREE_D, BattleSceneSprites.DS.next())
    }

    @Test
    fun BattleSceneSprites_OnlyDsUsesTheGenerationFiveGraphics() {
        assertTrue(BattleSceneSprites.DS.usesDsGraphics)
        assertFalse(BattleSceneSprites.THREE_D.usesDsGraphics)
    }

    @Test
    fun BattleSceneSprites_FromAnUnknownOrMissingSavedName_FallsBackToThreeD() {
        assertEquals(BattleSceneSprites.THREE_D, BattleSceneSprites.fromName("HD_FIRST"))
        assertEquals(BattleSceneSprites.THREE_D, BattleSceneSprites.fromName(null))
        assertEquals(BattleSceneSprites.DS, BattleSceneSprites.fromName("DS"))
    }

    @Test
    fun BattleSession_NewSession_ShowsThreeDSpritesInTheMenu() {
        val session = sessionOnMenu()

        assertEquals("Sprite style 3D", session.menuItems()[spriteMenuIndex])
    }

    @Test
    fun BattleSession_ConfirmingTheSpriteItem_SwitchesToDsAndAnnouncesIt() {
        val session = sessionOnMenu()
        val actions = session.publishedActions()
        session.selectMenuItem(spriteMenuIndex)

        session.confirmSelection()

        assertEquals(BattleSceneSprites.DS, session.sceneSprites)
        assertEquals("Sprite style DS", session.menuItems()[spriteMenuIndex])
        assertEquals("DS sprites enabled.", session.status)
        assertTrue(BattleSession.ClientAction.SETTINGS_CHANGED in actions)
    }

    @Test
    fun BattleSession_ConfirmingTheSpriteItemTwice_ReturnsToThreeD() {
        val session = sessionOnMenu()
        session.selectMenuItem(spriteMenuIndex)

        session.confirmSelection()
        session.confirmSelection()

        assertEquals(BattleSceneSprites.THREE_D, session.sceneSprites)
        assertEquals("3D sprites enabled.", session.status)
    }

    @Test
    fun BattleSession_AppliedPreferences_RestoreTheSavedSpriteStyle() {
        val session = sessionOnMenu()

        session.applyUserPreferences(soundEffects = true, music = true, haptics = true, sceneSprites = BattleSceneSprites.DS)

        assertEquals(BattleSceneSprites.DS, session.sceneSprites)
    }
}
