package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BattleSessionConfigurationCopyTest {
    private val spriteMenuIndex = 8

    private fun configuredSession() = BattleSession().apply {
        setLocalUsername("Alice")
        applyUserPreferences(soundEffects = false, music = false, haptics = false, announcer = true, sceneSprites = BattleSceneSprites.DS)
        setMatchFormat(BattleSession.MatchFormat.defaults.last())
        setBattleSearchActive(true)
        setOngoingBattleCount(3)
    }

    @Test
    fun BattleSession_CopyingConfiguration_KeepsThePreferences() {
        val source = configuredSession()
        val joined = BattleSession()

        joined.copyConfigurationFrom(source)

        assertFalse(joined.soundEffectsEnabled)
        assertFalse(joined.musicEnabled)
        assertFalse(joined.hapticsEnabled)
        assertEquals(true, joined.announcerEnabled)
        assertEquals(BattleSceneSprites.DS, joined.sceneSprites)
    }

    @Test
    fun BattleSession_CopyingConfiguration_KeepsTheFormatAndAccountMenuState() {
        val source = configuredSession()
        val joined = BattleSession()

        joined.copyConfigurationFrom(source)
        joined.selectPanel(BattleSession.Panel.MENU)

        assertEquals(source.matchFormat, joined.matchFormat)
        assertEquals(source.availableMatchFormats(), joined.availableMatchFormats())
        assertEquals("Cancel battle search", joined.menuItems()[0])
        assertEquals("Ongoing battles (3)", joined.menuItems()[15])
        assertEquals("Sprite style DS", joined.menuItems()[spriteMenuIndex])
    }
}
