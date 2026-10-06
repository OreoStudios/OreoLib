package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Binds a repository method to a native SQL statement. (Identical to {@link Query} here.) */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeQuery {
    String value();
}
