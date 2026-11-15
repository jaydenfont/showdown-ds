package dev.adrian.showdown

/** The account's ongoing battles as listed by the "Ongoing battles" menu item; looking at the list opens nothing. */
object ShowdownOngoingBattles {
    const val TITLE = "Ongoing battles"
    const val EMPTY_STATUS = "You have no ongoing battles."
    const val OPEN_STATUS = "That battle is already open."
    const val LEAVE_FIRST_STATUS = "Leave your current battle before opening another."
    const val JOIN_TITLE = "Open this battle?"
    const val JOIN_BUTTON = "Open"
    const val NO_RENAMES_MARKER = "*"
    private const val OPEN_SUFFIX = "open on this device"

    enum class Kind {
        /** Held since login: it can be resumed or forfeited. */
        HELD,

        /** Already shown on this device. */
        OPEN,

        /** Started elsewhere after login (the browser, for example): it can be opened here. */
        JOINABLE
    }

    data class Row(val roomId: String, val label: String, val kind: Kind)

    /** One row per game the server reports for the account, in the server's order. */
    fun rows(games: Map<String, String>, held: List<ShowdownUnfinishedBattles.Entry>, openRoomId: String?): List<Row> =
        games.map { (roomId, rawFormat) ->
            val format = rawFormat.removeSuffix(NO_RENAMES_MARKER)
            val heldEntry = held.firstOrNull { it.roomId == roomId }
            when {
                roomId == openRoomId -> Row(roomId, "${heldEntry?.title ?: format}\n$OPEN_SUFFIX", Kind.OPEN)
                heldEntry != null -> Row(roomId, ShowdownUnfinishedBattlePresentation.label(heldEntry), Kind.HELD)
                else -> Row(roomId, "$format\n$roomId", Kind.JOINABLE)
            }
        }
}
