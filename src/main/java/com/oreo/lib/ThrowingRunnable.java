package com.oreo.lib;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
