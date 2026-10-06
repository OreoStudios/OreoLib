package com.oreo.lib;

/** Small math helpers for the common cases Java makes verbose. */
public final class Maths {
    private Maths() {}

    public static int clamp(int value, int min, int max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static long clamp(long value, long min, long max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    /** Fraction of value between min and max, clamped to 0..1. */
    public static float fraction(float value, float min, float max) {
        if (max == min) return 0f;
        return clamp((value - min) / (max - min), 0f, 1f);
    }
}
