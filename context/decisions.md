# Decisions

Agreed with the developer before work started.

1. **Overlays live in the Pokémon tab.** The top screen is the pure Showdown web scene (display-only, no native overlays). Stat information (HP number and bar, level/gender, item, status, types, boosts for active Pokémon) goes on the team cards in the bottom-screen Pokémon tab, for both teams. Opponent cards only show revealed information. The existing detail view stays for ability, moves and stats.
2. **Safe testing.** Local Showdown server + bot opponents + a separate `local` build (`dev.adrian.showdown.local`). The developer's `.debug` install, saved login and stuck battle are never touched.
3. **Unfinished battles.** Lobby lists every unfinished battle the server reports with **Resume** and **Forfeit** (confirm dialog, `/forfeit`). Debug and local builds never auto-resume.
4. **Sprites.** Showdown's own sprites only in battles; the HD (pkparaiso) path is removed from battles. Pokédex and team views keep the sprite cache.
5. **Scope of native removal.** Only in-battle drawing is replaced. Lobby, team preview, team builder and command deck stay native. `BattleSession` stays. Old code is deleted once the WebView path has parity.
6. **Git.** `feature/webview-core`, small commits, no pushing.
7. **Testing.** Assume the Showdown WebView is correct; test only that the view exists and loads. Our own bridge tests stay. Tests are behavioral, named `Thing_Scenario_Expected`, no mocks.
8. **Repo essentials.** `AGENTS.md`, `build.sh --test`, `emulate.sh` now; ask before adding ktlint; Dockerfile last.
9. **Lightweight (no-WebView) mode removed.** WebView is required; a clear error shows if it is unavailable.
10. **Top screen is display-only** (no tap-to-inspect). *Reversed 2026-11-15 (decision 19).*
11. **Accessibility gap accepted.** The WebView scene has no accessibility tree; the same information stays reachable from the lower screen. Documented in `inconsistencies.md`.
12. **Embedded JS moves to assets.**
13. **Order of work:** scaffolding, reopening-battle fix + Resume/Forfeit, WebView-core top screen, Pokémon-tab overlays, cleanup/tests/lint. Run to completion unless blocked.
14. **`emulate.sh`:** default `local` variant, `--variant debug` opt-in, no hard-coded toolchain paths.
15. **Held-battle rule (step 2).** Rooms reported in the first `updatesearch` after a named login that the app did not expect are held for the prompt; later battles open normally. Release builds still reopen the room they had persisted as active (continuity after a process restart); debuggable builds never do.
16. **Dev login.** `./emulate.sh --login NAME` signs the `.local` build in as NAME on the local server (empty token, no password). It is gated on the local build so it cannot affect a real account or the `.debug` install.
17. **Native top-screen team preview is removed too.** The WebView exists from `|init|battle`, which precedes team preview, so the native preview was never visible; Showdown's own scene draws it and lead selection lives on the lower screen. The top-screen native view is now `LobbyView`, which only draws the lobby card and the replay-loading screen.
18. **Instrumented tests target the `local` build** so they cannot touch the developer's `.debug` install or saved login.
19. **The finished battle stays on screen** until the battle room is cleared (like the web client), instead of dropping to the lobby card when playback drains.

18. **Live battles are not paced by the app** (decided 2026-11-15 after the Thor lagged behind the browser). Room packets are applied to the session at once and handed to the web scene, which queues and paces its own animations like the browser client. The lower screen (HP overlays, result) can therefore be ahead of the top screen during a turn; the Activity log still follows the scene. Packets that already hold past turns are caught up silently (`ShowdownBattleDelivery`). Replays keep the chunked, paced playback because of their pause and speed controls.

19. **Tapping a Pokémon on the top screen shows Showdown's own tooltip** (types, HP, ability candidates, speed range, known moves with PP, and for the player's side item, ability and stats), as hovering does in the browser. Reverses decision 10 at the user's request. The page keeps the latest `|request|` team (the scene's own copy is cleared when the battle starts), loads Showdown's species, move, item and ability data on the first tap instead of at battle start, scales the tooltip to the screen and keeps it on screen. Tapping elsewhere dismisses it.

20. **Fresh launches restore nothing about battles or searches** (2026-11-15). Replaces the release-build auto-resume from decision 7: only a system-recreated activity reopens its battle room. Everything else goes through the unfinished-battle prompt, which only lists battles the server still reports.

21. **Switch selection indicator, sprite style and ongoing battles** (2026-11-15, user requests).
    - A round check badge marks the team member a switch would send out (teal) or has already chosen (gold), on the Pokémon tab (`BattleSession.teamSelectionMark`).
    - Menu item 8 now toggles the battle scene between 3D and DS (Gen 5) sprites through Showdown's own `bwgfx` preference. The older single-value native sprite style is unchanged, so the Pokémon-tab icons stay 3D. A running scene is rebuilt from history to pick the change up.
    - Menu item 15, "Ongoing battles", lists every game the server reports for the account. It opens nothing by itself: a held battle offers Resume/Forfeit/Later, a battle started elsewhere offers Open/Later, and the open one just says so. The one-time unfinished-battle prompt after login is unchanged.

22. **Several battles at once** (2026-11-15, user request). One connection stays open for the account; "Find battle" queues a search on it instead of reopening the connection, and never queues a second search while one is pending. Each battle the app has open is a `BattleRoom` with its own `BattleSession` and its own web view. One room is on screen; the others are background rooms that keep updating their session. The menu's "Ongoing battles" list is the switcher (chosen: menu list only), marks each battle "your turn" / "waiting" / "finished", and a background battle never takes the screen. Switching saves the room on screen, rebinds the lower screen, lobby view and audio to the other session, and brings that room's web view up to date silently. The old "Showdown rooms" picker behind Find battle was removed. Known limits: a web view is re-seeded on each switch (the backdrop can change); background web views are paused, not animated.
