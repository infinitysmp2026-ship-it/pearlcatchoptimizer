package com.example.pearlcatchoptimizer.handler;

import com.example.pearlcatchoptimizer.util.ItemUtils;
import com.example.pearlcatchoptimizer.util.UseKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

/** Uses the Wind Charge that is ALREADY the selected, held item. Never touches inventory. */
public final class WindChargeHandler {
    private ItemStack used = ItemStack.EMPTY;

    public boolean perform(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        ItemStack held = player.getMainHandStack();
        if (!ItemUtils.isWindCharge(held) || ItemUtils.isOnCooldown(player, held)) return false;
        used = held.copy();
        return UseKey.press(client);
    }

    public boolean wasAccepted(ClientPlayerEntity player) {
        return ItemUtils.isOnCooldown(player, used);
    }
}
