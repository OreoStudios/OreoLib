package com.oreo.lib;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
