# Pearl Catch Optimizer

Client-side Fabric mod for Minecraft Java 1.21.11. Hold an Ender Pearl and **left click**:
the pearl is thrown, the real hotbar slot switches to the first Wind Charge in slots 0-8,
and the Wind Charge is used. Right click stays 100% vanilla.

## How it stays vanilla
No Mixins, no rendering code. The mod only
1. reads the attack key (left click), and consumes the click only when the sequence really starts;
2. performs both item uses by registering one press of the player's own **Use Item** key, so
   Minecraft runs its normal `doItemUse` path (swing, first-person animation, cooldowns, packets);
3. switches the slot with `PlayerInventory#setSelectedSlot`, so the HUD, held-item transition and
   server slot sync are Minecraft's own.

Success of each action is detected through Minecraft's cooldown manager (never modified).
With `sequenceDelayTicks = 0` the Wind Charge is selected at the end of the pearl-throw tick and used
on the next tick, the earliest point Minecraft's own input loop allows.

## Behaviour notes
- No Wind Charge in hotbar (and `windChargeRequired=true`): nothing happens, left click is vanilla.
- Cancels safely on: screen opened, death, dimension change, disconnect, slot change, rejected action, timeout.
- Holding left click on a block can still start vanilla block breaking (only the click *event* is consumed).
- Many multiplayer servers forbid input macros. Check the rules before using it online.

## Config (`config/pearlcatchoptimizer.properties`)
`enabled`, `pearlCatchEnabled`, `sequenceDelayTicks` (0 = minimum), `windChargeRequired`, `debug`, `maxWaitTicks`.

## Build
Needs JDK 21 and the Gradle wrapper files (see below).
```
./gradlew build      # jar in build/libs/
```
Wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` could not be generated offline.
Run `gradle wrapper --gradle-version 8.14.3` once, or copy them from the Fabric example mod.
`gradle-wrapper.properties` is already included.

Versions (gradle.properties): MC 1.21.11, Yarn 1.21.11+build.4, Loader 0.18.1, Fabric API 0.141.3+1.21.11, Loom 1.14.
If a version is not found, take the current ones from https://fabricmc.net/develop/.
