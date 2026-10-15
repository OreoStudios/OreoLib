package com.oreo.lib.autotest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Opt-in automatic contract and fuzz tests. No arbitrary business outcomes are
 * guessed: only crashes and declared contracts are checked.
 */
public final class OreoTest {
    private final List<Class<?>> classes;
    private Integer sampleOverride;
    private long seed = 20261015L;
    private RegressionStore regressions;

    private OreoTest(List<Class<?>> classes) { this.classes = classes; }

    /** Discover annotated classes or methods from compiled classpath directories and JARs. */
    public static OreoTest scan(String packageName) {
        return new OreoTest(ClassScanner.scan(packageName));
    }

    /** Register test targets directly without classpath scanning. */
    public static OreoTest classes(Class<?>... types) {
        if (types == null || types.length == 0) throw new IllegalArgumentException("Provide test classes");
        return new OreoTest(Arrays.asList(types));
    }

    /** Override @AutoTest.samples() across all selected methods. */
    public OreoTest withFuzzing(int samples) {
        if (samples < 1 || samples > 1_000_000) throw new IllegalArgumentException("samples must be 1..1000000");
        this.sampleOverride = samples;
        return this;
    }

    /** Use a fixed seed to reproduce generated invocations. */
    public OreoTest withSeed(long seed) { this.seed = seed; return this; }

    /** Replay previously failing inputs and store new failing inputs in target/oreolib-regressions. */
    public OreoTest withRegressionTests() { return withRegressionTests(Path.of("target/oreolib-regressions")); }

    /** Choose where portable failure cases are replayed and stored. */
    public OreoTest withRegressionTests(Path directory) {
        if (directory == null) throw new IllegalArgumentException("Regression directory is null");
        this.regressions = new RegressionStore(directory);
        return this;
    }

    /** Run and return detailed results; call report.assertSuccess() to fail a build. */
    public TestReport run() {
        TestReport report = new TestReport();
        List<Method> methods = new ArrayList<>();
        for (Class<?> type : classes) {
            AutoTest onClass = type.getAnnotation(AutoTest.class);
            for (Method method : type.getDeclaredMethods()) {
                if (method.isSynthetic() || method.isBridge() || Modifier.isAbstract(method.getModifiers())) continue;
                if (method.isAnnotationPresent(AutoTest.class)
                        || (onClass != null && Modifier.isPublic(method.getModifiers()))) methods.add(method);
            }
        }
        methods.sort(Comparator.comparing(Method::toGenericString));
        if (methods.isEmpty()) {
            report.fail(new TestReport.Failure("<discovery>", -1, seed, "[]",
                    "No @AutoTest methods found. Annotate methods/classes and compile them first."));
            return report;
        }
        for (Method method : methods) execute(method, report);
        return report;
    }

    private void execute(Method method, TestReport report) {
        long methodSeed = seed ^ ((long) method.toGenericString().hashCode() * 0x9E3779B97F4A7C15L);
        AutoTest configuration = method.getAnnotation(AutoTest.class);
        if (configuration == null) configuration = method.getDeclaringClass().getAnnotation(AutoTest.class);
        int samples = sampleOverride != null ? sampleOverride : configuration.samples();
        if (samples < 1 || samples > 1_000_000) {
            fail(report, method, -1, methodSeed, new Object[0], "samples must be 1..1000000");
            return;
        }
        if (method.isAnnotationPresent(NotNullResult.class) && method.getReturnType() == void.class) {
            fail(report, method, -1, methodSeed, new Object[0], "@NotNullResult requires a return value");
            return;
        }
        ResultRange resultRange = method.getAnnotation(ResultRange.class);
        if (resultRange != null && (!Number.class.isAssignableFrom(boxed(method.getReturnType()))
                || !Double.isFinite(resultRange.min()) || !Double.isFinite(resultRange.max())
                || resultRange.min() > resultRange.max())) {
            fail(report, method, -1, methodSeed, new Object[0], "Invalid @ResultRange / non-numeric return type");
            return;
        }
        Constructor<?> constructor = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            try {
                constructor = method.getDeclaringClass().getDeclaredConstructor();
                constructor.setAccessible(true);
            } catch (ReflectiveOperationException | RuntimeException e) {
                fail(report, method, -1, methodSeed, new Object[0],
                        "A no-argument constructor is required: " + e);
                return;
            }
        }
        try {
            method.setAccessible(true);
        } catch (RuntimeException e) {
            fail(report, method, -1, methodSeed, new Object[0], "Cannot access method: " + e);
            return;
        }

