package com.github.davidmoten.chained.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(value = { ElementType.TYPE })
public @interface Builder {

    /**
     * Template for construction of generated builder class full name. Default is
     * "${pkg}.builder.${simpleName}Builder".
     * 
     * @return full class name template for generated builder class
     */
    String value();

    boolean alwaysIncludeBuildMethod() default false;

    String implementationClassName() default "${pkg}.builder.${simpleName}Impl";
}
