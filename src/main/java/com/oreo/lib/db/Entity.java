package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a persistable entity (Spring-Data style). Optional — a
 * {@link Table} alone is enough — but allowed so the mapping reads familiarly.
 * {@code name} overrides the table name when set.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    String name() default "";
}
