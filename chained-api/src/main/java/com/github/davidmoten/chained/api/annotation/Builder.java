package com.github.davidmoten.chained.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
@Documented
public @interface Builder {

    String value() default "${pkg}.builder.${simpleName}Builder";

    boolean alwaysIncludeBuildMethod() default false;

    String implementationClassName() default "${pkg}.builder.${simpleName}Impl";
}
