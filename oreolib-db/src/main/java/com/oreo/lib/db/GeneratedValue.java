package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the {@link Id} as database-generated (auto-increment). When present,
 * an empty id means INSERT and the key is written back. When absent, the id is
 * application-assigned (e.g. a {@link java.util.UUID}) and {@code save(...)}
 * inserts or updates based on whether the row already exists.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedValue {
}
