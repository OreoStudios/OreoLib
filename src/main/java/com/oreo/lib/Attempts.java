package com.oreo.lib;

import java.time.Duration;

public final class Attempts {
    private Attempts() {}

    public static <T> T retry(int attempts, Duration delay, ThrowingSupplier<T> supplier) {
        Checks.require(attempts > 0, "attempts must be > 0");
        Exception last = null;

        for (int i = 0; i < attempts; i++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                last = e;
                if (i + 1 < attempts && !delay.isZero() && !delay.isNegative()) {
                    Tasks.sleep(delay.toMillis());
                }
            }
        }

        throw new OreoException("Operation failed after " + attempts + " attempts", last);
    }
}
