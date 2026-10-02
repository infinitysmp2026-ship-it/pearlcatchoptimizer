package com.example.pearlcatchoptimizer.handler;

import com.example.pearlcatchoptimizer.util.ItemUtils;
import com.example.pearlcatchoptimizer.util.UseKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

/** Throws the held Ender Pearl via vanilla's use-key path (no custom rendering/animation). */
public final class PearlHandler {
    private ItemStack thrown = ItemStack.EMPTY;

    /** Must be called at START of a tick so vanilla handles the press in that same tick. */
    public boolean perform(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return false;
        ItemStack held = player.getMainHandStack();
        if (!ItemUtils.isPearl(held) || ItemUtils.isOnCooldown(player, held)) return false;
        thrown = held.copy(); // copy: the real stack may be emptied if it was the last pearl
        return UseKey.press(client);
    }

    /** Vanilla starts the pearl cooldown when the throw is accepted. */
    public boolean wasAccepted(ClientPlayerEntity player) {
        return ItemUtils.isOnCooldown(player, thrown);
    }
}
