package com.oreo.lib;

import java.util.Scanner;

/**
 * Short console input/output helpers.
 */
public final class Console {
    private static final Scanner SCANNER = new Scanner(System.in);

    private Console() {}

    public static String ask(String prompt) {
        print(prompt);
        return SCANNER.nextLine();
    }

    public static int askInt(String prompt) {
        while (true) {
            String input = ask(prompt);
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ignored) {
                warn("Please enter a valid integer.");
            }
        }
    }

    public static boolean confirm(String prompt) {
        String input = ask(prompt + " [y/N] ").trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }

    /** Equivalent to System.out.println(value). */
    public static void out(Object value) {
        System.out.println(value);
    }

    /** Prints several values separated by spaces and adds a newline. */
    public static void out(Object... values) {
        System.out.println(join(values));
    }

    /** Equivalent to System.out.print(value). */
    public static void print(Object value) {
        System.out.print(value);
    }

    /** Equivalent to System.out.printf(format, args). */
    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    /** Convenience formatted line. */
    public static void outf(String format, Object... args) {
        System.out.printf(format + "%n", args);
    }

    public static void info(Object value) {
        System.out.println("[INFO] " + value);
    }

    public static void success(Object value) {
        System.out.println("[SUCCESS] " + value);
    }

    public static void warn(Object value) {
        System.err.println("[WARN] " + value);
    }

    public static void error(Object value) {
        System.err.println("[ERROR] " + value);
    }

    /** Kept for compatibility with OreoLib 1.0.0. */
    public static void println(Object value) {
        out(value);
    }

    private static String join(Object[] values) {
        if (values == null) return "null";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(' ');
            result.append(String.valueOf(values[i]));
        }
        return result.toString();
    }
}
