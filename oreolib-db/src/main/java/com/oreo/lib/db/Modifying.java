package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Query}/{@link NativeQuery} method as an UPDATE/DELETE/INSERT.
 * The method runs {@code executeUpdate} and may return {@code void}, {@code int}
 * or {@code long} (the affected row count).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Modifying {
}
