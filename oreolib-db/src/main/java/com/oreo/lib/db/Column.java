package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field to a column. {@code name} defaults to the field name.
 * {@code nullable}/{@code unique} shape the generated {@code createTable()} DDL;
 * {@code updatable=false} keeps the column out of UPDATE statements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean nullable() default true;
    boolean unique() default false;
    boolean updatable() default true;
}
