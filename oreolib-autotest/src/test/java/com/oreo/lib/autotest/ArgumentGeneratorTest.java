package com.oreo.lib.autotest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentGeneratorTest {
    @TempDir
    Path tempDir;

    @Test
    void generatorBoxesPrimitiveAndWrapperFloatingPointArgumentsCorrectly() throws Exception {
        Method method = FloatingPointFixture.class.getDeclaredMethod(
                "mixed", float.class, Float.class, double.class, Double.class);
        Random random = new Random(42);
        for (int sample = 0; sample < 30; sample++) {
            Object[] args = ArgumentGenerator.generate(method, sample, random);
            assertInstanceOf(Float.class, args[0], "primitive float sample " + sample);
            assertInstanceOf(Float.class, args[1], "boxed Float sample " + sample);
            assertInstanceOf(Double.class, args[2], "primitive double sample " + sample);
            assertInstanceOf(Double.class, args[3], "boxed Double sample " + sample);
            // Reflection must accept all generated arguments without type mismatch.
            assertDoesNotThrow(() -> method.invoke(new FloatingPointFixture(), args));
        }
    }

    @Test
    void prismExecutesPrimitiveAndBoxedFloatParametersEndToEnd() {
        TestReport report = OreoTest.classes(FloatingPointFixture.class)
                .withSeed(20261015L)
                .withFuzzing(25)
                .run();
        assertEquals(125, report.passed(), report.toString());
        assertEquals(0, report.failed(), report.toString());
        report.assertSuccess();
    }

    @Test
    void floatFailureIsShrinkableAndRegressionFixturesReplay() {
        Path regressions = tempDir.resolve("floating-point-cases");
        TestReport initial = OreoTest.classes(FloatingPointFailure.class)
                .withFuzzing(1).withSeed(123).withShrinking(60)
                .withRegressionTests(regressions).run();
        assertEquals(1, initial.failed(), initial.toString());
        assertTrue(initial.failures().get(0).detail().contains("IllegalStateException"));

        TestReport replay = OreoTest.classes(FloatingPointFailure.class)
                .withFuzzing(1).withSeed(123).withShrinking(60)
                .withRegressionTests(regressions).run();
        assertEquals(2, replay.failed(), replay.toString());
        assertEquals(-1, replay.failures().get(0).sample());
        assertTrue(replay.failures().get(0).detail().contains("IllegalStateException"));
    }

    @AutoTest
    public static class FloatingPointFixture {
        @ResultRange(min = -10, max = 10)
        public float primitive(@TestRange(min = -2.5, max = 3.25) float f) {
            return f;
        }

        @NotNullResult
        @ResultRange(min = -10, max = 10)
        public Float boxed(@TestRange(min = -2.5, max = 3.25) Float f) {
            return f;
        }

        @ResultRange(min = -10, max = 10)
        public double primitiveDouble(@TestRange(min = -2.5, max = 3.25) double d) {
            return d;
        }

        @NotNullResult
        @ResultRange(min = -10, max = 10)
        public Double boxedDouble(@TestRange(min = -2.5, max = 3.25) Double d) {
            return d;
        }

        @Metamorphic(MetamorphicRule.DETERMINISTIC)
        @ResultRange(min = -10, max = 10)
        public double mixed(
                @TestRange(min = -2.5, max = 3.25) float f,
                @TestRange(min = -2.5, max = 3.25) Float boxedF,
                @TestRange(min = -2.5, max = 3.25) double d,
                @TestRange(min = -2.5, max = 3.25) Double boxedD) {
            return f;
        }
    }

    @AutoTest
    public static class FloatingPointFailure {
        public float crashesOnNegative(@TestRange(min = -1.5, max = 1.5) float f) {
            if (f < -0.5f) throw new IllegalStateException("float-regression");
            return f;
        }
    }
}
