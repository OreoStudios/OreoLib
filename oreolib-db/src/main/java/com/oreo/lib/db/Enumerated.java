package com.oreo.lib.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares how an enum field is persisted. Defaults to {@link EnumType#STRING}
 * (safer than ordinal, which breaks if the enum order changes). Reading always
 * accepts both forms.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Enumerated {
    EnumType value() default EnumType.STRING;
}
