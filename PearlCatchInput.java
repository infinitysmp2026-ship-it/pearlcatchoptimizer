package com.example.pearlcatchoptimizer.input;

import com.example.pearlcatchoptimizer.config.PearlCatchConfig;
import com.example.pearlcatchoptimizer.sequence.PearlCatchSequence;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

/**
 * LEFT CLICK detection. Only the attack key is read; the use key (RIGHT CLICK) is never
 * consumed or inspected, so right-click pearls stay fully vanilla.
 *
 * The click is consumed ONLY when the sequence actually starts. In every other case
 * (other item, no Wind Charge, pearl on cooldown, screen open...) the key is untouched and
 * vanilla handles the click normally.
 */
public final class PearlCatchInput {
    private final PearlCatchConfig config;
    private final PearlCatchSequence sequence;

    public PearlCatchInput(PearlCatchConfig config, PearlCatchSequence sequence) {
        this.config = config;
        this.sequence = sequence;
    }

    /** Registered on START_CLIENT_TICK (before vanilla handleInputEvents). */
    public void onStartTick(MinecraftClient client) {
        if (!config.enabled || !config.pearlCatchEnabled) return;
        KeyBinding attack = client.options.attackKey;

        if (sequence.isActive()) {
            // Repeated clicks during a running sequence are swallowed: no duplicates.
            if (client.currentScreen == null) drain(attack);
            return;
        }
        if (!sequence.canStart(client)) return;
        if (!attack.wasPressed()) return;
        drain(attack);
        sequence.start(client);
    }

    private static void drain(KeyBinding key) {
        //noinspection StatementWithEmptyBody
        while (key.wasPressed()) { }
    }
}
