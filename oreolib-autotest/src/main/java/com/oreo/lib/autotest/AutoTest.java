package com.oreo.lib.autotest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt in a method (or all public declared methods of a class) to generated contract tests.
 * The runner never invokes unmarked classes/methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AutoTest {
    int samples() default 100;
}
