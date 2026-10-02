package com.example.pearlcatchoptimizer.sequence;

import com.example.pearlcatchoptimizer.PearlCatchOptimizerClient;
import com.example.pearlcatchoptimizer.config.PearlCatchConfig;
import com.example.pearlcatchoptimizer.handler.PearlHandler;
import com.example.pearlcatchoptimizer.handler.WindChargeHandler;
import com.example.pearlcatchoptimizer.inventory.HotbarManager;
import com.example.pearlcatchoptimizer.timing.SequenceTimer;
import com.example.pearlcatchoptimizer.util.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

/**
 * State machine: Pearl use -> wait until vanilla accepted it -> real hotbar switch -> Wind Charge use.
 *
 * Timeline with sequenceDelayTicks = 0:
 *   tick N  START : click consumed, pearl use-key press injected, vanilla throws the pearl this tick
 *   tick N  END   : pearl cooldown seen (= accepted) -> select real Wind Charge slot -> inject use press
 *   tick N+1      : vanilla runs the Wind Charge use while rendering the normal held-item transition
 *   tick N+1 END  : Wind Charge cooldown seen -> COMPLETE
 */
public final class PearlCatchSequence {
    /** Ticks to wait for vanilla to accept an already-injected Wind Charge use. */
    private static final int POST_PRESS_TIMEOUT = 3;

    private final PearlCatchConfig config;
    private final PearlHandler pearlHandler = new PearlHandler();
    private final WindChargeHandler windHandler = new WindChargeHandler();
    private final SequenceTimer timer = new SequenceTimer();

    private SequenceState state = SequenceState.IDLE;
    private ClientWorld world;
    private int pearlSlot = -1;
    private int windSlot = -1;
    private int ticksInState;
    private boolean windUsePressed;

    public PearlCatchSequence(PearlCatchConfig config) {
        this.config = config;
    }

    public boolean isActive() {
        return state.isRunning();
    }

    public boolean canStart(MinecraftClient client) {
        if (isActive() || !ItemUtils.isGameplayReady(client)) return false;
        ClientPlayerEntity player = client.player;
        if (player.isUsingItem()) return false;
        ItemStack held = player.getMainHandStack();
        if (!ItemUtils.isPearl(held) || ItemUtils.isOnCooldown(player, held)) return false;
        // No Wind Charge in hotbar: do nothing, left click stays 100% vanilla.
        return !config.windChargeRequired || HotbarManager.findWindChargeSlot(player) >= 0;
    }

    /** Call at START of a tick. */
    public boolean start(MinecraftClient client) {
        if (!canStart(client)) return false;
        ClientPlayerEntity player = client.player;
        enter(SequenceState.PEARL_DETECTED);
        world = client.world;
        pearlSlot = player.getInventory().getSelectedSlot();
        windSlot = HotbarManager.findWindChargeSlot(player);
        windUsePressed = false;

        enter(SequenceState.PEARL_ACTION);
        if (!pearlHandler.perform(client)) {
            finish(SequenceState.CANCELLED, "pearl action rejected");
            return false;
        }
        enter(SequenceState.WAITING_FOR_VALID_TRANSITION);
        return true;
    }

    /** Call at END of every tick. */
    public void onEndTick(MinecraftClient client) {
        if (!isActive()) return;
        if (!ItemUtils.isGameplayReady(client) || client.world != world) {
            cancel("left normal gameplay (screen / death / dimension / disconnect)");
            return;
        }
        for (int i = 0; i < 4 && isActive(); i++) {
            if (!step(client)) break;
        }
    }

    public void cancel(String reason) {
        if (isActive()) finish(SequenceState.CANCELLED, reason);
    }

    /** @return true if the state changed and the next state may be evaluated in this same tick. */
    private boolean step(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        switch (state) {
            case WAITING_FOR_VALID_TRANSITION -> {
                if (player.getInventory().getSelectedSlot() != pearlSlot) {
                    cancel("player changed slot");
                    return false;
                }
                if (pearlHandler.wasAccepted(player)) {
                    if (windSlot < 0) { // only possible when windChargeRequired = false
                        finish(SequenceState.COMPLETE, "pearl only (no wind charge)");
                        return false;
                    }
                    timer.start(config.sequenceDelayTicks);
                    enter(SequenceState.SELECTING_WIND_CHARGE);
                    return true;
                }
                if (++ticksInState > config.maxWaitTicks) cancel("pearl not accepted by Minecraft");
                return false;
            }
            case SELECTING_WIND_CHARGE -> {
                if (!timer.isReady()) {
                    timer.tick();
                    return false;
                }
                int slot = HotbarManager.findWindChargeSlot(player); // re-check, inventory may have changed
                if (slot < 0) {
                    cancel("wind charge no longer in hotbar");
                    return false;
                }
                if (!HotbarManager.select(player, slot)) {
                    cancel("hotbar selection failed");
                    return false;
                }
                windSlot = slot;
                timer.start(config.sequenceDelayTicks);
                enter(SequenceState.WIND_CHARGE_ACTION);
                return true;
            }
            case WIND_CHARGE_ACTION -> {
                if (player.getInventory().getSelectedSlot() != windSlot) {
                    cancel("player changed slot");
                    return false;
                }
                if (!windUsePressed) {
                    if (!timer.isReady()) {
                        timer.tick();
                        return false;
                    }
                    if (ItemUtils.isOnCooldown(player, player.getMainHandStack())) {
                        // Respect Minecraft's cooldown; wait for it (bounded).
                        if (++ticksInState > config.maxWaitTicks) cancel("wind charge cooldown timeout");
                        return false;
                    }
                    if (!windHandler.perform(client)) {
                        cancel("wind charge action rejected");
                        return false;
                    }
                    windUsePressed = true;
                    ticksInState = 0;
                    return false;
                }
                if (windHandler.wasAccepted(player)) {
                    finish(SequenceState.COMPLETE, "done");
                } else if (++ticksInState >= POST_PRESS_TIMEOUT) {
                    cancel("wind charge not accepted by Minecraft");
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    private void enter(SequenceState next) {
        state = next;
        ticksInState = 0;
        PearlCatchOptimizerClient.debug("state -> {}", next);
    }

    private void finish(SequenceState terminal, String reason) {
        state = terminal;
        world = null;
        pearlSlot = -1;
        windSlot = -1;
        windUsePressed = false;
        ticksInState = 0;
        PearlCatchOptimizerClient.debug("sequence {}: {}", terminal, reason);
    }
}
