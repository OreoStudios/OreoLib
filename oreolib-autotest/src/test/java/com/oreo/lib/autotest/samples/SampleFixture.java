package com.oreo.lib.autotest.samples;

import com.oreo.lib.autotest.AutoTest;
import com.oreo.lib.autotest.ExpectThrows;
import com.oreo.lib.autotest.NotNullResult;
import com.oreo.lib.autotest.ResultRange;
import com.oreo.lib.autotest.TestRange;

/** Deliberately separate package to test classpath scanning without negative fixtures. */
@AutoTest(samples = 5)
public class SampleFixture {
    public SampleFixture() {}

    @ResultRange(min = 0, max = 10000)
    public int square(@TestRange(min = 0, max = 100) int value) {
        return value * value;
    }

    @NotNullResult
    public String normalize(String value) {
        return value.trim();
    }

    @ExpectThrows(IllegalArgumentException.class)
    public void alwaysFails() {
        throw new IllegalArgumentException("expected");
    }
}
