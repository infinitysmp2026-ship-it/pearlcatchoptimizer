package com.example.pearlcatchoptimizer.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Performs an item use by registering one press of the player's own "Use Item" key.
 * Vanilla's handleInputEvents() then runs its normal doItemUse() path, which includes
 * interactionManager.interactItem(), the hand swing and the first-person equip/reset animation.
 * Nothing is rendered or animated by this mod.
 */
public final class UseKey {
    private UseKey() {}

    public static boolean press(MinecraftClient client) {
        KeyBinding use = client.options.useKey;
        if (use.isUnbound()) {
            return false;
        }
        KeyBinding.onKeyPressed(InputUtil.fromTranslationKey(use.getBoundKeyTranslationKey()));
        return true;
    }
}
