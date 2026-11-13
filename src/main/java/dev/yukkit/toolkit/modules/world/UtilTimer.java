package dev.yukkit.toolkit.modules.world;

/** Holds the client tick speed factor read by {@code TimerMixin}. */
public final class UtilTimer {
    private static volatile float speed = 1.0f;

    private UtilTimer() {}

    public static float speed() { return speed; }

    public static void setSpeed(float value) {
        speed = Float.isFinite(value) && value > 0.0f ? Math.min(value, 20.0f) : 1.0f;
    }

    public static void reset() { speed = 1.0f; }
}
