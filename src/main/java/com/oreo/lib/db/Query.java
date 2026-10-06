package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a repository interface method to a SQL statement.
 *
 * <p>Note: Spring's {@code @Query} defaults to JPQL. OreoLib has no JPQL engine,
 * so the value is plain SQL (same as {@link NativeQuery}). Use {@code :name}
 * placeholders with {@link Param}, or {@code ?} positional placeholders.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Query {
    String value();
}
