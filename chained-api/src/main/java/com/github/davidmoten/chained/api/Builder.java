package com.github.davidmoten.chained.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for annotation processing.
 *
 * TODO: Please change it according to your needs.
 *
 * Demo annotation is used to create a class and must contain a non empty value.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Documented
public @interface Builder {

    /**
     * Value must not be empty String.
     * @return the value
     */
    String value();


}
