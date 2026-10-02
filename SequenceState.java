package com.example.pearlcatchoptimizer.sequence;

public enum SequenceState {
    IDLE,
    PEARL_DETECTED,
    PEARL_ACTION,
    WAITING_FOR_VALID_TRANSITION,
    SELECTING_WIND_CHARGE,
    WIND_CHARGE_ACTION,
    COMPLETE,
    CANCELLED;

    /** True while a sequence is running (blocks starting another one). */
    public boolean isRunning() {
        return this != IDLE && this != COMPLETE && this != CANCELLED;
    }
}
