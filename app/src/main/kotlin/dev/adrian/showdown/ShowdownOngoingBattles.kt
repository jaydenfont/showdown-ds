package dev.adrian.showdown

/** The account's ongoing battles as listed by the "Ongoing battles" menu item; looking at the list opens nothing. */
object ShowdownOngoingBattles {
    const val TITLE = "Ongoing battles"
    const val EMPTY_STATUS = "You have no ongoing battles."
    const val OPEN_STATUS = "That battle is already open."
    const val BACKGROUND_STARTED_STATUS = "A new battle started. Open it from Ongoing battles."
    const val LEAVE_FIRST_STATUS = "Leave your current battle before opening another."
    const val JOIN_TITLE = "Open this battle?"
    const val JOIN_BUTTON = "Open"
    const val NO_RENAMES_MARKER = "*"
    private const val OPEN_SUFFIX = "open on this device"

    enum class Kind {
        /** Held since login: it can be resumed or forfeited. */
        HELD,

        /** The battle on screen right now. */
        OPEN,

        /** Open in the app but not on screen: choosing it switches to it. */
        SWITCHABLE,

        /** Started elsewhere after login (the browser, for example): it can be opened here. */
        JOINABLE
    }

    /** A battle the app has open: its title, whether it waits on the player, and whether it has ended. */
    data class OpenRoom(val title: String, val yourTurn: Boolean, val finished: Boolean)

    data class Row(val roomId: String, val label: String, val kind: Kind)

    /** One row per game the server reports, in its order, followed by open battles it no longer reports (finished ones). */
    fun rows(
        games: Map<String, String>,
        held: List<ShowdownUnfinishedBattles.Entry>,
        openRoomId: String?,
        rooms: Map<String, OpenRoom> = emptyMap()
    ): List<Row> {
        val listed = games.map { (roomId, rawFormat) ->
            val format = rawFormat.removeSuffix(NO_RENAMES_MARKER)
            val heldEntry = held.firstOrNull { it.roomId == roomId }
            val room = rooms[roomId]
            when {
                roomId == openRoomId -> Row(roomId, "${room?.title ?: heldEntry?.title ?: format}\n${openState(room)}", Kind.OPEN)
                room != null -> Row(roomId, "${room.title}\n${stateOf(room, OPEN_SUFFIX)}", Kind.SWITCHABLE)
                heldEntry != null -> Row(roomId, ShowdownUnfinishedBattlePresentation.label(heldEntry), Kind.HELD)
                else -> Row(roomId, "$format\n$roomId", Kind.JOINABLE)
            }
        }
        val ended = rooms.filterKeys { it !in games && it != openRoomId }
            .map { (roomId, room) -> Row(roomId, "${room.title}\n${stateOf(room, OPEN_SUFFIX)}", Kind.SWITCHABLE) }
        return listed + ended
    }

    private fun openState(room: OpenRoom?): String = room?.let { "$OPEN_SUFFIX · ${stateOf(it, OPEN_SUFFIX)}" } ?: OPEN_SUFFIX

    private fun stateOf(room: OpenRoom?, fallback: String): String = when {
        room == null -> fallback
        room.finished -> "finished"
        room.yourTurn -> "your turn"
        else -> "waiting"
    }
}
