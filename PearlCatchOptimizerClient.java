package com.example.pearlcatchoptimizer;

import com.example.pearlcatchoptimizer.config.PearlCatchConfig;
import com.example.pearlcatchoptimizer.input.PearlCatchInput;
import com.example.pearlcatchoptimizer.sequence.PearlCatchSequence;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point. Wires config -> input -> sequence.
 * No Mixins are used: input is read through KeyBinding, actions are performed by
 * feeding Minecraft's own use-key path, so vanilla rendering/animation is untouched.
 */
public class PearlCatchOptimizerClient implements ClientModInitializer {
    public static final String MOD_ID = "pearlcatchoptimizer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static PearlCatchConfig config = new PearlCatchConfig();

    public static PearlCatchConfig config() {
        return config;
    }

    public static void debug(String message, Object... args) {
        if (config.debug) {
            LOGGER.info("[debug] " + message, args);
        }
    }

    @Override
    public void onInitializeClient() {
        config = PearlCatchConfig.load();

        PearlCatchSequence sequence = new PearlCatchSequence(config);
        PearlCatchInput input = new PearlCatchInput(config, sequence);

        // START of tick: before vanilla handleInputEvents(), so we can see/consume the click
        // and inject the use-key press that vanilla then processes in the same tick.
        ClientTickEvents.START_CLIENT_TICK.register(input::onStartTick);
        // END of tick: after vanilla processed input, so we can observe the result and advance.
        ClientTickEvents.END_CLIENT_TICK.register(sequence::onEndTick);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> sequence.cancel("join"));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> sequence.cancel("disconnect"));

        LOGGER.info("Pearl Catch Optimizer initialized (enabled={})", config.enabled);
    }
}
