package com.oreo.lib.autotest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static com.oreo.lib.autotest.MetamorphicRule.*;
import static org.junit.jupiter.api.Assertions.*;

class PrismChecksTest {
    @TempDir Path temporary;

    @Test
    void validMetamorphicRelationsPassWithoutExpectedOutputs() {
        TestReport report = OreoTest.classes(ValidRelations.class)
                .withFuzzing(100).withSeed(42).run();
        assertEquals(300, report.passed(), report.toString());
        report.assertSuccess();
    }

    @Test
    void nonCommutativeCodeIsFlagged() {
        TestReport report = OreoTest.classes(NonCommutative.class)
                .withFuzzing(40).withSeed(42).run();
        assertTrue(report.failed() > 0);
        assertTrue(report.failures().stream()
                .anyMatch(f -> f.detail().contains("METAMORPHIC_COMMUTATIVE")), report.toString());
    }

    @Test
    void idempotenceDriftIsDetectedAndAutomaticallyReduced() {
        TestReport report = OreoTest.classes(NonIdempotent.class)
                .withFuzzing(2).withShrinking(64).run();
        assertEquals(2, report.failed(), report.toString());
        assertEquals("[0]", report.failures().get(0).arguments());
        assertTrue(report.toString().contains("Minimal reproducer"));
    }

    @Test
    void nondeterministicCodeIsDetected() {
        Nondeterministic.counter = 0;
        TestReport report = OreoTest.classes(Nondeterministic.class).withFuzzing(1).run();
        assertEquals(1, report.failed());
        assertTrue(report.toString().contains("METAMORPHIC_DETERMINISTIC"));
    }

    @Test
    void differentialReferenceCanLiveInAnotherClass() {
        TestReport report = OreoTest.classes(Candidate.class)
                .withFuzzing(10).withShrinking(64).run();
        assertEquals(10, report.failed(), report.toString());
        assertTrue(report.failures().stream().allMatch(f -> f.detail().contains("[DIFFERENTIAL]")));
        assertEquals("[0]", report.failures().get(1).arguments());
    }

    @Test
    void shrinkingPreservesExceptionCategoryAndNumericRange() {
        TestReport report = OreoTest.classes(CrashesForNegative.class)
                .withFuzzing(1).withShrinking(100).run();
        assertEquals(1, report.failed());
        assertEquals("[-6]", report.failures().get(0).arguments(), report.toString());
        assertTrue(report.toString().contains("EXCEPTION:java.lang.IllegalStateException"));
    }

    @Test
    void minimalFailingInputCanBeReplayed() {
        Path cases = temporary.resolve("prism-cases");
        TestReport initial = OreoTest.classes(CrashesForNegative.class)
                .withFuzzing(1).withShrinking(100).withRegressionTests(cases).run();
        assertEquals("[-6]", initial.failures().get(0).arguments());
        TestReport repeated = OreoTest.classes(CrashesForNegative.class)
                .withFuzzing(1).withShrinking(100).withRegressionTests(cases).run();
        assertEquals(2, repeated.failed(), repeated.toString());
        assertEquals("[-6]", repeated.failures().get(0).arguments());
        assertEquals(-1, repeated.failures().get(0).sample());
    }

    @Test
    void roundTripFindsNonInvertibleTransformations() {
        TestReport correct = OreoTest.classes(WorkingCodec.class).withFuzzing(30).run();
        assertEquals(30, correct.passed(), correct.toString());
        TestReport broken = OreoTest.classes(BrokenCodec.class).withFuzzing(5).run();
        assertEquals(5, broken.failed(), broken.toString());
        assertTrue(broken.toString().contains("ROUND_TRIP"));
        assertEquals("[]", broken.failures().get(0).arguments()); // empty string is a valid fixture
    }

    @Test
    void invalidRelationsFailExplicitly() {
        TestReport report = OreoTest.classes(InvalidCommutativity.class)
                .withFuzzing(100).run();
        assertEquals(1, report.failed());
        assertTrue(report.toString().contains("COMMUTATIVE requires"));
    }

    @Test
    void shrinkingCanBeDisabled() {
        TestReport report = OreoTest.classes(CrashesForNegative.class)
                .withFuzzing(1).withShrinking(0).run();
        assertEquals("[-100]", report.failures().get(0).arguments());
    }

    @Test
    void matchingLegacyAndCandidateImplementationsPass() {
        TestReport report = OreoTest.classes(WorkingCandidate.class).withFuzzing(30).run();
        assertEquals(30, report.passed(), report.toString());
    }

    public static class ValidRelations {
        @AutoTest
        @Metamorphic(COMMUTATIVE)
        public int add(int a, int b) { return a + b; }

        @AutoTest
        @Metamorphic(IDEMPOTENT)
        public String normalize(String s) { return s.trim(); }

        @AutoTest
        @Metamorphic(DETERMINISTIC)
        public int triple(int n) { return n * 3; }
    }

    public static class NonCommutative {
        @AutoTest
        @Metamorphic(COMMUTATIVE)
        public int subtract(int a, int b) { return a - b; }
    }

    public static class NonIdempotent {
        @AutoTest
        @Metamorphic(IDEMPOTENT)
        public int increment(@TestRange(min = 0, max = 100) int a) { return a + 1; }
    }

    public static class Nondeterministic {
        static int counter;
        @AutoTest
        @Metamorphic(DETERMINISTIC)
        public int inconsistent() { return ++counter; }
    }

    public static class Candidate {
        @AutoTest
        @CompareWith(value = "original", in = Reference.class)
        public int plusOne(@TestRange(min = 0, max = 100) int a) { return a + 1; }
    }

    public static class Reference {
        public int original(int a) { return a; }
    }

    public static class WorkingCandidate {
        @AutoTest
        @CompareWith(value = "original", in = Reference.class)
        public int plusOne(@TestRange(min = 0, max = 100) int a) { return a; }
    }

    public static class CrashesForNegative {
        @AutoTest
        public int compute(@TestRange(min = -100, max = 0) int input) {
            if (input < -5) throw new IllegalStateException("failure");
            return input;
        }
    }

    public static class WorkingCodec {
        @AutoTest
        @RoundTrip("decode")
        public String encode(String input) { return "v:" + input; }
        private String decode(String input) { return input.substring(2); }
    }

    public static class BrokenCodec {
        @AutoTest
        @RoundTrip("decode")
        public String encode(String input) { return "v:" + input; }
        private String decode(String input) { return input.substring(1); }
    }

    public static class InvalidCommutativity {
        @AutoTest
        @Metamorphic(COMMUTATIVE)
        public int invalid(int onlyOne) { return onlyOne; }
    }
}
