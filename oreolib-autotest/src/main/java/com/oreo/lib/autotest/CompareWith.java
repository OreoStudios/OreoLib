package com.oreo.lib.autotest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compare this method's result against a reference implementation for the same
 * generated inputs (a local method or a method in a separate class).
 * The methods must have identical parameter and return types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompareWith {
    String value();
    /** Defaults to another method on this method's declaring class. */
    Class<?> in() default Void.class;
}
