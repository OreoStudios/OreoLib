package com.oreo.lib.autotest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * One self-consistency probe: outcome contracts, metamorphic properties, and
 * differential comparison. A violation has a stable category for shrinking.
 */
final class PrismChecks {
    record Violation(String category, String detail) {}
    private record Outcome(Object value, Throwable exception) {}

    private final Method method;
    private final Constructor<?> constructor;
    private final MetamorphicRule[] rules;
    private final Method reference;
    private final Constructor<?> referenceConstructor;
    private final Method inverse;
    private final Constructor<?> inverseConstructor;

    PrismChecks(Method method, Constructor<?> constructor) throws ReflectiveOperationException {
        this.method = method;
        this.constructor = constructor;
        Metamorphic metamorphic = method.getAnnotation(Metamorphic.class);
        this.rules = metamorphic == null ? new MetamorphicRule[0] : metamorphic.value();
        if (rules.length > 0 && method.isAnnotationPresent(ExpectThrows.class)) {
            throw new IllegalArgumentException("@Metamorphic and @ExpectThrows cannot be combined");
        }

        for (MetamorphicRule rule : rules) {
            switch (rule) {
                case COMMUTATIVE -> {
                    if (method.getParameterCount() != 2
                            || method.getParameterTypes()[0] != method.getParameterTypes()[1]
                            || method.getReturnType() == void.class) {
                        throw new IllegalArgumentException("COMMUTATIVE requires two equal parameter types and a result");
                    }
                    TestRange first = method.getParameters()[0].getAnnotation(TestRange.class);
                    TestRange second = method.getParameters()[1].getAnnotation(TestRange.class);
                    if ((first == null) != (second == null)
                            || (first != null && (first.min() != second.min() || first.max() != second.max()))) {
                        throw new IllegalArgumentException("COMMUTATIVE requires matching input ranges");
                    }
                }
                case IDEMPOTENT -> {
                    if (method.getParameterCount() != 1
                            || method.getReturnType() != method.getParameterTypes()[0]
                            || method.getReturnType() == void.class) {
                        throw new IllegalArgumentException("IDEMPOTENT requires f(T) -> T");
                    }
                }
                case DETERMINISTIC -> { /* no signature constraints */ }
            }
        }

        CompareWith comparison = method.getAnnotation(CompareWith.class);
        if (comparison == null) {
            reference = null;
            referenceConstructor = null;
        } else {
            if (method.isAnnotationPresent(ExpectThrows.class)) {
                throw new IllegalArgumentException("@CompareWith and @ExpectThrows cannot be combined");
            }
            Class<?> owner = comparison.in() == Void.class ? method.getDeclaringClass() : comparison.in();
            reference = owner.getDeclaredMethod(comparison.value(), method.getParameterTypes());
            if (reference.equals(method)) throw new IllegalArgumentException("Cannot compare a method to itself");
            if (reference.getReturnType() != method.getReturnType()) {
                throw new IllegalArgumentException("@CompareWith must use the same return type");
            }
            if (!Modifier.isStatic(reference.getModifiers())) {
                referenceConstructor = owner.getDeclaredConstructor();
                referenceConstructor.setAccessible(true);
            } else {
                referenceConstructor = null;
            }
            reference.setAccessible(true);
        }
        RoundTrip roundTrip = method.getAnnotation(RoundTrip.class);
        if (roundTrip == null) {
            inverse = null;
            inverseConstructor = null;
        } else {
            if (method.getParameterCount() != 1 || method.getReturnType() == void.class) {
                throw new IllegalArgumentException("@RoundTrip requires a unary function with a return value");
            }
            if (method.isAnnotationPresent(ExpectThrows.class)) {
                throw new IllegalArgumentException("@RoundTrip cannot be combined with @ExpectThrows");
            }
            Class<?> owner = roundTrip.in() == Void.class ? method.getDeclaringClass() : roundTrip.in();
            inverse = owner.getDeclaredMethod(roundTrip.value(), method.getReturnType());
            if (inverse.equals(method) || inverse.getReturnType() != method.getParameterTypes()[0]) {
                throw new IllegalArgumentException("@RoundTrip inverse must accept the forward output and return its input type");
            }
            if (!Modifier.isStatic(inverse.getModifiers())) {
                inverseConstructor = owner.getDeclaredConstructor();
                inverseConstructor.setAccessible(true);
            } else {
                inverseConstructor = null;
            }
            inverse.setAccessible(true);
        }
    }

