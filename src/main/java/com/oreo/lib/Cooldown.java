package com.oreo.lib;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple key-based cooldown utility. */
public final class Cooldown<K> {
    private final long cooldownMillis;
    private final Map<K, Long> nextUse = new ConcurrentHashMap<>();

    Cooldown(Duration duration) {
        Checks.notNull(duration, "duration");
        Checks.require(!duration.isNegative() && !duration.isZero(), "cooldown must be > 0");
        this.cooldownMillis = duration.toMillis();
    }

    /** Returns true and starts the cooldown when ready, otherwise false. */
    public boolean use(K key) {
        long now = System.currentTimeMillis();
        Long blockedUntil = nextUse.get(key);
        if (blockedUntil != null && blockedUntil > now) return false;
        nextUse.put(key, now + cooldownMillis);
        return true;
    }

    public boolean ready(K key) {
        return remainingMillis(key) <= 0;
    }

    public long remainingMillis(K key) {
        Long blockedUntil = nextUse.get(key);
        if (blockedUntil == null) return 0;
        long remaining = blockedUntil - System.currentTimeMillis();
        if (remaining <= 0) {
            nextUse.remove(key);
            return 0;
        }
        return remaining;
    }

    public Duration remaining(K key) {
        return Duration.ofMillis(remainingMillis(key));
    }

    public void reset(K key) {
        nextUse.remove(key);
    }

    public void clear() {
        nextUse.clear();
    }
}
