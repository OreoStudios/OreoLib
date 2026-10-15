package com.oreo.lib.autotest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Random;

/** Produces supported primitive, boxed, String and enum arguments without external libraries. */
final class ArgumentGenerator {
    private ArgumentGenerator() {}

    /** Non-destructive, deterministic text stress corpus. Requires @AdversarialInput. */
    private static final String[] ADVERSARIAL = {
            "",
            " ",
            "\t\n",
            String.valueOf((char) 0),
            "café",
            "🚀",
            String.valueOf((char) 0x202E) + "XYZ",
            "../etc/passwd",
            "<script>alert(1)</script>",
            "\' OR \'1\'=\'1",
            "{\"nested\":[null,true]}",
            "x".repeat(4096)
    };

    static Object[] generate(Method method, int sample, Random random) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Class<?> type = p.getType();
            TestRange range = p.getAnnotation(TestRange.class);
            boolean nullable = p.isAnnotationPresent(NullableInput.class);
            boolean hostile = p.isAnnotationPresent(AdversarialInput.class);
            if (nullable && type.isPrimitive()) {
                throw new IllegalArgumentException("@NullableInput requires a reference type: " + p);
            }
            if (hostile && type != String.class) {
                throw new IllegalArgumentException("@AdversarialInput requires String: " + p);
            }
            if (nullable && sample == 0) {
                args[i] = null;
                continue;
            }
            int caseSample = nullable ? sample - 1 : sample;
            if (hostile && caseSample < ADVERSARIAL.length) {
                args[i] = ADVERSARIAL[caseSample];
                continue;
            }
            if (range != null && !numeric(type)) {
                throw new IllegalArgumentException("@TestRange requires a numeric parameter: " + p);
            }
            if (numeric(type)) {
                double lo = range == null ? -100 : range.min();
                double hi = range == null ? 100 : range.max();
                if (!Double.isFinite(lo) || !Double.isFinite(hi) || lo > hi) {
                    throw new IllegalArgumentException("Invalid @TestRange for " + p);
                }
                if (type == byte.class || type == Byte.class) {
                    args[i] = (byte) integer(lo, hi, Byte.MIN_VALUE, Byte.MAX_VALUE, sample, random);
                } else if (type == short.class || type == Short.class) {
                    args[i] = (short) integer(lo, hi, Short.MIN_VALUE, Short.MAX_VALUE, sample, random);
                } else if (type == int.class || type == Integer.class) {
                    args[i] = (int) integer(lo, hi, Integer.MIN_VALUE, Integer.MAX_VALUE, sample, random);
                } else if (type == long.class || type == Long.class) {
                    args[i] = integer(lo, hi, Long.MIN_VALUE, Long.MAX_VALUE, sample, random);
                } else {
                    double v = sample == 0 ? lo : sample == 1 ? hi
                            : sample == 2 && lo <= 0 && hi >= 0 ? 0
                            : lo + random.nextDouble() * (hi - lo);
                    args[i] = type == float.class || type == Float.class ? (float) v : v;
                }
            } else if (type == boolean.class || type == Boolean.class) {
                args[i] = sample == 0 ? false : sample == 1 || random.nextBoolean();
            } else if (type == char.class || type == Character.class) {
                args[i] = sample == 0 ? 'a' : (char) ('a' + random.nextInt(26));
            } else if (type == String.class) {
                if (sample == 0) args[i] = "";
                else if (sample == 1) args[i] = "a";
                else {
                    int length = 1 + random.nextInt(16);
                    StringBuilder value = new StringBuilder(length);
                    for (int j = 0; j < length; j++) value.append((char) ('a' + random.nextInt(26)));
                    args[i] = value.toString();
                }
            } else if (type.isEnum()) {
                Object[] values = type.getEnumConstants();
                if (values.length == 0) throw new IllegalArgumentException("Empty enum: " + type);
                args[i] = values[sample < values.length ? sample : random.nextInt(values.length)];
            } else {
                throw new IllegalArgumentException("Unsupported parameter type " + type.getName()
                        + " in " + method + ". Provide a custom test for complex objects.");
            }
        }
        return args;
    }

    private static boolean numeric(Class<?> type) {
        return type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == float.class || type == Float.class || type == double.class || type == Double.class;
    }

    private static long integer(double lo, double hi, double typeMin, double typeMax, int sample, Random random) {
        double min = Math.ceil(Math.max(lo, typeMin));
        double max = Math.floor(Math.min(hi, typeMax));
        if (min > max) throw new IllegalArgumentException("Empty integral range: " + lo + ".." + hi);
        double chosen = sample == 0 ? min : sample == 1 ? max
                : sample == 2 && min <= 0 && max >= 0 ? 0
                : min + Math.floor(random.nextDouble() * (max - min + 1));
        return (long) Math.min(max, chosen);
    }
}
