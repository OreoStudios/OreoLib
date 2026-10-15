package com.oreo.lib.autotest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregated results of generated invocations and saved regression cases. */
public final class TestReport {
    public record Failure(String method, int sample, long seed, String arguments, String detail) {}

    private int passed;
    private final List<Failure> failures = new ArrayList<>();

    void pass() { passed++; }
    void fail(Failure failure) { failures.add(failure); }

    public int passed() { return passed; }
    public int failed() { return failures.size(); }
    public int total() { return passed + failed(); }
    public boolean successful() { return total() > 0 && failed() == 0; }
    public List<Failure> failures() { return Collections.unmodifiableList(failures); }

    /** Fail the build, including if no annotated test methods were discovered. */
    public void assertSuccess() {
        if (!successful()) throw new AssertionError(toString());
    }

    /** Save a self-contained HTML result; displayed content is escaped. */
    public void writeHtml(Path file) throws IOException {
        StringBuilder html = new StringBuilder("<!doctype html><html lang=\"en\"><meta charset=\"utf-8\">")
                .append("<title>OreoLib AutoTest</title><style>body{font:16px system-ui;margin:2rem;max-width:1000px}")
                .append("table{border-collapse:collapse;width:100%}td,th{padding:8px;border:1px solid #aaa;vertical-align:top}")
                .append("pre{white-space:pre-wrap;overflow-wrap:anywhere}</style><h1>OreoLib AutoTest</h1>")
                .append("<p>Passed: ").append(passed).append(" | Failed: ").append(failed()).append("</p>")
                .append("<table><tr><th>Method</th><th>Sample</th><th>Seed</th><th>Arguments</th><th>Failure</th></tr>");
        for (Failure f : failures) {
            html.append("<tr><td>").append(escape(f.method())).append("</td><td>").append(f.sample())
                    .append("</td><td>").append(f.seed()).append("</td><td><pre>")
                    .append(escape(f.arguments())).append("</pre></td><td><pre>")
                    .append(escape(f.detail())).append("</pre></td></tr>");
        }
        html.append("</table></html>");
        if (file.toAbsolutePath().getParent() != null) Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, html.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder("OreoLib AutoTest: ").append(passed)
                .append(" passed, ").append(failed()).append(" failed");
        for (Failure f : failures) {
            result.append("\n - ").append(f.method()).append(" sample=").append(f.sample())
                    .append(" seed=").append(f.seed()).append(" args=").append(f.arguments())
                    .append("\n   ").append(f.detail());
        }
        return result.toString();
    }
}
