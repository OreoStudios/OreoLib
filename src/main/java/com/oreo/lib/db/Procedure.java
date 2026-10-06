package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calls a database stored procedure via JDBC {@code CallableStatement}
 * ({@code {call name(?, ?)}}). Method arguments are bound in order.
 * Requires a driver/engine that supports stored procedures (not SQLite).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Procedure {
    String value();
}
