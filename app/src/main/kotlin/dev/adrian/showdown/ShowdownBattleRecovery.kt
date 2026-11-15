package dev.adrian.showdown

object ShowdownBattleRecovery {
    enum class Mode {
        NONE,
        REGISTERED_PARTICIPANT,
        SPECTATOR,
        GUEST_SPECTATOR
    }

    fun mode(activeRoomId: String?, registered: Boolean, participant: Boolean, spectator: Boolean = false): Mode = when {
        activeRoomId.isNullOrBlank() -> Mode.NONE
        spectator -> Mode.SPECTATOR
        participant && registered -> Mode.REGISTERED_PARTICIPANT
        participant -> Mode.GUEST_SPECTATOR
        else -> Mode.SPECTATOR
    }

    /**
     * A battle room is only reopened when the activity is recreated by the system (rotation, process restore). A fresh
     * launch never reopens it: a battle from a previous run is offered through the unfinished-battle prompt, and only
     * if the server still lists it, so a finished battle is never replayed.
     */
    fun restoredActiveRoomId(persisted: String?, recreated: Boolean): String? = persisted.takeIf { recreated }

    /** A search saved by an earlier run died with its connection; a fresh launch must not queue for a battle again. */
    fun restoredSearchActive(persistedSearching: Boolean, recreated: Boolean): Boolean = persistedSearching && recreated

    /** Search, challenge and accept commands queued by an earlier run are stale on a fresh launch. */
    fun restoredLobbyCommands(persisted: List<String>?, recreated: Boolean): List<String>? = persisted.takeIf { recreated }

    fun restoredSearchFormat(persisted: String?, recreated: Boolean): String? = persisted.takeIf { recreated }

    fun pendingDecisionCommand(mode: Mode, command: String?): String? = command.takeUnless {
        mode == Mode.GUEST_SPECTATOR
    }
}
