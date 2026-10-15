# 67client 2.0

Utility-klient til Minecraft **1.21.11** (Fabric, Java 21).

## Funktioner
- **ClickGUI** (Højre Shift): flydende vindue med sidebar, glidende kontakter, smooth scroll, 5 temaer (inkl. Rainbow) og partikler. Klik = til/fra, højreklik = indstillinger
- **Flytbar HUD**: Client → "Edit HUD layout", træk elementerne hvor du vil
- **Config gemmes automatisk** i `config/67client.json`
- Pr.-element farve og baggrund

### HUD-moduler
Watermark, Chunk Map (sus/prime chunks), FPS, Ping, CPS, Speed, Coordinates (+retning, nether-koordinater), Biome, Clock,
Memory, Keystrokes, Armor (med holdbarhed), Potions

### Utility
ESP (3D/2D), Storage ESP, Hole ESP, Sus Chunks (chunk-grænser i verden), NoFall, Flight (G; WASD + Mellemrum/Shift, justerbar hastighed), Auto Sprint, Fullbright (kun visuelt), Zoom (hold C, justerbar FOV)

### Taster
| Tast | Handling |
|------|----------|
| Højre Shift | Åbn ClickGUI |
| H | Skjul/vis HUD |
| C (hold) | Zoom |
| G | Slå Flight til/fra |
| F4 | Freecam (indbygget Freecam-mod) |

Alle taster kan ændres under Indstillinger → Styring → "67client".

## Byg
    gradle wrapper        # hvis der ikke er en wrapper
    ./gradlew runClient   # test
    ./gradlew build       # jar i build/libs/

Læg jar-filen i `mods/` sammen med Fabric API til 1.21.11.
Versionerne i gradle.properties kan tjekkes på https://fabricmc.net/develop

## Versioner (opdateret)
| Komponent | Version |
|-----------|---------|
| Minecraft | 1.21.11 |
| Fabric API | 0.141.4+1.21.11 |
| Fabric Loader | 0.19.3 |
| Fabric Loom | 1.17-SNAPSHOT (`net.fabricmc.fabric-loom-remap`) |
| Yarn | 1.21.11+build.4 |
| Gradle | nyeste (`gradle wrapper --gradle-version latest`) |
| Java | 21 |

GitHub Actions-workflow ligger i `.github/workflows/build.yml` og bygger med nyeste Gradle uden wrapper.

## Freecam (indbygget)
67client indeholder **Freecam** af hashalite og MattSturgeon (MIT-licens),
https://modrinth.com/mod/freecam, pakket ind som jar-in-jar. Tast: F4 (kan ændres under Styring).
Har du Freecam installeret i forvejen, så fjern den fra `mods`.
