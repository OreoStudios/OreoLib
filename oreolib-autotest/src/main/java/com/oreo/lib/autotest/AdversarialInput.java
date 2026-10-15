package com.oreo.lib.autotest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt in a String parameter to deterministic hostile text cases: Unicode,
 * controls, oversized strings and injection-like payloads.
 *
 * Use on isolated pure code; OreoTest invokes the annotated method for real.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AdversarialInput {}
