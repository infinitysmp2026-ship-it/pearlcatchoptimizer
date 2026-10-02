package com.example.pearlcatchoptimizer.timing;

/**
 * Tick-based gate. start(0) is ready immediately = "minimum valid transition":
 * the sequence advances as soon as Minecraft's own state allows (never artificially delayed).
 * Cooldowns are never touched; those are checked via Minecraft's cooldown manager.
 */
public final class SequenceTimer {
    private int remaining;

    public void start(int delayTicks) {
        remaining = Math.max(0, delayTicks);
    }

    public boolean isReady() {
        return remaining <= 0;
    }

    public void tick() {
        if (remaining > 0) remaining--;
    }
}
