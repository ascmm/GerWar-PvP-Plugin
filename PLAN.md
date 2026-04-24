# GerWar-PvP-Plugin — Implementierungsplan

## Context

Im (aktuell leeren) Ordner `GerWar-PvP-Plugin/` entsteht ein **neues, eigenständiges RuneLite-Plugin** für die GerWar-Community. Es vereint die Funktionalität zweier bestehender Plugin-Hub-Plugins in **einer** Codebasis unter **eigener** Identität:

1. **Auto Split Manager** — [elie605/split-session-tracker](https://github.com/elie605/split-session-tracker) @ `a82e8194…`, BSD-2-Clause, Paket `com.splitmanager.*` → *Gruppenloot-Splits, Chat-Detection, Session-Tracking, Settlement.*
2. **PvP Performance Tracker v1.7.3** — [Matsyir/pvp-performance-tracker](https://github.com/Matsyir/pvp-performance-tracker) @ `ae0e3020…`, BSD-2-Clause, Paket `matsyir.pvpperformancetracker.*` → *Fight-Detection, Damage-Simulation, Off-Pray-/Expected-DMG-/KO-Stats, Fight History.*

**Nutzerentscheidungen:**
- **Voller Merge** zu **einem** Plugin mit eigenem Paket `de.gerwar.pvp.*`, eigener Config-Gruppe, eigenem Branding. Im Runtime-Code, in der UI und in den Konfigurationsnamen darf **kein** Verweis mehr auf „Matsyir“, „split-session-tracker“, „auto-split-manager“ oder „pvp-performance-tracker“ sichtbar sein.
- **BSD-Lizenzkonformität** wird allein über `LICENSE` und `NOTICE` im Repo-Root erfüllt — diese Dateien sind nicht Teil des Runtime-JARs, das der User im Plugin-Menü sieht.
- **Auto-Handoff:** Beim Fight-Ende mit Gegner-Tod legt das Fight-Modul automatisch einen `PendingValue` im Split-Modul an (Gegner-Name als Quelle).
- **Panel:** ein einziger NavigationButton, ein `JTabbedPane` mit Tabs **Fights**, **Splits**, **Settings**.
- **Daten:** Frisch anfangen — kein Import aus `.runelite/pvp-performance-tracker2/` oder alten ASM-Config-Keys.

---

## Zielstruktur

```
GerWar-PvP-Plugin/
├── build.gradle                           # Java 11, Lombok, RuneLite latest.release
├── settings.gradle
├── gradle/wrapper/…                       # Standard RuneLite-Plugin-Wrapper
├── runelite-plugin.properties             # displayName="GerWar PvP", author="GerWar"
├── LICENSE                                # BSD-2-Clause, listet beide Upstream-Copyrights
├── NOTICE                                 # Attribution-Pflichttext
├── .gitignore
├── README.md                              # Feature-Liste (DE)
└── src/main/
    ├── java/de/gerwar/pvp/
    │   ├── GerWarPvpPlugin.java           # @PluginDescriptor, einziger Entrypoint
    │   ├── GerWarPvpConfig.java           # @ConfigGroup("gerwarpvp")
    │   ├── GerWarPvpOverlay.java          # fusionierte In-Game-Overlay-Logik
    │   │
    │   ├── fights/                         # ← aus matsyir.pvpperformancetracker
    │   │   ├── FightTracker.java          # ex-PvpPerformanceTrackerPlugin-Events
    │   │   ├── FightPerformance.java
    │   │   ├── Fighter.java
    │   │   ├── PvpDamageCalc.java
    │   │   ├── models/
    │   │   │   ├── AnimationData.java
    │   │   │   ├── EquipmentData.java
    │   │   │   ├── RangeAmmoData.java
    │   │   │   ├── RingData.java
    │   │   │   ├── CombatLevels.java
    │   │   │   ├── FightLogEntry.java
    │   │   │   ├── FightType.java
    │   │   │   ├── HitsplatInfo.java
    │   │   │   └── TrackedStatistic.java
    │   │   └── views/
    │   │       ├── FightsTabPanel.java    # ex-PvpPerformanceTrackerPanel
    │   │       ├── TotalStatsPanel.java
    │   │       ├── FightPerformancePanel.java
    │   │       ├── FightLogFrame.java
    │   │       ├── FightLogDetailFrame.java
    │   │       ├── FightAnalysisFrame.java
    │   │       ├── AttackSummaryFrame.java
    │   │       ├── TableComponent.java
    │   │       ├── PanelFactory.java
    │   │       └── TextComponentShadowless.java
    │   │
    │   ├── pk/                             # ← NEU: K/D & PK-Loot-Bilanz (Dink-inspiriert)
    │   │   ├── PkTracker.java              # zählt Kills/Deaths, verwaltet KillRecord-Liste
    │   │   ├── LootKeyListener.java        # validiert Gains via PlayerLootReceived + "Loot Chest"
    │   │   ├── PkBalance.java              # Aggregatwerte: totalGained, totalLost, K/D-Ratio
    │   │   ├── models/
    │   │   │   ├── KillRecord.java         # { victim, at, world, combatLevel, status=PENDING|VALIDATED|UNCLAIMED }
    │   │   │   ├── DeathRecord.java        # { killer, at, world, lostValueEstimate }
    │   │   │   └── LootKeyPayload.java     # { sourcePlayer, items[], totalGp, openedAt }
    │   │   └── views/
    │   │       └── PkTabPanel.java         # K/D-Header, Kill-Liste mit Status-Badge, Death-Liste
    │   │
    │   ├── splits/                         # ← aus com.splitmanager
    │   │   ├── SplitService.java          # ex-ManagerSession (Domänenlogik)
    │   │   ├── KnownPlayers.java          # ex-ManagerKnownPlayers
    │   │   ├── ChatDetector.java          # ex-ManagerPlugin (Chat-Parse-Logik)
    │   │   ├── PaymentProcessor.java
    │   │   ├── models/
    │   │   │   ├── Session.java
    │   │   │   ├── Kill.java
    │   │   │   ├── PendingValue.java
    │   │   │   ├── PlayerMetrics.java
    │   │   │   ├── Transfer.java
    │   │   │   ├── RecentSplitsTable.java
    │   │   │   └── WaitlistTable.java
    │   │   ├── controllers/
    │   │   │   ├── PanelActions.java
    │   │   │   └── PanelController.java
    │   │   └── views/
    │   │       ├── SplitsTabPanel.java    # ex-ManagerPanel + PanelView
    │   │       ├── PopoutView.java
    │   │       └── components/
    │   │           ├── DropdownRip.java
    │   │           ├── RemoveButtonEditor.java
    │   │           └── RemoveButtonRenderer.java
    │   │
    │   ├── common/
    │   │   ├── Formats.java               # Fusion der Helper aus beiden
    │   │   ├── InstantTypeAdapter.java
    │   │   ├── MarkdownFormatter.java
    │   │   ├── ChatStatusOverlay.java
    │   │   └── Utils.java
    │   │
    │   └── ui/
    │       └── GerWarPvpPanel.java        # JTabbedPane-Host (ein PluginPanel)
    │
    └── resources/de/gerwar/pvp/
        ├── icon.png                       # neues GerWar-Logo
        ├── skull_red.png                  # Fight-Icons übernommen (unbranded)
        └── trash-*.png                    # Split-Icons übernommen (unbranded)
```

---

## Merge-Regeln im Detail

### 1. Ein Plugin-Entrypoint, ein Config-Interface
- `GerWarPvpPlugin` registriert sich via `@PluginDescriptor(name = "GerWar PvP", description = "…", tags = {"pvp","splits","gerwar"})` und erzeugt genau **einen** `NavigationButton` mit der GerWar-Ikone.
- Event-Handler (`@Subscribe`) werden aus beiden Originalen übernommen: `onInteractingChanged`, `onAnimationChanged`, `onHitsplatApplied`, `onStatChanged`, `onFakeXpDrop`, `onGameTick`, `onPlayerDespawned`, `onGameStateChanged`, `onClientShutdown` (PPT) **plus** `onChatMessage`, `onMenuEntryAdded`, `onClanChannelChanged`, `onFriendsChatChanged`, `onWorldChanged`, `onConfigChanged` (ASM). Die Handler-Bodies werden an `fightTracker` bzw. `splitService`/`chatDetector` weiterdelegiert.
- `GerWarPvpConfig` ist **ein** Interface mit `@ConfigGroup("gerwarpvp")`. Alle ASM- und PPT-Config-Keys werden darin vereinheitlicht, gruppiert in Sektionen `fights`, `splits`, `overlay`, `gear`, `levels`, `chatDetection`, `settlement`, `general`. Doppelungen (z. B. beide hatten „enable overlay“-ähnliche Keys) werden zu einem Key konsolidiert.

### 2. Paket- und Identitäts-Rename
Jeder Import, Klassen-/Feldname, String-Literal, Overlay-Titel, Panel-Titel, Tooltip und Log-Message wird aus **beiden** Originalen umgeschrieben:

| Alt | Neu |
|---|---|
| `com.splitmanager.*` | `de.gerwar.pvp.splits.*` (bzw. `common/ui`) |
| `matsyir.pvpperformancetracker.*` | `de.gerwar.pvp.fights.*` (bzw. `common/ui`) |
| `ManagerPlugin`, `PvpPerformanceTrackerPlugin` | gelöscht; Logik in `GerWarPvpPlugin` + Services |
| `PvpPerformanceTrackerConfig`, `PluginConfig` | `GerWarPvpConfig` |
| Config-Group-Strings `"pvpperformancetracker"`, `"splitmanager"` | `"gerwarpvp"` |
| Panel-Überschriften „PvP Performance Tracker“, „Auto loot split“ | Tabs „Fights“, „Splits“ unter Titel „GerWar PvP“ |
| Data-Dir `.runelite/pvp-performance-tracker2/` | `.runelite/gerwar-pvp/` |
| Daten-Datei `FightHistoryData.json` | `fight-history.json` |

### 2b. K/D-Tracker & PK-Loot-Bilanz (neues Modul `pk/`)

Ein drittes Modul, das die bestehenden Plugins **nicht** haben. Inspiriert an der Loot-Detection von [pajlads/DinkPlugin](https://github.com/pajlads/DinkPlugin).

**Erkennungslogik (1:1 aus Dink übernommen):**

| Ereignis | Quelle |
|---|---|
| Player-Kill | `@Subscribe onHitsplatApplied(HitsplatApplied)` — wenn `actor instanceof Player`, Opfer-HP fällt auf 0 und Local-Player hat zuletzt damage applied → **Kill-Record**. Name via `((Player) actor).getName()`. Entspricht Dinks `PlayerKillNotifier`. |
| PK-Loot validiert | `@Subscribe onPlayerLootReceived(PlayerLootReceived)` **plus** `@Subscribe onLootReceived(LootReceived)` mit `event.getName().equals("Loot Chest")`. Letzteres ist der **Loot-Key-Container**, der genau dann feuert, wenn der User den Loot-Key öffnet. Siehe Dink `LootNotifier.java` (Referenz Issue #403). |
| Death | `onActorDeath` auf Local-Player, letzter Angreifer (Player) wird als Killer geloggt. |

**Datenfluss:**

```
Kill (Hitsplat → Death)
   └─► PkTracker.addKill(victim)
        KillRecord { status = PENDING, victim, at, world }
        K/D-Counter: kills++

Opens Loot Key UI (OSRS)
   └─► RuneLite fires LootReceived(name="Loot Chest", items=…)
        or PlayerLootReceived(player=…, items=…)
   └─► LootKeyListener.onLootKeyOpened(items, sourceName, gpValue)
        - sucht jüngsten PENDING KillRecord mit matching victim/source
        - falls gefunden: status = VALIDATED, totalGp = gpValue
        - falls nicht gefunden (Chest-Lootkey ohne konkretem Opfer):
          LootKeyPayload wird als eigener Validated-Eintrag geloggt
        PkBalance.totalGained += gpValue

Own Death
   └─► PkTracker.addDeath(killer, estLostValue)
        DeathRecord
        K/D-Counter: deaths++
        PkBalance.totalLost += estLostValue (optional via RuneLite DeathPrice-API)
```

**Validation-Regel (wichtig, aus User-Request):**
- Kills bleiben im Status `PENDING` bis der Loot-Key vom User **im Bank-/Interface geöffnet** wird. Erst dann zählt der Loot zur `totalGained`-Bilanz.
- `UNCLAIMED` = PENDING älter als X Minuten (konfigurierbar, default 30 min) → wird im Panel grau dargestellt.
- `VALIDATED` = Loot-Key wurde geöffnet, Wert verrechnet.

**UI (`PkTabPanel`):**
```
+-------------------------------------+
|  K  D  KD-Ratio     Net Profit      |
|  42 17   2.47     +12.3M / -3.1M    |
|                    = +9.2M          |
+-------------------------------------+
|  Kills (Liste, neueste oben):       |
|   • Zezima      2m ago  ✓ 1.2M      |
|   • Player2    15m ago  ⏳ PENDING  |
|   • Player3    45m ago  ⚠ UNCLAIMED |
+-------------------------------------+
|  Deaths:                            |
|   • Killer1    1h ago   ~400k lost  |
+-------------------------------------+
|  [Reset Session] [Export JSON]      |
+-------------------------------------+
```

**Persistenz:** `.runelite/gerwar-pvp/pk-history.json` — Liste von `KillRecord` und `DeathRecord`, bei Shutdown geschrieben.

**Kopplung mit anderen Modulen:**
- `PkTracker` und `FightTracker` teilen sich nichts — aber beide reagieren auf dieselben `HitsplatApplied`-Events, daher werden die Handler in `GerWarPvpPlugin` aufgerufen und beide Services benachrichtigt.
- `PkTracker` → `SplitService`: wenn `autoHandoffKillsToSplits` aktiv **und** `Split-Session aktiv` **und** `LootKey validated`, dann wird der GP-Wert als `PendingValue` in die Session gepackt (löst den Auto-Handoff-Punkt unten präziser: nicht mehr „bei Kill“, sondern „bei validiertem Loot-Key“ — das ist die eigentliche Wunschsemantik des Users).

### 3. Auto-Handoff PK-Loot → Splits (präzisiert)
Der Handoff hängt **nicht** mehr am Fight-Ende (Wert wäre zu diesem Zeitpunkt unbekannt), sondern an der Loot-Key-Validierung:
- In `LootKeyListener.onLootKeyOpened(payload)`:
  - Wenn `config.handoffValidatedLootToSplits() == true` **und** `splitService.hasActiveSession()`:
    - Neuer `PendingValue` mit `type = PVP`, `source = payload.sourcePlayer` (oder "Loot Chest"), `amount = payload.totalGp`.
    - `splitService.addPendingValue(pv)`.
    - `chatStatusOverlay` zeigt kurz „Loot-Key (X gp) an Split-Session übergeben“.
- Wenn `autoApplyWhenInSession = true` **und** die Session genau einen „aktiven Looter“ hat, wird `PendingValue` direkt ihm zugeordnet; sonst wartet es auf manuelle Zuweisung.
- Zusätzlich: Jeder Kill (Fight-End mit opponent.dead) triggert schon ein **Split-Session-Reminder** als `ChatStatusOverlay`-Hinweis („Kill erkannt — warte auf Loot-Key-Öffnung“), damit der User weiß, dass der Wert automatisch kommt.

### 4. Persistenz (fresh start)
- Alle Daten im neuen Verzeichnis `.runelite/gerwar-pvp/`:
  - `fight-history.json` — Liste `FightPerformance`, von `FightTracker` verwaltet (ersetzt `pvp-performance-tracker2/FightHistoryData.json`).
  - `sessions.json` — Map `<id, Session>` (vorher Config-String in ASM; wird nun Datei, sauberer).
  - `players.json` — Known Players + Alts.
- Kein Import-Mechanismus. Legacy-Dateien bleiben unberührt auf der Platte.

### 5. Tab-Panel
- `GerWarPvpPanel extends PluginPanel` enthält ein `JTabbedPane` mit vier Tabs: **Fights** (`FightsTabPanel`), **PK K/D** (`PkTabPanel`), **Splits** (`SplitsTabPanel`), **Settings** (`JPanel` mit Button „Plugin-Settings öffnen“ + Kurzbeschreibung). Optional Popout-Button für den Splits-Tab aus ASM bleibt erhalten (aber als `PopoutFrame`, nicht mehr „PopoutView“).
- Das bestehende PPT-Overlay (`PvpPerformanceTrackerOverlay`) wird zu `GerWarPvpOverlay`, bleibt in-game dasselbe, nur umbenannt.

### 6. Ressourcen
- Alle icons, die Author- oder Plugin-Namen enthalten, werden ersetzt. Generische Bilder (`skull_red.png`, `trash-solid-full.png`, `trash-arrow-up-solid-full.png`) übernehmen wir unverändert, sie tragen kein Branding.
- Ein neues `icon.png` (GerWar) für den NavigationButton. Falls noch keins existiert, wird ein Placeholder eingebaut und später vom User ausgetauscht.

### 7. Build
- `build.gradle` wie bei PPT: Java 11, Lombok 1.18.30, RuneLite `latest.release`, JUnit 4.12.
- `group 'de.gerwar.pvp'`, `version '1.0.0'`.
- `runelite-plugin.properties`:
  ```
  displayName=GerWar PvP
  author=GerWar
  description=All-in-one PvP companion for the GerWar community (fight tracking + loot splits).
  tags=pvp,lms,splits,loot,gerwar,pking
  plugins=de.gerwar.pvp.GerWarPvpPlugin
  ```

---

## Ausführungs-Reihenfolge (Milestones)

**M1 — Skelett & Build** ⬜
- Gradle-Projekt anlegen, `build.gradle`, Wrapper, `settings.gradle`, `LICENSE`, `NOTICE`, `README.md`, `runelite-plugin.properties`.
- Leerer `GerWarPvpPlugin` mit `@PluginDescriptor` und Minimal-Panel, `gradle build` grün.

**M2 — Fights-Modul portiert** ⬜
- Quellen aus Upstream `Matsyir/pvp-performance-tracker` @ `ae0e3020…` kopieren, Pakete nach `de.gerwar.pvp.fights` umschreiben, PPT-Config-Keys in `GerWarPvpConfig` (Sektionen `fights/overlay/gear/levels`) überführen.
- `FightTracker` als Service statt als `Plugin`; Events über `GerWarPvpPlugin` delegieren.
- Daten-Dir und Dateiname umstellen; keine Versions-Migrations-Methoden übernehmen.
- Fights-Tab im Panel sichtbar.

**M3 — Splits-Modul portiert** ⬜
- Quellen aus Upstream `elie605/split-session-tracker` @ `a82e8194…` kopieren, Pakete nach `de.gerwar.pvp.splits` umschreiben.
- `ManagerSession → SplitService`, `ManagerKnownPlayers → KnownPlayers`, Chat-Detection-Logik aus `ManagerPlugin` zu `ChatDetector`.
- Config-Keys in `GerWarPvpConfig` (Sektionen `splits/chatDetection/settlement/general`) vereinheitlicht.
- Splits-Tab im Panel sichtbar, Popout funktioniert.

**M4 — PK-Tracker & Loot-Key-Validation** ⬜
- `PkTracker` Service: `HitsplatApplied`-Handler erkennt Player-Kills, schreibt `KillRecord(status=PENDING)`; `onActorDeath` (eigener Tod) → `DeathRecord`.
- `LootKeyListener`: subscribt `PlayerLootReceived` (direkter PK-Loot) **und** `LootReceived` mit `name == "Loot Chest"` (geöffneter Loot-Key). Validiert passendes `KillRecord` oder erzeugt fallback-validierten Eintrag.
- `PkBalance`: aggregierte Summen `kills/deaths/KD`, `totalGained/totalLost/net`.
- `PkTabPanel`: Header + Kill- + Death-Liste mit Status-Badges, Reset-/Export-Buttons.
- Persistenz `pk-history.json`.
- Config-Sektion `pk`: Toggle `showPkTab`, `pendingTimeoutMinutes` (default 30), `trackDeathValueFromPrice` (bool).

**M5 — Auto-Handoff (Loot-Key → Splits)** ⬜
- In `LootKeyListener.onLootKeyOpened`: bei validem Wert und aktiver Split-Session → `splitService.addPendingValue(new PendingValue(PVP, source, gpValue, now))`.
- `ChatStatusOverlay`-Hinweis bei Kill („warte auf Loot-Key“) und bei erfolgreichem Handoff.
- Config-Toggle `handoffValidatedLootToSplits` (default on) unter Sektion `general`.

**M6 — Polish & Release** ⬜
- Neues Icon.
- README (DE) mit Feature-Liste, Screenshots-Platzhalter, Kurz-FAQ.
- Smoke-Test in RuneLite-Dev-Client (s. Verification).

---

## Kritische Dateien (bei Umsetzung zuerst zu bearbeiten)

- [GerWar-PvP-Plugin/build.gradle](GerWar-PvP-Plugin/build.gradle)
- [GerWar-PvP-Plugin/runelite-plugin.properties](GerWar-PvP-Plugin/runelite-plugin.properties)
- [src/main/java/de/gerwar/pvp/GerWarPvpPlugin.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/GerWarPvpPlugin.java)
- [src/main/java/de/gerwar/pvp/GerWarPvpConfig.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/GerWarPvpConfig.java)
- [src/main/java/de/gerwar/pvp/ui/GerWarPvpPanel.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/ui/GerWarPvpPanel.java)
- [src/main/java/de/gerwar/pvp/fights/FightTracker.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/fights/FightTracker.java) (+ models, views)
- [src/main/java/de/gerwar/pvp/pk/PkTracker.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/pk/PkTracker.java)
- [src/main/java/de/gerwar/pvp/pk/LootKeyListener.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/pk/LootKeyListener.java)
- [src/main/java/de/gerwar/pvp/splits/SplitService.java](GerWar-PvP-Plugin/src/main/java/de/gerwar/pvp/splits/SplitService.java) (+ models, views, ChatDetector)
- [LICENSE](GerWar-PvP-Plugin/LICENSE), [NOTICE](GerWar-PvP-Plugin/NOTICE)

---

## Wiederverwendete Funktionen (aus den Upstreams, nicht neu schreiben)

- Splits-Algorithmen: `com.splitmanager.utils.PaymentProcessor.computeDirectPaymentsStructured(List<PlayerMetrics>) → List<Transfer>`
- Splits-Persistenz-Muster: `ManagerSession.loadFromConfig / saveToConfig` → adaptieren auf Datei-Persistenz
- Chat-Parse-Logik: `ManagerPlugin.onChatMessage` + `queuePending`
- Fight-Detection: `PvpPerformanceTrackerPlugin.onInteractingChanged` + `checkForFightEnd` + `onFightEnded`
- OSRS-Damage-Engine: `matsyir.pvpperformancetracker.controllers.PvpDamageCalc` (unverändert)
- Off-Pray/KO-Heuristiken: `FightPerformance.updateKoChanceStats`, `Fighter.addAttack`, `Fighter.calculateOffPraySuccessPercentage`
- Overlay-Table-Rendering: `TrackedStatistic` Enum mit `getPanelComponent` / `getOverlayComponent` — ein elegantes Muster, wird 1:1 übernommen.
- PK-Kill-Detection-Muster: analog zu [`pajlads/DinkPlugin` → `PlayerKillNotifier`](https://github.com/pajlads/DinkPlugin/blob/master/src/main/java/dinkplugin/notifiers/PlayerKillNotifier.java) (HitsplatApplied + Player-Actor).
- Loot-Key-Validation-Muster: analog zu [`pajlads/DinkPlugin` → `LootNotifier`](https://github.com/pajlads/DinkPlugin/blob/master/src/main/java/dinkplugin/notifiers/LootNotifier.java), das `PlayerLootReceived` und `LootReceived` mit `name == "Loot Chest"` verarbeitet.

---

## Verifikation

1. `./gradlew build` im Plugin-Ordner → JAR entsteht unter `build/libs/`, Tests laufen durch.
2. RuneLite-Dev-Client (oder `runelite-client` mit externalPlugin-Arg):
   - Plugin lädt, **ein** NavigationButton mit GerWar-Icon erscheint.
   - Panel öffnet sich mit drei Tabs.
3. **Fights-Smoke-Test** (LMS oder Clan-Wars):
   - Gegner anwählen → nach erstem Swing erscheint Fight im Overlay und in der Fights-Tab-Liste.
   - KO-Chance, Off-Pray%, Expected-DMG werden aktualisiert.
   - Fight-Ende → Eintrag in History, `fight-history.json` geschrieben.
4. **PK-K/D-Smoke-Test** (Wildy / LMS):
   - Ersten Kill in Wildy → PK-Tab: Eintrag erscheint mit Status **⏳ PENDING**, K-Counter +1.
   - Loot-Key öffnen im Inventar → RuneLite feuert `LootReceived(name="Loot Chest")` → Eintrag wechselt auf **✓ VALIDATED** mit GP-Wert, `totalGained` steigt.
   - 30 Minuten warten ohne zu öffnen → Eintrag wechselt auf **⚠ UNCLAIMED**.
   - Eigener Tod durch Player → Death-Eintrag erscheint, D-Counter +1.
5. **Splits-Smoke-Test:**
   - Neue Session starten, 2 Player (ggf. Alts) eintragen.
   - Chat-Nachricht mit GP-Wert im Clanchat → PendingValue erscheint.
   - Wert einem Spieler zuweisen, Session beenden → Settlement-Tabelle listet `Transfer`s.
   - Discord-Markdown-Export funktioniert.
6. **Integrationstest Auto-Handoff:**
   - Split-Session aktiv, Kill in Wildy → ChatStatusOverlay: „warte auf Loot-Key“.
   - Loot-Key öffnen → automatisch erscheint PendingValue (Gegner-Name, GP-Wert vorbefüllt) in der Split-Session.
7. **Namens-Audit:**
   - `grep -Rni 'matsyir\|pvpperformancetracker\|splitmanager\|elie605'` auf `src/` und `resources/` → ausschließlich Treffer in `LICENSE` und `NOTICE`, sonst keine.

---

## Nicht im Scope

- Elo/MMR-Berechnung (bekanntes PPT-Feature-Request-Issue #21).
- Multi-Combat-Fight-Support (upstream-bekannte Limitierung).
- Cloud-Sync / Discord-Bot-Anbindung an die bestehende GerWar-Bot-Infrastruktur in diesem Repo — hier geht es nur um das eigenständige RuneLite-Plugin.
