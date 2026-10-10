# T0Rbem client (singleplayer)

Schematic builder, area miner og udforsket-chunks-kort.

## Bygge
1. Ret versionerne i `gradle.properties`, så de matcher din Minecraft-version (https://fabricmc.net/develop/).
2. Kør `gradle build` (eller `./gradlew build`, hvis du har tilføjet Gradle wrapper).
3. Din .jar ligger i `build/libs/` (brug filen uden `-sources`).

## Brug
- Right Shift eller `/t0rbem` åbner menuen.
- End = nødstop.
- Læg schematics (.litematic, .schem, .nbt) i `schematics/` i din spilmappe.
- Miner og builder virker kun i singleplayer og stopper af sig selv på servere.