        if (regressions != null) {
            try {
                List<Object[]> saved = regressions.load(method);
                for (int i = 0; i < saved.size(); i++) invoke(method, constructor, saved.get(i), -i - 1, methodSeed, report);
            } catch (Exception e) {
                fail(report, method, -1, methodSeed, new Object[0], "Cannot read regression cases: " + trace(e));
            }
        }
        Random random = new Random(methodSeed);
        for (int sample = 0; sample < samples; sample++) {
            Object[] args;
            try {
                args = ArgumentGenerator.generate(method, sample, random);
            } catch (RuntimeException e) {
                fail(report, method, sample, methodSeed, new Object[0], "Cannot generate inputs: " + trace(e));
                break; // do not count the same configuration error many times
            }
            invoke(method, constructor, args, sample, methodSeed, report);
        }
    }

    private void invoke(Method method, Constructor<?> ctor, Object[] args,
                        int sample, long methodSeed, TestReport report) {
        Throwable thrown = null;
        Object result = null;
        try {
            Object instance = ctor == null ? null : ctor.newInstance();
            result = method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            thrown = e.getCause();
        } catch (Throwable e) {
            thrown = e;
        }
        ExpectThrows expected = method.getAnnotation(ExpectThrows.class);
        String error = null;
        if (expected != null) {
            if (thrown == null) error = "Expected exception " + expected.value().getName() + " but none was thrown";
            else if (!expected.value().isInstance(thrown)) error = "Unexpected exception: " + trace(thrown);
        } else if (thrown != null) {
            error = "Unexpected exception: " + trace(thrown);
        } else if (method.isAnnotationPresent(NotNullResult.class) && result == null) {
            error = "@NotNullResult contract failed";
        } else {
            ResultRange range = method.getAnnotation(ResultRange.class);
            if (range != null) {
                if (!(result instanceof Number number) || !Double.isFinite(number.doubleValue())
                        || number.doubleValue() < range.min() || number.doubleValue() > range.max()) {
                    error = "@ResultRange contract failed: " + result + " not in " + range.min() + ".." + range.max();
                }
            }
        }
        if (error == null) {
            report.pass();
        } else {
            fail(report, method, sample, methodSeed, args, error);
            if (regressions != null) {
                try { regressions.record(method, args); }
                catch (Exception e) {
                    fail(report, method, sample, methodSeed, args, "Could not save regression case: " + trace(e));
                }
            }
        }
    }

    private static void fail(TestReport report, Method method, int sample, long seed, Object[] args, String detail) {
        report.fail(new TestReport.Failure(method.toGenericString(), sample, seed,
                Arrays.deepToString(args), detail));
    }

    private static String trace(Throwable e) {
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        return out.toString();
    }

    private static Class<?> boxed(Class<?> type) {
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return type;
    }

    /**
     * Maven exec:java entry point. Arguments: package [samples] [regressionDirectory].
     * Always writes target/oreolib-autotest/report.html and fails on any error.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 3) {
            throw new IllegalArgumentException("Usage: OreoTest <java.package> [samples] [regressionDirectory]");
        }
        int samples = args.length >= 2 ? Integer.parseInt(args[1]) : 100;
        Path regressionDir = args.length == 3 ? Path.of(args[2]) : Path.of("target/oreolib-regressions");
        TestReport report = scan(args[0]).withFuzzing(samples).withRegressionTests(regressionDir).run();
        report.writeHtml(Path.of("target/oreolib-autotest/report.html"));
        System.out.println(report);
        report.assertSuccess();
    }
}
