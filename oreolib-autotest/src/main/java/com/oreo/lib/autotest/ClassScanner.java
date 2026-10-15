package com.oreo.lib.autotest;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Classpath directory/JAR scanner. Only classes explicitly opted in with
 * @AutoTest are subsequently executed by OreoTest.
 */
final class ClassScanner {
    private ClassScanner() {}

    static List<Class<?>> scan(String packageName) {
        if (packageName == null || !packageName.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*")) {
            throw new IllegalArgumentException("A non-empty Java package name is required");
        }
        String prefix = packageName.replace('.', '/');
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) loader = ClassScanner.class.getClassLoader();
        Set<String> names = new TreeSet<>();
        try {
            Enumeration<URL> resources = loader.getResources(prefix);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    Path directory = Path.of(url.toURI());
                    try (Stream<Path> paths = Files.walk(directory)) {
                        paths.filter(Files::isRegularFile)
                                .filter(p -> p.getFileName().toString().endsWith(".class"))
                                .forEach(p -> {
                                    String relative = directory.relativize(p).toString()
                                            .replace('\\', '/');
                                    names.add(packageName + "." +
                                            relative.substring(0, relative.length() - 6).replace('/', '.'));
                                });
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    try (JarFile jar = connection.getJarFile()) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            String name = entries.nextElement().getName();
                            if (name.startsWith(prefix + "/") && name.endsWith(".class")) {
                                names.add(name.substring(0, name.length() - 6).replace('/', '.'));
                            }
                        }
                    }
                } else {
                    throw new IOException("Unsupported classpath URL protocol: " + url);
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Could not scan " + packageName, e);
        }
        if (names.isEmpty()) throw new IllegalArgumentException("No .class files found under " + packageName);
        List<Class<?>> classes = new ArrayList<>();
        for (String name : names) {
            if (name.endsWith("package-info") || name.endsWith("module-info")) continue;
            try {
                classes.add(Class.forName(name, false, loader));
            } catch (ClassNotFoundException | LinkageError e) {
                throw new IllegalStateException("Could not load " + name + " while scanning " + packageName, e);
            }
        }
        return classes;
    }
}
