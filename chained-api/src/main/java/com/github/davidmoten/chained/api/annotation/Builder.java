package com.github.davidmoten.chained.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(value = { ElementType.TYPE })
public @interface Builder {

    String value() default "${pkg}.builder.${simpleName}Builder";

    boolean alwaysIncludeBuildMethod() default false;

    String implementationClassName() default "${pkg}.builder.${simpleName}Impl";
}
