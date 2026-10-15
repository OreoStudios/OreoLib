package com.oreo.lib.autotest;

import com.oreo.lib.autotest.samples.SampleFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OreoTestTest {
    @TempDir Path temporary;

    @Test
    void discoversAnnotatedClassesAndAppliesContracts() {
        TestReport report = OreoTest.scan("com.oreo.lib.autotest.samples").run();
        assertEquals(15, report.passed(), report.toString());
        assertEquals(0, report.failed(), report.toString());
        report.assertSuccess();
    }

    @Test
    void generatedResultsAreDeterministic() {
        TestReport first = OreoTest.classes(Generated.class).withSeed(42).withFuzzing(30).run();
        TestReport second = OreoTest.classes(Generated.class).withSeed(42).withFuzzing(30).run();
        assertEquals(first.toString(), second.toString());
        assertEquals(30, first.passed());
    }

    @Test
    void reportsContractFailuresAndFailsBuild() {
        TestReport report = OreoTest.classes(Broken.class).withFuzzing(3).run();
        assertEquals(3, report.failed());
        assertThrows(AssertionError.class, report::assertSuccess);
        assertTrue(report.failures().get(0).detail().contains("@ResultRange"));
    }

    @Test
    void regressionCasesArePersistedAndReplayed() throws IOException {
        Path fixtures = temporary.resolve("cases");
        TestReport initial = OreoTest.classes(Broken.class)
                .withFuzzing(3).withRegressionTests(fixtures).run();
        assertEquals(3, initial.failed());
        try (Stream<Path> saved = Files.list(fixtures)) {
            assertEquals(1, saved.count());
        }
        // Previous failing case is replayed ahead of this invocation's generated case.
        TestReport replay = OreoTest.classes(Broken.class)
                .withFuzzing(1).withRegressionTests(fixtures).run();
        assertEquals(2, replay.failed());
        assertEquals(-1, replay.failures().get(0).sample());
    }

    @Test
    void emptyStringFailureIsSavedAndReplayed() throws IOException {
        Path fixtures = temporary.resolve("empty-string-fixtures");
        TestReport first = OreoTest.classes(EmptyStringFailure.class)
                .withFuzzing(1).withRegressionTests(fixtures).run();
        assertEquals(1, first.failed());
        TestReport replay = OreoTest.classes(EmptyStringFailure.class)
                .withFuzzing(1).withRegressionTests(fixtures).run();
        assertEquals(2, replay.failed());
        assertEquals(-1, replay.failures().get(0).sample());
    }

    @Test
    void unsupportedInputsAndMissingConstructorsAreFailures() {
        TestReport unsupported = OreoTest.classes(Unsupported.class).run();
        assertEquals(1, unsupported.failed());
        assertTrue(unsupported.toString().contains("Unsupported parameter type"));
        TestReport noConstructor = OreoTest.classes(NoDefaultConstructor.class).run();
        assertEquals(1, noConstructor.failed());
        assertTrue(noConstructor.toString().contains("no-argument constructor"));
    }

    @Test
    void emptyDiscoveryFailsRatherThanPassing() {
        TestReport report = OreoTest.classes(NotAnnotated.class).run();
        assertFalse(report.successful());
        assertThrows(AssertionError.class, report::assertSuccess);
    }

    @Test
    void htmlReportEscapesFailureData() throws IOException {
        TestReport report = new TestReport();
        report.fail(new TestReport.Failure("<method>", 1, 23, "<script>", "&bad"));
        Path html = temporary.resolve("reports/result.html");
        report.writeHtml(html);
        String content = Files.readString(html);
        assertTrue(content.contains("&lt;script&gt;"));
        assertTrue(content.contains("&amp;bad"));
        assertFalse(content.contains("<script>"));
    }

    @Test
    void unexpectedExceptionsAndExpectedExceptionsAreHandled() {
        TestReport crash = OreoTest.classes(ThrowsUnexpectedly.class).withFuzzing(2).run();
        assertEquals(2, crash.failed());
        assertTrue(crash.failures().get(0).detail().contains("IllegalStateException"));
        assertEquals(3, OreoTest.classes(SampleFixture.class).withFuzzing(1).run().passed());
    }

    @AutoTest
    public static class Generated {
        public int positive(@TestRange(min = 0, max = 100) int n) { return n; }
    }

    @AutoTest
    public static class Broken {
        @ResultRange(min = 0, max = 5)
        public int result() { return 10; }
    }

    @AutoTest
    public static class Unsupported {
        public int process(List<String> value) { return value.size(); }
    }

    @AutoTest
    public static class NoDefaultConstructor {
        public NoDefaultConstructor(int n) {}
        public int calculate() { return 1; }
    }

    @AutoTest
    public static class ThrowsUnexpectedly {
        public int calculate() { throw new IllegalStateException("broken"); }
    }

    @AutoTest
    public static class EmptyStringFailure {
        @NotNullResult
        public String process(String input) { return null; }
    }

    public static class NotAnnotated {
        public int call() { return 1; }
    }
}
