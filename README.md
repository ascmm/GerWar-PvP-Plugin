# GerWar PvP

All-in-one RuneLite-Plugin für den **GerWar-Clan**.

Ein einziges Panel, vier Funktionsbereiche:

- **Fights** — Fight-Detection in LMS, PvP-Arena, Wildy. Off-Pray-%, Expected-vs-Actual-DMG,
  Magic-Hit-Luck, Offensive-Pray-Success, HP-Healed, Robe-Hits, KO-Chance, Ghost-Barrage.
- **PK K/D** — automatische Kill/Death-Zählung. Kills landen zuerst im Status **PENDING** und
  werden erst als **VALIDATED** gebucht, wenn du den Loot-Key öffnest. Zeigt `totalGained`,
  `totalLost`, Net-Profit und K/D-Ratio.
- **Splits** — Session-basierte Loot-Splits für Gruppen-PvM/PvP, Chat-Detection,
  Alt-Accounts-Roster, Direct-Payment-Settlement, Discord-Markdown-Export.
- **Auto-Handoff** — bei offener Split-Session wird der validierte Loot-Key-Wert
  automatisch als Pending-Value in die Session übertragen.

## Build

```
./gradlew build
```

Das Plugin-JAR landet unter `build/libs/`.

## Daten

Alle Plugin-Daten liegen unter `~/.runelite/gerwar-pvp/`:
- `fight-history.json`
- `pk-history.json`
- `sessions.json`
- `players.json`

## Author

GW-Roflgrins — GerWar clan

## Lizenz

BSD 2-Clause — siehe [LICENSE](LICENSE).
