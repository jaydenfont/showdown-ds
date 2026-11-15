package dev.adrian.showdown

/** Which sprite family the battle scene shows: Showdown's 3D animated sprites or the Gen 5 (DS) pixel sprites. */
enum class BattleSceneSprites(val displayName: String, val usesDsGraphics: Boolean) {
    THREE_D("3D", false),
    DS("DS", true);

    fun next(): BattleSceneSprites = if (this == THREE_D) DS else THREE_D

    companion object {
        fun fromName(name: String?): BattleSceneSprites = entries.firstOrNull { it.name == name } ?: THREE_D
    }
}
