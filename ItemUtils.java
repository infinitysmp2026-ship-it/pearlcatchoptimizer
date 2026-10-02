package com.example.pearlcatchoptimizer.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Reusable item / state checks. */
public final class ItemUtils {
    private ItemUtils() {}

    public static boolean isPearl(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(Items.ENDER_PEARL);
    }

    public static boolean isWindCharge(ItemStack stack) {
        return !stack.isEmpty() && stack.isOf(Items.WIND_CHARGE);
    }

    /** True when the player is in normal gameplay (no screen, alive, connected, not spectating). */
    public static boolean isGameplayReady(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        return player != null
                && client.world != null
                && client.interactionManager != null
                && client.getNetworkHandler() != null
                && client.currentScreen == null
                && player.isAlive()
                && !player.isSpectator();
    }

    /** Uses Minecraft's own cooldown manager; we never touch or reset cooldowns. */
    public static boolean isOnCooldown(ClientPlayerEntity player, ItemStack stack) {
        return !stack.isEmpty() && player.getItemCooldownManager().isCoolingDown(stack);
    }
}