    Violation check(Object[] args) {
        Outcome first = call(method, constructor, args);
        ExpectThrows expected = method.getAnnotation(ExpectThrows.class);
        if (expected != null) {
            if (first.exception() == null) {
                return new Violation("EXPECTED_EXCEPTION", "Expected " + expected.value().getName() + " but returned");
            }
            if (!expected.value().isInstance(first.exception())) {
                return exception(first.exception());
            }
            return null;
        }
        if (first.exception() != null) return exception(first.exception());

        if (method.isAnnotationPresent(NotNullResult.class) && first.value() == null) {
            return new Violation("NOT_NULL", "@NotNullResult contract failed");
        }
        ResultRange range = method.getAnnotation(ResultRange.class);
        if (range != null && (!(first.value() instanceof Number n)
                || !Double.isFinite(n.doubleValue())
                || n.doubleValue() < range.min() || n.doubleValue() > range.max())) {
            return new Violation("RESULT_RANGE", "@ResultRange failed: "
                    + first.value() + " not in " + range.min() + ".." + range.max());
        }

        for (MetamorphicRule rule : rules) {
            Outcome second;
            Object[] transformed = args.clone();
            switch (rule) {
                case COMMUTATIVE -> {
                    transformed[0] = args[1];
                    transformed[1] = args[0];
                    second = call(method, constructor, transformed);
                }
                case IDEMPOTENT -> {
                    transformed[0] = first.value();
                    second = call(method, constructor, transformed);
                }
                case DETERMINISTIC -> second = call(method, constructor, transformed);
                default -> throw new IllegalStateException("Unknown metamorphic rule " + rule);
            }
            if (second.exception() != null || !Objects.deepEquals(first.value(), second.value())) {
                return new Violation("METAMORPHIC_" + rule.name(),
                        rule + " mismatch: initial=" + display(first)
                        + ", transformed args=" + Arrays.deepToString(transformed)
                        + ", transformed result=" + display(second));
            }
        }

        if (reference != null) {
            Outcome other = call(reference, referenceConstructor, args);
            if (other.exception() != null || !Objects.deepEquals(first.value(), other.value())) {
                return new Violation("DIFFERENTIAL",
                        "Behavioral drift against " + reference.toGenericString()
                        + ": candidate=" + display(first) + ", reference=" + display(other));
            }
        }
        if (inverse != null) {
            Outcome reconstructed = call(inverse, inverseConstructor, new Object[] {first.value()});
            if (reconstructed.exception() != null || !Objects.deepEquals(args[0], reconstructed.value())) {
                return new Violation("ROUND_TRIP",
                        "Round-trip invariant failed: input=" + Arrays.deepToString(args)
                        + ", forward=" + display(first)
                        + ", inverse=" + display(reconstructed));
            }
        }
        return null;
    }

    private static Violation exception(Throwable error) {
        return new Violation("EXCEPTION:" + error.getClass().getName(),
                "Unexpected exception: " + error.getClass().getName() + ": " + error.getMessage());
    }

    private static String display(Outcome outcome) {
        if (outcome.exception() != null) return "throws " + outcome.exception().getClass().getName()
                + ": " + outcome.exception().getMessage();
        if (outcome.value() instanceof Object[] values) return Arrays.deepToString(values);
        if (outcome.value() instanceof int[] values) return Arrays.toString(values);
        if (outcome.value() instanceof long[] values) return Arrays.toString(values);
        if (outcome.value() instanceof double[] values) return Arrays.toString(values);
        return String.valueOf(outcome.value());
    }

    private static Outcome call(Method target, Constructor<?> constructor, Object[] args) {
        try {
            Object instance = constructor == null ? null : constructor.newInstance();
            return new Outcome(target.invoke(instance, args), null);
        } catch (InvocationTargetException e) {
            return new Outcome(null, e.getCause());
        } catch (ReflectiveOperationException | RuntimeException e) {
            return new Outcome(null, e);
        }
    }
}
