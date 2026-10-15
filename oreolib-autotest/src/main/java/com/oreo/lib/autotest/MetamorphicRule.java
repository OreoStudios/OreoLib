package com.oreo.lib.autotest;

/**
 * Relations checked on fresh instances of an explicitly opted-in target.
 * All rules require pure, deterministic, isolated functions and a meaningful
 * equals() implementation on returned objects.
 */
public enum MetamorphicRule {
    /** f(a, b) == f(b, a), for two same-typed arguments. */
    COMMUTATIVE,
    /** f(f(a)) == f(a), for one argument whose type matches the return type. */
    IDEMPOTENT,
    /** Two invocations with identical inputs must produce equivalent outcomes. */
    DETERMINISTIC
}
