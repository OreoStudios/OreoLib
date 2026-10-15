package com.oreo.lib.autotest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Bounded, deterministic counterexample reducer.
 * Accept a candidate only when it reproduces the same violation category.
 * This is a heuristic local minimum, not proof of a globally minimal input.
 */
final class PrismShrinker {
    private final Method method;
    private final PrismChecks checks;
    private final int budget;

    PrismShrinker(Method method, PrismChecks checks, int budget) {
        this.method = method;
        this.checks = checks;
        this.budget = budget;
    }

    Object[] minimize(Object[] original, String category) {
        Object[] best = original.clone();
        int remaining = budget;
        boolean improved;
        do {
            improved = false;
            for (int i = 0; i < best.length && remaining > 0; i++) {
                for (Object value : candidates(method.getParameters()[i], best[i])) {
                    if (remaining <= 0) break;
                    if (Objects.deepEquals(value, best[i])) continue;
                    Object[] attempt = best.clone();
                    attempt[i] = value;
                    remaining--;
                    PrismChecks.Violation failure = checks.check(attempt);
                    if (failure != null && failure.category().equals(category)) {
                        best = attempt;
                        improved = true;
                        break;
                    }
                }
            }
        } while (improved && remaining > 0);
        return best;
    }

    private static List<Object> candidates(Parameter parameter, Object value) {
        List<Object> values = new ArrayList<>();
        if (value == null) return values;
        Class<?> type = parameter.getType();
        if (value instanceof String s) {
            if (!s.isEmpty()) {
                values.add("");
                if (s.length() > 1) values.add(s.substring(0, s.length() / 2));
                if (s.length() > 1) values.add(s.substring(0, s.length() - 1));
                if (s.length() > 1) values.add("a");
            }
        } else if (value instanceof Boolean b) {
            if (b) values.add(false);
        } else if (value instanceof Character c) {
            if (c != 'a') values.add('a');
        } else if (value instanceof Enum<?> e) {
            Object[] constants = e.getDeclaringClass().getEnumConstants();
            if (constants.length > 0 && constants[0] != e) values.add(constants[0]);
        } else if (value instanceof Number n) {
            TestRange range = parameter.getAnnotation(TestRange.class);
            double lo = range == null ? -100 : range.min();
            double hi = range == null ? 100 : range.max();
            double current = n.doubleValue();
            double target = Math.max(lo, Math.min(hi, 0));
            if (current == target) return values;
            addNumeric(values, type, target, lo, hi);
            double halfway = target + (current - target) / 2;
            if (integral(type)) halfway = current > target ? Math.floor(halfway) : Math.ceil(halfway);
            addNumeric(values, type, halfway, lo, hi);
            addNumeric(values, type, current + (current > target ? -1 : 1), lo, hi);
        }
        return values;
    }

    private static boolean integral(Class<?> type) {
        return type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class;
    }

    private static void addNumeric(List<Object> out, Class<?> type, double n, double lo, double hi) {
        if (!Double.isFinite(n) || n < lo || n > hi) return;
        Object value;
        if (type == byte.class || type == Byte.class) value = (byte) n;
        else if (type == short.class || type == Short.class) value = (short) n;
        else if (type == int.class || type == Integer.class) value = (int) n;
        else if (type == long.class || type == Long.class) value = (long) n;
        else if (type == float.class || type == Float.class) value = (float) n;
        else if (type == double.class || type == Double.class) value = n;
        else return;
        if (!out.contains(value)) out.add(value);
    }
}
