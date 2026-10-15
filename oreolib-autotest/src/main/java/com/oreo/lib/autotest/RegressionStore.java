package com.oreo.lib.autotest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/** Text-based reproduction fixtures keyed by method signature, never Java deserialization. */
final class RegressionStore {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private final Path directory;

    RegressionStore(Path directory) { this.directory = directory; }

    List<Object[]> load(Method method) throws IOException {
        Path path = path(method);
        List<Object[]> cases = new ArrayList<>();
        if (!Files.exists(path)) return cases;
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            if (line.isBlank()) continue;
            String[] parts = line.equals("-") && method.getParameterCount() == 0
                    ? new String[0] : line.split("\\|", -1);
            Class<?>[] types = method.getParameterTypes();
            if (parts.length != types.length) {
                throw new IOException("Corrupted regression case in " + path + ": " + line);
            }
            Object[] args = new Object[parts.length];
            try {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals("~")) { args[i] = null; continue; }
                    String encoded = parts[i].startsWith(".") ? parts[i].substring(1) : parts[i];
                    String raw = new String(DECODER.decode(encoded), StandardCharsets.UTF_8);
                    args[i] = decode(types[i], raw);
                }
            } catch (RuntimeException e) {
                throw new IOException("Cannot decode regression case in " + path, e);
            }
            cases.add(args);
        }
        return cases;
    }

    void record(Method method, Object[] args) throws IOException {
        List<String> parts = new ArrayList<>();
        for (Object arg : args) {
            parts.add(arg == null ? "~" : "." + ENCODER.encodeToString(
                    String.valueOf(arg instanceof Enum<?> e ? e.name() : arg).getBytes(StandardCharsets.UTF_8)));
        }
        String line = args.length == 0 ? "-" : String.join("|", parts);
        Path path = path(method);
        Files.createDirectories(directory);
        if (Files.exists(path) && Files.readAllLines(path, StandardCharsets.UTF_8).contains(line)) return;
        Files.writeString(path, line + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private Path path(Method method) {
        byte[] sha;
        try {
            sha = MessageDigest.getInstance("SHA-256").digest(method.toGenericString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Missing SHA-256", e);
        }
        return directory.resolve(HexFormat.of().formatHex(sha) + ".cases");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object decode(Class<?> type, String value) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == short.class || type == Short.class) return Short.parseShort(value);
        if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == boolean.class || type == Boolean.class) {
            if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Invalid boolean");
            return Boolean.parseBoolean(value);
        }
        if (type == char.class || type == Character.class) {
            if (value.length() != 1) throw new IllegalArgumentException("Invalid char");
            return value.charAt(0);
        }
        if (type.isEnum()) return Enum.valueOf((Class<? extends Enum>) type, value);
        throw new IllegalArgumentException("Unsupported saved type: " + type.getName());
    }
}
