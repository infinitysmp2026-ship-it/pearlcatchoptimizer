package com.example.pearlcatchoptimizer.inventory;

import com.example.pearlcatchoptimizer.util.ItemUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;

/**
 * Finds and selects the real Wind Charge hotbar slot.
 * Selection rule: the FIRST slot in 0..8 that contains a Wind Charge wins (deterministic).
 * The selection uses the real PlayerInventory selected slot, so the HUD, held-item
 * transition and server sync (done by vanilla) all behave exactly as for a manual switch.
 */
public final class HotbarManager {
    private HotbarManager() {}

    public static int findWindChargeSlot(ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < 9; slot++) {
            if (ItemUtils.isWindCharge(inv.getStack(slot))) {
                return slot;
            }
        }
        return -1;
    }

    public static boolean select(ClientPlayerEntity player, int slot) {
        if (slot < 0 || slot > 8) {
            return false;
        }
        PlayerInventory inv = player.getInventory();
        if (inv.getSelectedSlot() != slot) {
            inv.setSelectedSlot(slot);
        }
        return inv.getSelectedSlot() == slot;
    }
}
