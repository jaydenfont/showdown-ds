package dev.adrian.showdown

import org.junit.Assert.assertEquals
import org.junit.Test

class BattleSessionTeamSelectionMarkTest {
    private val forcedSwitchRequest =
        "|request|{\"rqid\":31,\"forceSwitch\":[true],\"side\":{\"pokemon\":[" +
            "{\"ident\":\"p1: Incineroar\",\"details\":\"Incineroar, L50, M\",\"condition\":\"0 fnt\",\"active\":false}," +
            "{\"ident\":\"p1: Naganadel\",\"details\":\"Naganadel, L50\",\"condition\":\"100/100\",\"active\":false}," +
            "{\"ident\":\"p1: Mimikyu\",\"details\":\"Mimikyu, L50\",\"condition\":\"100/100\",\"active\":false}]}}"
    private val nextRequest =
        "|request|{\"rqid\":32,\"active\":[{\"moves\":[{\"move\":\"Tackle\",\"id\":\"tackle\",\"pp\":35,\"maxpp\":35,\"target\":\"normal\"}]}],\"side\":{\"pokemon\":[" +
            "{\"ident\":\"p1: Naganadel\",\"details\":\"Naganadel, L50\",\"condition\":\"100/100\",\"active\":true}]}}"

    private fun sessionAskingForASwitch() = BattleSession().also { it.applyProtocolLine(forcedSwitchRequest) }

    @Test
    fun BattleSession_FocusedSwitchableMember_IsMarkedSelected() {
        val session = sessionAskingForASwitch()

        session.moveFocus(1, 0)

        assertEquals(BattleSession.TeamSelectionMark.SELECTED, session.teamSelectionMark(session.focusedTeam))
        assertEquals(null, session.teamSelectionMark(0))
    }

    @Test
    fun BattleSession_MovingTheFocus_MovesTheSelectedMark() {
        val session = sessionAskingForASwitch()
        session.moveFocus(1, 0)
        val first = session.focusedTeam

        session.moveFocus(0, 1)

        assertEquals(null, session.teamSelectionMark(first))
        assertEquals(BattleSession.TeamSelectionMark.SELECTED, session.teamSelectionMark(session.focusedTeam))
    }

    @Test
    fun BattleSession_FaintedFocusedMember_IsNotMarked() {
        val session = sessionAskingForASwitch()

        val mark = session.teamSelectionMark(0)

        assertEquals(null, mark)
    }

    @Test
    fun BattleSession_SwitchSent_KeepsTheChosenMemberMarkedAsChosen() {
        val session = sessionAskingForASwitch()
        session.moveFocus(1, 0)
        val chosen = session.focusedTeam

        session.confirmSelection()

        assertEquals(BattleSession.TeamSelectionMark.CHOSEN, session.teamSelectionMark(chosen))
    }

    @Test
    fun BattleSession_NewRequestAfterTheSwitch_ClearsTheChosenMark() {
        val session = sessionAskingForASwitch()
        session.moveFocus(1, 0)
        val chosen = session.focusedTeam
        session.confirmSelection()

        session.applyProtocolLine(nextRequest)

        assertEquals(null, session.teamSelectionMark(chosen))
    }
}
