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
     * <code>${pkg}.builder.${simpleName}Builder</code>.
     * 
     * @return full class name template for generated builder class
     */
    String value() default "";

    /**
     * Returns false if and only if the generated builder of a class with only
     * mandatory properties should immediately return the built object (skipping the
     * need to call {@code build()}) when the last mandatory property is specified.
     * 
     * @return false if should skip the final {@code build()} method when all
     *         properties are mandatory
     */
    boolean alwaysIncludeBuildMethod() default false;

    /**
     * When the annotated class is an interface, returns template for construction
     * of generated implementation class full name that is returned by a builder.
     * Available parameters (related to the class that the annotation is placed on)
     * are <code>${pkg}</code>, <code>${simpleName}</code>. Default is
     * <code>${pkg}.builder.${simpleName}Impl</code>
     * 
     * @return template to use for the generated builder class full name
     */
    String implementationClassName() default "";

    /**
     * If true then the generated builder will have a static `copy` method that
     * returns a builder that is initiated with the fields of the given object.
     * 
     * @return true if the generated builder should have a static `copy` method
     */
    boolean copy() default true;
}
