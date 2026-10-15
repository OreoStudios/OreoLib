package com.oreo.lib.autotest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check inverse behavior without knowing a hard-coded expected output:
 * inverse(forward(x)) must equal x.
 *
 * The annotated forward method must take exactly one argument and have a
 * non-void result. The inverse method accepts the forward result type and
 * returns the original input type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RoundTrip {
    String value();
    /** Defaults to another method in the forward method's declaring class. */
    Class<?> in() default Void.class;
}
